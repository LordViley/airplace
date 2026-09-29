# AirPlace (Fabric port)

Same mod as the NeoForge version, ported to Fabric for Minecraft 26.3 / Java 25.

## Building

```
./gradlew build
```
(`gradlew.bat build` on Windows)

Jar lands at `build/libs/airplace-1.0.0.jar`.

```
./gradlew runClient
```
to test without installing anything.

## What changed vs. the NeoForge version

Because both loaders use Mojang's real class names, the actual game logic
(`GhostTargeting.java` - the raycast/candidate math) is essentially unchanged. What's
different is the loader-specific plumbing:

| Piece | NeoForge | Fabric |
|---|---|---|
| Mod entrypoint | `@Mod` annotation | `ModInitializer` / `ClientModInitializer` |
| Config | `ModConfigSpec` (built in) | hand-rolled JSON via Gson → `config/airplace.json` |
| Keybind | `RegisterKeyMappingsEvent` | `KeyMappingHelper` + `KeyMapping.Category`, `InputConstants.Type.KEYBOARD` + `KEY_G` (26.3 uses SDL, not GLFW) |
| Tick loop | `ClientTickEvent.Post` | `ClientTickEvents.END_CLIENT_TICK` (Fabric API) |
| Rendering | `RenderLevelStageEvent` + `renderLineBox` | `LevelRenderEvents.BEFORE_GIZMOS` (client-tick fallback) + vanilla `Gizmos.cuboid` (wireframe; independent of the 26.3 renderer rewrite) |
| Networking | `PayloadRegistrar` | `PayloadTypeRegistry` + `ServerPlayNetworking` / `ClientPlayNetworking` (Fabric API) |
| Intercepting right-click | `InputEvent.InteractionKeyMappingTriggered` | `UseItemCallback` + `UseBlockCallback` (return `InteractionResult`) |

The actual packet class (`PlaceInAirPayload`) and the server-side validation
(`AirPlaceNetwork#place`) are nearly line-for-line identical to the NeoForge version,
since both are built on vanilla's own `CustomPacketPayload` system.

## Config

`config/airplace.json`, generated on first launch. Same fields as the NeoForge version
(radius, reach, colors, toggle mode), just JSON instead of TOML.

## A heads-up on this port

This targets Minecraft 26.3 / Fabric Loader 0.19.5 / Fabric API 0.161.0+26.3 - a toolchain
released after my training data, so I can't personally verify every Fabric API class/package
name against the real 26.3 jars the way I could reason confidently about 1.21.1's NeoForge
API. The parts most likely to need a small fix if they've moved:

- `net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents` and its `AFTER_TRANSLUCENT`
  phase
- `net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents`
- `net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper`
- `net.fabricmc.fabric.api.event.player.UseItemCallback`
- `net.fabricmc.fabric.api.networking.v1.*`

If `./gradlew build` throws an "cannot find symbol" or "package does not exist" error on any
of these, that's almost certainly just a version-suffix bump (e.g. `v1` → `v2`) rather than a
deeper problem - paste me the exact error and I can fix it in one pass, the same way we
iterated on the NeoForge build.

## License

MIT
