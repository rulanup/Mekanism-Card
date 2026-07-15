package com.mekanism.card;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = MekanismCard.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        BlockPos firstCorner = ModKeyHandler.getFirstSelectionCorner();
        if (firstCorner == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            ModKeyHandler.clearSelectionCorner();
            return;
        }
        ItemStack stack = minecraft.player.getMainHandItem();
        if (!ModKeyHandler.isBatchSelectionTool(stack)) {
            ModKeyHandler.clearSelectionCorner();
            return;
        }

        BlockPos secondCorner = firstCorner;
        HitResult hitResult = minecraft.hitResult;
        if (hitResult instanceof BlockHitResult blockHit && hitResult.getType() == HitResult.Type.BLOCK) {
            secondCorner = blockHit.getBlockPos();
        }

        double minX = Math.min(firstCorner.getX(), secondCorner.getX());
        double minY = Math.min(firstCorner.getY(), secondCorner.getY());
        double minZ = Math.min(firstCorner.getZ(), secondCorner.getZ());
        double maxX = Math.max(firstCorner.getX(), secondCorner.getX()) + 1;
        double maxY = Math.max(firstCorner.getY(), secondCorner.getY()) + 1;
        double maxZ = Math.max(firstCorner.getZ(), secondCorner.getZ()) + 1;

        Vec3 cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        event.getPoseStack().pushPose();
        event.getPoseStack().translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        LevelRenderer.renderLineBox(event.getPoseStack(), buffer,
                minX, minY, minZ, maxX, maxY, maxZ,
                1.0F, 0.2F, 0.2F, 1.0F);
        event.getPoseStack().popPose();
        bufferSource.endBatch(RenderType.lines());
    }
}
