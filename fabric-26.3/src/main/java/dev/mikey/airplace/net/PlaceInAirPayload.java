package dev.mikey.airplace.net;

import dev.mikey.airplace.AirPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client -> server request to place the held block at {@code pos}.
 * Built on vanilla's own CustomPacketPayload system, so this class is identical whether the
 * mod runs on Fabric or NeoForge - only the registration/send/receive plumbing differs.
 */
public record PlaceInAirPayload(BlockPos pos, byte face, boolean offhand) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlaceInAirPayload> TYPE =
            new CustomPacketPayload.Type<>(AirPlace.id("place_in_air"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceInAirPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PlaceInAirPayload::pos,
                    ByteBufCodecs.BYTE, PlaceInAirPayload::face,
                    ByteBufCodecs.BOOL, PlaceInAirPayload::offhand,
                    PlaceInAirPayload::new);

    public PlaceInAirPayload(BlockPos pos, Direction face, boolean offhand) {
        this(pos, (byte) face.get3DDataValue(), offhand);
    }

    public Direction direction() {
        return Direction.from3DDataValue(face);
    }

    @Override
    public Type<PlaceInAirPayload> type() {
        return TYPE;
    }
}
