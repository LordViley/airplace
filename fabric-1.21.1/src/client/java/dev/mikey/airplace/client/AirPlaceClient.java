package dev.mikey.airplace.client;

import dev.mikey.airplace.AirPlaceConfig;
import dev.mikey.airplace.net.PlaceInAirPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AirPlaceClient implements ClientModInitializer {

    private static volatile boolean active;
    @Nullable
    private static volatile GhostTargeting.Result state;

    public static boolean isActive() {
        return active;
    }

    @Nullable
    public static GhostTargeting.Result getState() {
        return state;
    }

    @Override
    public void onInitializeClient() {
        AirPlaceKeys.register();
        GhostRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(AirPlaceClient::onClientTick);

        // Aiming into open air fires UseItemCallback; aiming at a real block face fires
        // UseBlockCallback first. Handle both so the green cell always wins over vanilla placement.
        UseItemCallback.EVENT.register((player, level, hand) ->
                tryPlace(player, level, hand)
                        ? InteractionResultHolder.success(player.getItemInHand(hand))
                        : InteractionResultHolder.pass(player.getItemInHand(hand)));

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
                tryPlace(player, level, hand) ? InteractionResult.SUCCESS : InteractionResult.PASS);
    }

    /** Sends the placement request if the mode is on and a green cell is targeted. */
    private static boolean tryPlace(Player player, Level level, InteractionHand hand) {
        // These events also fire for the integrated server in singleplayer - client side only.
        if (!level.isClientSide()) return false;

        GhostTargeting.Result current = state;
        if (!active || current == null || !current.hasTarget()) return false;

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BlockItem)) return false;

        // If the server doesn't have AirPlace installed, leave vanilla behaviour alone.
        if (!ClientPlayNetworking.canSend(PlaceInAirPayload.TYPE)) return false;

        var hit = current.target();
        ClientPlayNetworking.send(new PlaceInAirPayload(
                hit.getBlockPos(), hit.getDirection(), hand == InteractionHand.OFF_HAND));
        return true; // SUCCESS also makes vanilla play the arm swing
    }

    private static void onClientTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            active = false;
            state = null;
            return;
        }

        AirPlaceConfig.Data cfg = AirPlaceConfig.CURRENT;
        if (cfg.toggleMode) {
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

        state = GhostTargeting.compute(mc.player, mc.level, 1.0F);
    }
}
