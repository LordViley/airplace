package dev.mikey.airplace.net;

import dev.mikey.airplace.AirPlaceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class AirPlaceNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(PlaceInAirPayload.TYPE, PlaceInAirPayload.CODEC, AirPlaceNetwork::handle);
    }

    private static void handle(PlaceInAirPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                place(player, payload);
            }
        });
    }

    /**
     * Re-checks everything the client claimed. Never trust the packet: a modified client
     * could otherwise place blocks across the map or through claim protection.
     */
    private static void place(ServerPlayer player, PlaceInAirPayload payload) {
        BlockPos pos = payload.pos();
        ServerLevel level = player.serverLevel();

        if (player.isSpectator() || !player.mayBuild()) return;
        if (!level.isLoaded(pos)) return;
        if (level.isOutsideBuildHeight(pos)) return;

        // Distance: config reach plus a small allowance for latency and eye height.
        double maxDist = AirPlaceConfig.REACH.get() + 1.5D;
        if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > maxDist * maxDist) return;

        // The block must be inside the advertised box around the player.
        BlockPos anchor = player.blockPosition();
        int hr = AirPlaceConfig.HORIZONTAL_RADIUS.get();
        int vr = AirPlaceConfig.VERTICAL_RADIUS.get();
        if (Math.abs(pos.getX() - anchor.getX()) > hr) return;
        if (Math.abs(pos.getZ() - anchor.getZ()) > hr) return;
        if (Math.abs(pos.getY() - anchor.getY()) > vr) return;

        // Spawn protection, world border, claim mods, etc.
        if (!level.mayInteract(player, pos)) return;

        if (AirPlaceConfig.STRICT_AIR_ONLY.get()) {
            if (!level.getBlockState(pos).isAir()) return;
        } else if (!level.getBlockState(pos).canBeReplaced()) {
            return;
        }

        // Don't let anyone seal themselves inside a block.
        if (player.getBoundingBox().intersects(new AABB(pos))) return;

        InteractionHand hand = payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;

        Direction face = payload.direction();
        Vec3 hitVec = Vec3.atCenterOf(pos)
                .add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5D));

        // inside == false, so BlockPlaceContext resolves to `pos` itself (it is replaceable).
        BlockHitResult hit = new BlockHitResult(hitVec, face, pos, false);
        BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, hand, hit));

        // BlockItem#place handles state-from-context, waterlogging, setPlacedBy,
        // the placement sound, advancement triggers and item consumption.
        blockItem.place(context);
    }

    private AirPlaceNetwork() {}
}
