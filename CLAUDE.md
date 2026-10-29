# Grimoire Knights

Mod NeoForge 1.21.1 (Java 21). Design: https://claude.ai/artifact/TrcT8PC62YBtGZVcwWHpJx (ler pelo conector Claude Docs). Arquitetura: `docs/arquitetura.md`. Decisões: `docs/adr/`. Ambiente: `docs/ambiente.md`.

- Código, identificadores e commits em inglês (conventional commits); docs em pt-BR.
- Textos de jogo em `assets/grimoireknights/lang/` (`en_us.json` e `pt_br.json`), nunca literal no código.
- Preferir eventos e data attachments do NeoForge a mixins.
- Ler a doc do NeoForge 1.21.1 (https://docs.neoforged.net/docs/1.21.1/) antes de usar uma API; não de memória.
- `./gradlew build` antes de pedir revisão; `./gradlew runClient` para ver no jogo.
