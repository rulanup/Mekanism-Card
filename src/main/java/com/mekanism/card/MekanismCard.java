package com.mekanism.card;

import com.mekanism.card.datagen.ModRecipeProvider;
import com.mekanism.card.item.GuideBookItem;
import com.mekanism.card.item.MassUpgradeConfigurator;
import com.mekanism.card.item.MemoryCard;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mod(MekanismCard.MOD_ID)
public class MekanismCard {
    public static final String MOD_ID = "mekanism_card";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Supplier<Item> MASS_UPGRADE_CONFIGURATOR =
            ITEMS.register("mass_upgrade_configurator", MassUpgradeConfigurator::new);

    public static final Supplier<Item> MEMORY_CARD =
            ITEMS.register("memory_card", MemoryCard::new);

    public static final Supplier<Item> GUIDE_BOOK =
            ITEMS.register("guide_book", GuideBookItem::new);

    public static final Supplier<CreativeModeTab> MEKANISM_CARD_TAB = CREATIVE_MODE_TABS.register("mekanism_card_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mekanism_card"))
                    .icon(() -> new ItemStack(MASS_UPGRADE_CONFIGURATOR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(MASS_UPGRADE_CONFIGURATOR.get());
                        output.accept(MEMORY_CARD.get());
                        output.accept(GUIDE_BOOK.get());
                    })
                    .build());

    public MekanismCard(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        
        modEventBus.addListener(this::gatherData);
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookupProvider));
    }
}