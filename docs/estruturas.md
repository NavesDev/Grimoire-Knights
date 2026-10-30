# Estruturas

Templates `.nbt` gerados por script; o script é a fonte, como nos [modelos](modelos.md).

| Arquivo | Papel |
|---|---|
| `art/structures/run.py` | Fonte: bloco a bloco de cada template |
| `art/structures/nbt.py` | Leitura e escrita de NBT (gzip) |
| `art/structures/source/` | Construções prontas baixadas, base de algumas estruturas ([ADR 0005](adr/0005-construcoes-prontas.md)); não editar |
| `data/grimoireknights/structure/` | Gerado; usado pelo jogo |

Gerar (da raiz do repo): `python3 -I art/structures/run.py`. A saída é determinística.

## Templates
| Template | Uso |
|---|---|
| `tower/grimoire_tower` | Torre dos Grimórios: a Observation Tower (base 25×87×25) com o poço de pedra (15×15, 44 de altura) virado biblioteca: estantes do chão ao bloco 40 com faixas de madeira e glowstone a cada 8, janelas livres, grimórios (`floating_grimoire`) na coluna central girando em volta dela (sem colisão nem seleção), Altar do Grimório no centro, sacerdote ao lado, porta ao sul. A sala de observação do topo fica fechada, só de enfeite. Fora do poço é `structure_void` (mantém o terreno). `/gk tower place` |
| `starter_village/center` | Peça inicial do vilarejo inicial: praça 39x39 com a torre no meio (7 blocos de folga em volta do topo da torre). Jigsaws: ruas vanilla de planície a oeste, norte e leste (`minecraft:street`); a igreja ao sul, em frente à porta da torre |
| `starter_village/church` | Igreja (12×22, 32 de altura) com sacerdote; os bancos são camas (renascimento opcional). Jigsaw `grimoireknights:church` depois da escada da porta, que fica virada para a torre |
| `empty` | Chão 5x5 dos GameTests |

## Vilarejo inicial
- Estrutura `grimoireknights:starter_village` (jigsaw, `size` 6): começa na praça da torre; ruas e casas vêm dos pools vanilla de planície.
- `structure_set` em anéis concêntricos com `distance` 0 e `count` 1: uma só, no chunk 0,0 (ou no bioma de vilarejo mais perto, até 112 blocos). `/locate structure grimoireknights:starter_village` acha.
- Ao criar o mundo, o `world` põe o spawn na borda da praça, perto da torre, e põe `spawnRadius` em 4: os jogadores se espalham num quadrado 9x9 da praça, sem alcançar a torre (a sala do topo da torre, mais larga que o poço, começa a 6 blocos do spawn). Spawn obstruído: o vanilla usa o bloco mais alto livre. Mundos já criados não mudam; `/gamerule spawnRadius` volta ao vanilla.
- Zona segura: mobs hostis não nascem dentro das peças do vilarejo (spawn natural, patrulhas e phantoms) e raides não começam ali: o Mau Presságio de quem entra é gasto sem virar raide.

## Créditos
| Estrutura | Construção | Autor | Página |
|---|---|---|---|
| `tower/grimoire_tower` | Observation Tower | maik12490 | https://bloxelizer.com/schematics/observation-tower |
| `starter_village/church` | church | teylo | https://bloxelizer.com/schematics/church-2 |
