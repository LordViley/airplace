package dev.mikey.airplace.client;

import dev.mikey.airplace.AirPlace;
import dev.mikey.airplace.AirPlaceConfig;
import dev.mikey.airplace.net.PlaceInAirPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = AirPlace.MODID, value = Dist.CLIENT)
public final class AirPlaceClient {

    private static boolean active;
    @Nullable
    private static GhostTargeting.Result state;

    public static boolean isActive() {
        return active;
    }

    @Nullable
    public static GhostTargeting.Result getState() {
        return state;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) {
            active = false;
            state = null;
            return;
        }

        if (AirPlaceConfig.TOGGLE_MODE.get()) {
            while (AirPlaceKeys.TOGGLE.consumeClick()) {
                active = !active;
            }
        } else {
            while (AirPlaceKeys.TOGGLE.consumeClick()) { /* drain queued clicks */ }
            active = AirPlaceKeys.TOGGLE.isDown();
        }

        // NOTE: Minecraft#screen was renamed/moved in 26.x (confirmed by compiler error) - the
        // check for "is a menu currently open" is dropped rather than guessed at; worst case
        // the highlight stays visible while a screen is open, which is harmless.
        if (!active) {
            state = null;
            return;
        }

        state = GhostTargeting.compute(mc.player, mc.level, 1.0F);
    }

    @SubscribeEvent
    public static void onUseItem(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) return;
        if (!active || state == null || !state.hasTarget()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        InteractionHand hand = handWithBlock(mc);
        if (hand == null) return;

        var hit = state.target();
        PlaceInAirPayload payload = new PlaceInAirPayload(
                hit.getBlockPos(), hit.getDirection(), hand == InteractionHand.OFF_HAND);

        // NOTE: NeoForge's convenience wrapper for client->server sends (previously
        // PacketDistributor.sendToServer) moved in 26.x, and its exact current name/package
        // for this specific beta build isn't confirmed. Sending the vanilla packet directly
        // sidesteps that uncertainty entirely - CustomPacketPayload/ServerboundCustomPayloadPacket
        // are stable vanilla classes, not a NeoForge convenience API that keeps moving.
        var connection = mc.getConnection();
        if (connection != null) {
            connection.send(new ServerboundCustomPayloadPacket(payload));
        }

        // Arm swing is cosmetic only, and LivingEntity#swing's signature changed in 26.x
        // (confirmed during the Fabric port), so it's skipped here rather than guessed at.
        event.setCanceled(true);
    }

    @Nullable
    private static InteractionHand handWithBlock(Minecraft mc) {
        if (mc.player == null) return null;
        if (mc.player.getMainHandItem().getItem() instanceof BlockItem) return InteractionHand.MAIN_HAND;
        if (mc.player.getOffhandItem().getItem() instanceof BlockItem) return InteractionHand.OFF_HAND;
        return null;
    }

    private AirPlaceClient() {}
}
