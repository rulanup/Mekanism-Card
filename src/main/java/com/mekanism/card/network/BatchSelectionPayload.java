package com.mekanism.card.network;

import com.mekanism.card.MekanismCard;
import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.MemoryCard;
import com.mekanism.card.item.SuperFusionCard;
import com.mekanism.card.item.UltimateTierInstaller;
import com.mekanism.card.util.BatchSelectionHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BatchSelectionPayload(BlockPos firstPos, BlockPos secondPos) implements CustomPacketPayload {
    public static final Type<BatchSelectionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekanismCard.MOD_ID, "batch_selection"));
    public static final StreamCodec<FriendlyByteBuf, BatchSelectionPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BatchSelectionPayload::firstPos,
            BlockPos.STREAM_CODEC, BatchSelectionPayload::secondPos,
            BatchSelectionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BatchSelectionPayload payload, IPayloadContext context) {
        var player = context.player();
        ItemStack stack = player.getMainHandItem();
        List<BlockPos> positions = BatchSelectionHelper.collectPositions(
                player.level(), payload.firstPos(), payload.secondPos(), player);
        if (positions.isEmpty()) {
            return;
        }
        if (stack.getItem() instanceof SuperFusionCard fusionCard) {
            boolean fuzzy = fusionCard.isFuzzyMode(stack);
            if (!fuzzy && (fusionCard.getFusionMode(stack) == SuperFusionCard.FusionMode.TIER_INSTALL
                    || fusionCard.getFusionMode(stack) == SuperFusionCard.FusionMode.MODULE_UPGRADE)) {
                var referenceBlock = MemoryCard.getStoredSourceBlock(stack);
                if (referenceBlock == null) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                            "message.mekanism_card.memory_card.no_data"), true);
                    return;
                }
                positions = positions.stream()
                        .filter(pos -> player.level().getBlockState(pos).getBlock() == referenceBlock)
                        .toList();
            }
            switch (fusionCard.getFusionMode(stack)) {
                case TIER_INSTALL -> fusionCard.handleBatchSelection(player.level(), positions, player, stack);
                case MODULE_UPGRADE -> fusionCard.moduleConfigurator()
                        .handlePositions(player.level(), positions, player, stack);
                case MEMORY_COPY -> MemoryCard.handleBatchPasteStatic(player.level(), positions, player, stack);
                case FULL_PASTE -> MemoryCard.handleBatchFullPasteStatic(
                        player.level(), positions, player, stack, fusionCard);
            }
        } else if (stack.getItem() instanceof MassUpgradeConfigurator heldConfigurator) {
            heldConfigurator.handlePositions(player.level(), positions, player, stack);
        } else if (stack.getItem() instanceof MemoryCard) {
            MemoryCard.handleBatchPasteStatic(player.level(), positions, player, stack);
        } else if (stack.getItem() instanceof UltimateTierInstaller installer) {
            installer.handleBatchSelection(player.level(), positions, player, stack);
        }
    }
}
