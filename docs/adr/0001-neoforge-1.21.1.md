# 0001 NeoForge 1.21.1 with Java 21

**Status:** accepted (2026-10-28)

**Context:** the mod will be played alongside Create, L_Ender's Cataclysm and Twilight Forest. Mods for different loaders don't load together.

**Decision:** NeoForge 1.21.1, Java 21, ModDevGradle, starting from the official MDK (`NeoForgeMDKs/MDK-1.21.1-ModDevGradle`). Package `com.navesdev.grimoireknights`, mod id `grimoireknights`.

**Rejected alternatives:**
- Fabric: the target modpack lives on NeoForge.
- NeoForge 26.1: the modpack's ecosystem hasn't migrated yet.

**Consequence:** player data (mana, level, stats, tree, grimoire) uses data attachments; networking and config come from NeoForge; mixins only when there is no event. Port to 26.1 when the modpack migrates.
