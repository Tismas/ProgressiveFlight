package com.naszos.progressiveflight;

import com.naszos.progressiveflight.blocks.FlightBeacon;
import com.naszos.progressiveflight.blocks.FlightBeaconBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ProgressiveFlightMod.MODID)
public class ProgressiveFlightMod {
    public static final String MODID = "progressiveflight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<FlightBeacon> FLIGHT_BEACON_WOODEN_BLOCK = BLOCKS.registerBlock("flight_beacon_wooden", (BlockBehaviour.Properties properties) -> new FlightBeacon(properties, 0),
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.0F));
    public static final DeferredBlock<FlightBeacon> FLIGHT_BEACON_COPPER_BLOCK = BLOCKS.registerBlock("flight_beacon_copper", (BlockBehaviour.Properties properties) -> new FlightBeacon(properties, 1),
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.COPPER).strength(3.0F, 6.0F));
    public static final DeferredBlock<FlightBeacon> FLIGHT_BEACON_IRON_BLOCK = BLOCKS.registerBlock("flight_beacon_iron", (BlockBehaviour.Properties properties) -> new FlightBeacon(properties, 2),
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(5.0F, 6.0F));
    public static final DeferredBlock<FlightBeacon> FLIGHT_BEACON_DIAMOND_BLOCK = BLOCKS.registerBlock("flight_beacon_diamond", (BlockBehaviour.Properties properties) -> new FlightBeacon(properties, 3),
            BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).sound(SoundType.METAL).strength(5.0F, 6.0F));
    public static final DeferredBlock<FlightBeacon> FLIGHT_BEACON_NETHERITE_BLOCK = BLOCKS.registerBlock("flight_beacon_netherite", (BlockBehaviour.Properties properties) -> new FlightBeacon(properties, 4),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).sound(SoundType.NETHERITE_BLOCK).strength(50.0F, 1200.0F));

    public static final DeferredItem<BlockItem> FLIGHT_BEACON_WOODEN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("flight_beacon_wooden", FLIGHT_BEACON_WOODEN_BLOCK);
    public static final DeferredItem<BlockItem> FLIGHT_BEACON_COPPER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("flight_beacon_copper", FLIGHT_BEACON_COPPER_BLOCK);
    public static final DeferredItem<BlockItem> FLIGHT_BEACON_IRON_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("flight_beacon_iron", FLIGHT_BEACON_IRON_BLOCK);
    public static final DeferredItem<BlockItem> FLIGHT_BEACON_DIAMOND_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("flight_beacon_diamond", FLIGHT_BEACON_DIAMOND_BLOCK);
    public static final DeferredItem<BlockItem> FLIGHT_BEACON_NETHERITE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("flight_beacon_netherite", FLIGHT_BEACON_NETHERITE_BLOCK);

    public static final DeferredItem<Item> FLIGHT_BEACON_UPGRADE_COPPER = ITEMS.registerSimpleItem("flight_beacon_upgrade_copper");
    public static final DeferredItem<Item> FLIGHT_BEACON_UPGRADE_IRON = ITEMS.registerSimpleItem("flight_beacon_upgrade_iron");
    public static final DeferredItem<Item> FLIGHT_BEACON_UPGRADE_DIAMOND = ITEMS.registerSimpleItem("flight_beacon_upgrade_diamond");
    public static final DeferredItem<Item> FLIGHT_BEACON_UPGRADE_NETHERITE = ITEMS.registerSimpleItem("flight_beacon_upgrade_netherite");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlightBeaconBE>> FLIGHT_BEACON_WOODEN_BLOCK_ENTITY = BLOCK_ENTITIES
            .register("flight_beacon_wooden", () -> BlockEntityType.Builder.of((BlockPos pPos, BlockState pBlockState) -> new FlightBeaconBE(pPos, pBlockState, 0), FLIGHT_BEACON_WOODEN_BLOCK.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlightBeaconBE>> FLIGHT_BEACON_COPPER_BLOCK_ENTITY = BLOCK_ENTITIES
            .register("flight_beacon_copper", () -> BlockEntityType.Builder.of((BlockPos pPos, BlockState pBlockState) -> new FlightBeaconBE(pPos, pBlockState, 1), FLIGHT_BEACON_COPPER_BLOCK.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlightBeaconBE>> FLIGHT_BEACON_IRON_BLOCK_ENTITY = BLOCK_ENTITIES
            .register("flight_beacon_iron", () -> BlockEntityType.Builder.of((BlockPos pPos, BlockState pBlockState) -> new FlightBeaconBE(pPos, pBlockState, 2), FLIGHT_BEACON_IRON_BLOCK.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlightBeaconBE>> FLIGHT_BEACON_DIAMOND_BLOCK_ENTITY = BLOCK_ENTITIES
            .register("flight_beacon_diamond", () -> BlockEntityType.Builder.of((BlockPos pPos, BlockState pBlockState) -> new FlightBeaconBE(pPos, pBlockState, 3), FLIGHT_BEACON_DIAMOND_BLOCK.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FlightBeaconBE>> FLIGHT_BEACON_NETHERITE_BLOCK_ENTITY = BLOCK_ENTITIES
            .register("flight_beacon_netherite", () -> BlockEntityType.Builder.of((BlockPos pPos, BlockState pBlockState) -> new FlightBeaconBE(pPos, pBlockState, 4), FLIGHT_BEACON_NETHERITE_BLOCK.get())
                    .build(null));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS_TAB = CREATIVE_MODE_TABS.register("progressive_flight", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.progressiveflight"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> FLIGHT_BEACON_WOODEN_BLOCK_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(FLIGHT_BEACON_WOODEN_BLOCK_ITEM);
                output.accept(FLIGHT_BEACON_COPPER_BLOCK_ITEM);
                output.accept(FLIGHT_BEACON_IRON_BLOCK_ITEM);
                output.accept(FLIGHT_BEACON_DIAMOND_BLOCK_ITEM);
                output.accept(FLIGHT_BEACON_NETHERITE_BLOCK_ITEM);

                output.accept(FLIGHT_BEACON_UPGRADE_COPPER);
                output.accept(FLIGHT_BEACON_UPGRADE_IRON);
                output.accept(FLIGHT_BEACON_UPGRADE_DIAMOND);
                output.accept(FLIGHT_BEACON_UPGRADE_NETHERITE);
            }).build());

    public ProgressiveFlightMod(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                FLIGHT_BEACON_WOODEN_BLOCK_ENTITY.get(),
                FlightBeaconBE::getCapability
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                FLIGHT_BEACON_COPPER_BLOCK_ENTITY.get(),
                FlightBeaconBE::getCapability
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                FLIGHT_BEACON_IRON_BLOCK_ENTITY.get(),
                FlightBeaconBE::getCapability
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                FLIGHT_BEACON_DIAMOND_BLOCK_ENTITY.get(),
                FlightBeaconBE::getCapability
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                FLIGHT_BEACON_NETHERITE_BLOCK_ENTITY.get(),
                FlightBeaconBE::getCapability
        );
    }
}
