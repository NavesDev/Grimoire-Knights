# Ambiente de desenvolvimento

| Ferramenta | Versão | Onde |
|---|---|---|
| JDK | 21 (Temurin) | SDKMAN `21.0.13.0+9-tem`; daemon fixado em `gradle/gradle-daemon-jvm.properties` (o OpenJDK 21 do sistema é só JRE, sem `javac`) |
| Gradle | 9.2.1 (wrapper) | `./gradlew` |
| ModDevGradle | 2.0.148 | `build.gradle` |
| NeoForge | 21.1.265 | `gradle.properties` |
| Blockbench | 5.2.2 | `~/Apps/Blockbench/Blockbench.AppImage` |
| Blockbench MCP | 1.10.1 | `~/Apps/Blockbench/mcp.js` |

O ambiente de dev baixa sua própria cópia do Minecraft; não usa nem altera a instalação de jogo.

## Blockbench MCP
Deixa o Claude operar o Blockbench (modelos e animações).

1. Abrir o Blockbench, **File > Plugins > Load Plugin from File** e escolher `~/Apps/Blockbench/mcp.js`.
2. Com o Blockbench aberto, o servidor fica em `http://localhost:3000/bb-mcp`.
3. Registrar no Claude Code: `claude mcp add --transport http blockbench http://localhost:3000/bb-mcp`.

## Versões das dependências
Ficam em `gradle.properties`. Para atualizar, conferir o `maven-metadata.xml` do repositório de cada uma (URLs em `build.gradle`).
