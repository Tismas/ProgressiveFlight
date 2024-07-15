package com.naszos.progressiveflight.blocks;

import com.naszos.progressiveflight.Config;
import com.naszos.progressiveflight.ProgressiveFlightMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;

import java.util.List;

@EventBusSubscriber(modid = ProgressiveFlightMod.MODID)
public class FlightBeacon extends Block implements EntityBlock {
    int tier;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<FlightBeaconBE>>[] blockEntities = new DeferredHolder[]{
            ProgressiveFlightMod.FLIGHT_BEACON_WOODEN_BLOCK_ENTITY,
            ProgressiveFlightMod.FLIGHT_BEACON_COPPER_BLOCK_ENTITY,
            ProgressiveFlightMod.FLIGHT_BEACON_IRON_BLOCK_ENTITY,
            ProgressiveFlightMod.FLIGHT_BEACON_DIAMOND_BLOCK_ENTITY,
            ProgressiveFlightMod.FLIGHT_BEACON_NETHERITE_BLOCK_ENTITY
    };
    public static DeferredItem<Item>[] correctUpgrades = new DeferredItem[]{
            ProgressiveFlightMod.FLIGHT_BEACON_UPGRADE_COPPER,
            ProgressiveFlightMod.FLIGHT_BEACON_UPGRADE_IRON,
            ProgressiveFlightMod.FLIGHT_BEACON_UPGRADE_DIAMOND,
            ProgressiveFlightMod.FLIGHT_BEACON_UPGRADE_NETHERITE
    };
    public static DeferredBlock<Block>[] nextUpgrades = new DeferredBlock[]{
            ProgressiveFlightMod.FLIGHT_BEACON_COPPER_BLOCK,
            ProgressiveFlightMod.FLIGHT_BEACON_IRON_BLOCK,
            ProgressiveFlightMod.FLIGHT_BEACON_DIAMOND_BLOCK,
            ProgressiveFlightMod.FLIGHT_BEACON_NETHERITE_BLOCK,
    };

    public FlightBeacon(Properties properties, int tier) {
        super(properties.destroyTime(2).lightLevel((bs) -> 8).noOcclusion());
        assert tier >= 0 && tier <= 4;
        this.tier = tier;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return blockEntities[this.tier].get().create(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return type == blockEntities[this.tier].get() ? FlightBeaconBE::tick : null;
    }

    @Override
    protected void onRemove(BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof FlightBeaconBE) {
                ((FlightBeaconBE) blockEntity).onRemove(pLevel, pPos);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    protected void onPlace(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pOldState, boolean pMovedByPiston) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (blockEntity instanceof FlightBeaconBE) {
            ((FlightBeaconBE) blockEntity).onPlace(pLevel, pPos);
        }
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("block.progressiveflight.flight_beacon_%s.hover".formatted(tier)).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack pStack, @NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHitResult) {
        DeferredItem<Item> correctUpgrade = correctUpgrades[this.tier];
        if (this.tier == 4 || !pStack.is(correctUpgrade.get()))
            return super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult);

        if (pLevel.isClientSide()) {
            for (int i = 0; i < 10; i++) {
                double x = pPos.getX() + Math.random();
                double y = pPos.getY() + Math.random();
                double z = pPos.getZ() + Math.random();
                pLevel.addParticle(ParticleTypes.GLOW, false, x, y, z, 0, 1, 0);
            }
        } else {
            DeferredBlock<Block> nextUpgrade = nextUpgrades[this.tier];
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            int storedEnergy = 0;
            if (blockEntity instanceof FlightBeaconBE) {
                storedEnergy = ((FlightBeaconBE) blockEntity).energy.getEnergyStored();
            }

            pLevel.setBlock(pPos, nextUpgrade.get().defaultBlockState(), 2);
            pStack.consume(1, pPlayer);

            BlockEntity newBlockEntity = pLevel.getBlockEntity(pPos);
            if (newBlockEntity instanceof FlightBeaconBE) {
                ((FlightBeaconBE) newBlockEntity).energy.receiveEnergy(storedEnergy, false);
            }
        }

        return super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult);
    }

    private static boolean canBePlaced(Level pLevel) {
        return Config.allowToPlaceInAnyDimension || pLevel.dimension().compareTo(Level.OVERWORLD) == 0;
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent e) {
        if (e.getLevel().isClientSide()) return;

        Entity entity = e.getEntity();
        if (entity != null && e.getPlacedBlock().getBlock() instanceof FlightBeacon) {
            if (!canBePlaced(entity.level())) {
                e.setCanceled(true);
                if (entity instanceof Player) {
                    entity.sendSystemMessage(Component.translatable("block.progressiveflight.flight_beacon.invalid_position"));
                }
            }
        }
    }
}
