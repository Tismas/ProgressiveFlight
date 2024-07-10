package com.naszos.progressiveflight.blocks;

import com.naszos.progressiveflight.ProgressiveFlightMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.HashSet;
import java.util.UUID;

public class FlightBeaconBE extends BlockEntity {
    private final int tier;
    private final static int[] ranges = {4, 16, 64};
    private final HashSet<Player> playersWithGrantedFlight = new HashSet<Player>();
    private final UUID id = UUID.randomUUID();

    public FlightBeaconBE(BlockPos pPos, BlockState pBlockState, int tier) {
        super(FlightBeacon.blockEntities[tier].get(), pPos, pBlockState);
        this.tier = tier;
    }

    private ResourceLocation getFlightModId() {
        return ResourceLocation.fromNamespaceAndPath(ProgressiveFlightMod.MODID, id.toString());
    }

    private static double dist(Player player, BlockPos pos) {
        double dx = player.getX() - pos.getX();
        double dz = player.getZ() - pos.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private boolean isInRange(Player player, BlockPos pos) {
        if (this.tier == 0 || this.tier == 1 || this.tier == 2) {
            int range = ranges[this.tier];
            double distance = dist(player, pos);
            return range > distance;
        }
        if (tier == 4) {
            return player.getCommandSenderWorld().dimension() == this.level.dimension();
        }
        return true;
    }

    private void takeAwayFlight(Player player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        ResourceLocation flightModId = getFlightModId();
        if (flight != null) {
            if (flight.hasModifier(flightModId)) {
                flight.removeModifier(flightModId);
            }
            if (flight.getModifiers().isEmpty()) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 5));
            }
        }
        playersWithGrantedFlight.remove(player);
    }

    private void grantFlight(Player player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        ResourceLocation flightModId = getFlightModId();
        if (flight != null && !flight.hasModifier(flightModId)) {
            flight.addTransientModifier(new AttributeModifier(flightModId, 1, AttributeModifier.Operation.ADD_VALUE));
        }
        playersWithGrantedFlight.add(player);
    }

    private static void showRangeParticles(Level level, BlockPos pos, int range) {
        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / (range * 2)) {
            double deltaX = Math.cos(angle) * range;
            double deltaZ = Math.sin(angle) * range;
            level.addParticle(ParticleTypes.GLOW, false, pos.getX() + 0.5 + deltaX, pos.getY(), pos.getZ() + 0.5 + deltaZ, 0, 0.2, 0);
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        FlightBeaconBE beacon = (FlightBeaconBE) blockEntity;
        long tickInSecond = level.getGameTime() % 20;

        if (level.isClientSide) {
            if (tickInSecond != 0) return;
            if (beacon.tier < 3) {
                showRangeParticles(level, pos, ranges[beacon.tier]);
            }
        } else {
            level.players().forEach(player -> {
                if (beacon.isInRange(player, pos)) {
                    beacon.grantFlight(player);
                }
            });
            beacon.playersWithGrantedFlight.forEach(player -> {
                if (!beacon.isInRange(player, pos)) {
                    beacon.takeAwayFlight(player);
                }
            });
        }


    }

    public void onRemove() {
        playersWithGrantedFlight.forEach(player -> {
            takeAwayFlight(player);
        });
    }
}
