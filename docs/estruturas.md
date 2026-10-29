# Estruturas

Templates `.nbt` gerados por script; o script é a fonte, como nos [modelos](modelos.md).

| Arquivo | Papel |
|---|---|
| `art/structures/run.py` | Fonte: bloco a bloco de cada template |
| `art/structures/nbt.py` | Leitura e escrita de NBT (gzip) |
| `data/grimoireknights/structure/` | Gerado; usado pelo jogo |

Gerar (da raiz do repo): `python3 -I art/structures/run.py`. A saída é determinística.

## Templates
| Template | Uso |
|---|---|
| `tower/grimoire_tower` | Torre dos Grimórios (11x11, parede redonda): estantes altas, grimórios flutuando (`floating_grimoire`), Altar do Grimório no centro, sacerdote ao lado, porta ao sul. Fora da parede é `structure_void` (mantém o terreno). `/gk tower place` |
| `starter_village/center` | Peça inicial do vilarejo inicial: praça 23x23 com a torre no meio. Jigsaws: ruas vanilla de planície a oeste, norte e leste (`minecraft:street`); a igreja ao sul, em frente à porta da torre |
| `starter_village/church` | Igreja com sacerdote e uma cama (renascimento opcional). Jigsaw `grimoireknights:church` na varanda |
| `empty` | Chão 5x5 dos GameTests |

## Vilarejo inicial
- Estrutura `grimoireknights:starter_village` (jigsaw, `size` 6): começa na praça da torre; ruas e casas vêm dos pools vanilla de planície.
- `structure_set` em anéis concêntricos com `distance` 0 e `count` 1: uma só, no chunk 0,0 (ou no bioma de vilarejo mais perto, até 112 blocos). `/locate structure grimoireknights:starter_village` acha.
- Ao criar o mundo, o `world` põe o spawn na borda da praça, perto da torre, e zera `spawnRadius`: todos nascem no mesmo ponto (sem cair no telhado da torre). Mundos já criados não mudam; `/gamerule spawnRadius` volta ao vanilla.
- Zona segura: mobs hostis não nascem dentro das peças do vilarejo (spawn natural, patrulhas e phantoms) e raides não começam ali: o Mau Presságio de quem entra é gasto sem virar raide.
