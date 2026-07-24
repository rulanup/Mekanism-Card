package com.mekanism.card;

import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.MemoryCard;
import com.mekanism.card.item.SuperFusionCard;
import com.mekanism.card.item.UltimateTierInstaller;
import mekanism.api.IConfigCardAccess;
import mekanism.api.inventory.qio.IQIOFrequency;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;


@EventBusSubscriber(modid = MekanismCard.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (stack.getItem() instanceof MemoryCard) {
            BlockPos pos = event.getPos();
            if (player.isShiftKeyDown()
                    && event.getLevel().getBlockEntity(pos) instanceof IQIOFrequencyHolder
                    && handleQIOBinding(event, player, stack)) {
                return;
            }
            boolean isTargetingMachine = event.getLevel().getBlockEntity(pos) instanceof IConfigCardAccess;
            if (isTargetingMachine) {
                if (!event.getLevel().isClientSide) {
                    if (player.isShiftKeyDown()) {
                        MemoryCard.handleCopyStatic(event.getLevel(), pos, player, stack);
                    } else {
                        handleMemoryPaste(event, player, stack);
                    }
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }

        if (stack.getItem() instanceof SuperFusionCard fusionCard) {
            if (player.isShiftKeyDown()) {
                if (event.getLevel().getBlockEntity(event.getPos()) instanceof IQIOFrequencyHolder
                        && handleQIOBinding(event, player, stack)) {
                    return;
                }
                if (event.getLevel().getBlockEntity(event.getPos()) instanceof IConfigCardAccess) {
                    if (!event.getLevel().isClientSide) {
                        fusionCard.handleMemoryCopy(event.getLevel(), event.getPos(), player, stack);
                    }
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                    return;
                }
                if (!event.getLevel().isClientSide) {
                    fusionCard.handleMemoryCopy(event.getLevel(), event.getPos(), player, stack);
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
            if (event.getLevel().isClientSide) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }

            switch (fusionCard.getFusionMode(stack)) {
                case TIER_INSTALL -> fusionCard.handleTierOperation(event.getLevel(), event.getPos(),
                        event.getFace() == null ? net.minecraft.core.Direction.UP : event.getFace(), player, stack);
                case MODULE_UPGRADE -> fusionCard.handleModuleOperation(event.getLevel(), event.getPos(),
                        event.getFace() == null ? net.minecraft.core.Direction.UP : event.getFace(), player, stack);
                case MEMORY_COPY -> fusionCard.handleMemoryOperation(event.getLevel(), event.getPos(),
                        event.getFace() == null ? net.minecraft.core.Direction.UP : event.getFace(), player, stack);
                case FULL_PASTE -> fusionCard.handleFullOperation(event.getLevel(), event.getPos(),
                        event.getFace() == null ? net.minecraft.core.Direction.UP : event.getFace(), player, stack);
            }
            event.setCanceled(true);
            return;
        }

        if (stack.getItem() instanceof UltimateTierInstaller installer) {
            if (!UltimateTierInstaller.isUpgradeTarget(event.getLevel(), event.getPos())) {
                return;
            }
            if (!event.getLevel().isClientSide) {
                installer.useOn(new UseOnContext(player, event.getHand(), event.getHitVec()));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (stack.getItem() instanceof IFrequencyItem freqItem && freqItem.getFrequencyType() == FrequencyType.QIO && handleQIOBinding(event, player, stack)) {
            return;
        }

        if (!(stack.getItem() instanceof MassUpgradeConfigurator configurator)) {
            return;
        }

        if (player.isShiftKeyDown()
                && event.getLevel().getBlockEntity(event.getPos()) instanceof IConfigCardAccess) {
            if (!event.getLevel().isClientSide) {
                MemoryCard.handleCopyStatic(event.getLevel(), event.getPos(), player, stack);
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (event.getLevel().isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && com.mekanism.card.compat.UltimineCompat.isPressed(serverPlayer)) {
            configurator.handleWithUltimine(event.getLevel(), event.getPos(),
                    event.getFace() == null ? net.minecraft.core.Direction.UP : event.getFace(), player, stack);
        } else {
            configurator.handleSingleMode(event.getLevel(), event.getPos(), player, stack);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void handleMemoryPaste(PlayerInteractEvent.RightClickBlock event, Player player, ItemStack stack) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && com.mekanism.card.compat.UltimineCompat.isPressed(serverPlayer)) {
            var positions = com.mekanism.card.compat.UltimineCompat.getCachedPositions(
                    serverPlayer, event.getPos(),
                    event.getFace() == null ? net.minecraft.core.Direction.UP : event.getFace());
            if (positions.size() > 1) {
                MemoryCard.handleBatchPasteStatic(event.getLevel(), positions, player, stack);
                return;
            }
        }
        MemoryCard.handlePasteStatic(event.getLevel(), event.getPos(), player, stack);
    }

    private static boolean handleQIOBinding(PlayerInteractEvent.RightClickBlock event, Player player, ItemStack stack) {
        if (!player.isShiftKeyDown()) {
            return false;
        }

        BlockPos pos = event.getPos();
        if (!(event.getLevel().getBlockEntity(pos) instanceof IQIOFrequencyHolder holder)) {
            return false;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (event.getLevel().isClientSide) {
            return true;
        }

        QIOFrequency freq = holder.getQIOFrequency();
        if (freq == null || !freq.isValid()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.qio_no_frequency")
                    .withStyle(ChatFormatting.RED), true);
            return true;
        }

        FrequencyIdentity identity = freq.getIdentity();
        stack.set(MekanismDataComponents.QIO_FREQUENCY.get(), FrequencyAware.create(FrequencyType.QIO, identity, player.getUUID()));

        player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.qio_bound_success", freq.getName())
                .withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (stack.getItem() instanceof MemoryCard) {
            if (event.getLevel().isClientSide()) {
                return;
            }

            if (player.isShiftKeyDown()) {
                MemoryCard.handleClearMachineDataStatic(player, stack);
                event.setCanceled(true);
            }
        }
    }

}
