package com.mekanism.card.item;

import com.mekanism.card.client.ModKeyBindings;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

final class TooltipHelper {

    private TooltipHelper() {
    }

    static boolean isDescriptionKeyDown() {
        return MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey);
    }

    static void addHoldForDescription(List<Component> tooltip) {
        tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(
                EnumColor.GRAY,
                EnumColor.AQUA,
                MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()
        ));
    }

    static Component shortcutLine(String translationKey, String... shortcutKeys) {
        Object[] shortcuts = new Object[shortcutKeys.length];
        for (int index = 0; index < shortcutKeys.length; index++) {
            shortcuts[index] = Component.translatable(shortcutKeys[index]).withStyle(ChatFormatting.GREEN);
        }
        return Component.translatable(translationKey, shortcuts).withStyle(ChatFormatting.GRAY);
    }

    static Component selectionShortcutLine(String translationKey) {
        Component shortcut = Component.translatable("tooltip.mekanism_card.key.selection_pattern",
                ModKeyBindings.BATCH_SELECTION.getTranslatedKeyMessage()).withStyle(ChatFormatting.GREEN);
        return Component.translatable(translationKey, shortcut).withStyle(ChatFormatting.GRAY);
    }
}
