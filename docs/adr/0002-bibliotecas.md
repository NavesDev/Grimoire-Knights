# 0002 Bibliotecas do mod

**Status:** aceita (2026-10-28)

**Decisão:**
| Biblioteca | Versão | Uso |
|---|---|---|
| GeckoLib | 4.9.3 | Modelos e animações 3D: bosses, magos, criaturas, espadas, grimório flutuante |
| playerAnimator (KosmX) | 2.0.4+1.21.1 | Poses do jogador: lançar magia, espada, vassoura, voo na espada |
| Curios API | 9.5.1+1.21.1 | Slot de costas para o manto |

Todas obrigatórias em `neoforge.mods.toml`.

**Alternativas descartadas:**
- PlayerAnimationLib (zigythebird): build 1.21.1 parada em 1.1.6; playerAnimator tem build 1.21.1 mantida e é o que o modpack de 1.21.1 já usa (ex.: Better Combat).
- Lodestone (VFX): fica fora até as partículas vanilla não bastarem.

**Consequência:** animações do jogador tocam no cliente; sincronizar com outros jogadores exige pacote próprio.
