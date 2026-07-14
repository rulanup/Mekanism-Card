package com.mekanism.card.network;

import com.mekanism.card.MekanismCard;
import com.mekanism.card.item.SuperFusionCard;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetFusionModePayload(int modeId) implements CustomPacketPayload {
    public static final Type<SetFusionModePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekanismCard.MOD_ID, "set_fusion_mode"));
    public static final StreamCodec<FriendlyByteBuf, SetFusionModePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetFusionModePayload::modeId,
            SetFusionModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetFusionModePayload payload, IPayloadContext context) {
        if (payload.modeId() < 0 || payload.modeId() >= SuperFusionCard.FusionMode.values().length) {
            return;
        }
        var player = context.player();
        var stack = player.getMainHandItem();
        if (stack.getItem() instanceof SuperFusionCard fusionCard) {
            fusionCard.setFusionMode(stack, player, SuperFusionCard.FusionMode.byId(payload.modeId()));
        }
    }
}
