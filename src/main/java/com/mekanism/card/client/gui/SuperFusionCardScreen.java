package com.mekanism.card.client.gui;

import com.mekanism.card.ModDataComponents;
import com.mekanism.card.MekanismCard;
import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.SuperFusionCard;
import com.mekanism.card.network.FusionActionPayload;
import com.mekanism.card.network.ToggleModePayload;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 超级融合卡的菜单界面（LLMERA 风格）。
 *
 * <p>视觉风格与 LLMERA 的 ConfigScreen 保持一致：
 * 深蓝灰背景 + 亮蓝色 1px 边框 + 浅蓝白色主文字 + 琥珀色强调。</p>
 *
 * <p>状态同步：render 时实时从 player.getMainHandItem() 获取最新 stack；
 * 按钮点击做客户端预测立即响应，同时发送网络包到服务端。</p>
 */
public class SuperFusionCardScreen extends Screen {

    // 面板尺寸
    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 240;

    // LLMERA 配色
    private static final int COLOR_BG = 0xEE10151C;
    private static final int COLOR_BORDER = 0xFF6EA8FE;
    private static final int COLOR_TEXT = 0xE8EEF7;
    private static final int COLOR_LABEL = 0xAFC8FF;
    private static final int COLOR_ACCENT = 0xFFFFD080;
    private static final int COLOR_OK = 0x9BE89B;
    private static final int COLOR_WARN = 0xFFAA66;
    private static final int COLOR_DIM = 0x888888;

    private final SuperFusionCard fusionCard;

    public SuperFusionCardScreen(ItemStack cardStack) {
        super(Component.translatable("gui.mekanism_card.super_fusion.title"));
        this.fusionCard = (SuperFusionCard) cardStack.getItem();
    }

