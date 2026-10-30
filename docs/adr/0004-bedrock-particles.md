# 0004 Particle effects made in Blockbench, played by our own runtime

**Status:** accepted (2026-10-29)

**Decision:**
- Visual effects (VFX) are made in Blockbench's particle editor, in the Bedrock (Snowstorm) format, and saved to `assets/grimoireknights/particles/<effect>.json`; textures in `assets/grimoireknights/textures/particle/`.
- The mod reads these files (they reload with F3+T) and plays them on the client, in `core/client/vfx`. It simulates and draws them itself in `RenderLevelStageEvent`, outside the vanilla `ParticleEngine`, to have a texture per effect and additive glow.
- The server only asks for the effect: a payload with id, position and Molang variables (`variable.<name>`) sent to whoever can see the place.
- Only what the mod's effects use is supported, growing with them. Molang: numbers, `+ - * /`, parentheses, comparison, `?:`, common `math.*`, `variable.*` and age and random `query.*`. Any other component or function fails on load, with its name in the log (fail-fast).

**Rejected alternatives:**
- Translating by hand into vanilla particles: Blockbench's preview doesn't match the game and every tweak is done twice.
- Ready-made library: none for NeoForge 1.21.1 reads the Bedrock format (WorldSeedGames' ParticleEmitter is for Minestom).
- Lodestone, Photon and Veil: their own formats and editors, outside Blockbench.

**Consequence:** a new effect is one more JSON, no code. A new Bedrock component needs runtime code.
