package dev.mikey.airplace.client;

import dev.mikey.airplace.AirPlace;
import dev.mikey.airplace.AirPlaceConfig;
import dev.mikey.airplace.net.PlaceInAirPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
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

        if (!active || mc.screen != null) {
            state = null;
            return;
        }

        // Recomputed every tick; the renderer just reads it. Cheap at these radii
        // (27 positions by default) and keeps the render thread free of world lookups.
        state = GhostTargeting.compute(mc.player, mc.level, 1.0F);
    }

    /**
     * While the mode is on and a spot is highlighted, right-click places there instead of
     * doing whatever vanilla would have done with the block under the crosshair.
     */
    @SubscribeEvent
    public static void onUseItem(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) return;
        if (!active || state == null || !state.hasTarget()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        InteractionHand hand = handWithBlock(mc);
        if (hand == null) return;

        var hit = state.target();
        PacketDistributor.sendToServer(
                new PlaceInAirPayload(hit.getBlockPos(), hit.getDirection(), hand == InteractionHand.OFF_HAND));

        mc.player.swing(hand);
        event.setSwingHand(false);
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
