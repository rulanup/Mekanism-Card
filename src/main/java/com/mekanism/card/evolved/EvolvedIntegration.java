package com.mekanism.card.evolved;

import mekanism.api.Upgrade;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeable;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * EvolvedMekanism 模组联动集成。
 *
 * <p>通过反射访问 EvolvedMekanism（mod id: {@code evolvedmekanism}）注入到 Mekanism
 * 的 {@link BaseTier} 与 {@link Upgrade} 枚举里的新常量，运行时检测模组是否加载。
 * 这样 Mekanism-Card 不需要在编译期依赖 EvolvedMekanism，运行时按需启用功能。</p>
 *
 * <p>EvolvedMekanism 与 Mekanism Extras 不同：它复用 Mekanism 原生的
 * {@link AttributeUpgradeable} 与 {@link BaseTier}，通过 Mixin 向 BaseTier 注入
 * OVERCLOCKED/QUANTUM/DENSE/MULTIVERSAL 四个新等级（CREATIVE 复用 Mekanism 自带），
 * 形成 ULTIMATE → OVERCLOCKED → QUANTUM → DENSE → MULTIVERSAL → CREATIVE 的升级链。
 * 因此本集成不需要像 ExtrasIntegration 那样维护独立的 ExtraAttributeUpgradeable，
 * 只需拿到注入后的 BaseTier 常量与对应的 TierInstaller 物品即可。</p>
 *
 * <p>提供的能力：
 * <ul>
 *   <li>检测 EvolvedMekanism 是否加载</li>
 *   <li>获取注入后的 BaseTier 常量（overclocked/quantum/dense/multiversal）</li>
 *   <li>判断方块是否支持 EvolvedMekanism 等级升级（ULTIMATE 之后继续）</li>
 *   <li>获取对应等级的 TierInstaller 物品</li>
 *   <li>计算从当前等级升级到 CREATIVE 所需的 installer 物品链</li>
 *   <li>把 EvolvedMekanism 新增的 Upgrade（RADIOACTIVE）映射到对应物品
 *       （SOLAR/LUNAR 没有对应物品，返回 null）</li>
 * </ul></p>
 */
