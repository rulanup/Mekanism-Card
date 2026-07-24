package com.mekanism.card.item;

import com.mekanism.card.ModDataComponents;
import com.mekanism.card.compat.UltimineCompat;
import com.mekanism.card.evolved.EvolvedIntegration;
import com.mekanism.card.extras.ExtrasIntegration;
import com.mekanism.card.util.NetworkItemSource;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.StorageUtils;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.WorldUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UltimateTierInstaller extends Item {

    private static final int MAX_ENERGY = 200000;
    private static final int ENERGY_PER_UPGRADE = 1000;

    public UltimateTierInstaller() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    public static boolean isUpgradeTarget(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.is(MekanismBlocks.BOUNDING_BLOCK)) {
            BlockPos mainPos = BlockBounding.getMainBlockPos(world, pos);
            if (mainPos == null) {
                return false;
            }
            state = world.getBlockState(mainPos);
        }
        return Attribute.get(state.getBlockHolder(), AttributeUpgradeable.class) != null;
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        Level world = context.getLevel();
        if (player == null || world.isClientSide()) {
            return InteractionResult.PASS;
        }

        ItemStack handStack = player.getItemInHand(context.getHand());
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && UltimineCompat.isPressed(serverPlayer)) {
            Collection<BlockPos> positions = UltimineCompat.getCachedPositions(
                    serverPlayer, context.getClickedPos(), context.getClickedFace());
            if (positions.size() > 1) {
                return handleUltimineUpgrade(context, player, world, handStack, positions);
            }
        }
        return handleSingleUpgrade(context, player, world, handStack);
    }

    private InteractionResult handleUltimineUpgrade(UseOnContext context, Player player, Level world,
                                                     ItemStack handStack, Collection<BlockPos> positions) {
        return handlePositionBatch(world, positions, player, handStack, context.getHand(),
                context.getClickedFace(), "message.mekanism_card.ultimate_installer.ultimine_success");
    }

    public void handleBatchSelection(Level level, Collection<BlockPos> positions, Player player, ItemStack stack) {
        handlePositionBatch(level, positions, player, stack, InteractionHand.MAIN_HAND, Direction.UP,
                "message.mekanism_card.ultimate_installer.selection_success");
    }

    private InteractionResult handlePositionBatch(Level world, Collection<BlockPos> positions, Player player,
                                                  ItemStack handStack, InteractionHand hand, Direction face,
                                                  String successTranslationKey) {
        int upgradedMachines = 0;
        for (BlockPos pos : new LinkedHashSet<>(positions)) {
            if (!world.hasChunkAt(pos)) {
                continue;
            }
            BlockHitResult hitResult = new BlockHitResult(
                    Vec3.atCenterOf(pos), face, pos, false);
            InteractionResult result = handleSingleUpgrade(
                    new UseOnContext(player, hand, hitResult), player, world, handStack);
            if (result.consumesAction()) {
                upgradedMachines++;
            }
        }
        if (upgradedMachines > 0) {
            player.displayClientMessage(Component.translatable(
                    successTranslationKey, upgradedMachines)
                    .withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    private InteractionResult handleSingleUpgrade(UseOnContext context, Player player, Level world, ItemStack handStack) {
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);

        if (state.is(MekanismBlocks.BOUNDING_BLOCK)) {
            BlockPos mainPos = BlockBounding.getMainBlockPos(world, pos);
            if (mainPos != null) {
                pos = mainPos;
                state = world.getBlockState(mainPos);
            }
        }

        Holder<Block> block = state.getBlockHolder();
        if (Attribute.get(block, AttributeUpgradeable.class) == null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.not_upgradeable")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        BaseTier currentTier = Attribute.getBaseTier(block);
        if (!isBelowUltimate(currentTier) || ExtrasIntegration.getAdvancedTierName(block) != null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.already_ultimate")
                    .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.FAIL;
        }

        BlockEntity tile = WorldUtils.getTileEntity(world, pos);
        if (tile instanceof TileEntityMekanism tileMek && !tileMek.playersUsing.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.machine_in_use")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (!(tile instanceof ITierUpgradable tierUpgradable)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.not_tier_upgradable")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (!tierUpgradable.canBeUpgraded()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.cannot_upgrade")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        boolean creative = player.isCreative();
        IStrictEnergyHandler energyHandler = Capabilities.STRICT_ENERGY.getCapability(handStack);
        if (!creative && (energyHandler == null || energyHandler.getEnergy(0) < ENERGY_PER_UPGRADE)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.no_energy")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        int steps = performStrictUpgradeToUltimate(world, pos, state);
        if (steps <= 0) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.cannot_upgrade")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        if (!creative) {
            energyHandler.extractEnergy(0, ENERGY_PER_UPGRADE, Action.EXECUTE);
        }
        player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.success", steps)
                .withStyle(ChatFormatting.GREEN), true);
        return InteractionResult.CONSUME;
    }

    private static boolean isBelowUltimate(@Nullable BaseTier tier) {
        return tier == null || tier == BaseTier.BASIC || tier == BaseTier.ADVANCED || tier == BaseTier.ELITE;
    }

    public static BaseTier getEffectiveBaseTier(Holder<Block> block) {
        BaseTier baseTier = Attribute.getBaseTier(block);
        if (baseTier == null && ExtrasIntegration.getAdvancedTierName(block) != null) {
            return BaseTier.ULTIMATE;
        }
        return baseTier;
    }
    public boolean upgradeToTier(Level world, BlockPos pos, Player player, ItemStack handStack, BaseTier desiredTier) {
        return upgradeToTier(world, pos, player, handStack, desiredTier, null);
    }

    public boolean upgradeToTier(Level world, BlockPos pos, Player player, ItemStack handStack, BaseTier desiredTier,
                                 @Nullable String desiredAdvancedTier) {
        BlockState state = world.getBlockState(pos);
        Holder<Block> block = state.getBlockHolder();
        BaseTier currentTier = getEffectiveBaseTier(block);
        String currentAdvancedTier = ExtrasIntegration.getAdvancedTierName(block);
        if (desiredAdvancedTier == null) {
            if (currentAdvancedTier != null || currentTier == desiredTier
                    || currentTier != null && currentTier.ordinal() > desiredTier.ordinal()) {
                return true;
            }
        } else {
            int desiredAdvancedIndex = ExtrasIntegration.getTierIndex(desiredAdvancedTier);
            int currentAdvancedIndex = ExtrasIntegration.getTierIndex(currentAdvancedTier);
            if (desiredAdvancedIndex == Integer.MIN_VALUE) {
                return false;
            }
            if (currentAdvancedIndex >= desiredAdvancedIndex) {
                return true;
            }
            if (currentTier != null && currentTier.ordinal() > desiredTier.ordinal()) {
                return false;
            }
        }

        boolean needsBaseUpgrade = currentTier != desiredTier;
        boolean needsAdvancedUpgrade = desiredAdvancedTier != null;
        AttributeUpgradeable baseUpgradeable = Attribute.get(block, AttributeUpgradeable.class);
        Object extraUpgradeable = ExtrasIntegration.getExtraAttributeUpgradeable(block);
        if (needsBaseUpgrade && baseUpgradeable == null
                || needsAdvancedUpgrade && !needsBaseUpgrade && extraUpgradeable == null
                || !(WorldUtils.getTileEntity(world, pos) instanceof ITierUpgradable tierUpgradable)
                || needsBaseUpgrade && !tierUpgradable.canBeUpgraded()) {
            return false;
        }

        List<Item> requiredInstallers = new ArrayList<>();
        if (needsBaseUpgrade) {
            List<Item> baseInstallers = getRequiredInstallersToTier(currentTier, desiredTier);
            if (baseInstallers.isEmpty()) {
                return false;
            }
            requiredInstallers.addAll(baseInstallers);
        }
        if (needsAdvancedUpgrade) {
            List<Item> advancedInstallers = ExtrasIntegration.getRequiredInstallersToTier(
                    currentAdvancedTier, desiredAdvancedTier);
            if (advancedInstallers.isEmpty()) {
                return false;
            }
            requiredInstallers.addAll(advancedInstallers);
        }
        if (requiredInstallers.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.super_fusion.tier_unsupported")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        boolean creative = player.isCreative();
        IStrictEnergyHandler energyHandler = Capabilities.STRICT_ENERGY.getCapability(handStack);
        if (!creative && (energyHandler == null || energyHandler.getEnergy(0) < ENERGY_PER_UPGRADE)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.no_energy")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        NetworkItemSource itemSource = NetworkItemSource.create(world, player, handStack);
        if (!creative && !hasRequiredInstallers(requiredInstallers, itemSource)) {
            showMissingInstallersMessage(player, requiredInstallers, itemSource);
            return false;
        }

        int steps = needsBaseUpgrade
                ? performTierUpgradeTo(world, pos, state, currentTier, desiredTier)
                : 0;
        if (needsAdvancedUpgrade) {
            BlockState advancedState = world.getBlockState(pos);
            steps += performExtrasTierUpgradeTo(world, pos, advancedState, desiredAdvancedTier);
        }
        Holder<Block> finalBlock = world.getBlockState(pos).getBlockHolder();
        BaseTier finalTier = getEffectiveBaseTier(finalBlock);
        String finalAdvancedTier = ExtrasIntegration.getAdvancedTierName(finalBlock);
        if (steps <= 0 || finalTier != desiredTier
                || desiredAdvancedTier != null && !desiredAdvancedTier.equals(finalAdvancedTier)) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.super_fusion.tier_failed")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (!creative) {
            consumeInstallers(itemSource, requiredInstallers);
            energyHandler.extractEnergy(0, ENERGY_PER_UPGRADE, Action.EXECUTE);
        }
        return true;
    }

    private List<Item> getRequiredInstallersToTier(@Nullable BaseTier currentTier, BaseTier desiredTier) {
        List<Item> installers = new ArrayList<>();
        BaseTier tier = currentTier;
        while (tier != desiredTier) {
            BaseTier next = getNextTierToward(tier);
            if (next == null || next.ordinal() > desiredTier.ordinal()) {
                return List.of();
            }
            Item installer = getInstallerForTransition(tier, next);
            if (installer == null) {
                return List.of();
            }
            installers.add(installer);
            tier = next;
        }
        return installers;
    }

    @Nullable
    private BaseTier getNextTierToward(@Nullable BaseTier currentTier) {
        BaseTier standardNext = getNextTier(currentTier);
        if (standardNext != null) {
            return standardNext;
        }
        return currentTier == null ? null : EvolvedIntegration.getNextTier(currentTier);
    }

    @Nullable
    private Item getInstallerForTransition(@Nullable BaseTier currentTier, BaseTier nextTier) {
        Item standard = getInstallerForNextTier(currentTier);
        if (standard != null) {
            return standard;
        }
        String tierName = EvolvedIntegration.getTierLowerName(nextTier);
        return tierName == null ? null : EvolvedIntegration.getTierInstallerItem(tierName);
    }

    private int performStrictUpgradeToUltimate(Level world, BlockPos pos, BlockState initialState) {
        List<BlockState> path = new ArrayList<>();
        BlockState probe = initialState;
        Set<Block> seenBlocks = new HashSet<>();

        for (int i = 0; i < 16; i++) {
            Holder<Block> probeBlock = probe.getBlockHolder();
            BaseTier probeTier = Attribute.getBaseTier(probeBlock);
            if (probeTier == BaseTier.ULTIMATE && ExtrasIntegration.getAdvancedTierName(probeBlock) == null) {
                break;
            }
            if (!isBelowUltimate(probeTier) || ExtrasIntegration.getAdvancedTierName(probeBlock) != null
                    || !seenBlocks.add(probe.getBlock())) {
                return 0;
            }
            AttributeUpgradeable upgradeable = Attribute.get(probeBlock, AttributeUpgradeable.class);
            if (upgradeable == null) {
                return 0;
            }
            BlockState next = upgradeable.upgradeResult(probe, BaseTier.ULTIMATE);
            if (next == probe) {
                return 0;
            }
            BaseTier nextTier = Attribute.getBaseTier(next.getBlockHolder());
            if (ExtrasIntegration.getAdvancedTierName(next.getBlockHolder()) != null
                    || nextTier != BaseTier.ULTIMATE && !isBelowUltimate(nextTier)) {
                return 0;
            }
            path.add(next);
            probe = next;
        }

        if (path.isEmpty() || Attribute.getBaseTier(probe.getBlockHolder()) != BaseTier.ULTIMATE
                || ExtrasIntegration.getAdvancedTierName(probe.getBlockHolder()) != null) {
            return 0;
        }

        int steps = 0;
        for (BlockState nextState : path) {
            BlockEntity currentTile = WorldUtils.getTileEntity(world, pos);
            if (!(currentTile instanceof ITierUpgradable upgradable)) {
                return steps;
            }
            IUpgradeData upgradeData = upgradable.getUpgradeData(world.registryAccess());
            if (upgradeData == null) {
                return steps;
            }
            AttributeHasBounding nextBounding = Attribute.get(nextState, AttributeHasBounding.class);
            if (nextBounding != null && !nextBounding.handle(world, pos, nextState, pos,
                    (level, boundingPos, mainPos) -> {
                        Optional<BlockState> blockState = WorldUtils.getBlockState(level, boundingPos);
                        if (blockState.isEmpty()) {
                            return false;
                        }
                        BlockState boundingState = blockState.get();
                        return boundingState.canBeReplaced()
                                || boundingState.is(MekanismBlocks.BOUNDING_BLOCK)
                                && mainPos.equals(BlockBounding.getMainBlockPos(level, boundingPos));
                    })) {
                return steps;
            }
            if (!world.setBlockAndUpdate(pos, nextState)) {
                return steps;
            }
            if (nextBounding != null) {
                nextBounding.placeBoundingBlocks(world, pos, nextState);
            }
            TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (upgradedTile == null) {
                return steps;
            }
            if (currentTile instanceof ITileDirectional directional && directional.isDirectional()) {
                upgradedTile.setFacing(directional.getDirection(), false);
            }
            upgradedTile.parseUpgradeData(world.registryAccess(), upgradeData);
            upgradedTile.sendUpdatePacket();
            upgradedTile.setChanged();
            upgradedTile.invalidateCapabilitiesFull();
            steps++;
        }

        Holder<Block> finalBlock = world.getBlockState(pos).getBlockHolder();
        return Attribute.getBaseTier(finalBlock) == BaseTier.ULTIMATE
                && ExtrasIntegration.getAdvancedTierName(finalBlock) == null ? steps : 0;
    }

    private int performTierUpgradeTo(Level world, BlockPos pos, BlockState initialState,
                                     @Nullable BaseTier startTier, BaseTier desiredTier) {
        BlockState currentState = initialState;
        BaseTier currentTier = startTier;
        int steps = 0;
        while (currentTier != desiredTier) {
            AttributeUpgradeable upgradeable = Attribute.get(currentState.getBlockHolder(), AttributeUpgradeable.class);
            BlockEntity currentTile = WorldUtils.getTileEntity(world, pos);
            if (upgradeable == null || !(currentTile instanceof ITierUpgradable upgradable)) {
                break;
            }
            IUpgradeData upgradeData = upgradable.getUpgradeData(world.registryAccess());
            if (upgradeData == null) {
                break;
            }
            BlockState nextState = upgradeable.upgradeResult(currentState, desiredTier);
            if (currentState == nextState) {
                break;
            }
            AttributeHasBounding nextBounding = Attribute.get(nextState, AttributeHasBounding.class);
            if (nextBounding != null && !nextBounding.handle(world, pos, nextState, pos,
                    (level, boundingPos, mainPos) -> {
                        Optional<BlockState> blockState = WorldUtils.getBlockState(level, boundingPos);
                        if (blockState.isEmpty()) {
                            return false;
                        }
                        BlockState boundingState = blockState.get();
                        return boundingState.canBeReplaced()
                                || boundingState.is(MekanismBlocks.BOUNDING_BLOCK)
                                && mainPos.equals(BlockBounding.getMainBlockPos(level, boundingPos));
                    })) {
                break;
            }
            if (!world.setBlockAndUpdate(pos, nextState)) {
                break;
            }
            if (nextBounding != null) {
                nextBounding.placeBoundingBlocks(world, pos, nextState);
            }
            TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (upgradedTile == null) {
                break;
            }
            if (currentTile instanceof ITileDirectional directional && directional.isDirectional()) {
                upgradedTile.setFacing(directional.getDirection(), false);
            }
            upgradedTile.parseUpgradeData(world.registryAccess(), upgradeData);
            upgradedTile.sendUpdatePacket();
            upgradedTile.setChanged();
            upgradedTile.invalidateCapabilitiesFull();
            currentState = nextState;
            currentTier = getEffectiveBaseTier(currentState.getBlockHolder());
            steps++;
            if (currentTier == null || currentTier.ordinal() > desiredTier.ordinal()) {
                break;
            }
        }
        return steps;
    }

    private InteractionResult handleAreaUpgrade(UseOnContext context, Player player, Level world, ItemStack handStack) {
        BlockPos startPos = context.getClickedPos();
        BlockState startState = world.getBlockState(startPos);

        if (startState.is(MekanismBlocks.BOUNDING_BLOCK)) {
            BlockPos mainPos = BlockBounding.getMainBlockPos(world, startPos);
            if (mainPos != null) {
                startPos = mainPos;
                startState = world.getBlockState(mainPos);
            }
        }

        Holder<Block> startBlock = startState.getBlockHolder();
        boolean isUpgradeable = Attribute.get(startBlock, AttributeUpgradeable.class) != null;
        if (!isUpgradeable) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.not_upgradeable")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        List<UpgradeTarget> targets = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState currentState = world.getBlockState(current);

            if (currentState.is(MekanismBlocks.BOUNDING_BLOCK)) {
                BlockPos mainPos = BlockBounding.getMainBlockPos(world, current);
                if (mainPos != null && !visited.contains(mainPos)) {
                    visited.add(mainPos);
                    queue.add(mainPos);
                }
                continue;
            }

            Holder<Block> block = currentState.getBlockHolder();
            AttributeUpgradeable upgradeable = Attribute.get(block, AttributeUpgradeable.class);
            if (upgradeable == null) {
                continue;
            }

            BaseTier currentTier = Attribute.getBaseTier(block);
            if (!isBelowUltimate(currentTier) || ExtrasIntegration.getAdvancedTierName(block) != null) {
                continue;
            }

            BlockEntity tile = WorldUtils.getTileEntity(world, current);
            if (tile instanceof TileEntityMekanism tileMek && !tileMek.playersUsing.isEmpty()) {
                continue;
            }

            if (!(tile instanceof ITierUpgradable tierUpgradable) || !tierUpgradable.canBeUpgraded()) {
                continue;
            }

            targets.add(new UpgradeTarget(current, currentState, currentTier));

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        if (targets.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.no_machines_to_upgrade")
                    .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.FAIL;
        }

        boolean isCreative = player.isCreative();
        IStrictEnergyHandler energyHandler = Capabilities.STRICT_ENERGY.getCapability(handStack);
        long totalEnergyNeeded = (long) targets.size() * ENERGY_PER_UPGRADE;
        long energy = energyHandler != null ? energyHandler.getEnergy(0) : 0;

        if (!isCreative && energy < totalEnergyNeeded) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.no_energy_area", targets.size(), totalEnergyNeeded)
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        int upgradedCount = 0;
        int totalSteps = 0;

        for (UpgradeTarget target : targets) {
            int steps = performStrictUpgradeToUltimate(world, target.pos, target.state);
            if (steps > 0) {
                upgradedCount++;
                totalSteps += steps;
            }
        }

        if (upgradedCount > 0) {
            if (!isCreative && energyHandler != null) {
                energyHandler.extractEnergy(0, (long) upgradedCount * ENERGY_PER_UPGRADE, Action.EXECUTE);
            }
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.area_success", upgradedCount, totalSteps)
                    .withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Nullable
    private Item getInstallerForNextTier(@Nullable BaseTier currentTier) {
        if (currentTier == null) {
            return MekanismItems.BASIC_TIER_INSTALLER.get();
        }
        return switch (currentTier) {
            case BASIC -> MekanismItems.ADVANCED_TIER_INSTALLER.get();
            case ADVANCED -> MekanismItems.ELITE_TIER_INSTALLER.get();
            case ELITE -> MekanismItems.ULTIMATE_TIER_INSTALLER.get();
            default -> null;
        };
    }

    @Nullable
    private BaseTier getNextTier(@Nullable BaseTier currentTier) {
        if (currentTier == null) {
            return BaseTier.BASIC;
        }
        return switch (currentTier) {
            case BASIC -> BaseTier.ADVANCED;
            case ADVANCED -> BaseTier.ELITE;
            case ELITE -> BaseTier.ULTIMATE;
            default -> null;
        };
    }

    private boolean hasRequiredInstallers(List<Item> requiredInstallers, NetworkItemSource itemSource) {
        for (Item installer : requiredInstallers) {
            if (!itemSource.has(installer, 1)) {
                return false;
            }
        }
        return true;
    }

    private void showMissingInstallersMessage(Player player, List<Item> requiredInstallers, NetworkItemSource itemSource) {
        Item missing = null;
        for (Item installer : requiredInstallers) {
            if (!itemSource.has(installer, 1)) {
                missing = installer;
                break;
            }
        }
        if (missing != null) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.missing_installer",
                            missing.getDescription())
                    .withStyle(ChatFormatting.RED), true);
        } else if (!itemSource.hasExternalStorage()) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.ultimate_installer.no_network")
                    .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    private void consumeInstallers(NetworkItemSource itemSource, List<Item> requiredInstallers) {
        for (Item installer : requiredInstallers) {
            itemSource.consume(installer, 1);
        }
    }

    private int performExtrasTierUpgradeTo(Level world, BlockPos pos, BlockState initialState,
                                           String desiredAdvancedTier) {
        BlockState currentState = initialState;
        int steps = 0;

        while (true) {
            Holder<Block> currentBlock = currentState.getBlockHolder();
            String currentAdvancedTier = ExtrasIntegration.getAdvancedTierName(currentBlock);
            if (desiredAdvancedTier.equals(currentAdvancedTier)
                    || ExtrasIntegration.getTierIndex(currentAdvancedTier)
                    > ExtrasIntegration.getTierIndex(desiredAdvancedTier)) {
                break;
            }

            String nextTierName = currentAdvancedTier == null
                    ? ExtrasIntegration.TIER_ABSOLUTE
                    : ExtrasIntegration.getNextTierName(currentAdvancedTier);
            if (nextTierName == null) {
                break;
            }

            Object upgradeable = ExtrasIntegration.getExtraAttributeUpgradeable(currentBlock);
            if (upgradeable == null) {
                break;
            }

            BlockEntity currentTile = WorldUtils.getTileEntity(world, pos);
            if (!(currentTile instanceof ITierUpgradable upgradable)) {
                break;
            }

            IUpgradeData upgradeData = upgradable.getUpgradeData(world.registryAccess());
            if (upgradeData == null) {
                break;
            }

            BlockState nextState = ExtrasIntegration.upgradeResult(upgradeable, currentState, nextTierName);
            if (nextState == null || currentState == nextState) {
                break;
            }

            AttributeHasBounding nextBounding = Attribute.get(nextState, AttributeHasBounding.class);
            if (nextBounding != null) {
                if (!nextBounding.handle(world, pos, nextState, pos, (level, boundingPos, mainPos) -> {
                    Optional<BlockState> blockState = WorldUtils.getBlockState(level, boundingPos);
                    if (blockState.isPresent()) {
                        BlockState boundingCurrentState = blockState.get();
                        if (boundingCurrentState.canBeReplaced()) {
                            return true;
                        } else if (boundingCurrentState.is(MekanismBlocks.BOUNDING_BLOCK)) {
                            return mainPos.equals(BlockBounding.getMainBlockPos(level, boundingPos));
                        }
                    }
                    return false;
                })) {
                    break;
                }
            }

            if (!world.setBlockAndUpdate(pos, nextState)) {
                break;
            }

            if (nextBounding != null) {
                nextBounding.placeBoundingBlocks(world, pos, nextState);
            }

            TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (upgradedTile == null) {
                break;
            }

            if (currentTile instanceof ITileDirectional directional && directional.isDirectional()) {
                upgradedTile.setFacing(directional.getDirection(), false);
            }
            upgradedTile.parseUpgradeData(world.registryAccess(), upgradeData);
            upgradedTile.sendUpdatePacket();
            upgradedTile.setChanged();
            upgradedTile.invalidateCapabilitiesFull();

            currentState = nextState;
            steps++;
        }

        return steps;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (TooltipHelper.isDescriptionKeyDown()) {
            tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.description")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.tier_execute",
                    "tooltip.mekanism_card.key.right_machine"));
            tooltip.add(TooltipHelper.selectionShortcutLine("tooltip.mekanism_card.shortcut.tier_batch"));
            tooltip.add(TooltipHelper.shortcutLine("tooltip.mekanism_card.shortcut.tier_ultimine",
                    "tooltip.mekanism_card.key.ultimine_right"));
            tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.energy_only")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.ultimate_cap")
                    .withStyle(ChatFormatting.GOLD));
            // 当 Mekanism: MoreMachine 加载时，显示 MoreMachine 联动支持说明
            // MoreMachine 方块走标准 AttributeUpgradeable 路径，已天然兼容，此 tooltip 仅作可见性提示
            if (com.mekanism.card.moremachine.MoreMachineIntegration.isLoaded()) {
                tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.moremachine_support")
                        .withStyle(ChatFormatting.DARK_AQUA));
            }
            super.appendHoverText(stack, context, tooltip, flag);
            return;
        }

        IStrictEnergyHandler energyHandler = Capabilities.STRICT_ENERGY.getCapability(stack);
        if (energyHandler != null) {
            StorageUtils.addStoredEnergy(stack, tooltip, false);
        }

        if (supportsAreaMode()) {
            boolean areaMode = stack.getOrDefault(ModDataComponents.AREA_UPGRADE_MODE.get(), false);
            tooltip.add(Component.translatable("tooltip.mekanism_card.ultimate_installer.mode",
                            areaMode
                                    ? Component.translatable("tooltip.mekanism_card.ultimate_installer.mode_area")
                                    : Component.translatable("tooltip.mekanism_card.ultimate_installer.mode_single"))
                    .withStyle(ChatFormatting.GOLD));
        }

        TooltipHelper.addHoldForDescription(tooltip);

        super.appendHoverText(stack, context, tooltip, flag);
    }

    protected boolean supportsAreaMode() {
        return false;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return mekanism.common.config.MekanismConfig.client.energyColor.get();
    }

    private record UpgradeTarget(BlockPos pos, BlockState state, BaseTier currentTier) {
    }
}
