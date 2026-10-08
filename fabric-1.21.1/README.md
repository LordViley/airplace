# AirPlace (Fabric, Minecraft 1.21.1)

Hotkey (default **G**) outlines every placeable spot around you in red; the one under your
crosshair turns green. Right-click places the held block there - no supporting block needed.

## Building

Needs **JDK 21** (not 25 - that's the 26.x Fabric/NeoForge ports).

```
gradlew build
```

The mod jar is `build/libs/airplace-fabric-1.21.1-1.0.0.jar`. Ignore the `-sources.jar`.

```
gradlew runClient
```

## Config

`config/airplace.json`, created on first launch: workspace size (`horizontalRadius` /
`verticalRadius`), `reach`, `strictAirOnly`, `toggleMode`, outline colours (RRGGBB hex).

## Notes

- Both client and server need the mod (the server validates every placement).
- If the server doesn't have AirPlace, the client leaves normal right-click alone.
- Uses Mojang mappings, so the game logic matches the NeoForge 1.21.1 version almost line
  for line; only the loader plumbing (keybind, tick, render hook, networking) differs.

## License

MIT
