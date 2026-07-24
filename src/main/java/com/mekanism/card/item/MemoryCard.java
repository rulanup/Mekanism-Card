package com.mekanism.card.item;

import com.mekanism.card.extras.ExtrasIntegration;
import com.mekanism.card.moremachine.MoreMachineIntegration;
import com.mekanism.card.util.NetworkItemSource;
import mekanism.api.IConfigCardAccess;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemoryCard extends net.minecraft.world.item.Item implements IFrequencyItem {

    public MemoryCard() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static boolean hasData(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return tag.contains("MachineData");
    }

    public static Map<Upgrade, Integer> getStoredUpgradeLevels(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        Map<Upgrade, Integer> levels = new EnumMap<>(Upgrade.class);
        if (!tag.contains("MachineData")) {
            return levels;
        }
        CompoundTag machineData = tag.getCompound("MachineData");
        int sourceMachineCount = Math.max(1, machineData.getInt("MachineCount"));
        ListTag upgradeList = machineData.getList(
                SerializationConstants.UPGRADES, net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int index = 0; index < upgradeList.size(); index++) {
            CompoundTag upgradeTag = upgradeList.getCompound(index);
            Upgrade upgrade = findStoredUpgrade(upgradeTag);
            if (upgrade != null) {
                int amount = Math.min(upgrade.getMax(),
                        upgradeTag.getInt(SerializationConstants.AMOUNT) / sourceMachineCount);
                if (amount > 0) {
                    levels.put(upgrade, amount);
                }
            }
        }
        return levels;
    }

    @Nullable
    public static Block getStoredSourceBlock(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("MachineData")) {
            return null;
        }
        String sourceBlockType = tag.getCompound("MachineData").getString("SourceBlockType");
        ResourceLocation id = ResourceLocation.tryParse(sourceBlockType);
        return id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (TooltipHelper.isDescriptionKeyDown()) {
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.memory_copy",
                    "tooltip.mekanism_card.key.shift_right_machine"));
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.memory_paste",
                    "tooltip.mekanism_card.key.right_machine"));
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.memory_clear",
                    "tooltip.mekanism_card.key.shift_right_air"));
            tooltip.add(TooltipHelper.selectionShortcutLine("tooltip.mekanism_card.shortcut.memory_batch"));
            tooltip.add(Component.translatable("tooltip.mekanism_card.network_support")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.mekanism_card.network_priority")
                    .withStyle(ChatFormatting.RED));
            super.appendHoverText(stack, context, tooltip, flag);
            return;
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("MachineData")) {
            CompoundTag data = tag.getCompound("MachineData");
            String blockType = data.getString("SourceBlockType");
            if (!blockType.isEmpty()) {
                ResourceLocation location = ResourceLocation.parse(blockType);
                net.minecraft.world.level.block.Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(location);
                if (block != null) {
                    Component blockName = block.getName();
                    tooltip.add(Component.translatable("tooltip.mekanism_card.memory_card.has_data", blockName.getString())
                            .withStyle(ChatFormatting.GREEN));
                }
            }
        } else {
            tooltip.add(Component.translatable("tooltip.mekanism_card.memory_card.no_data")
                    .withStyle(ChatFormatting.RED));
        }
        TooltipHelper.addHoldForDescription(tooltip);

        super.appendHoverText(stack, context, tooltip, flag);
    }

    public static void handleCopyStatic(Level level, BlockPos pos, Player player, ItemStack stack) {
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof IConfigCardAccess)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.not_mekanism")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        IConfigCardAccess machine = (IConfigCardAccess) be;
        Block targetBlock = level.getBlockState(pos).getBlock();

        CompoundTag data = new CompoundTag();

        ListTag upgradeList = new ListTag();
        if (be instanceof TileEntityMekanism tile) {
            TileComponentUpgrade comp = tile.getComponent();
            if (comp != null) {
                for (Upgrade upgrade : Upgrade.values()) {
                    int upgradeLevel = comp.getUpgrades(upgrade);
                    if (upgradeLevel > 0) {
                        CompoundTag upgradeTag = new CompoundTag();
                        upgradeTag.putInt(SerializationConstants.TYPE, upgrade.ordinal());
                        upgradeTag.putString("UpgradeName", upgrade.getSerializedName());
                        upgradeTag.putInt(SerializationConstants.AMOUNT, upgradeLevel);
                        upgradeList.add(upgradeTag);
                    }
                }
            }
        }
        data.put(SerializationConstants.UPGRADES, upgradeList);

        CompoundTag configData = machine.getConfigurationData(level.registryAccess(), player);
        if (configData != null && !configData.isEmpty()) {
            data.put("ConfigData", configData);
            data.putString("BlockType", BuiltInRegistries.BLOCK.getKey(targetBlock).toString());
        }

        data.putInt("MachineCount", 1);
        data.putString("SourceBlockType", BuiltInRegistries.BLOCK.getKey(targetBlock).toString());

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.put("MachineData", data);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.copied", 1, upgradeList.size())
                .withStyle(ChatFormatting.GREEN), true);
    }

    public static void handleClearStatic(Player player, ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_DATA);
        player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.cleared")
                .withStyle(ChatFormatting.YELLOW), true);
    }

    public static void handleClearMachineDataStatic(Player player, ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains("MachineData")) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.no_data")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        tag.remove("MachineData");
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.cleared")
                .withStyle(ChatFormatting.YELLOW), true);
    }

    public static void handlePasteStatic(Level level, BlockPos pos, Player player, ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (!tag.contains("MachineData")) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.no_data")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof IConfigCardAccess)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.not_mekanism")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        CompoundTag machineData = tag.getCompound("MachineData");
        String sourceBlockType = machineData.getString("SourceBlockType");
        Block targetBlock = level.getBlockState(pos).getBlock();
        String targetBlockType = BuiltInRegistries.BLOCK.getKey(targetBlock).toString();

        if (!sourceBlockType.isEmpty() && !sourceBlockType.equals(targetBlockType)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.type_mismatch")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        pasteToMachines(level, List.of(pos), player, stack, machineData, true);
    }

    public static void handleBatchPasteStatic(Level level, Collection<BlockPos> positions, Player player, ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("MachineData")) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.no_data")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        CompoundTag machineData = tag.getCompound("MachineData");
        String sourceBlockType = machineData.getString("SourceBlockType");
        List<BlockPos> targets = new ArrayList<>();
        for (BlockPos targetPos : new LinkedHashSet<>(positions)) {
            if (!level.hasChunkAt(targetPos)) {
                continue;
            }
            String targetBlockType = BuiltInRegistries.BLOCK.getKey(level.getBlockState(targetPos).getBlock()).toString();
            if (level.getBlockEntity(targetPos) instanceof IConfigCardAccess
                    && (sourceBlockType.isEmpty() || sourceBlockType.equals(targetBlockType))) {
                targets.add(targetPos);
            }
        }
        if (targets.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.paste_failed")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }
        pasteToMachines(level, targets, player, stack, machineData, true);
    }

    public static void handleFullPasteStatic(Level level, BlockPos pos, Player player, ItemStack stack,
                                             SuperFusionCard fusionCard) {
        handleBatchFullPasteStatic(level, List.of(pos), player, stack, fusionCard);
    }

    public static void handleBatchFullPasteStatic(Level level, Collection<BlockPos> positions, Player player,
                                                  ItemStack stack, SuperFusionCard fusionCard) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("MachineData")) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.no_data")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        CompoundTag machineData = tag.getCompound("MachineData");
        Block sourceBlock = getStoredSourceBlock(stack);
        BaseTier desiredTier = sourceBlock == null ? null
                : UltimateTierInstaller.getEffectiveBaseTier(sourceBlock.builtInRegistryHolder());
        String desiredAdvancedTier = sourceBlock == null ? null
                : ExtrasIntegration.getAdvancedTierName(sourceBlock.builtInRegistryHolder());
        if (sourceBlock == null || desiredTier == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.no_tier")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }

        boolean fuzzy = fusionCard.isFuzzyMode(stack);
        List<BlockPos> upgradedTargets = new ArrayList<>();
        for (BlockPos targetPos : new LinkedHashSet<>(positions)) {
            if (!level.hasChunkAt(targetPos)) {
                continue;
            }
            BlockEntity target = level.getBlockEntity(targetPos);
            if (!(target instanceof TileEntityMekanism)
                    || !fuzzy && (!(target instanceof IConfigCardAccess)
                    || !isSameMachineFamily(sourceBlock, level.getBlockState(targetPos).getBlock()))) {
                continue;
            }
            if (fusionCard.upgradeToTier(level, targetPos, player, stack, desiredTier, desiredAdvancedTier)) {
                upgradedTargets.add(targetPos);
            }
        }
        if (upgradedTargets.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.paste_failed")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }
        pasteToMachines(level, upgradedTargets, player, stack, machineData, !fuzzy);
    }

    private static void pasteToMachines(Level level, List<BlockPos> connectedMachines, Player player,
                                        ItemStack stack, CompoundTag machineData, boolean includeConfiguration) {

        Map<Upgrade, Integer> perMachineUpgrade = new EnumMap<>(Upgrade.class);
        if (machineData.contains(SerializationConstants.UPGRADES)) {
            ListTag upgradeList = machineData.getList(SerializationConstants.UPGRADES, net.minecraft.nbt.Tag.TAG_COMPOUND);
            int originalMachineCount = machineData.getInt("MachineCount");
            if (originalMachineCount <= 0) originalMachineCount = 1;
            for (int i = 0; i < upgradeList.size(); i++) {
                CompoundTag upgradeTag = upgradeList.getCompound(i);
                int totalAmount = upgradeTag.getInt(SerializationConstants.AMOUNT);
                Upgrade upgrade = findStoredUpgrade(upgradeTag);
                if (upgrade != null && totalAmount > 0) {
                    int amountPerMachine = totalAmount / originalMachineCount;
                    amountPerMachine = Math.min(amountPerMachine, upgrade.getMax());
                    perMachineUpgrade.put(upgrade, amountPerMachine);
                }
            }
        }

        boolean isCreative = player.getAbilities().instabuild;

        NetworkItemSource itemSource = NetworkItemSource.create(level, player, stack);
        Map<Upgrade, Integer> neededUpgrades = new EnumMap<>(Upgrade.class);
        if (!isCreative && machineData.contains(SerializationConstants.UPGRADES)) {
            neededUpgrades = getNeededUpgrades(level, connectedMachines, perMachineUpgrade);

            if (!neededUpgrades.isEmpty() && !hasEnoughUpgrades(itemSource, neededUpgrades)) {
                player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.not_enough_upgrades")
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
        }

        Map<Upgrade, Integer> consumedUpgrades = new EnumMap<>(Upgrade.class);

        int machinesAffected = 0;

        for (BlockPos machinePos : connectedMachines) {
            BlockEntity be = level.getBlockEntity(machinePos);
            if (!(be instanceof TileEntityMekanism machine)) {
                continue;
            }

            boolean affected = false;

            if (machineData.contains(SerializationConstants.UPGRADES)) {
                ListTag upgradeList = machineData.getList(SerializationConstants.UPGRADES, net.minecraft.nbt.Tag.TAG_COMPOUND);
                TileComponentUpgrade upgradeComp = machine.getComponent();

                if (upgradeComp != null) {
                    for (Map.Entry<Upgrade, Integer> entry : perMachineUpgrade.entrySet()) {
                        Upgrade upgrade = entry.getKey();
                        int targetLevel = entry.getValue();

                        if (!upgradeComp.supports(upgrade)) {
                            continue;
                        }

                        int current = upgradeComp.getUpgrades(upgrade);
                        int toAdd = Math.min(targetLevel - current, upgrade.getMax() - current);
                        if (toAdd > 0) {
                            int added = upgradeComp.addUpgrades(upgrade, toAdd);
                            if (added > 0) {
                                affected = true;

                                if (!isCreative) {
                                    consumedUpgrades.put(upgrade, consumedUpgrades.getOrDefault(upgrade, 0) + added);
                                }
                            }
                        }
                    }
                }
            }

            if (includeConfiguration && machineData.contains("ConfigData") && be instanceof IConfigCardAccess configAccess) {
                CompoundTag configData = machineData.getCompound("ConfigData");
                configAccess.setConfigurationData(level.registryAccess(), player, configData);
                configAccess.configurationDataSet();
                affected = true;
            }

            if (affected) {
                machinesAffected++;
            }
        }

        if (!isCreative && !consumedUpgrades.isEmpty()) {
            consumeUpgradeCardsByType(itemSource, consumedUpgrades);
        }

        if (machinesAffected > 0) {
            if (isCreative) {
                player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.pasted_creative")
                        .withStyle(ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.pasted")
                        .withStyle(ChatFormatting.GREEN), true);
            }
        } else {
            player.displayClientMessage(Component.translatable("message.mekanism_card.memory_card.paste_failed")
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    public static boolean isSameMachineFamily(Block source, Block target) {
        return MoreMachineIntegration.isSameMachineFamily(source, target)
                || normalizeMachineFamily(BuiltInRegistries.BLOCK.getKey(source).getPath())
                .equals(normalizeMachineFamily(BuiltInRegistries.BLOCK.getKey(target).getPath()));
    }

    private static String normalizeMachineFamily(String path) {
        String normalized = path.toLowerCase(java.util.Locale.ROOT);
        String[] prefixes = {
                "creative_", "multiversal_", "dense_", "quantum_", "overclocked_",
                "infinite_", "cosmic_", "supreme_", "absolute_", "ultimate_",
                "elite_", "advanced_", "basic_"
        };
        boolean changed;
        do {
            changed = false;
            for (String prefix : prefixes) {
                if (normalized.startsWith(prefix)) {
                    normalized = normalized.substring(prefix.length());
                    changed = true;
                    break;
                }
            }
        } while (changed);
        return normalized;
    }

    @Nullable
    private static Upgrade findStoredUpgrade(CompoundTag upgradeTag) {
        if (upgradeTag.contains("UpgradeName")) {
            String name = upgradeTag.getString("UpgradeName");
            for (Upgrade upgrade : Upgrade.values()) {
                if (upgrade.getSerializedName().equals(name)) {
                    return upgrade;
                }
            }
        }
        int ordinal = upgradeTag.getInt(SerializationConstants.TYPE);
        return ordinal >= 0 && ordinal < Upgrade.values().length ? Upgrade.values()[ordinal] : null;
    }

    private static int countTotalUpgradeCards(Player player) {
        int count = 0;
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof mekanism.common.item.interfaces.IUpgradeItem upgradeItem) {
                Upgrade upgradeType = upgradeItem.getUpgradeType(itemStack);
                count += itemStack.getCount();
            }
        }
        return count;
    }

    private static Map<Upgrade, Integer> getNeededUpgrades(Level level, List<BlockPos> connectedMachines, Map<Upgrade, Integer> perMachineUpgrade) {
        Map<Upgrade, Integer> needed = new EnumMap<>(Upgrade.class);
        for (BlockPos machinePos : connectedMachines) {
            if (!(level.getBlockEntity(machinePos) instanceof TileEntityMekanism machine)) {
                continue;
            }
            TileComponentUpgrade upgradeComp = machine.getComponent();
            if (upgradeComp == null) {
                continue;
            }
            for (Map.Entry<Upgrade, Integer> entry : perMachineUpgrade.entrySet()) {
                Upgrade upgrade = entry.getKey();
                if (!upgradeComp.supports(upgrade)) {
                    continue;
                }
                int current = upgradeComp.getUpgrades(upgrade);
                int targetLevel = entry.getValue();
                int toAdd = Math.min(targetLevel - current, upgrade.getMax() - current);
                if (toAdd > 0) {
                    needed.merge(upgrade, toAdd, Integer::sum);
                }
            }
        }
        return needed;
    }

    private static boolean hasEnoughUpgrades(NetworkItemSource itemSource, Map<Upgrade, Integer> required) {
        for (Map.Entry<Upgrade, Integer> entry : required.entrySet()) {
            if (!itemSource.hasUpgrade(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static void consumeUpgradeCardsByType(NetworkItemSource itemSource, Map<Upgrade, Integer> required) {
        for (Map.Entry<Upgrade, Integer> entry : required.entrySet()) {
            itemSource.consumeUpgrade(entry.getKey(), entry.getValue());
        }
    }

    private static String upgradeKeyToSimpleName(Upgrade upgrade) {
        return switch (upgrade) {
            case SPEED -> "速度升级";
            case ENERGY -> "能源升级";
            case FILTER -> "过滤升级";
            case CHEMICAL -> "化学升级";
            case MUFFLING -> "降噪升级";
            case ANCHOR -> "锚点升级";
            case STONE_GENERATOR -> "石头生成升级";
            default -> upgrade.getSerializedName();
        };
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyType.QIO;
    }
}
