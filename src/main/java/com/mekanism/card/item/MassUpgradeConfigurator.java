package com.mekanism.card.item;

import com.mekanism.card.ModDataComponents;
import com.mekanism.card.compat.UltimineCompat;
import com.mekanism.card.util.BatchSelectionHelper;
import com.mekanism.card.util.NetworkItemSource;
import mekanism.api.Upgrade;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class MassUpgradeConfigurator extends Item implements IFrequencyItem {

    public enum Mode {
        INSTALL("mekanism_card.mode.install", ChatFormatting.GREEN),
        CLEAR("mekanism_card.mode.clear", ChatFormatting.RED);

        public final String translationKey;
        public final ChatFormatting color;

        Mode(String translationKey, ChatFormatting color) {
            this.translationKey = translationKey;
            this.color = color;
        }

        public Component getDisplayName() {
            return Component.translatable(translationKey);
        }
    }

    private static final int DEFAULT_RADIUS = 5;

    public MassUpgradeConfigurator() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    // 对方块右键：实际处理逻辑已移到事件监听中，这里只返回 CONSUME 避免原版 GUI
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.CONSUME;
    }

    // ================== 公共方法供事件调用 ==================
    public void handleRadiusMode(Level level, BlockPos pos, Player player, ItemStack toolStack) {
        TileComponentUpgrade exampleComp = getUpgradeComponent(level, pos);
        if (exampleComp == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.not_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        net.minecraft.world.level.block.Block clickedBlock = level.getBlockState(pos).getBlock();
        List<BlockPos> machines = findConnectedMachines(level, pos, clickedBlock);

        if (machines.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.no_machines_connected")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        handlePositions(level, machines, player, toolStack);
    }

    public void handleSingleMode(Level level, BlockPos pos, Player player, ItemStack toolStack) {
        TileComponentUpgrade comp = getUpgradeComponent(level, pos);
        if (comp == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.not_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        handlePositions(level, List.of(pos), player, toolStack);
    }

    public void handleWithUltimine(Level level, BlockPos pos, Direction face, Player player, ItemStack toolStack) {
        if (player instanceof ServerPlayer serverPlayer && UltimineCompat.isPressed(serverPlayer)) {
            Collection<BlockPos> positions = UltimineCompat.getCachedPositions(serverPlayer, pos, face);
            if (positions.size() > 1) {
                handlePositions(level, positions, player, toolStack);
                return;
            }
        }
        handleSingleMode(level, pos, player, toolStack);
    }

    public void handleMiddleClick(Level level, BlockPos pos, Player player, ItemStack toolStack) {
        TileComponentUpgrade comp = getUpgradeComponent(level, pos);
        if (comp == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.not_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        NetworkItemSource itemSource = NetworkItemSource.create(level, player, toolStack);
        int totalInstalled = 0;
        int upgradeTypesCount = 0;

        for (Upgrade upgradeType : Upgrade.values()) {
            if (!comp.supports(upgradeType)) {
                continue;
            }

            int current = comp.getUpgrades(upgradeType);
            int max = upgradeType.getMax();
            if (current >= max) {
                continue;
            }

            int toInstall = max - current;
            int available = itemSource.countUpgrade(upgradeType);
            if (available == 0) {
                continue;
            }

            toInstall = Math.min(toInstall, available);
            if (toInstall <= 0) {
                continue;
            }

            if (!itemSource.consumeUpgrade(upgradeType, toInstall)) {
                continue;
            }

            int added = comp.addUpgrades(upgradeType, toInstall);
            if (added > 0) {
                totalInstalled += added;
                upgradeTypesCount++;
            }
        }

        if (totalInstalled > 0) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.middle_click.install_all",
                            totalInstalled, upgradeTypesCount)
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(Component.translatable("message.mekanism_card.middle_click.no_upgrades")
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    public void handleMiddleClickRadius(Level level, BlockPos pos, Player player, ItemStack toolStack) {
        TileComponentUpgrade exampleComp = getUpgradeComponent(level, pos);
        if (exampleComp == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.not_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        net.minecraft.world.level.block.Block clickedBlock = level.getBlockState(pos).getBlock();
        List<BlockPos> machines = findConnectedMachines(level, pos, clickedBlock);

        handleMiddleClickPositions(level, machines, player, toolStack);
    }

    public void handleMiddleClickPositions(Level level, Collection<BlockPos> positions, Player player, ItemStack toolStack) {
        NetworkItemSource itemSource = NetworkItemSource.create(level, player, toolStack);

        int totalInstalled = 0;
        int totalMachines = 0;

        for (BlockPos machinePos : new LinkedHashSet<>(positions)) {
            TileComponentUpgrade comp = getUpgradeComponent(level, machinePos);
            if (comp == null) continue;

            int machineInstalled = installAllUpgrades(comp, itemSource);
            if (machineInstalled > 0) {
                totalInstalled += machineInstalled;
                totalMachines++;
            }
        }

        if (totalInstalled > 0) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.middle_click.install_all_area",
                            totalInstalled, totalMachines)
                    .withStyle(ChatFormatting.GREEN), true);
        } else {
            player.displayClientMessage(Component.translatable("message.mekanism_card.middle_click.no_upgrades")
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    private int installAllUpgrades(TileComponentUpgrade comp, NetworkItemSource itemSource) {
        int totalInstalled = 0;

        for (Upgrade upgradeType : Upgrade.values()) {
            if (!comp.supports(upgradeType)) continue;

            int current = comp.getUpgrades(upgradeType);
            int max = upgradeType.getMax();
            if (current >= max) continue;

            int toInstall = max - current;
            int available = itemSource.countUpgrade(upgradeType);
            if (available == 0) continue;

            toInstall = Math.min(toInstall, available);
            if (toInstall <= 0) continue;

            if (!itemSource.consumeUpgrade(upgradeType, toInstall)) continue;

            int added = comp.addUpgrades(upgradeType, toInstall);
            if (added > 0) {
                totalInstalled += added;
            }
        }

        return totalInstalled;
    }

    public Mode getCurrentMode(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.MODULE_CLEAR_MODE.get(), false) ? Mode.CLEAR : Mode.INSTALL;
    }

    // ================== 内部辅助方法 ==================
    public void toggleMode(ItemStack stack, Player player) {
        setMode(stack, player, getCurrentMode(stack) == Mode.INSTALL ? Mode.CLEAR : Mode.INSTALL);
    }

    public void setMode(ItemStack stack, Player player, Mode mode) {
        stack.set(ModDataComponents.MODULE_CLEAR_MODE.get(), mode == Mode.CLEAR);
        player.displayClientMessage(Component.translatable("message.mekanism_card.mode_switched",
                mode.getDisplayName()).withStyle(mode.color), true);
    }

    public void handleBatchSelection(Level level, BlockPos p1, BlockPos p2, Player player, ItemStack toolStack) {
        List<BlockPos> positions = BatchSelectionHelper.collectPositions(level, p1, p2, player);
        if (positions.isEmpty()) {
            return;
        }
        handlePositions(level, positions, player, toolStack);
    }

    public void handlePositions(Level level, Collection<BlockPos> positions, Player player, ItemStack toolStack) {
        NetworkItemSource itemSource = NetworkItemSource.create(level, player, toolStack);
        Mode mode = getCurrentMode(toolStack);
        Map<Upgrade, Integer> storedLevels = Map.of();
        if (mode == Mode.INSTALL) {
            if (!MemoryCard.hasData(toolStack)) {
                player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.no_data")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
            storedLevels = MemoryCard.getStoredUpgradeLevels(toolStack);
            Map<Upgrade, Integer> required = getRequiredUpgrades(level, positions, storedLevels);
            if (!player.isCreative() && !hasRequiredUpgrades(itemSource, required)) {
                player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.not_enough_upgrades")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
        }
        int affectedMachines = 0;
        int totalAmount = 0;
        for (BlockPos pos : new LinkedHashSet<>(positions)) {
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            TileComponentUpgrade comp = getUpgradeComponent(level, pos);
            if (comp == null) {
                continue;
            }
            int amount = mode == Mode.INSTALL
                    ? syncUpgradeProfile(comp, storedLevels, itemSource, player)
                    : clearAllUpgrades(comp, player);
            if (amount > 0) {
                affectedMachines++;
                totalAmount += amount;
            }
        }
        feedbackDetailed(player, mode, affectedMachines, totalAmount);
    }

    private void feedbackDetailed(Player player, Mode mode, int affectedMachines, int totalAmount) {
        if (totalAmount > 0) {
            String actionKey = mode == Mode.INSTALL
                    ? "message.mekanism_card.operation.sync_profile"
                    : "message.mekanism_card.operation.clear_all";
            player.displayClientMessage(Component.translatable(actionKey, totalAmount, affectedMachines)
                    .withStyle(mode == Mode.INSTALL ? ChatFormatting.GREEN : ChatFormatting.YELLOW), true);
        } else {
            player.displayClientMessage(Component.translatable("message.mekanism_card.operation.none")
                    .withStyle(ChatFormatting.RED), true);
        }
    }

    private Map<Upgrade, Integer> getRequiredUpgrades(Level level, Collection<BlockPos> positions,
                                                       Map<Upgrade, Integer> storedLevels) {
        Map<Upgrade, Integer> required = new java.util.EnumMap<>(Upgrade.class);
        for (BlockPos pos : new LinkedHashSet<>(positions)) {
            TileComponentUpgrade comp = getUpgradeComponent(level, pos);
            if (comp == null) {
                continue;
            }
            for (Upgrade upgrade : Upgrade.values()) {
                if (!comp.supports(upgrade)) {
                    continue;
                }
                int target = storedLevels.getOrDefault(upgrade, 0);
                int missing = Math.max(0, target - comp.getUpgrades(upgrade));
                if (missing > 0) {
                    required.merge(upgrade, missing, Integer::sum);
                }
            }
        }
        return required;
    }

    private boolean hasRequiredUpgrades(NetworkItemSource itemSource, Map<Upgrade, Integer> required) {
        for (Map.Entry<Upgrade, Integer> entry : required.entrySet()) {
            if (!itemSource.hasUpgrade(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private int syncUpgradeProfile(TileComponentUpgrade comp, Map<Upgrade, Integer> storedLevels,
                                   NetworkItemSource itemSource, Player player) {
        int changed = 0;
        for (Upgrade upgrade : Upgrade.values()) {
            if (!comp.supports(upgrade)) {
                continue;
            }
            int target = storedLevels.getOrDefault(upgrade, 0);
            while (comp.getUpgrades(upgrade) > target) {
                int before = comp.getUpgrades(upgrade);
                comp.removeUpgrade(upgrade, false);
                int removed = before - comp.getUpgrades(upgrade);
                if (removed <= 0) {
                    break;
                }
                handleRemovedUpgrade(comp, player);
                changed += removed;
            }
            int missing = target - comp.getUpgrades(upgrade);
            if (missing > 0 && itemSource.consumeUpgrade(upgrade, missing)) {
                changed += comp.addUpgrades(upgrade, missing);
            }
        }
        return changed;
    }

    private int clearAllUpgrades(TileComponentUpgrade comp, Player player) {
        int totalRemoved = 0;
        for (Upgrade upgradeType : Upgrade.values()) {
            while (comp.getUpgrades(upgradeType) > 0) {
                int before = comp.getUpgrades(upgradeType);
                comp.removeUpgrade(upgradeType, false);
                int removed = before - comp.getUpgrades(upgradeType);
                if (removed <= 0) {
                    break;
                }
                handleRemovedUpgrade(comp, player);
                totalRemoved += removed;
            }
        }
        return totalRemoved;
    }

    private void handleRemovedUpgrade(TileComponentUpgrade comp, Player player) {
        mekanism.common.inventory.slot.UpgradeInventorySlot outputSlot = comp.getUpgradeOutputSlot();
        ItemStack stack = outputSlot.getStack();
        if (!stack.isEmpty()) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            outputSlot.setStack(ItemStack.EMPTY);
        }
    }

    @Nullable
    private TileComponentUpgrade getUpgradeComponent(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntityMekanism mekTile) {
            return mekTile.getComponent();
        }
        return null;
    }

    private List<BlockPos> findNearbyMachines(Level level, BlockPos center, int radius) {
        List<BlockPos> machines = new ArrayList<>();
        if (radius == 1) {
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos pos = center.relative(dir);
                if (getUpgradeComponent(level, pos) != null) {
                    machines.add(pos);
                }
            }
        } else {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (getUpgradeComponent(level, pos) != null) {
                            machines.add(pos);
                        }
                    }
                }
            }
        }
        return machines;
    }

    private List<BlockPos> findConnectedMachines(Level level, BlockPos start, net.minecraft.world.level.block.Block targetBlock) {
        List<BlockPos> machines = new ArrayList<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (getUpgradeComponent(level, current) != null) {
                machines.add(current);
            }

            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor)) {
                    net.minecraft.world.level.block.Block neighborBlock = level.getBlockState(neighbor).getBlock();
                    if (neighborBlock == targetBlock && getUpgradeComponent(level, neighbor) != null) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return machines;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (TooltipHelper.isDescriptionKeyDown()) {
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.module_mode",
                    "tooltip.mekanism_card.key.shift_left_air"));
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.module_copy",
                    "tooltip.mekanism_card.key.shift_right_machine"));
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.module_execute",
                    "tooltip.mekanism_card.key.right_machine"));
            tooltip.add(TooltipHelper.selectionShortcutLine("tooltip.mekanism_card.shortcut.module_batch"));
            tooltip.add(Component.translatable("tooltip.mekanism_card.network_support")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.mekanism_card.network_priority")
                    .withStyle(ChatFormatting.RED));
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.middle_install",
                    "tooltip.mekanism_card.key.middle_machine", "tooltip.mekanism_card.key.ultimine_middle"));
            super.appendHoverText(stack, context, tooltip, flag);
            return;
        }

        Mode mode = getCurrentMode(stack);
        tooltip.add(Component.translatable(mode == Mode.INSTALL
                        ? "tooltip.mekanism_card.stored_upgrade_profile"
                        : "tooltip.mekanism_card.clear_all_upgrades")
                .withStyle(mode == Mode.INSTALL ? ChatFormatting.AQUA : ChatFormatting.RED));

        tooltip.add(Component.translatable("tooltip.mekanism_card.current_operation", mode.getDisplayName())
                .withStyle(ChatFormatting.GOLD));
        TooltipHelper.addHoldForDescription(tooltip);
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyType.QIO;
    }
}
