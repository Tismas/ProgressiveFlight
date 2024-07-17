package com.naszos.progressiveflight.blocks;

import com.naszos.progressiveflight.Config;
import com.naszos.progressiveflight.ProgressiveFlightMod;
import com.naszos.progressiveflight.net.BeaconWorkingPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.*;

@EventBusSubscriber(modid = ProgressiveFlightMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class FlightBeaconBE extends BlockEntity {
    private final int tier;
    private final static int[] ranges = {4, 16, 64};
    private final static int[] rfCost = {4, 16, 64, 1024, 4096};
    private final HashSet<Player> playersWithGrantedFlight = new HashSet<Player>();
    private final UUID id = UUID.randomUUID();
    private final static TicketController ticketController = new TicketController(ResourceLocation.fromNamespaceAndPath(ProgressiveFlightMod.MODID, "chunk_loader"));
    public final EnergyStorage energy;
    private boolean hadPowerLastTick = false;
    private boolean showParticles = !Config.needsPower;

    public FlightBeaconBE(BlockPos pPos, BlockState pBlockState, int tier) {
        super(FlightBeacon.blockEntities[tier].get(), pPos, pBlockState);
        this.tier = tier;
        this.energy = new EnergyStorage((int) (rfCost[tier] * 1000 * Config.powerMultiplier), 100000);
    }

    public IEnergyStorage getCapability(Direction side) {
        if (!Config.needsPower) return null;
        return energy;
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
            if (player.getCommandSenderWorld().dimension().compareTo(this.level.dimension()) != 0) return false;
            int range = ranges[this.tier];
            double distance = dist(player, pos);
            return range > distance;
        }

        List<String> owners = this.getData(ProgressiveFlightMod.BLOCK_OWNERS);
        if (!owners.isEmpty() && !owners.contains(player.getStringUUID())) return false;

        if (tier == 3) {
            return player.getCommandSenderWorld().dimension().compareTo(this.level.dimension()) == 0;
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

            double x = pos.getX() + 0.5 + deltaX;
            double z = pos.getZ() + 0.5 + deltaZ;
            double y = getProperYForParticle(level, x, pos.getY(), z);
            level.addParticle(ParticleTypes.GLOW, false, x, y, z, 0, 0.4, 0);
        }
    }

    private static double getProperYForParticle(Level level, double x, double y, double z) {
        int maxDiff = 20;
        double res = y;

        while (Math.abs(y - res) < maxDiff && res > level.getMinBuildHeight() && level.getBlockState(new BlockPos((int) x, (int) res, (int) z)).is(Blocks.AIR)) {
            res -= 1;
        }
        while (Math.abs(y - res) < maxDiff && res < level.getMaxBuildHeight() && !level.getBlockState(new BlockPos((int) x, (int) res, (int) z)).is(Blocks.AIR)) {
            res += 1;
        }

        if (Math.abs(y - res) == maxDiff) return y;

        return res;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        FlightBeaconBE beacon = (FlightBeaconBE) blockEntity;
        long tickInSecond = level.getGameTime() % 20;

        if (level.isClientSide) {
            if (tickInSecond != 0) return;
            if (beacon.tier < 3 && beacon.showParticles) {
                showRangeParticles(level, pos, ranges[beacon.tier]);
            }
        } else {
            ServerLevel sLevel = (ServerLevel) level;
            int cost = Config.needsPower ? (int) (rfCost[beacon.tier] * Config.powerMultiplier) : 0;
            boolean hasEnoughPower = beacon.energy.getEnergyStored() >= cost;
            if (hasEnoughPower) {
                if (!beacon.hadPowerLastTick) {
                    beacon.hadPowerLastTick = true;
                    if (beacon.tier < 3) {
                        PacketDistributor.sendToPlayersTrackingChunk(sLevel, new ChunkPos(pos), new BeaconWorkingPayload(new Vector3f(pos.getX(), pos.getY(), pos.getZ()), true));
                    }
                }
                beacon.energy.extractEnergy(cost, false);

                level.players().forEach(player -> {
                    if (beacon.isInRange(player, pos)) {
                        beacon.grantFlight(player);
                    }
                });
            } else if (beacon.hadPowerLastTick) {
                beacon.hadPowerLastTick = false;
                if (beacon.tier < 3) {
                    PacketDistributor.sendToPlayersTrackingChunk(sLevel, new ChunkPos(pos), new BeaconWorkingPayload(new Vector3f(pos.getX(), pos.getY(), pos.getZ()), false));
                }
            }

            beacon.playersWithGrantedFlight.forEach(player -> {
                if (!beacon.isInRange(player, pos) || !hasEnoughPower) {
                    beacon.takeAwayFlight(player);
                }
            });
        }


    }

    public void onRemove(Level pLevel, BlockPos pPos) {
        playersWithGrantedFlight.forEach(player -> {
            takeAwayFlight(player);
        });

        if (pLevel.isClientSide) return;
        ticketController.forceChunk((ServerLevel) pLevel, pPos, SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()), false, true);
    }

    public void onPlace(ServerLevel pLevel, BlockPos pPos, Entity owner) {
        if (owner instanceof Player) {
            ArrayList<String> owners = new ArrayList<>();
            owners.add(owner.getStringUUID());
            this.setData(ProgressiveFlightMod.BLOCK_OWNERS, owners);
        }
        ticketController.forceChunk(pLevel, pPos, SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()), true, true);
    }

    public void setParticlesVisible(boolean visible) {
        this.showParticles = visible;
    }

    @SubscribeEvent
    public static void onBlockPlace(RegisterTicketControllersEvent e) {
        e.register(ticketController);
    }
}
