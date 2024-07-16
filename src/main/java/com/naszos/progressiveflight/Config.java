package com.naszos.progressiveflight;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ProgressiveFlightMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue NEEDS_POWER = BUILDER
            .comment("Should flight beacon require power")
            .define("needsPower", false);

    public static final ModConfigSpec.DoubleValue POWER_REQ_MULTIPLIER = BUILDER
            .comment("How much power should it use (if needsPower is true)? (by default (multiplier = 1) it's 4/16/64/1024/4096 RF/T)")
            .defineInRange("powerMultiplier", 1.0, 0.01, 100.0);

    private static final ModConfigSpec.BooleanValue ALLOW_TO_PLACE_IN_ANY_DIM = BUILDER
            .comment("Should players be allowed to place flight beacon in any dimension?")
            .define("allowToPlaceInAnyDimension", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean needsPower;
    public static boolean allowToPlaceInAnyDimension;
    public static double powerMultiplier;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        needsPower = NEEDS_POWER.get();
        allowToPlaceInAnyDimension = ALLOW_TO_PLACE_IN_ANY_DIM.get();
        powerMultiplier = POWER_REQ_MULTIPLIER.get();
    }
}
