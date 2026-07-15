package com.mekanism.card.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mekanism.card.ModDataComponents;
import com.mekanism.card.MekanismCard;
import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.SuperFusionCard;
import com.mekanism.card.network.FusionActionPayload;
import com.mekanism.card.network.ToolModePayload;
import com.mekanism.card.network.TargetModePayload;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class SuperFusionCardScreen extends Screen {

    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 116;
    private static final int PADDING = 8;
    private static final int COLOR_PANEL = 0xFFC6C6C6;
    private static final int COLOR_HIGHLIGHT = 0xFFFFFFFF;
    private static final int COLOR_MID_HIGHLIGHT = 0xFFDBDBDB;
    private static final int COLOR_SHADOW = 0xFF555555;
    private static final int COLOR_DEEP_SHADOW = 0xFF373737;
    private static final int COLOR_RECESSED = 0xFF9B9B9B;
    private static final int COLOR_TEXT = 0xFF404040;
    private static final int COLOR_DIM = 0xFF666666;
    private static final int COLOR_CYAN = 0xFF007F8F;
    private static final int COLOR_GREEN = 0xFF2E7D32;
    private static final int COLOR_ORANGE = 0xFF9A5A00;
    private static final ResourceLocation MEKANISM_BUTTON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mekanism", "gui/button.png");

    private final SuperFusionCard fusionCard;
    private Button fusionModeButton;
    private Button moduleModeButton;
    private Button targetModeButton;

    public SuperFusionCardScreen(ItemStack cardStack) {
        super(Component.translatable("gui.mekanism_card.super_fusion.title"));
        this.fusionCard = (SuperFusionCard) cardStack.getItem();
    }

    private ItemStack getCardStack() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack mainHand = player.getMainHandItem();
        return mainHand.getItem() instanceof SuperFusionCard ? mainHand : ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        int buttonX = x + 66;
        int buttonWidth = 100;
        int buttonHeight = 14;

        fusionModeButton = addRenderableWidget(new MekanismStyleButton(buttonX, y + 34,
                buttonWidth, buttonHeight, Component.empty(), button -> onCycleFusionMode()));
        moduleModeButton = addRenderableWidget(new MekanismStyleButton(buttonX, y + 51,
                buttonWidth, buttonHeight, Component.empty(), button -> onToggleModuleMode()));
        targetModeButton = addRenderableWidget(new MekanismStyleButton(buttonX, y + 68,
                buttonWidth, buttonHeight, Component.empty(), button -> onToggleTargetMode()));
    }

    private void onCycleFusionMode() {
        ItemStack stack = getCardStack();
        if (stack.isEmpty()) {
            return;
        }
        SuperFusionCard.FusionMode current = fusionCard.getFusionMode(stack);
        SuperFusionCard.FusionMode[] values = SuperFusionCard.FusionMode.values();
        SuperFusionCard.FusionMode next = values[(current.ordinal() + 1) % values.length];
        stack.set(ModDataComponents.FUSION_MODE.get(), next.ordinal());
        if (next == SuperFusionCard.FusionMode.MEMORY_COPY) {
            fusionCard.setFuzzyMode(stack, false);
        }
        PacketDistributor.sendToServer(FusionActionPayload.cycleMode());
    }

    private void onToggleModuleMode() {
        ItemStack stack = getCardStack();
        if (stack.isEmpty() || fusionCard.getFusionMode(stack) != SuperFusionCard.FusionMode.MODULE_UPGRADE) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            MassUpgradeConfigurator configurator =
                    (MassUpgradeConfigurator) MekanismCard.MASS_UPGRADE_CONFIGURATOR.get();
            MassUpgradeConfigurator.Mode targetMode = configurator.getCurrentMode(stack) == MassUpgradeConfigurator.Mode.INSTALL
                    ? MassUpgradeConfigurator.Mode.CLEAR
                    : MassUpgradeConfigurator.Mode.INSTALL;
            configurator.setMode(stack, player, targetMode);
            PacketDistributor.sendToServer(new ToolModePayload(targetMode == MassUpgradeConfigurator.Mode.CLEAR));
        }
    }

    private void onToggleTargetMode() {
        ItemStack stack = getCardStack();
        if (stack.isEmpty() || fusionCard.getFusionMode(stack) == SuperFusionCard.FusionMode.MEMORY_COPY) {
            return;
        }
        boolean fuzzy = !fusionCard.isFuzzyMode(stack);
        fusionCard.setFuzzyMode(stack, fuzzy);
        PacketDistributor.sendToServer(new TargetModePayload(fuzzy));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ItemStack stack = getCardStack();
        if (stack.isEmpty()) {
            onClose();
            return;
        }

        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;
        drawRaisedPanel(graphics, x, y, PANEL_WIDTH, PANEL_HEIGHT);

        graphics.renderItem(stack, x + PADDING, y + 6);
        graphics.drawString(font, title, x + 29, y + 10, COLOR_TEXT, false);

        IStrictEnergyHandler energyHandler = Capabilities.STRICT_ENERGY.getCapability(stack);
        long energy = energyHandler == null ? 0 : energyHandler.getEnergy(0);
        long maxEnergy = energyHandler == null ? 0 : energyHandler.getMaxEnergy(0);
        boolean hasEnergy = energy > 0;
        Component status = Component.translatable(hasEnergy
                ? "gui.mekanism_card.super_fusion.status_ready"
                : "gui.mekanism_card.super_fusion.status_no_energy");
        int statusColor = hasEnergy ? COLOR_GREEN : COLOR_ORANGE;
        int statusX = x + PANEL_WIDTH - PADDING - font.width(status);
        graphics.fill(x + PANEL_WIDTH - PADDING - 4, y + 12,
                x + PANEL_WIDTH - PADDING, y + 16, statusColor);
        if (x + 29 + font.width(title) + 8 < statusX) {
            graphics.drawString(font, status, statusX - 7, y + 10, statusColor, false);
        }

        drawRecessedPanel(graphics, x + PADDING, y + 31, PANEL_WIDTH - PADDING * 2, 54);
        SuperFusionCard.FusionMode fusionMode = fusionCard.getFusionMode(stack);
        MassUpgradeConfigurator.Mode moduleMode = fusionCard.getModuleMode(stack);
        fusionModeButton.setMessage(compactFusionModeName(fusionMode));
        moduleModeButton.setMessage(moduleMode.getDisplayName());
        targetModeButton.setMessage(Component.translatable(fusionCard.isFuzzyMode(stack)
                ? "mekanism_card.target_mode.fuzzy"
                : "mekanism_card.target_mode.precise"));
        moduleModeButton.active = fusionMode == SuperFusionCard.FusionMode.MODULE_UPGRADE;
        targetModeButton.active = fusionMode != SuperFusionCard.FusionMode.MEMORY_COPY;
        updateButtonTooltips(fusionMode, moduleMode, fusionCard.isFuzzyMode(stack));

        drawStatusLabel(graphics, x + 13, y + 37,
                Component.translatable("gui.mekanism_card.super_fusion.fusion_mode_short"));
        drawStatusLabel(graphics, x + 13, y + 54,
                Component.translatable("gui.mekanism_card.super_fusion.module_mode_short"));
        drawStatusLabel(graphics, x + 13, y + 71,
                Component.translatable("gui.mekanism_card.super_fusion.target_mode_short"));

        Component energyLabel = Component.translatable("gui.mekanism_card.super_fusion.energy_short");
        Component energyValue = Component.literal(energy + " / " + maxEnergy + " FE");
        graphics.drawString(font, energyLabel, x + PADDING, y + 90, COLOR_TEXT, false);
        graphics.drawString(font, energyValue,
                x + PANEL_WIDTH - PADDING - font.width(energyValue), y + 90, COLOR_DIM, false);
        drawRecessedPanel(graphics, x + PADDING, y + 101, PANEL_WIDTH - PADDING * 2, 7);
        int barWidth = PANEL_WIDTH - PADDING * 2 - 4;
        int filledWidth = maxEnergy <= 0 ? 0
                : (int) Math.round(barWidth * Math.min(1.0, (double) energy / maxEnergy));
        graphics.fill(x + PADDING + 2, y + 103,
                x + PADDING + 2 + filledWidth, y + 106, hasEnergy ? COLOR_CYAN : COLOR_DIM);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawStatusLabel(GuiGraphics graphics, int x, int y, Component label) {
        graphics.drawString(font, label, x, y, COLOR_TEXT, false);
    }

    private Component compactFusionModeName(SuperFusionCard.FusionMode mode) {
        return Component.translatable(switch (mode) {
            case TIER_INSTALL -> "gui.mekanism_card.super_fusion.mode_compact.tier";
            case MODULE_UPGRADE -> "gui.mekanism_card.super_fusion.mode_compact.module";
            case MEMORY_COPY -> "gui.mekanism_card.super_fusion.mode_compact.memory";
            case FULL_PASTE -> "gui.mekanism_card.super_fusion.mode_compact.full";
        });
    }

    private void updateButtonTooltips(SuperFusionCard.FusionMode fusionMode,
                                      MassUpgradeConfigurator.Mode moduleMode, boolean fuzzy) {
        String fusionTooltip = switch (fusionMode) {
            case TIER_INSTALL -> "gui.mekanism_card.super_fusion.tooltip.mode.tier";
            case MODULE_UPGRADE -> "gui.mekanism_card.super_fusion.tooltip.mode.module";
            case MEMORY_COPY -> "gui.mekanism_card.super_fusion.tooltip.mode.memory";
            case FULL_PASTE -> "gui.mekanism_card.super_fusion.tooltip.mode.full";
        };
        fusionModeButton.setTooltip(Tooltip.create(Component.translatable(fusionTooltip)));
        moduleModeButton.setTooltip(Tooltip.create(Component.translatable(moduleMode == MassUpgradeConfigurator.Mode.INSTALL
                ? "gui.mekanism_card.super_fusion.tooltip.module.install"
                : "gui.mekanism_card.super_fusion.tooltip.module.clear")));
        String targetTooltip = fusionMode == SuperFusionCard.FusionMode.MEMORY_COPY
                ? "gui.mekanism_card.super_fusion.tooltip.target.memory"
                : fuzzy
                ? "gui.mekanism_card.super_fusion.tooltip.target.fuzzy"
                : "gui.mekanism_card.super_fusion.tooltip.target.precise";
        targetModeButton.setTooltip(Tooltip.create(Component.translatable(targetTooltip)));
    }

    private void drawRaisedPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, COLOR_DEEP_SHADOW);
        graphics.fill(x, y, x + width - 2, y + height - 2, COLOR_PANEL);
        graphics.fill(x, y, x + width - 2, y + 2, COLOR_HIGHLIGHT);
        graphics.fill(x, y, x + 2, y + height - 2, COLOR_HIGHLIGHT);
        graphics.fill(x + 2, y + 2, x + width - 2, y + 3, COLOR_MID_HIGHLIGHT);
        graphics.fill(x + 2, y + 2, x + 3, y + height - 2, COLOR_MID_HIGHLIGHT);
        graphics.fill(x + 2, y + height - 4, x + width - 2, y + height - 2, COLOR_SHADOW);
        graphics.fill(x + width - 4, y + 2, x + width - 2, y + height - 2, COLOR_SHADOW);
    }

    private void drawRecessedPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, COLOR_HIGHLIGHT);
        graphics.fill(x, y, x + width - 1, y + height - 1, COLOR_SHADOW);
        graphics.fill(x + 2, y + 2, x + width - 1, y + height - 1, COLOR_RECESSED);
    }

    private static class MekanismStyleButton extends Button {

        private MekanismStyleButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int textureY = !active ? 0 : isHoveredOrFocused() ? 40 : 20;
            GuiUtils.blitNineSlicedSized(graphics, MEKANISM_BUTTON_TEXTURE,
                    getX(), getY(), getWidth(), getHeight(),
                    20, 4, 200, 20, 0, textureY, 200, 60);
            renderString(graphics, Minecraft.getInstance().font, active ? 0xFFFFFFFF : 0xFFA0A0A0);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
