package com.mekanism.card.extras;

import mekanism.api.Upgrade;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Mekanism Extras 模组联动集成。
 *
 * <p>通过反射访问 Extras 的 API，运行时检测模组是否加载。
 * 这样 Mekanism-Card 不需要在编译期依赖 Extras，运行时按需启用功能。</p>
 *
 * <p>提供的能力：
 * <ul>
 *   <li>检测 Extras 是否加载</li>
 *   <li>获取方块的 AdvancedTier（absolute/supreme/cosmic/infinite）</li>
 *   <li>升级方块到指定的 AdvancedTier</li>
 *   <li>获取对应等级的 TierInstaller 物品</li>
 *   <li>把 Extras 新增的 Upgrade（STACK/IONIC_MEMBRANE/CREATIVE）映射到对应物品</li>
 * </ul></p>
 */
public final class ExtrasIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtrasIntegration.class);

    public static final String MOD_ID = "mekanism_extras";

    /** AdvancedTier 名称常量，按从低到高排序 */
    public static final String TIER_ABSOLUTE = "absolute";
    public static final String TIER_SUPREME = "supreme";
    public static final String TIER_COSMIC = "cosmic";
    public static final String TIER_INFINITE = "infinite";

    private static volatile boolean initialized = false;
    private static volatile boolean available = false;

    // 反射缓存
    private static Class<?> advancedTierClass;
    private static Class<?> extraAttributeClass;
    private static Class<?> extraAttributeUpgradeableClass;
    private static Class<?> extraItemsClass;
    private static Class<?> extraUpgradeClass;

    private static Method getAdvancedTierMethod;
    private static Method extraAttributeUpgradeResultMethod;
    private static Method extraAttributeGetUpgradeableMethod;

    // AdvancedTier 枚举常量缓存（按 lower name 索引）
    private static final Map<String, Object> ADVANCED_TIER_VALUES = new HashMap<>();

    // Upgrade -> Item 映射缓存（Extras 的 STACK/IONIC_MEMBRANE/CREATIVE）
    private static final Map<Upgrade, Item> EXTRA_UPGRADE_ITEMS = new HashMap<>();

    // TierInstaller 物品缓存（按 tier name 索引）
    private static final Map<String, Item> TIER_INSTALLER_ITEMS = new HashMap<>();

    private ExtrasIntegration() {
    }

    /** 检测 Extras 模组是否加载，并惰性初始化反射缓存 */
    public static boolean isLoaded() {
        if (!ModList.get().isLoaded(MOD_ID)) {
            return false;
        }
        if (!initialized) {
            synchronized (ExtrasIntegration.class) {
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
            advancedTierClass = Class.forName("com.jerry.mekextras.api.tier.AdvancedTier");
            extraAttributeClass = Class.forName("com.jerry.mekextras.common.block.attribute.ExtraAttribute");
            extraAttributeUpgradeableClass = Class.forName("com.jerry.mekextras.common.block.attribute.ExtraAttributeUpgradeable");
            extraItemsClass = Class.forName("com.jerry.mekextras.common.registries.ExtraItems");
            extraUpgradeClass = Class.forName("com.jerry.mekextras.api.ExtraUpgrade");

            // 枚举常量：AdvancedTier.values()
            Object[] tiers = advancedTierClass.getEnumConstants();
            Method getLowerName = advancedTierClass.getMethod("getLowerName");
            for (Object tier : tiers) {
                String lower = (String) getLowerName.invoke(tier);
                ADVANCED_TIER_VALUES.put(lower, tier);
            }

            // ExtraAttribute.getAdvancedTier(Holder) 静态方法
            getAdvancedTierMethod = extraAttributeClass.getMethod("getAdvancedTier", Holder.class);

            // Attribute.get(Holder, Class) 静态方法（来自 Mekanism）
            Class<?> attributeClass = Class.forName("mekanism.common.block.attribute.Attribute");
            extraAttributeGetUpgradeableMethod = attributeClass.getMethod("get", Holder.class, Class.class);

            // ExtraAttributeUpgradeable.upgradeResult(BlockState, AdvancedTier)
            extraAttributeUpgradeResultMethod = extraAttributeUpgradeableClass.getMethod(
                    "upgradeResult", BlockState.class, advancedTierClass);

            // 缓存 TierInstaller 物品
            cacheTierInstallerItem(TIER_ABSOLUTE, "ABSOLUTE_TIER_INSTALLER");
            cacheTierInstallerItem(TIER_SUPREME, "SUPREME_TIER_INSTALLER");
            cacheTierInstallerItem(TIER_COSMIC, "COSMIC_TIER_INSTALLER");
            cacheTierInstallerItem(TIER_INFINITE, "INFINITE_TIER_INSTALLER");

            // 缓存 Upgrade -> Item 映射
            cacheExtraUpgradeItem("STACK", "STACK");
            cacheExtraUpgradeItem("IONIC_MEMBRANE", "IONIC_MEMBRANE");
            cacheExtraUpgradeItem("CREATIVE", "CREATIVE");

            available = true;
            LOGGER.info("[Mekanism-Card] Mekanism Extras integration enabled");
        } catch (Exception e) {
            available = false;
            LOGGER.warn("[Mekanism-Card] Failed to initialize Mekanism Extras integration, features will be limited", e);
        }
    }

    private static void cacheTierInstallerItem(String tierName, String fieldName) {
        try {
            Field field = extraItemsClass.getField(fieldName);
            Object registryObj = field.get(null);
            // ItemRegistryObject 有 get() 方法返回 Item
            Method getMethod = registryObj.getClass().getMethod("get");
            Item item = (Item) getMethod.invoke(registryObj);
            if (item != null) {
                TIER_INSTALLER_ITEMS.put(tierName, item);
            }
        } catch (Exception e) {
            LOGGER.debug("[Mekanism-Card] Failed to cache tier installer for {}: {}", tierName, e.getMessage());
        }
    }

    private static void cacheExtraUpgradeItem(String upgradeFieldName, String itemFieldName) {
        try {
            Field upgradeField = extraUpgradeClass.getField(upgradeFieldName);
            Upgrade upgrade = (Upgrade) upgradeField.get(null);

            Field itemField = extraItemsClass.getField(itemFieldName);
            Object registryObj = itemField.get(null);
            Method getMethod = registryObj.getClass().getMethod("get");
            Item item = (Item) getMethod.invoke(registryObj);

            if (upgrade != null && item != null) {
                EXTRA_UPGRADE_ITEMS.put(upgrade, item);
            }
        } catch (Exception e) {
            LOGGER.debug("[Mekanism-Card] Failed to cache extra upgrade item for {}: {}", upgradeFieldName, e.getMessage());
        }
    }

    /**
     * 获取方块当前的 AdvancedTier 名称（lower case），若不是 Extras 方块返回 null。
     */
    @Nullable
    public static String getAdvancedTierName(Holder<Block> blockHolder) {
        if (!isLoaded()) {
            return null;
        }
        try {
            Object tier = getAdvancedTierMethod.invoke(null, blockHolder);
            if (tier == null) {
                return null;
            }
            Method getLowerName = advancedTierClass.getMethod("getLowerName");
            return (String) getLowerName.invoke(tier);
        } catch (Exception e) {
            LOGGER.debug("[Mekanism-Card] Failed to get AdvancedTier for block", e);
            return null;
        }
    }

    /**
     * 获取方块的 ExtraAttributeUpgradeable 实例，若不是 Extras 可升级方块返回 null。
     */
    @Nullable
    public static Object getExtraAttributeUpgradeable(Holder<Block> blockHolder) {
        if (!isLoaded()) {
            return null;
        }
        try {
            return extraAttributeGetUpgradeableMethod.invoke(null, blockHolder, extraAttributeUpgradeableClass);
        } catch (Exception e) {
            LOGGER.debug("[Mekanism-Card] Failed to get ExtraAttributeUpgradeable", e);
            return null;
        }
    }

    /**
     * 调用 ExtraAttributeUpgradeable.upgradeResult(current, tier) 获取升级后的 BlockState。
     */
    @Nullable
    public static BlockState upgradeResult(Object upgradeable, BlockState current, String targetTierName) {
        if (!isLoaded() || upgradeable == null) {
            return null;
        }
        Object targetTier = ADVANCED_TIER_VALUES.get(targetTierName);
        if (targetTier == null) {
            return null;
        }
        try {
            return (BlockState) extraAttributeUpgradeResultMethod.invoke(upgradeable, current, targetTier);
        } catch (Exception e) {
            LOGGER.debug("[Mekanism-Card] Failed to upgrade result to {}", targetTierName, e);
            return null;
        }
    }

    /**
     * 获取指定 AdvancedTier 对应的 TierInstaller 物品。
     */
    @Nullable
    public static Item getTierInstallerItem(String tierName) {
        if (!isLoaded()) {
            return null;
        }
        return TIER_INSTALLER_ITEMS.get(tierName);
    }

    /**
     * 把 Extras 新增的 Upgrade（STACK/IONIC_MEMBRANE/CREATIVE）映射到对应 Item。
     * 其他 Upgrade 返回 null。
     */
    @Nullable
    public static Item getExtraUpgradeItem(Upgrade upgrade) {
        if (!isLoaded()) {
            return null;
        }
        return EXTRA_UPGRADE_ITEMS.get(upgrade);
    }

    /**
     * 获取下一个 AdvancedTier 名称。输入 "absolute" 返回 "supreme"，依此类推。
     * 输入 "infinite" 或未知值返回 null。
     */
    @Nullable
    public static String getNextTierName(String currentTierName) {
        return switch (currentTierName) {
            case TIER_ABSOLUTE -> TIER_SUPREME;
            case TIER_SUPREME -> TIER_COSMIC;
            case TIER_COSMIC -> TIER_INFINITE;
            default -> null;
        };
    }

    public static int getTierIndex(@Nullable String tierName) {
        if (tierName == null) {
            return -1;
        }
        return switch (tierName) {
            case TIER_ABSOLUTE -> 0;
            case TIER_SUPREME -> 1;
            case TIER_COSMIC -> 2;
            case TIER_INFINITE -> 3;
            default -> Integer.MIN_VALUE;
        };
    }

    public static java.util.List<Item> getRequiredInstallersToTier(@Nullable String currentTierName,
                                                                   String desiredTierName) {
        int currentIndex = getTierIndex(currentTierName);
        int desiredIndex = getTierIndex(desiredTierName);
        if (currentIndex == Integer.MIN_VALUE || desiredIndex == Integer.MIN_VALUE || currentIndex >= desiredIndex) {
            return java.util.List.of();
        }
        java.util.List<Item> installers = new java.util.ArrayList<>();
        for (int index = currentIndex + 1; index <= desiredIndex; index++) {
            String tierName = switch (index) {
                case 0 -> TIER_ABSOLUTE;
                case 1 -> TIER_SUPREME;
                case 2 -> TIER_COSMIC;
                case 3 -> TIER_INFINITE;
                default -> null;
            };
            Item installer = tierName == null ? null : getTierInstallerItem(tierName);
            if (installer == null) {
                return java.util.List.of();
            }
            installers.add(installer);
        }
        return installers;
    }

    /**
     * 获取从当前 AdvancedTier 升级到 INFINITE 所需的所有 TierInstaller 物品列表（按顺序）。
     * 例如当前是 "absolute"，返回 [SUPREME_TIER_INSTALLER, COSMIC_TIER_INSTALLER, INFINITE_TIER_INSTALLER]。
     */
    public static java.util.List<Item> getRequiredInstallersToInfinite(String currentTierName) {
        return getRequiredInstallersToTier(currentTierName, TIER_INFINITE);
    }
}
