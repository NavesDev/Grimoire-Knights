# 0005 Community builds as the base of structures

**Status:** accepted (2026-10-29)

**Decision:**
- Large structures may start from a ready-made build (Bloxelizer and the like), exported as a structure block `.nbt` for 1.21.1 and kept as source in `art/structures/source/`.
- `run.py` is still the source of what the game uses: it reads the build, replaces the interior with the mod's (Altar, priest, bookshelves, grimoires) and writes the template to `data/grimoireknights/structure/`. The downloaded file is never edited by hand.
- Each build used has its author and page recorded in `docs/structures.md`. Before publishing the mod, the author's permission is required; without it, the structure goes back to being made in the script.

**Rejected alternatives:**
- Building everything block by block in the script: the result looked bad and was costly to iterate on.
- Building in-game and saving with a structure block: the saved template becomes the source and the interior changes are lost on every new version.

**Consequence:** swapping a build means downloading another `.nbt` and adjusting the interior in the script. The downloaded builds don't state a license.