public final class EvolvedIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvolvedIntegration.class);

    public static final String MOD_ID = "evolvedmekanism";

    /** EvolvedMekanism 新增等级名称常量（lower case，与 BaseTier.getLowerName 一致），按从低到高排序 */
    public static final String TIER_OVERCLOCKED = "overclocked";
    public static final String TIER_QUANTUM = "quantum";
    public static final String TIER_DENSE = "dense";
    public static final String TIER_MULTIVERSAL = "multiversal";
    public static final String TIER_CREATIVE = "creative";

    /** ULTIMATE 之后的 EM 等级顺序，末位为 CREATIVE（复用 Mekanism 自带 BaseTier.CREATIVE） */
    private static final List<String> EM_TIER_ORDER = List.of(
            TIER_OVERCLOCKED, TIER_QUANTUM, TIER_DENSE, TIER_MULTIVERSAL, TIER_CREATIVE);

    private static volatile boolean initialized = false;
    private static volatile boolean available = false;

    // 反射缓存
    private static Class<?> emBaseTierClass;
    private static Class<?> emItemsClass;
    private static Class<?> emUpgradesClass;

    // BaseTier 常量缓存（按 lower name 索引）。CREATIVE 不在此 map 中，直接用 BaseTier.CREATIVE。
    private static final Map<String, BaseTier> EM_TIERS = new LinkedHashMap<>();

    // TierInstaller 物品缓存（按目标 tier lower name 索引）
    private static final Map<String, Item> TIER_INSTALLER_ITEMS = new LinkedHashMap<>();

    // EM 新增 Upgrade -> Item 映射（只有 RADIOACTIVE 有物品）
    private static final Map<Upgrade, Item> EM_UPGRADE_ITEMS = new LinkedHashMap<>();

    private EvolvedIntegration() {
    }

    /** 检测 EvolvedMekanism 模组是否加载，并惰性初始化反射缓存 */
    public static boolean isLoaded() {
        if (!ModList.get().isLoaded(MOD_ID)) {
            return false;
        }
        if (!initialized) {
            synchronized (EvolvedIntegration.class) {
                if (!initialized) {
                    tryInit();
                }
            }
        }
        return available;
    }

    private static void tryInit() {
        initialized = true;
        try {
            emBaseTierClass = Class.forName("fr.iglee42.evolvedmekanism.tiers.EMBaseTier");
            emItemsClass = Class.forName("fr.iglee42.evolvedmekanism.registries.EMItems");
            emUpgradesClass = Class.forName("fr.iglee42.evolvedmekanism.registries.EMUpgrades");

            // 缓存注入后的 BaseTier 常量（EM 通过 BaseTierMixin 注入到 Mekanism 的 BaseTier 枚举）
            cacheEMTier(TIER_OVERCLOCKED, "OVERCLOCKED");
            cacheEMTier(TIER_QUANTUM, "QUANTUM");
            cacheEMTier(TIER_DENSE, "DENSE");
            cacheEMTier(TIER_MULTIVERSAL, "MULTIVERSAL");

            // 缓存 TierInstaller 物品（EMItems 中的 ItemTierInstaller 注册项）
            cacheTierInstallerItem(TIER_OVERCLOCKED, "OVERCLOCKED_TIER_INSTALLER");
            cacheTierInstallerItem(TIER_QUANTUM, "QUANTUM_TIER_INSTALLER");
            cacheTierInstallerItem(TIER_DENSE, "DENSE_TIER_INSTALLER");
            cacheTierInstallerItem(TIER_MULTIVERSAL, "MULTIVERSAL_TIER_INSTALLER");
            cacheTierInstallerItem(TIER_CREATIVE, "CREATIVE_TIER_INSTALLER");

            // 缓存 Upgrade -> Item 映射（只有 RADIOACTIVE 有对应物品，SOLAR/LUNAR 没有物品）
            cacheEMUpgradeItem("RADIOACTIVE_UPGRADE", "RADIOACTIVE_UPGRADE");

            available = true;
            LOGGER.info("[Mekanism-Card] EvolvedMekanism integration enabled");
        } catch (Exception e) {
            available = false;
            LOGGER.warn("[Mekanism-Card] Failed to initialize EvolvedMekanism integration, features will be limited", e);
        }
    }

    private static void cacheEMTier(String tierName, String fieldName) {
        try {
            Field field = emBaseTierClass.getField(fieldName);
            BaseTier tier = (BaseTier) field.get(null);
            if (tier != null) {
                EM_TIERS.put(tierName, tier);
            }
        } catch (Exception e) {
            LOGGER.debug("[Mekanism-Card] Failed to cache EM tier {}: {}", tierName, e.getMessage());
        }
    }

    private static void cacheTierInstallerItem(String tierName, String fieldName) {
        try {
            Field field = emItemsClass.getField(fieldName);
            Object registryObj = field.get(null);
            // ItemRegistryObject 有 get() 方法返回 Item
            Method getMethod = registryObj.getClass().getMethod("get");
            Item item = (Item) getMethod.invoke(registryObj);
            if (item != null) {
                TIER_INSTALLER_ITEMS.put(tierName, item);
            }
        } catch (Exception e) {
            LOGGER.debug("[Mekanism-Card] Failed to cache EM tier installer for {}: {}", tierName, e.getMessage());
        }
    }

    private static void cacheEMUpgradeItem(String upgradeFieldName, String itemFieldName) {
        try {
            Field upgradeField = emUpgradesClass.getField(upgradeFieldName);
            Upgrade upgrade = (Upgrade) upgradeField.get(null);

            Field itemField = emItemsClass.getField(itemFieldName);
            Object registryObj = itemField.get(null);
            Method getMethod = registryObj.getClass().getMethod("get");
            Item item = (Item) getMethod.invoke(registryObj);

            if (upgrade != null && item != null) {
                EM_UPGRADE_ITEMS.put(upgrade, item);
            }
        } catch (Exception e) {
            LOGGER.debug("[Mekanism-Card] Failed to cache EM upgrade item for {}: {}", upgradeFieldName, e.getMessage());
        }
    }

    /**
     * 获取指定名称的 EM BaseTier 常量（overclocked/quantum/dense/multiversal）。
     * creative 返回 Mekanism 自带的 {@link BaseTier#CREATIVE}。
     */
    @Nullable
    public static BaseTier getEMTier(String name) {
        if (!isLoaded()) {
            return null;
        }
        if (TIER_CREATIVE.equals(name)) {
            return BaseTier.CREATIVE;
        }
        return EM_TIERS.get(name);
    }

    /**
     * 获取 BaseTier 对应的 lower name。EM 注入的等级返回 overclocked/quantum/dense/multiversal，
     * CREATIVE 返回 "creative"，其他返回 null。
     */
    @Nullable
    public static String getTierLowerName(BaseTier tier) {
        if (!isLoaded()) {
            return null;
        }
        for (Map.Entry<String, BaseTier> e : EM_TIERS.entrySet()) {
            if (e.getValue() == tier) {
                return e.getKey();
            }
        }
        if (tier == BaseTier.CREATIVE) {
            return TIER_CREATIVE;
        }
        return null;
    }

    /**
     * 判断方块是否支持 EvolvedMekanism 等级升级（即在 ULTIMATE 之后还能继续升级到 OVERCLOCKED）。
     *
     * <p>实现方式：取方块的 {@link AttributeUpgradeable}，调用
     * {@code upgradeResult(state, OVERCLOCKED)}，若返回的 BlockState 与原状态不同则认为支持。
     * 这与 EM 的 FactoryMixin 在每个等级注入指向下一等级方块的 AttributeUpgradeable 行为一致。</p>
     */
    public static boolean supportsEvolvedMekanism(Holder<Block> blockHolder, BlockState state) {
        if (!isLoaded()) {
            return false;
        }
        AttributeUpgradeable upgradeable = Attribute.get(blockHolder, AttributeUpgradeable.class);
        if (upgradeable == null) {
            return false;
        }
        BaseTier overclocked = EM_TIERS.get(TIER_OVERCLOCKED);
        if (overclocked == null) {
            return false;
        }
        try {
            BlockState result = upgradeable.upgradeResult(state, overclocked);
            return result != state;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取指定目标 tier 对应的 TierInstaller 物品。
     */
    @Nullable
    public static Item getTierInstallerItem(String tierName) {
        if (!isLoaded()) {
            return null;
        }
        return TIER_INSTALLER_ITEMS.get(tierName);
    }

    /**
     * 把 EvolvedMekanism 新增的 Upgrade（RADIOACTIVE）映射到对应 Item。
     * SOLAR/LUNAR 没有对应物品，返回 null。其他 Upgrade 也返回 null。
     */
    @Nullable
    public static Item getEMUpgradeItem(Upgrade upgrade) {
        if (!isLoaded()) {
            return null;
        }
        return EM_UPGRADE_ITEMS.get(upgrade);
    }

    /**
     * 获取下一个 EM 等级的 BaseTier。
     * <ul>
     *   <li>ULTIMATE → OVERCLOCKED</li>
     *   <li>OVERCLOCKED → QUANTUM</li>
     *   <li>QUANTUM → DENSE</li>
     *   <li>DENSE → MULTIVERSAL</li>
     *   <li>MULTIVERSAL → CREATIVE</li>
     *   <li>CREATIVE 或未知 → null</li>
     * </ul>
     */
    @Nullable
    public static BaseTier getNextTier(BaseTier current) {
        if (!isLoaded()) {
            return null;
        }
        if (current == BaseTier.ULTIMATE) {
            return EM_TIERS.get(TIER_OVERCLOCKED);
        }
        if (current == BaseTier.CREATIVE) {
            return null;
        }
        String name = getTierLowerName(current);
        if (name == null) {
            return null;
        }
        int idx = EM_TIER_ORDER.indexOf(name);
        if (idx < 0 || idx >= EM_TIER_ORDER.size() - 1) {
            return null;
        }
        String nextName = EM_TIER_ORDER.get(idx + 1);
        return getEMTier(nextName);
    }

    /**
     * 获取从当前等级升级到 CREATIVE 所需的所有 TierInstaller 物品列表（按顺序）。
     * 例如当前是 ULTIMATE，返回
     * [OVERCLOCKED_TIER_INSTALLER, QUANTUM_TIER_INSTALLER, DENSE_TIER_INSTALLER,
     *  MULTIVERSAL_TIER_INSTALLER, CREATIVE_TIER_INSTALLER]。
     * 若中途某个 installer 取不到则截断返回已收集的部分。
     */
    public static List<Item> getRequiredInstallersToCreative(BaseTier currentTier) {
        List<Item> installers = new ArrayList<>();
        BaseTier tier = currentTier;
        while (tier != BaseTier.CREATIVE) {
            BaseTier next = getNextTier(tier);
            if (next == null) {
                break;
            }
            String nextName = (next == BaseTier.CREATIVE) ? TIER_CREATIVE : getTierLowerName(next);
            if (nextName == null) {
                break;
            }
            Item installer = getTierInstallerItem(nextName);
            if (installer == null) {
                break;
            }
            installers.add(installer);
            tier = next;
        }
        return installers;
    }
}
