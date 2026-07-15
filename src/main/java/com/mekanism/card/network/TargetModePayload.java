package com.mekanism.card.network;

import com.mekanism.card.MekanismCard;
import com.mekanism.card.item.SuperFusionCard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TargetModePayload(boolean fuzzy) implements CustomPacketPayload {

    public static final Type<TargetModePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekanismCard.MOD_ID, "target_mode"));
    public static final StreamCodec<FriendlyByteBuf, TargetModePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, TargetModePayload::fuzzy,
            TargetModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TargetModePayload payload, IPayloadContext context) {
        var player = context.player();
        var stack = player.getMainHandItem();
        if (stack.getItem() instanceof SuperFusionCard fusionCard) {
            boolean fuzzy = payload.fuzzy && fusionCard.getFusionMode(stack) != SuperFusionCard.FusionMode.MEMORY_COPY;
            fusionCard.setFuzzyMode(stack, fuzzy);
            player.displayClientMessage(Component.translatable("message.mekanism_card.target_mode_switched",
                            Component.translatable(fuzzy
                                    ? "mekanism_card.target_mode.fuzzy"
                                    : "mekanism_card.target_mode.precise"))
                    .withStyle(fuzzy ? ChatFormatting.GOLD : ChatFormatting.AQUA), true);
        }
    }
}
