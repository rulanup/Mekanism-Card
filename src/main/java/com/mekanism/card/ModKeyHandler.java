package com.mekanism.card;

import com.mekanism.card.client.ModKeyBindings;
import com.mekanism.card.client.gui.SuperFusionCardScreen;
import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.MemoryCard;
import com.mekanism.card.item.SuperFusionCard;
import com.mekanism.card.item.UltimateTierInstaller;
import com.mekanism.card.network.BatchSelectionPayload;
import com.mekanism.card.network.FusionActionPayload;
import com.mekanism.card.network.MiddleClickPayload;
import com.mekanism.card.network.ToolModePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MekanismCard.MOD_ID, value = Dist.CLIENT)
public class ModKeyHandler {

    private static BlockPos firstSelectionCorner;
    private static boolean attackKeyWasDown;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            firstSelectionCorner = null;
            attackKeyWasDown = false;
            return;
        }
        handleAttackInput(minecraft, player);
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack()) {
            if (shouldCaptureAttack()) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
            return;
        }
        if (!event.isUseItem()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        boolean targetingAir = minecraft.hitResult == null || minecraft.hitResult.getType() == HitResult.Type.MISS;
        if (stack.getItem() instanceof SuperFusionCard && player.isShiftKeyDown() && targetingAir) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(FusionActionPayload.clearMemory());
            event.setSwingHand(false);
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SuperFusionCard) && !(stack.getItem() instanceof MassUpgradeConfigurator)) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit)
                || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new MiddleClickPayload(hit.getBlockPos(), hit.getDirection()));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide() || event.getEntity().isShiftKeyDown()) {
            return;
        }
        ItemStack stack = event.getEntity().getItemInHand(event.getHand());
        if (ModKeyBindings.BATCH_SELECTION.isDown() && isBatchSelectionTool(stack)) {
            event.setCanceled(true);
        }
    }

    static BlockPos getFirstSelectionCorner() {
        return firstSelectionCorner;
    }

    static void clearSelectionCorner() {
        firstSelectionCorner = null;
    }

    static boolean isBatchSelectionTool(ItemStack stack) {
        return stack.getItem() instanceof SuperFusionCard
                || stack.getItem() instanceof MassUpgradeConfigurator
                || stack.getItem() instanceof MemoryCard
                || stack.getItem() instanceof UltimateTierInstaller;
    }

    private static boolean isHandledTool(ItemStack stack) {
        return isBatchSelectionTool(stack);
    }

    private static boolean shouldCaptureAttack() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (minecraft.screen != null || player == null || !isHandledTool(player.getMainHandItem())) {
            return false;
        }
        boolean targetingBlock = minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK;
        boolean targetingAir = minecraft.hitResult == null || minecraft.hitResult.getType() == HitResult.Type.MISS;
        if (player.isShiftKeyDown()) {
            return targetingAir && (player.getMainHandItem().getItem() instanceof SuperFusionCard
                    || player.getMainHandItem().getItem() instanceof MassUpgradeConfigurator);
        }
        return targetingBlock
                ? ModKeyBindings.BATCH_SELECTION.isDown() && isBatchSelectionTool(player.getMainHandItem())
                : targetingAir && player.getMainHandItem().getItem() instanceof SuperFusionCard;
    }

    private static void handleAttackInput(Minecraft minecraft, Player player) {
        boolean attackDown = minecraft.options.keyAttack.isDown();
        if (!attackDown) {
            attackKeyWasDown = false;
            return;
        }
        if (attackKeyWasDown) {
            return;
        }
        attackKeyWasDown = true;
        if (minecraft.screen != null || minecraft.level == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!isHandledTool(stack)) {
            firstSelectionCorner = null;
            return;
        }

        HitResult hitResult = minecraft.hitResult;
        if (hitResult instanceof BlockHitResult blockHit && hitResult.getType() == HitResult.Type.BLOCK) {
            if (player.isShiftKeyDown()) {
                return;
            }
            if (!minecraft.level.getWorldBorder().isWithinBounds(blockHit.getBlockPos())) {
                return;
            }
            if (!ModKeyBindings.BATCH_SELECTION.isDown()) {
                return;
            }
            handleSelectionClick(player, stack, blockHit.getBlockPos().immutable());
            return;
        }
        if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            return;
        }

        if (ModKeyBindings.BATCH_SELECTION.isDown()) {
            if (firstSelectionCorner != null) {
                firstSelectionCorner = null;
                player.displayClientMessage(Component.translatable("message.mekanism_card.batch_selection_cleared"), false);
            }
            return;
        }
        if (player.isShiftKeyDown()) {
            if (stack.getItem() instanceof SuperFusionCard) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(FusionActionPayload.cycleMode());
            } else if (stack.getItem() instanceof MassUpgradeConfigurator configurator) {
                MassUpgradeConfigurator.Mode targetMode = configurator.getCurrentMode(stack) == MassUpgradeConfigurator.Mode.INSTALL
                        ? MassUpgradeConfigurator.Mode.CLEAR
                        : MassUpgradeConfigurator.Mode.INSTALL;
                configurator.setMode(stack, player, targetMode);
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new ToolModePayload(targetMode == MassUpgradeConfigurator.Mode.CLEAR));
            }
            return;
        }
        if (stack.getItem() instanceof SuperFusionCard) {
            firstSelectionCorner = null;
            minecraft.setScreen(new SuperFusionCardScreen(stack));
        }
    }

    private static void handleSelectionClick(Player player, ItemStack stack, BlockPos clickedPos) {
        if (!isBatchSelectionTool(stack)) {
            firstSelectionCorner = null;
            return;
        }
        if (firstSelectionCorner == null) {
            firstSelectionCorner = clickedPos;
            player.displayClientMessage(Component.translatable(
                    "message.mekanism_card.selection_point.first", clickedPos.toShortString()), false);
        } else {
            BlockPos firstPos = firstSelectionCorner;
            firstSelectionCorner = null;
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new BatchSelectionPayload(firstPos, clickedPos));
        }
    }
}
