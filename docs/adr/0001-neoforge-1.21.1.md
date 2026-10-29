# 0001 NeoForge 1.21.1 com Java 21

**Status:** aceita (2026-10-28)

**Contexto:** o mod será jogado junto com Create, L_Ender's Cataclysm e Twilight Forest. Mods de loaders diferentes não carregam juntos.

**Decisão:** NeoForge 1.21.1, Java 21, ModDevGradle, a partir do MDK oficial (`NeoForgeMDKs/MDK-1.21.1-ModDevGradle`). Pacote `com.navesdev.grimoireknights`, mod id `grimoireknights`.

**Alternativas descartadas:**
- Fabric: o modpack desejado vive no NeoForge.
- NeoForge 26.1: ecossistema do modpack ainda não migrou.

**Consequência:** dados do jogador (mana, nível, status, árvore, grimório) usam data attachments; rede e config vêm do NeoForge; mixins só quando não houver evento. Portar para 26.1 quando o modpack migrar.
