# 0003 Feature-based architecture with pure rules

**Status:** accepted (2026-10-28)

**Decision:** details in [`docs/architecture.md`](../architecture.md).
- Packages by feature (`grimoire`, `progression`, `ability`...), with subpackages by technical type inside; `core` for what is general.
- Game rules in `rules/`, plain Java tested with JUnit.
- Balancing in a `SERVER` config per feature.
- Spells and swords: one class per ability, a single `Ability` interface, own registry.
- Quests: generic engine in `quest`, right above `core`. Quests are datapack JSON; objective and reward types are classes registered by the features that own them; special behavior listens to the quest's events.

**Rejected alternatives:**
- Packages by technical type (`block/`, `item/`, `entity/`): spreads each system over several packages.
- Rules inside the handlers: only testable through GameTest.
- Balancing in datapack JSON or constants: more loading code, or a rebuild on every tweak.
- Abilities composed from JSON: too early an abstraction for 3 spells per element.
- Special quests in their own code: every new task would need a new system.
