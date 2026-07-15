package com.mekanism.card.moremachine;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Optional integration for Mekanism: MoreMachine.
 *
 * <p>Its machines use Mekanism's standard upgrade and tier components, so normal configuration,
 * upgrade-card, Jade, and tier-install operations need no special implementation. The additional
 * integration here delegates machine-family comparisons to MoreMachine's own factory attributes,
 * including the advanced factories extended by Mekanism Extras.</p>
 */
public final class MoreMachineIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoreMachineIntegration.class);
    public static final String MOD_ID = "mekmm";

    private static volatile boolean initialized;
    private static volatile boolean available;
    private static Method isSameMoreMachineFactory;
    private static Method isSameAdvancedFactory;

    private MoreMachineIntegration() {
    }

    public static boolean isSameMachineFamily(Block source, Block target) {
        if (!isLoaded()) {
            return false;
        }
        initializeIfNeeded();
        if (!available) {
            return false;
        }
        Holder<Block> sourceHolder = source.builtInRegistryHolder();
        Holder<Block> targetHolder = target.builtInRegistryHolder();
        return invokeFamilyCheck(isSameMoreMachineFactory, sourceHolder, target)
                || invokeFamilyCheck(isSameMoreMachineFactory, targetHolder, source)
                || invokeFamilyCheck(isSameAdvancedFactory, sourceHolder, target)
                || invokeFamilyCheck(isSameAdvancedFactory, targetHolder, source);
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    private static void initializeIfNeeded() {
        if (!initialized) {
            synchronized (MoreMachineIntegration.class) {
                if (!initialized) {
                    initialize();
                }
            }
        }
    }

    private static void initialize() {
        initialized = true;
        try {
            Class<?> utilsClass = Class.forName("com.jerry.mekmm.common.util.MoreMachineUtils");
            isSameMoreMachineFactory = utilsClass.getMethod(
                    "isSameMMTypeFactory", Holder.class, Block.class);
            isSameAdvancedFactory = utilsClass.getMethod(
                    "isSameAFTypeFactory", Holder.class, Block.class);
            available = true;
            LOGGER.info("[Mekanism-Card] Mekanism More Machine integration enabled");
        } catch (ReflectiveOperationException | LinkageError exception) {
            available = false;
            LOGGER.warn("[Mekanism-Card] Failed to initialize Mekanism More Machine integration", exception);
        }
    }

    private static boolean invokeFamilyCheck(Method method, Holder<Block> source, Block target) {
        try {
            return (boolean) method.invoke(null, source, target);
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.debug("[Mekanism-Card] Failed to compare Mekanism More Machine families", exception);
            return false;
        }
    }
}
