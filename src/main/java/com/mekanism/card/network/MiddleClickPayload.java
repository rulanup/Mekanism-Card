package com.mekanism.card.network;

import com.mekanism.card.MekanismCard;
import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.SuperFusionCard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MiddleClickPayload(BlockPos pos, Direction face) implements CustomPacketPayload {
    public static final Type<MiddleClickPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MekanismCard.MOD_ID, "middle_click"));

    public static final StreamCodec<FriendlyByteBuf, MiddleClickPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MiddleClickPayload::pos,
            ByteBufCodecs.idMapper(Direction::from3DDataValue, Direction::get3DDataValue),
            MiddleClickPayload::face,
            MiddleClickPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MiddleClickPayload payload, IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();
        ItemStack stack = player.getMainHandItem();
        MassUpgradeConfigurator configurator;

        if (stack.getItem() instanceof SuperFusionCard fusionCard) {
            configurator = fusionCard.moduleConfigurator();
        } else if (stack.getItem() instanceof MassUpgradeConfigurator heldConfigurator) {
            configurator = heldConfigurator;
        } else {
            return;
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && com.mekanism.card.compat.UltimineCompat.isPressed(serverPlayer)) {
            var positions = com.mekanism.card.compat.UltimineCompat.getCachedPositions(
                    serverPlayer, payload.pos(), payload.face());
            if (positions.size() > 1) {
                configurator.handleMiddleClickPositions(level, positions, player, stack);
                return;
            }
        }
        configurator.handleMiddleClick(level, payload.pos(), player, stack);
    }
}
