package com.mekanism.card.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class BatchSelectionHelper {

    private BatchSelectionHelper() {
    }

    public static List<BlockPos> collectPositions(Level level, BlockPos first, BlockPos second, Player player) {
        int minX = Math.min(first.getX(), second.getX());
        int maxX = Math.max(first.getX(), second.getX());
        int minY = Math.min(first.getY(), second.getY());
        int maxY = Math.max(first.getY(), second.getY());
        int minZ = Math.min(first.getZ(), second.getZ());
        int maxZ = Math.max(first.getZ(), second.getZ());

        long sizeX = (long) maxX - minX + 1;
        long sizeY = (long) maxY - minY + 1;
        long sizeZ = (long) maxZ - minZ + 1;
        long volume = sizeX * sizeY * sizeZ;
        if (sizeX > 64 || sizeY > 64 || sizeZ > 64 || volume > 32_768
                || !level.getWorldBorder().isWithinBounds(first)
                || !level.getWorldBorder().isWithinBounds(second)
                || player.blockPosition().distSqr(first) > 128 * 128
                || player.blockPosition().distSqr(second) > 128 * 128) {
            player.displayClientMessage(Component.translatable("message.mekanism_card.selection_too_large")
                    .withStyle(ChatFormatting.RED), true);
            return List.of();
        }

        List<BlockPos> positions = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.hasChunkAt(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;
    }
}
