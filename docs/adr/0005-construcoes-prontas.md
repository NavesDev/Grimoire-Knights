# 0005 Construções prontas da comunidade como base das estruturas

**Status:** aceita (2026-10-29)

**Decisão:**
- Estruturas grandes podem partir de uma construção pronta (Bloxelizer e afins), exportada em `.nbt` de structure block para a 1.21.1 e guardada como fonte em `art/structures/source/`.
- O `run.py` continua sendo a fonte do que o jogo usa: lê a construção, troca o interior pelo do mod (Altar, sacerdote, estantes, grimórios) e grava o template em `data/grimoireknights/structure/`. O arquivo baixado nunca é editado à mão.
- Cada construção usada tem autor e página registrados em `docs/estruturas.md`. Antes de publicar o mod, é preciso a permissão do autor; sem ela, a estrutura volta a ser feita no script.

**Alternativas descartadas:**
- Construir tudo bloco a bloco no script: o resultado ficou feio e caro de iterar.
- Construir no jogo e salvar com structure block: o template salvo vira a fonte e as trocas de interior se perdem a cada nova versão.

**Consequência:** trocar a construção é baixar outro `.nbt` e ajustar o interior no script. As construções baixadas não declaram licença.
