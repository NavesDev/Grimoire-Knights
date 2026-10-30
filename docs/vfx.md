# Efeitos visuais (VFX)

Partículas feitas no editor de partículas do Blockbench (formato Bedrock/Snowstorm) e tocadas por runtime próprio. Decisão em [ADR 0004](adr/0004-particulas-bedrock.md).

## Onde fica cada coisa
| Arquivo | Papel |
|---|---|
| `assets/grimoireknights/vfx/particles/<emissor>.json` | Um emissor (um tipo de partícula), editado no Blockbench |
| `assets/grimoireknights/vfx/textures/particle/*.png` | Texturas dos emissores; gerado por `art/particles/textures.py` |
| `assets/grimoireknights/vfx/animations/vfx.animation.json` | Sequências: uma animação `animation.vfx.<efeito>` por efeito, com keyframes de partícula dizendo qual emissor dispara e quando |
| `art/blockbench/vfx.bbmodel` | Projeto que monta as sequências; abrir no Blockbench para ver e ajustar |

Tudo fica em `vfx/` (e não em `particles/`) porque o Minecraft Java lê `assets/<mod>/particles/` como partícula vanilla.

## Fazer ou ajustar um efeito
1. Abrir `art/blockbench/vfx.bbmodel`, modo Animate, escolher a animação do efeito.
2. Ajustar o emissor no editor de partículas, ou criar um novo em `vfx/particles/` (pack root = `assets/grimoireknights/vfx`).
3. Pôr ou mover o keyframe de partícula na linha do tempo da animação.
4. Exportar as animações para `vfx/animations/vfx.animation.json` e salvar o `.bbmodel`.
5. No jogo, F3+T recarrega.

Tocar no jogo: servidor chama `Vfx.play(level, Ids.id("<efeito>"), posição, variáveis)`; código de cliente chama `VfxClient.play`. Variáveis viram `variable.<nome>` no Molang (ex.: `radius` na explosão, `tint_r/g/b` nas faíscas do grimório). No Blockbench elas valem 0, então o efeito usa `variable.x > 0 ? variable.x : padrão`.

## O que o runtime suporta
- Emissor: `emitter_rate_instant`, `emitter_rate_steady`, `emitter_lifetime_once`, formas `point`, `sphere`, `disc`, `box` (direção `outwards`, `inwards` ou vetor).
- Partícula: `particle_lifetime_expression`, `particle_initial_speed`, `particle_initial_spin`, `particle_motion_dynamic` (aceleração e arrasto), `particle_appearance_billboard` (tamanho, UV fixa ou flipbook, faces `lookat_xyz`, `lookat_y`, `lookat_direction`, `emitter_transform_xy/xz/yz`), `particle_appearance_tinting` (gradiente ou RGBA), `particle_appearance_lighting`.
- Materiais: `particles_alpha`, `particles_blend`, `particles_add`.
- Molang: números, `+ - * /`, comparação, `&& || !`, `?:`, `math.*` comuns (graus), `variable.*`, `query.*`.

Componente fora da lista quebra o carregamento com o nome do arquivo; o teste `VfxEffectTest` carrega todos os efeitos do mod.
