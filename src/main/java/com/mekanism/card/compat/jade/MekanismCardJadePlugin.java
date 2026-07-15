package com.mekanism.card.compat.jade;

import com.mekanism.card.MekanismCard;
import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.MemoryCard;
import com.mekanism.card.item.SuperFusionCard;
import mekanism.api.Upgrade;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class MekanismCardJadePlugin implements IWailaPlugin {

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            MekanismCard.MOD_ID, "installed_upgrades");
    private static final String UPGRADE_PREFIX = "Upgrade_";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(UpgradeComponentProvider.INSTANCE, TileEntityMekanism.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(UpgradeComponentProvider.INSTANCE, Block.class);
    }

    private enum UpgradeComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public boolean shouldRequestData(BlockAccessor accessor) {
            return isHoldingConfigurator(accessor);
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof TileEntityMekanism tile)) {
                return;
            }
            TileComponentUpgrade component = tile.getComponent();
            if (component == null) {
                return;
            }
            for (Upgrade upgrade : Upgrade.values()) {
                int installed = component.getUpgrades(upgrade);
                if (installed > 0) {
                    data.putInt(UPGRADE_PREFIX + upgrade.getSerializedName(), installed);
                }
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!isHoldingConfigurator(accessor)) {
                return;
            }

            boolean hasUpgrades = false;
            for (Upgrade upgrade : Upgrade.values()) {
                int installed = accessor.getServerData().getInt(UPGRADE_PREFIX + upgrade.getSerializedName());
                if (installed <= 0) {
                    continue;
                }
                if (!hasUpgrades) {
                    tooltip.add(Component.translatable("jade.mekanism_card.upgrades")
                            .withStyle(ChatFormatting.AQUA));
                    hasUpgrades = true;
                }
                tooltip.add(Component.translatable("jade.mekanism_card.upgrade_entry",
                                UpgradeUtils.getStack(upgrade).getHoverName(), installed, upgrade.getMax())
                        .withStyle(ChatFormatting.GRAY));
            }
            if (!hasUpgrades) {
                tooltip.add(Component.translatable("jade.mekanism_card.no_upgrades")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        private static boolean isConfigurator(ItemStack stack) {
            return stack.getItem() instanceof MassUpgradeConfigurator
                    || stack.getItem() instanceof SuperFusionCard
                    || stack.getItem() instanceof MemoryCard
                    || stack.getItem() instanceof ItemConfigurationCard;
        }

        private static boolean isHoldingConfigurator(BlockAccessor accessor) {
            return isConfigurator(accessor.getPlayer().getMainHandItem())
                    || isConfigurator(accessor.getPlayer().getOffhandItem());
        }
    }
}
