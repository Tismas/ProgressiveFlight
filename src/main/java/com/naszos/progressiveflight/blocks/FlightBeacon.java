package com.naszos.progressiveflight.blocks;

import com.naszos.progressiveflight.ProgressiveFlightMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlightBeacon extends Block implements EntityBlock {
    int tier;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<FlightBeaconBE>>[] blockEntities = new DeferredHolder[]{
            ProgressiveFlightMod.FLIGHT_BEACON_WOODEN_BLOCK_ENTITY,
            ProgressiveFlightMod.FLIGHT_BEACON_COPPER_BLOCK_ENTITY,
            ProgressiveFlightMod.FLIGHT_BEACON_IRON_BLOCK_ENTITY,
            ProgressiveFlightMod.FLIGHT_BEACON_DIAMOND_BLOCK_ENTITY,
            ProgressiveFlightMod.FLIGHT_BEACON_NETHERITE_BLOCK_ENTITY
    };

    public FlightBeacon(Properties properties, int tier) {
        super(properties);
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
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof FlightBeaconBE) {
                ((FlightBeaconBE) blockEntity).onRemove();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }
}
