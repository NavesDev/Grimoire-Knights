# 0004 Efeitos de partícula feitos no Blockbench, tocados por runtime próprio

**Status:** aceita (2026-10-29)

**Decisão:**
- Efeitos visuais (VFX) são feitos no editor de partículas do Blockbench, no formato Bedrock (Snowstorm), e salvos em `assets/grimoireknights/particles/<efeito>.json`; texturas em `assets/grimoireknights/textures/particle/`.
- O mod lê esses arquivos (recarregam com F3+T) e toca no cliente, em `core/client/vfx`. Simula e desenha por conta própria em `RenderLevelStageEvent`, fora do `ParticleEngine` vanilla, para ter textura própria por efeito e brilho aditivo.
- O servidor só pede o efeito: payload com id, posição e variáveis Molang (`variable.<nome>`) para quem vê o lugar.
- Suporte só ao que os efeitos do mod usam, crescendo com eles. Molang: números, `+ - * /`, parênteses, comparação, `?:`, `math.*` comuns, `variable.*` e `query.*` de idade e aleatório. Componente ou função fora disso quebra ao carregar, com o nome no log (fail-fast).

**Alternativas descartadas:**
- Traduzir à mão para partículas vanilla: a prévia do Blockbench não bate com o jogo e cada ajuste é feito duas vezes.
- Biblioteca pronta: nenhuma para NeoForge 1.21.1 lê o formato Bedrock (o ParticleEmitter da WorldSeedGames é para Minestom).
- Lodestone, Photon e Veil: formatos e editores próprios, fora do Blockbench.

**Consequência:** efeito novo é um JSON a mais, sem código. Componente Bedrock novo exige código no runtime.
