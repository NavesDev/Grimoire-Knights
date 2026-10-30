# 0002 Mod libraries

**Status:** accepted (2026-10-28)

**Decision:**
| Library | Version | Use |
|---|---|---|
| GeckoLib | 4.9.3 | 3D models and animations: bosses, mages, creatures, swords, floating grimoire |
| playerAnimator (KosmX) | 2.0.4+1.21.1 | Player poses: casting, sword, broom, sword flight |
| Curios API | 9.5.1+1.21.1 | Back slot for the mantle |

All required in `neoforge.mods.toml`.

**Rejected alternatives:**
- PlayerAnimationLib (zigythebird): 1.21.1 build stuck at 1.1.6; playerAnimator has a maintained 1.21.1 build and is what the 1.21.1 modpack already uses (e.g. Better Combat).
- Lodestone (VFX): left out until vanilla particles aren't enough.

**Consequence:** player animations play on the client; syncing them to other players needs our own payload.
