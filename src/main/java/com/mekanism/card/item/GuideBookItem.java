package com.mekanism.card.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import vazkii.patchouli.api.PatchouliAPI;
import org.jetbrains.annotations.Nullable;

public class GuideBookItem extends Item {

    public GuideBookItem() {
        super(new Properties());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            Minecraft.getInstance().execute(() -> {
                PatchouliAPI.get().openBookGUI(
                    ResourceLocation.fromNamespaceAndPath("mekanism_card", "mekanism_card_guide")
                );
            });
        }
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()), player.getItemInHand(hand));
    }
}
