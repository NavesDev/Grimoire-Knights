# Grimoire Knights

NeoForge 1.21.1 mod (Java 21). Design: https://claude.ai/artifact/TrcT8PC62YBtGZVcwWHpJx (read it through the Claude Docs connector). Architecture: `docs/architecture.md`. Decisions: `docs/adr/`. Environment: `docs/environment.md`. Models: `docs/models.md`.

- Everything in the repo in English: code, identifiers, commits (conventional commits), docs, changelog, issues and PRs. The only exception is the game translation in `pt_br.json`.
- Game texts in `assets/grimoireknights/lang/` (`en_us.json` and `pt_br.json`), never as literals in code.
- Prefer NeoForge events and data attachments over mixins.
- Read the NeoForge 1.21.1 docs (https://docs.neoforged.net/docs/1.21.1/) before using an API; not from memory.
- `./gradlew build` before asking for review; `./gradlew runClient` to see it in game.
