# AirPlace

A builder's tool for Minecraft 1.21.1 (NeoForge). Press a hotkey and every placeable spot
around you outlines in red; the one under your crosshair turns green. Right-click places a
block there — no supporting block required.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
![Loader](https://img.shields.io/badge/loader-NeoForge-orange)

## Features

- Rebindable hotkey (default **G**), under Options → Controls → AirPlace
- Toggle mode or hold mode
- Configurable radius, reach, and outline colours
- Works in survival: consumes the item, plays the sound, fires advancements
- Server-authoritative — respects build permission, spawn protection and claim mods

## Building

Needs **JDK 21**. Nothing else; the Gradle wrapper fetches the rest.

```bash
./gradlew build
```

The mod jar lands in `build/libs/airplace-1.0.0.jar`.

To test in-game without installing anything:

```bash
./gradlew runClient
```

## Configuration

`config/airplace-common.toml`, generated on first launch.

| Key | Default | Meaning |
|---|---|---|
| `horizontalRadius` | 1 | 1 = 3×3 footprint |
| `verticalRadius` | 1 | 1 = 3 layers, so 3×3×3 total |
| `reach` | 6.0 | targeting distance in blocks |
| `strictAirOnly` | false | true = pure air only, no tall grass/snow/water |
| `toggleMode` | true | false = highlight only while the key is held |
| `candidateColor` | FF3B30 | RRGGBB hex |
| `targetColor` | 34C759 | RRGGBB hex |

## How it works

`Minecraft#hitResult` skips air entirely, so `GhostTargeting` gives each candidate position a
phantom unit cube and uses the static `AABB.clip(boxes, from, to, pos)` overload — that returns
a `BlockHitResult` including the entry face, which is what lets stairs and slabs orient
correctly.

Placement can't use `ServerboundUseItemOnPacket` (it requires a hit against a real block), so
the client cancels the right-click and sends `PlaceInAirPayload`. The server rebuilds a
synthetic `BlockHitResult` and runs `BlockItem#place`. `AirPlaceNetwork#place` re-validates
everything the packet claims before touching the world.

## Porting past 1.21.1

1.21.11 was the last `1.x` release; Minecraft moved to calver in 2026 and the current line is
26.x. NeoForge's migration notes for 1.21.1 → 26.1 span breaking releases 21.2, 21.4, 21.5,
21.6, 21.9 and 21.11.

`GhostRenderer` is the file that will fight you — the render pipeline rewrite across 21.5/21.6
changed `RenderType` construction, the `RenderLevelStageEvent.Stage` constants, and moved
vertex colours from four floats to packed ARGB ints. Everything else ports fairly
mechanically. For one source tree across many versions use
[Stonecutter](https://stonecutter.kikugie.dev/); reflection shims will not survive that gap.

## License

MIT
