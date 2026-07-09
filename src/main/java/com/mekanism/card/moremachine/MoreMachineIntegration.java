package com.mekanism.card.moremachine;

import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mekanism: MoreMachine 模组联动集成。
 *
 * <p>与 Extras / EvolvedMekanism 不同,MoreMachine(mod id: {@code mekmm})
 * <b>没有引入任何新的 BaseTier 值</b>,也没有自定义的 AdvancedTier 系统、
 * tier installer 物品或可用的新 Upgrade(THREAD 是未注册的 WIP 死代码)。</p>
 *
 * <p>MoreMachine 的所有可升级方块(工厂、Advanced Factory、大型化学罐)
 * 都走 Mekanism 原生的 {@code AttributeUpgradeable} + {@code AttributeTier}
 * + {@code BaseTier} 路径。因此 Mekanism-Card 的 UltimateTierInstaller
 * 已经能直接升级这些方块,无需额外反射获取 tier 常量或 installer 物品。</p>
 *
 * <p>当 EvolvedMekanism 同时加载时,MoreMachine 的工厂会复用 EMek 注入的
 * BaseTier(OVERCLOCKED/QUANTUM/DENSE/MULTIVERSAL/CREATIVE),这部分升级链
 * 由 {@link com.mekanism.card.evolved.EvolvedIntegration} 自动覆盖。</p>
 *
 * <p>本类仅用于模组加载检测,以便在 Ultimate Tier Installer 的 tooltip 中
 * 明确显示对 MoreMachine 的支持,增强用户可见性。</p>
 */
public final class MoreMachineIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoreMachineIntegration.class);

    public static final String MOD_ID = "mekmm";

    private MoreMachineIntegration() {
    }

    /** 检测 Mekanism: MoreMachine 模组是否加载 */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}
