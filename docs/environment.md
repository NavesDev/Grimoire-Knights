# Development environment

| Tool | Version | Where |
|---|---|---|
| JDK | 21 (Temurin) | SDKMAN `21.0.13.0+9-tem`; daemon pinned in `gradle/gradle-daemon-jvm.properties` (the system OpenJDK 21 is only a JRE, without `javac`) |
| Gradle | 9.2.1 (wrapper) | `./gradlew` |
| ModDevGradle | 2.0.148 | `build.gradle` |
| NeoForge | 21.1.265 | `gradle.properties` |
| Blockbench | 5.2.2 | `~/Apps/Blockbench/Blockbench.AppImage` |
| Blockbench MCP | 1.10.1 | `~/Apps/Blockbench/mcp.js` |

The dev environment downloads its own copy of Minecraft; it neither uses nor changes the game installation.

## Blockbench MCP
Lets Claude drive Blockbench (models and animations).

1. Open Blockbench, **File > Plugins > Load Plugin from File** and pick `~/Apps/Blockbench/mcp.js`.
2. With Blockbench open, the server is at `http://localhost:3000/bb-mcp`.
3. Register it in Claude Code: `claude mcp add --transport http blockbench http://localhost:3000/bb-mcp`.

## Dependency versions
They live in `gradle.properties`. To update, check each repository's `maven-metadata.xml` (URLs in `build.gradle`).
