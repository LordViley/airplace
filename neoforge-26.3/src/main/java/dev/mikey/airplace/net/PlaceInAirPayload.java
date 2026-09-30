package dev.mikey.airplace.net;

import dev.mikey.airplace.AirPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client -> server request to place the held block at {@code pos}.
 * NOTE: 26.x renamed ResourceLocation -> Identifier (vanilla-level rename, confirmed against
 * the real 26.3 NeoForge template's own Config.java, which already uses Identifier).
 */
public record PlaceInAirPayload(BlockPos pos, byte face, boolean offhand) implements CustomPacketPayload {

    public static final Type<PlaceInAirPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(AirPlace.MODID, "place_in_air"));

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
