package com.mekanism.card.item;

import com.mekanism.card.MekanismCard;
import com.mekanism.card.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.List;

public class SuperFusionCard extends UltimateTierInstaller {

    public enum FusionMode {
        TIER_INSTALL("tooltip.mekanism_card.super_fusion.mode.tier", ChatFormatting.GREEN),
        MODULE_UPGRADE("tooltip.mekanism_card.super_fusion.mode.module", ChatFormatting.GOLD),
        MEMORY_COPY("tooltip.mekanism_card.super_fusion.mode.memory", ChatFormatting.AQUA),
        FULL_PASTE("tooltip.mekanism_card.super_fusion.mode.full", ChatFormatting.LIGHT_PURPLE);

        private final String translationKey;
        private final ChatFormatting color;

        FusionMode(String translationKey, ChatFormatting color) {
            this.translationKey = translationKey;
            this.color = color;
        }

        public Component getDisplayName() {
            return Component.translatable(translationKey);
        }

        public ChatFormatting getColor() {
            return color;
        }

        private FusionMode next() {
            FusionMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        public static FusionMode byId(int id) {
            FusionMode[] values = values();
            if (id < 0 || id >= values.length) {
                return TIER_INSTALL;
            }
            return values[id];
        }
    }

    public FusionMode getFusionMode(ItemStack stack) {
        return FusionMode.byId(stack.getOrDefault(ModDataComponents.FUSION_MODE.get(), FusionMode.TIER_INSTALL.ordinal()));
    }

    public void cycleFusionMode(ItemStack stack, Player player) {
        setFusionMode(stack, player, getFusionMode(stack).next());
    }

    public void setFusionMode(ItemStack stack, Player player, FusionMode mode) {
        stack.set(ModDataComponents.FUSION_MODE.get(), mode.ordinal());
        if (mode == FusionMode.MEMORY_COPY) {
            setFuzzyMode(stack, false);
        }
        player.displayClientMessage(Component.translatable("message.mekanism_card.super_fusion.mode_switched", mode.getDisplayName())
                .withStyle(mode.getColor()), true);
    }

    public void clearMemoryData(ItemStack stack, Player player) {
        MemoryCard.handleClearMachineDataStatic(player, stack);
    }

    public boolean isFuzzyMode(ItemStack stack) {
        return getFusionMode(stack) != FusionMode.MEMORY_COPY
                && stack.getOrDefault(ModDataComponents.FUZZY_TARGET_MODE.get(), false);
    }

    public void setFuzzyMode(ItemStack stack, boolean fuzzy) {
        stack.set(ModDataComponents.FUZZY_TARGET_MODE.get(), fuzzy);
    }

    public void handleModuleOperation(Level level, BlockPos pos, Direction face, Player player, ItemStack stack) {
        Collection<BlockPos> positions = List.of(pos);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && com.mekanism.card.compat.UltimineCompat.isPressed(serverPlayer)) {
            Collection<BlockPos> ultiminePositions = com.mekanism.card.compat.UltimineCompat.getCachedPositions(
                    serverPlayer, pos, face);
            if (ultiminePositions.size() > 1) {
                positions = ultiminePositions;
            }
        }
        if (!isFuzzyMode(stack)) {
            var sourceBlock = MemoryCard.getStoredSourceBlock(stack);
            if (sourceBlock == null) {
                player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.no_data")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
            positions = positions.stream()
                    .filter(targetPos -> level.getBlockState(targetPos).getBlock() == sourceBlock)
                    .toList();
            if (positions.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.type_mismatch")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
        }
        moduleConfigurator().handlePositions(level, positions, player, stack);
    }

    public MassUpgradeConfigurator.Mode getModuleMode(ItemStack stack) {
        return moduleConfigurator().getCurrentMode(stack);
    }

    public void handleMemoryOperation(Level level, BlockPos pos, Direction face, Player player, ItemStack stack) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && com.mekanism.card.compat.UltimineCompat.isPressed(serverPlayer)) {
            var positions = com.mekanism.card.compat.UltimineCompat.getCachedPositions(serverPlayer, pos, face);
            if (positions.size() > 1) {
                MemoryCard.handleBatchPasteStatic(level, positions, player, stack);
                return;
            }
        }
        MemoryCard.handlePasteStatic(level, pos, player, stack);
    }

    public void handleFullOperation(Level level, BlockPos pos, Direction face, Player player, ItemStack stack) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && com.mekanism.card.compat.UltimineCompat.isPressed(serverPlayer)) {
            var positions = com.mekanism.card.compat.UltimineCompat.getCachedPositions(serverPlayer, pos, face);
            if (positions.size() > 1) {
                MemoryCard.handleBatchFullPasteStatic(level, positions, player, stack, this);
                return;
            }
        }
        MemoryCard.handleFullPasteStatic(level, pos, player, stack, this);
    }

    public void handleMemoryCopy(Level level, BlockPos pos, Player player, ItemStack stack) {
        MemoryCard.handleCopyStatic(level, pos, player, stack);
    }

    public void handleMiddleClick(Level level, BlockPos pos, Player player, ItemStack stack) {
        moduleConfigurator().handleMiddleClick(level, pos, player, stack);
    }

    @Override
    protected boolean supportsAreaMode() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (TooltipHelper.isDescriptionKeyDown()) {
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.fusion_execute",
                    "tooltip.mekanism_card.key.right_machine", "tooltip.mekanism_card.key.shift_right_machine"));
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.fusion_settings",
                    "tooltip.mekanism_card.key.left_air", "tooltip.mekanism_card.key.shift_left_air"));
            tooltip.add(TooltipHelper.selectionShortcutLine("tooltip.mekanism_card.shortcut.fusion_batch"));
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.memory_clear",
                    "tooltip.mekanism_card.key.shift_right_air"));
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.middle_install",
                    "tooltip.mekanism_card.key.middle_machine", "tooltip.mekanism_card.key.ultimine_middle"));
            return;
        }

        super.appendHoverText(stack, context, tooltip, flag);

        FusionMode fusionMode = getFusionMode(stack);
        tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.current_mode", fusionMode.getDisplayName())
                .withStyle(fusionMode.getColor()));
        tooltip.add(Component.translatable("tooltip.mekanism_card.super_fusion.module_mode", getModuleMode(stack).getDisplayName())
                .withStyle(ChatFormatting.GOLD));
    }

    public MassUpgradeConfigurator moduleConfigurator() {
        return (MassUpgradeConfigurator) MekanismCard.MASS_UPGRADE_CONFIGURATOR.get();
    }
}
