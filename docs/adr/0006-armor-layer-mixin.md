# 0006 A mixin to hide armor pieces

**Status:** accepted (2026-11-01)

**Decision:**
- One client mixin, `HumanoidArmorLayerMixin`, cancels `HumanoidArmorLayer.renderArmorPiece` at its head for a player whose piece is hidden: the armor toggle is on, or a cosmetic accessory with rendering on sits in that piece's slot.
- The mixin only asks `cosmetic` (`ArmorLook.hidden`); the rule lives there. The armor stays equipped, so defense and durability are untouched.

**Rejected alternatives:**
- An event: NeoForge 21.1 has none per armor piece (`RenderLivingEvent`/`RenderPlayerEvent` only cover the whole entity, `IClientItemExtensions` only our own items).
- Emptying the armor slots on the client for the frame: other code reading the slots in that frame (other layers, mods) would see no armor.

**Consequence:** the mod ships a `grimoireknights.mixins.json`. A change to `renderArmorPiece`'s signature in a NeoForge update breaks the mixin at load, loudly.
