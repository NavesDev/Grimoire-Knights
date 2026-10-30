# Grimoire Knights

Minecraft mod (NeoForge 1.21.1) about grimoires in the style of Black Clover, made to be played as a duo.

- Mod design: [Grimoire Knights — Mod design document](https://claude.ai/artifact/TrcT8PC62YBtGZVcwWHpJx)
- Architecture: [`docs/architecture.md`](docs/architecture.md)
- Technical decisions: [`docs/adr/`](docs/adr/)
- Tools and environment: [`docs/environment.md`](docs/environment.md)
- Changes: [`CHANGELOG.md`](CHANGELOG.md)

## Commands
```bash
./gradlew build        # compiles, tests and builds build/libs/grimoireknights-<version>.jar
./gradlew runClient    # opens the dev client with the mod
./gradlew runClient2   # second dev client (Dev2) for LAN multiplayer tests
./gradlew runServer    # dedicated dev server
./gradlew runGameTestServer
```

To play: copy the `.jar` from `build/libs/` and the dependencies (GeckoLib, Curios API, playerAnimator) into the `mods` folder of a NeoForge 1.21.1 installation.
