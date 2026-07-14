package com.mekanism.card.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mekanism.card.ModDataComponents;
import com.mekanism.card.client.ModKeyBindings;
import com.mekanism.card.item.SuperFusionCard;
import com.mekanism.card.network.SetFusionModePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

public class SuperFusionRadialScreen extends Screen {
    private static final int INNER_RADIUS = 34;
    private static final int OUTER_RADIUS = 94;
    private static final int LABEL_RADIUS = 67;
    private static final int SEGMENT_STEPS = 18;
    private static final int[] COLORS = {0xCC2D8A57, 0xCCD68A2E, 0xCC247F9E};
    private static final int[] HIGHLIGHT_COLORS = {0xF04BC77D, 0xF0F0B44D, 0xF03FB8DD};

    private final SuperFusionCard fusionCard;
    private final int initialModeId;
    private int selectedModeId;
    private boolean committed;

    public SuperFusionRadialScreen(ItemStack stack) {
        super(Component.translatable("gui.mekanism_card.super_fusion.radial.title"));
        this.fusionCard = (SuperFusionCard) stack.getItem();
        this.initialModeId = fusionCard.getFusionMode(stack).ordinal();
        this.selectedModeId = initialModeId;
    }

    @Override
    protected void init() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.mouseHandler.setIgnoreFirstMove();
        org.lwjgl.glfw.GLFW.glfwSetCursorPos(
                minecraft.getWindow().getWindow(),
                minecraft.getWindow().getWidth() / 2.0,
                minecraft.getWindow().getHeight() / 2.0);
    }

    @Override
    public void tick() {
        if (!(getCardStack().getItem() instanceof SuperFusionCard)) {
            onClose();
            return;
        }
        if (!ModKeyBindings.OPEN_FUSION_WHEEL.isDown()) {
            commitSelection();
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int centerY = height / 2;
        updateSelection(mouseX - centerX, mouseY - centerY);

        graphics.fill(0, 0, width, height, 0x66000000);
        for (int modeId = 0; modeId < SuperFusionCard.FusionMode.values().length; modeId++) {
            double start = Math.toRadians(-150 + modeId * 120 + 2);
            double end = Math.toRadians(-30 + modeId * 120 - 2);
            drawSegment(graphics, centerX, centerY, start, end,
                    modeId == selectedModeId ? HIGHLIGHT_COLORS[modeId] : COLORS[modeId]);
            drawModeLabel(graphics, centerX, centerY, modeId);
        }

        ItemStack stack = getCardStack();
        graphics.renderItem(stack, centerX - 8, centerY - 8);
        Component selectedName = SuperFusionCard.FusionMode.byId(selectedModeId).getDisplayName();
        graphics.drawCenteredString(font, selectedName, centerX, centerY + OUTER_RADIUS + 14, 0xFFFFFFFF);
        graphics.drawCenteredString(font, title, centerX, centerY - OUTER_RADIUS - 22, 0xFFE8EEF7);
    }

    private void updateSelection(double deltaX, double deltaY) {
        if (deltaX * deltaX + deltaY * deltaY < INNER_RADIUS * INNER_RADIUS) {
            selectedModeId = initialModeId;
            return;
        }
        double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
        if (angle >= -150 && angle < -30) {
            selectedModeId = SuperFusionCard.FusionMode.TIER_INSTALL.ordinal();
        } else if (angle >= -30 && angle < 90) {
            selectedModeId = SuperFusionCard.FusionMode.MODULE_UPGRADE.ordinal();
        } else {
            selectedModeId = SuperFusionCard.FusionMode.MEMORY_COPY.ordinal();
        }
    }

    private void drawSegment(GuiGraphics graphics, int centerX, int centerY, double start, double end, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (int step = 0; step < SEGMENT_STEPS; step++) {
            double angle1 = start + (end - start) * step / SEGMENT_STEPS;
            double angle2 = start + (end - start) * (step + 1) / SEGMENT_STEPS;
            float innerX1 = centerX + (float) Math.cos(angle1) * INNER_RADIUS;
            float innerY1 = centerY + (float) Math.sin(angle1) * INNER_RADIUS;
            float outerX1 = centerX + (float) Math.cos(angle1) * OUTER_RADIUS;
            float outerY1 = centerY + (float) Math.sin(angle1) * OUTER_RADIUS;
            float innerX2 = centerX + (float) Math.cos(angle2) * INNER_RADIUS;
            float innerY2 = centerY + (float) Math.sin(angle2) * INNER_RADIUS;
            float outerX2 = centerX + (float) Math.cos(angle2) * OUTER_RADIUS;
            float outerY2 = centerY + (float) Math.sin(angle2) * OUTER_RADIUS;

            vertex(buffer, matrix, innerX1, innerY1, color);
            vertex(buffer, matrix, outerX1, outerY1, color);
            vertex(buffer, matrix, outerX2, outerY2, color);
            vertex(buffer, matrix, innerX1, innerY1, color);
            vertex(buffer, matrix, outerX2, outerY2, color);
            vertex(buffer, matrix, innerX2, innerY2, color);
        }
        BufferUploader.drawWithShader(buffer.build());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, int color) {
        buffer.addVertex(matrix, x, y, 0).setColor(color);
    }

    private void drawModeLabel(GuiGraphics graphics, int centerX, int centerY, int modeId) {
        double angle = Math.toRadians(-90 + modeId * 120);
        int labelX = centerX + (int) (Math.cos(angle) * LABEL_RADIUS);
        int labelY = centerY + (int) (Math.sin(angle) * LABEL_RADIUS) - font.lineHeight / 2;
        graphics.drawCenteredString(font, SuperFusionCard.FusionMode.byId(modeId).getDisplayName(), labelX, labelY, 0xFFFFFFFF);
    }

    private ItemStack getCardStack() {
        var player = Minecraft.getInstance().player;
        return player == null ? ItemStack.EMPTY : player.getMainHandItem();
    }

    private void commitSelection() {
        if (committed) {
            return;
        }
        committed = true;
        ItemStack stack = getCardStack();
        if (stack.getItem() instanceof SuperFusionCard && selectedModeId != initialModeId) {
            stack.set(ModDataComponents.FUSION_MODE.get(), selectedModeId);
            PacketDistributor.sendToServer(new SetFusionModePayload(selectedModeId));
        }
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            commitSelection();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
