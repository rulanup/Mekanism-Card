package com.mekanism.card;

import com.mekanism.card.network.BatchSelectionPayload;
import com.mekanism.card.network.FusionActionPayload;
import com.mekanism.card.network.MiddleClickPayload;
import com.mekanism.card.network.ToolModePayload;
import com.mekanism.card.network.TargetModePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MekanismCard.MOD_ID)
public class ModNetwork {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(FusionActionPayload.TYPE, FusionActionPayload.CODEC, FusionActionPayload::handle);
        registrar.playToServer(MiddleClickPayload.TYPE, MiddleClickPayload.CODEC, MiddleClickPayload::handle);
        registrar.playToServer(BatchSelectionPayload.TYPE, BatchSelectionPayload.CODEC, BatchSelectionPayload::handle);
        registrar.playToServer(ToolModePayload.TYPE, ToolModePayload.CODEC, ToolModePayload::handle);
        registrar.playToServer(TargetModePayload.TYPE, TargetModePayload.CODEC, TargetModePayload::handle);
    }
}
