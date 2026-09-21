package dev.mikey.airplace.net;

import dev.mikey.airplace.AirPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> server request to place the held block at {@code pos}.
 * {@code face} is the face of the phantom cube the look-ray entered, which lets the
 * server orient stairs, slabs and other directional blocks the way the player expects.
 * Every field here is untrusted; see {@link AirPlaceNetwork} for validation.
 */
public record PlaceInAirPayload(BlockPos pos, byte face, boolean offhand) implements CustomPacketPayload {

    public static final Type<PlaceInAirPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AirPlace.MODID, "place_in_air"));

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
        // from3DDataValue wraps out-of-range input, so a hostile client cannot crash us here.
        return Direction.from3DDataValue(face);
    }

    @Override
    public Type<PlaceInAirPayload> type() {
        return TYPE;
    }
}
