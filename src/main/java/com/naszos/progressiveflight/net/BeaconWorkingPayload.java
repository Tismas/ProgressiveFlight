package com.naszos.progressiveflight.net;

import com.naszos.progressiveflight.ProgressiveFlightMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record BeaconWorkingPayload(Vector3f pos, boolean isWorking) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BeaconWorkingPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ProgressiveFlightMod.MODID, "beacon_working"));

    public static final StreamCodec<ByteBuf, BeaconWorkingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F,
            BeaconWorkingPayload::pos,
            ByteBufCodecs.BOOL,
            BeaconWorkingPayload::isWorking,
            BeaconWorkingPayload::new
    );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
