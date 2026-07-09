package com.mekanism.card.client;

import com.mekanism.card.MekanismCard;
import com.mekanism.card.client.gui.SuperFusionCardScreen;
import com.mekanism.card.item.SuperFusionCard;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端按键绑定注册。
 *
 * <p>当前注册的按键：
 * <ul>
 *   <li><b>M 键</b>（key.mekanism_card.open_fusion_menu）— 手持超级融合卡时打开菜单界面</li>
 * </ul></p>
 */
@EventBusSubscriber(modid = MekanismCard.MOD_ID, value = Dist.CLIENT)
public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.mekanism_card";

    public static final KeyMapping OPEN_FUSION_MENU = new KeyMapping(
            "key.mekanism_card.open_fusion_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_FUSION_MENU);
    }

    /**
     * 客户端 tick 时检测按键。放到 GAME bus 上是因为需要访问玩家状态。
     */
    @EventBusSubscriber(modid = MekanismCard.MOD_ID, value = Dist.CLIENT)
    public static class GameBus {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null || mc.screen != null) {
                return;
            }
            while (OPEN_FUSION_MENU.consumeClick()) {
                ItemStack mainHand = player.getMainHandItem();
                if (mainHand.getItem() instanceof SuperFusionCard) {
                    mc.setScreen(new SuperFusionCardScreen(mainHand));
                    break;
                }
            }
        }
    }
}
