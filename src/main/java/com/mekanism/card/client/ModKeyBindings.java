package com.mekanism.card.client;

import com.mekanism.card.MekanismCard;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端按键绑定注册。
 *
 * <p>当前注册的按键：
 * <ul>
 *   <li><b>左 Ctrl</b>（key.mekanism_card.batch_selection）— 左键两个角点并立即执行批量模块操作</li>
 * </ul></p>
 */
@EventBusSubscriber(modid = MekanismCard.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.mekanism_card";

    public static final KeyMapping BATCH_SELECTION = new KeyMapping(
            "key.mekanism_card.batch_selection",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BATCH_SELECTION);
    }
}