    private ItemStack getCardStack() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return ItemStack.EMPTY;
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof SuperFusionCard) return mainHand;
        return ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - PANEL_WIDTH) / 2 + 12;
        int y = (this.height - PANEL_HEIGHT) / 2;
        int btnW = 124;
        int btnH = 20;
        int gap = 6;
        int row1Y = y + 132;
        int row2Y = row1Y + btnH + gap;
        int row3Y = row2Y + btnH + gap;

        // 第一行：切换融合模式 | 切换范围
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.mekanism_card.super_fusion.button.cycle_fusion_mode"),
                        b -> onCycleFusionMode())
                .pos(x, row1Y).size(btnW, btnH).build());
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.mekanism_card.super_fusion.button.toggle_area_mode"),
                        b -> onToggleAreaMode())
                .pos(x + btnW + gap, row1Y).size(btnW, btnH).build());

        // 第二行：切换模块选区 | 切换安装/移除
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.mekanism_card.super_fusion.button.toggle_selection"),
                        b -> onToggleSelection())
                .pos(x, row2Y).size(btnW, btnH).build());
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.mekanism_card.super_fusion.button.toggle_module_mode"),
                        b -> onToggleModuleMode())
                .pos(x + btnW + gap, row2Y).size(btnW, btnH).build());

        // 第三行：清除内存（居中）
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.mekanism_card.super_fusion.button.clear_memory"),
                        b -> onClearMemory())
                .pos(x + btnW / 2 + gap / 2, row3Y).size(btnW, btnH).build());
    }

    // ===== 按钮回调：客户端预测 + 发送网络包 =====

    private void onCycleFusionMode() {
        ItemStack stack = getCardStack();
        if (stack.isEmpty()) return;
        SuperFusionCard.FusionMode current = fusionCard.getFusionMode(stack);
        SuperFusionCard.FusionMode[] values = SuperFusionCard.FusionMode.values();
        SuperFusionCard.FusionMode next = values[(current.ordinal() + 1) % values.length];
        stack.set(ModDataComponents.FUSION_MODE.get(), next.ordinal());
        PacketDistributor.sendToServer(FusionActionPayload.cycleMode());
    }

    private void onToggleAreaMode() {
        ItemStack stack = getCardStack();
        if (stack.isEmpty()) return;
        boolean current = stack.getOrDefault(ModDataComponents.AREA_UPGRADE_MODE.get(), false);
        stack.set(ModDataComponents.AREA_UPGRADE_MODE.get(), !current);
        PacketDistributor.sendToServer(new ToggleModePayload());
    }

    private void onToggleSelection() {
        if (getCardStack().isEmpty()) return;
        PacketDistributor.sendToServer(FusionActionPayload.toggleSelection());
    }

    private void onToggleModuleMode() {
        ItemStack stack = getCardStack();
        if (stack.isEmpty()) return;
        if (fusionCard.getFusionMode(stack) == SuperFusionCard.FusionMode.MODULE_UPGRADE) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                MassUpgradeConfigurator configurator = (MassUpgradeConfigurator) MekanismCard.MASS_UPGRADE_CONFIGURATOR.get();
                configurator.toggleMode(player);
            }
        }
    }

    private void onClearMemory() {
        if (getCardStack().isEmpty()) return;
        PacketDistributor.sendToServer(FusionActionPayload.clearMemory());
    }

    /** 覆盖 renderBackground，完全不绘制背景（不使用 1.21.1 默认的世界模糊效果，也不画遮罩）。 */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 不做任何绘制，让游戏世界直接显示在背景
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 不调用 renderBackground（已在覆盖的方法中禁用），直接渲染面板和 widget

        ItemStack stack = getCardStack();
        if (stack.isEmpty()) {
            this.onClose();
            return;
        }

        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;
        int w = PANEL_WIDTH;
        int h = PANEL_HEIGHT;

        // LLMERA 风格：深蓝灰背景 + 亮蓝色 1px 边框
        graphics.fill(x, y, x + w, y + h, COLOR_BG);
        graphics.fill(x, y, x + w, y + 1, COLOR_BORDER);
        graphics.fill(x, y + h - 1, x + w, y + h, COLOR_BORDER);
        graphics.fill(x, y, x + 1, y + h, COLOR_BORDER);
        graphics.fill(x + w - 1, y, x + w, y + h, COLOR_BORDER);

        int tx = x + 12;
        int ty = y;

        // 标题
        graphics.drawString(font, this.title, tx, ty + 8, COLOR_TEXT, false);

        // 在线状态（能量 > 0 视为可用）
        IStrictEnergyHandler energyHandler = Capabilities.STRICT_ENERGY.getCapability(stack);
        boolean hasEnergy = energyHandler != null && energyHandler.getEnergy(0) > 0;
        String statusKey = hasEnergy
                ? "gui.mekanism_card.super_fusion.status_ready"
                : "gui.mekanism_card.super_fusion.status_no_energy";
        int statusColor = hasEnergy ? COLOR_OK : COLOR_WARN;
        graphics.drawString(font, Component.translatable(statusKey), tx, ty + 24, statusColor, false);

        // ===== 状态信息 =====
        int sy = ty + 48;
        graphics.drawString(font,
                Component.translatable("gui.mekanism_card.super_fusion.section_status"),
                tx, sy, COLOR_LABEL, false);
        sy += 16;

        // 融合模式
        SuperFusionCard.FusionMode mode = fusionCard.getFusionMode(stack);
        drawKV(graphics, tx, sy,
                Component.translatable("gui.mekanism_card.super_fusion.fusion_mode_short"),
                mode.getDisplayName(), modeColor(mode));
        sy += 14;

        // 范围模式
        boolean areaMode = stack.getOrDefault(ModDataComponents.AREA_UPGRADE_MODE.get(), false);
        Component rangeText = areaMode
                ? Component.translatable("gui.mekanism_card.super_fusion.range_area")
                : Component.translatable("gui.mekanism_card.super_fusion.range_single");
        drawKV(graphics, tx, sy,
                Component.translatable("gui.mekanism_card.super_fusion.range_mode_short"),
                rangeText, COLOR_ACCENT);
        sy += 14;

        // 模块选区
        boolean selectionMode = fusionCard.isModuleSelectionModeActive(stack);
        Component selectionText = selectionMode
                ? Component.translatable("gui.mekanism_card.super_fusion.selection_on")
                : Component.translatable("gui.mekanism_card.super_fusion.selection_off");
        drawKV(graphics, tx, sy,
                Component.translatable("gui.mekanism_card.super_fusion.selection_short"),
                selectionText, selectionMode ? COLOR_OK : COLOR_DIM);
        sy += 14;

        // 模块安装/移除模式
        MassUpgradeConfigurator.Mode moduleMode = fusionCard.getModuleMode();
        drawKV(graphics, tx, sy,
                Component.translatable("gui.mekanism_card.super_fusion.module_mode_short"),
                moduleMode.getDisplayName(), COLOR_TEXT);
        sy += 14;

        // 能量
        if (energyHandler != null) {
            long energy = energyHandler.getEnergy(0);
            long maxEnergy = energyHandler.getMaxEnergy(0);
            drawKV(graphics, tx, sy,
                    Component.translatable("gui.mekanism_card.super_fusion.energy_short"),
                    Component.literal(energy + " / " + maxEnergy + " FE"),
                    energy > 0 ? COLOR_WARN : COLOR_DIM);
        }

        // ===== 操作分组标题 =====
        int ay = ty + 120;
        graphics.drawString(font,
                Component.translatable("gui.mekanism_card.super_fusion.section_actions"),
                tx, ay, COLOR_LABEL, false);

        // 底部提示
        graphics.drawString(font,
                Component.translatable("gui.mekanism_card.super_fusion.esc_to_close"),
                tx, y + h - 14, COLOR_DIM, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** 绘制 键: 值 格式的状态行。键用浅蓝色，值用指定颜色。 */
    private void drawKV(GuiGraphics g, int x, int y, Component key, Component value, int valueColor) {
        g.drawString(font, key, x, y, COLOR_LABEL, false);
        int keyW = font.width(key);
        g.drawString(font, ": ", x + keyW, y, COLOR_DIM, false);
        int colonW = font.width(": ");
        // 给值上色：用 Component 的 style 不太方便（颜色是 ARGB int），直接 drawString 带 color
        g.drawString(font, value, x + keyW + colonW, y, valueColor, false);
    }

    /** 融合模式对应的显示颜色。 */
    private int modeColor(SuperFusionCard.FusionMode mode) {
        return switch (mode) {
            case TIER_INSTALL -> COLOR_ACCENT;
            case MODULE_UPGRADE -> COLOR_OK;
            case MEMORY_COPY -> 0xB58CFF;
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
