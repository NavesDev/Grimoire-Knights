# Grimoire Knights

Mod de Minecraft (NeoForge 1.21.1) de grimórios no estilo Black Clover, feito para jogar em dupla.

- Design do mod: [Grimoire Knights — Documento de design do mod](https://claude.ai/artifact/TrcT8PC62YBtGZVcwWHpJx)
- Arquitetura: [`docs/arquitetura.md`](docs/arquitetura.md)
- Decisões técnicas: [`docs/adr/`](docs/adr/)
- Ferramentas e ambiente: [`docs/ambiente.md`](docs/ambiente.md)

## Comandos
```bash
./gradlew build        # compila, testa e gera build/libs/grimoireknights-<versão>.jar
./gradlew runClient    # abre o cliente de dev com o mod
./gradlew runServer    # servidor dedicado de dev
./gradlew runGameTestServer
```

Para jogar: copiar o `.jar` de `build/libs/` e as dependências (GeckoLib, Curios API, playerAnimator) para a pasta `mods` de uma instalação NeoForge 1.21.1.
