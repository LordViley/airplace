# AirPlace (NeoForge 26.3 port)

Same mod, ported to NeoForge for Minecraft 26.3. **NeoForge's own 26.3 build is tagged beta**
(`neo_version=26.3.0.36-beta` in gradle.properties) - so expect this to be less stable ground
than the 1.21.1 or Fabric 26.3 versions.

## Building

```
gradlew build
```
Jar lands at `build/libs/airplace-1.0.0.jar`.

```
gradlew runClient
```
to test without installing anything.

## What's different from the 1.21.1 NeoForge version

This was built by applying every vanilla-level (loader-independent) rename already confirmed
while porting to Fabric 26.3, rather than rediscovering them one compiler error at a time:

| Old (1.21.1) | New (26.3) | Confirmed via |
|---|---|---|
| `ResourceLocation` | `Identifier` | The real 26.3 NeoForge template's own `Config.java` |
| `ServerPlayer#serverLevel()` | `ServerPlayer#level()` (covariant) | Real 26.1 vanilla docs |
| `Level#mayInteract(player, pos)` | `player.mayInteract(level, pos)` | Real 26.1 vanilla docs |
| `Direction#getNormal()` | `getStepX()/getStepY()/getStepZ()` | Real 26.1 vanilla docs |
| `InputConstants.Type.KEYSYM` + GLFW codes | `Type.KEYBOARD` + `InputConstants.KEY_*` | Confirmed during the Fabric 26.3 build (Mojang moved from GLFW to SDL3) |
| `player.swing(hand)` | removed (signature changed, cosmetic only) | Real compiler error during the Fabric 26.3 build |
| `RenderType.lines()` / `renderLineBox` / `MultiBufferSource` | `Gizmos.cuboid()` / `GizmoStyle` | The old path broke on Fabric because vanilla's whole GPU layer was rewritten (`blaze3d` -> `renderpearl`), which affects every loader equally |

## Round 3 fix: my own directory mistake, not a 26.3 change

`neoforge.mods.toml` was placed directly under `src/main/resources/META-INF/`. This specific
generated `build.gradle` only expands `${...}` placeholders for files under `src/main/templates/`
(via the `generateModMetadata` task), copying the result into `build/generated/...` and adding
that as an extra resources directory. A file placed straight in `src/main/resources/` skips that
step entirely and gets copied to the build output with the literal `${mod_id}` etc. still in it -
which is exactly what caused `Invalid bare key: '${mod_id}'` at runtime. Fixed by moving the file
to `src/main/templates/META-INF/neoforge.mods.toml`, matching this template's actual convention.

## Round 2 fixes (from the first real compile attempt)

| Old | New | Confirmed via |
|---|---|---|
| `@EventBusSubscriber(..., bus = EventBusSubscriber.Bus.MOD, ...)` | drop `bus = ...` entirely | Compiler: `Bus`/`bus()` no longer exist; the other two subscriber classes already compiled fine without it |
| `KeyMapping(..., String category)` | `KeyMapping(..., KeyMapping.Category category)`, via `KeyMapping.Category.register(Identifier)` | The compiler error spelled out the exact required type |
| `PacketDistributor.sendToServer(...)` | Sends the vanilla `ServerboundCustomPayloadPacket` directly via `mc.getConnection().send(...)`, bypassing NeoForge's convenience wrapper entirely | Real compiler error confirmed `PacketDistributor` no longer has this method; the exact current replacement class/package for this specific beta snapshot could not be confirmed via search, so this sidesteps the question by using only stable vanilla networking classes |
| `RenderLevelStageEvent` + `Stage` enum + `getStage()` | `RenderLevelStageEvent.AfterLevel` (a nested event class you subscribe to directly) | Real NeoForge javadoc: the whole event was restructured from stage-enum-based to separate nested event classes (`AfterSky`, `AfterOpaqueBlocks`, `AfterEntities`, `AfterLevel`, etc.) |
| `mc.screen != null` | dropped (harmless if skipped) | Compiler: field/accessor moved, not worth guessing for a minor UX nicety |

## The one real unknown: `GhostRenderer`

Everything above came from something verifiable. This one still isn't:

On Fabric, gizmos only draw successfully when collected during a specific extraction-phase
event (`LevelExtractionEvents.END_EXTRACTION`) - calling `Gizmos.cuboid()` outside that window
throws `IllegalStateException`. Whether NeoForge's `RenderLevelStageEvent.AfterLevel` overlaps
that same window is **not verified** - it's a reasonable guess (fires latest, most likely to
overlap), not a confirmed fact.

If it's wrong, the mod won't crash - `GhostRenderer` catches `IllegalStateException` and logs
one warning instead of drawing. **If you toggle the mod on and see no red/green outlines at
all, check `logs/latest.log` for a line starting with "AirPlace: gizmos could not be drawn"**
- that confirms this guess was wrong, and the fix is trying a different nested event class
(`AfterEntities`, `AfterOpaqueBlocks`, etc.).

## License

MIT
