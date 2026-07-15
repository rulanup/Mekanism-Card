package com.mekanism.card.network;

import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.SuperFusionCard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToolModePayload(boolean clearMode) implements CustomPacketPayload {
    public static final Type<ToolModePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("mekanism_card", "tool_mode"));
    public static final StreamCodec<FriendlyByteBuf, ToolModePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ToolModePayload::clearMode,
            ToolModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToolModePayload payload, IPayloadContext context) {
        var player = context.player();
        var stack = player.getMainHandItem();
        MassUpgradeConfigurator.Mode mode = payload.clearMode ? MassUpgradeConfigurator.Mode.CLEAR : MassUpgradeConfigurator.Mode.INSTALL;
        if (player.getMainHandItem().getItem() instanceof SuperFusionCard fusionCard
                && fusionCard.getFusionMode(stack) == SuperFusionCard.FusionMode.MODULE_UPGRADE) {
            fusionCard.moduleConfigurator().setMode(stack, player, mode);
        } else if (player.getMainHandItem().getItem() instanceof MassUpgradeConfigurator configurator) {
            configurator.setMode(stack, player, mode);
        }
    }
}
