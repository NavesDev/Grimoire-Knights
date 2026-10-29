# Modelos

Modelos 3D (GeckoLib) gerados por script dentro do Blockbench. O script é a fonte; o resto é gerado.

## Onde fica cada coisa
| Arquivo | Papel |
|---|---|
| `art/models/<modelo>.js` | Fonte: ossos, cubos, materiais e pintura da textura |
| `art/models/lib.js` | Montagem, empacotamento de UV e pintura comum |
| `art/models/run.js` | Lista de modelos e geração |
| `art/blockbench/<modelo>.bbmodel` | Gerado; versionado para abrir e ver no Blockbench |
| `assets/grimoireknights/geo/<funcionalidade>/` | Gerado; geometria usada pelo jogo |
| `assets/grimoireknights/textures/{entity,block}/<funcionalidade>/` | Gerado; texturas e variantes |
| `assets/grimoireknights/animations/<funcionalidade>/` | Gerado; animações usadas pelo jogo |

## Regras
- Mudança de modelo vai no `.js`. Editar `.geo.json`, `.png` ou `.bbmodel` à mão se perde na próxima geração.
- Variante nova (elemento, patente) é uma paleta nova no `.js` do modelo.
- Modelo novo: criar `art/models/<modelo>.js` e incluir em `MODELS` no `run.js`.
- Todos os modelos usam 2 pixels de textura por unidade (`DENSITY` no `run.js`); um cubo pode multiplicar isso com `res` quando a forma é recortada na textura (asas, headband e marca do peito da forma demônio).
- Animação nova ou ajuste de animação também vai no `.js` (campo `animations`); animação feita à mão no Blockbench se perde na próxima geração.
- A geração é determinística para geo, texturas e animações; o `.bbmodel` muda só nos UUIDs.
- Um agente por vez no Blockbench: a geração abre e fecha projetos no app.

## Como gerar
Com o Blockbench aberto e o servidor MCP do Blockbench ligado, rodar pelo `risky_eval` (lista vazia gera todos):

```js
(()=>{const fs=require('fs'),path=require('path');const cache={};const load=f=>{if(cache[f])return cache[f];const m={exports:{}};new Function('module','exports','load','fs','path','__dirname',fs.readFileSync(f,'utf8'))(m,m.exports,p=>load(path.resolve(path.dirname(f),p)),fs,path,path.dirname(f));return cache[f]=m.exports;};return load('/home/naves/Projetos/Grimoire-Knights/art/models/run.js')(['grimoire'])})()
```

O Blockbench não carrega arquivos com `require`; o trecho acima lê cada `.js` e entrega `load`, `fs`, `path` e `__dirname`.

## Convenções que o código precisa saber
- **Grimório:** ossos `front_cover`, `back_cover` e `flip_page` giram no eixo Y em volta da lombada para abrir. `sword_anchor` marca onde a espada anti-magia sai. Texturas `grimoire_<elemento>_<folhas>`.
- **Manto e estandarte:** duas texturas desenhadas uma sobre a outra. `_cloth` é cinza claro, tingida com a cor do esquadrão; `_trim` tem borda, madeira, ouro e a placa do brasão. O osso `tail` do manto aparece a partir de Cavaleiro.
- **Manto:** frente aberta (duas abas e gola com fecho), placa do brasão na aba esquerda. Esquadrão preto usa tinta escura no `_cloth`.
- **Forma demônio:** camada sobre o jogador. Ossos `s1_` a `s4_` são os estágios (braço, meio corpo, forma completa, união); o renderer mostra todos até o estágio atual, e osso escondido esconde os filhos. Estágios 1 a 3 são só preto (anti-magia): 1 braço direito e tentáculos no peito; 2 chifre direito e asa pequena; 3 braço esquerdo com seus tentáculos e as duas asas inteiras (a pequena fica escondida dentro da direita), tronco e pernas ainda à mostra. Estágio 4 (união) cobre tronco e pernas e põe chifre esquerdo, headband preta com a marca vermelha, marcas vermelhas fixas (peito; risco com espinhos na frente e atrás dos braços e na canela), ombreiras com estrela vermelha em relevo, garras e cauda. O rosto do jogador fica à mostra. Asas (preto escuro, chama rasgada) e frente da headband são planos com a forma recortada na textura; asas, tentáculos, marcas e ponta da cauda usam transparência: renderizar com render type cutout.
- **Capitão renegado:** mesmo esqueleto do mago; o tamanho maior vem da escala no renderer.
- **Âncoras:** `grimoire_anchor` (magos) e `book_anchor` (altar) marcam onde o grimório flutua.

## Animações
Campo `animations` no `.js` do modelo; `K.animate` (`lib.js`) cria no projeto e o `run.js` grava o `.animation.json`.

```js
animations: {
  float: { loop: 'loop', length: 3, bones: { root: { position: [0, 'math.sin(query.anim_time * 120) * 0.6', 0] } } },
  open: { loop: 'hold', length: 0.5, bones: { front_cover: { rotation: [[0, [0, 0, 0]], [0.5, [0, -75, 0], 'catmullrom']] } } }
}
```

- Nome no jogo: `animation.<modelo>.<nome>` (ex.: `animation.grimoire.open`).
- `loop`: `loop`, `hold` (para no último quadro) ou `once`.
- Canal: lista de `[tempo, [x, y, z], interpolação]` (padrão `linear`), ou um `[x, y, z]` só, constante ou Molang. Loop contínuo fica melhor em Molang com `query.anim_time`.
- Rotação na convenção do Blockbench; a exportação inverte para o GeckoLib.
- Evitar dois controllers mexendo no mesmo osso ao mesmo tempo: o balanço fica no `root` e o resto nos ossos de baixo.
- Conferir no Blockbench: modo Animate, escolher a animação e mover a linha do tempo.

### Grimório
| Animação | Loop | Uso |
|---|---|---|
| `float` | loop | Flutua ao lado do jogador (`root`), controller próprio |
| `open` / `close` | hold / once | Abre e fecha as capas |
| `flip` | once | Vira uma página com o livro aberto, ao lançar magia |
| `draw` | once | Anti-magia: sai da esquerda do jogador para a frente da mão direita, deita, abre e folheia enquanto a espada sai em `sword_anchor` (pega por volta de 1,05 s), depois fecha e volta |
| `choose` | once | Cerimônia: desce girando até o jogador |

A entidade do grimório fica parada em `FLOAT_AT` (`grimoire.js`): à esquerda do jogador, longe da mão da espada, virada para onde ele olha. O `draw` leva o livro dali até a mão pelo osso `book` (posição), sem mexer na entidade; o `float` continua no `root`. A pose do jogador esticando o braço e puxando a espada vai no playerAnimator, sincronizada com o `draw`.

### Mago da Ordem e Capitão renegado
Mesmas animações (`humanoid` no `order_mage.js`); morte e dano ficam com o renderer vanilla (tomba e pinta de vermelho).

| Animação | Loop | Uso |
|---|---|---|
| `idle` | loop | Parado: respiração, braços e ponta do capuz balançando |
| `walk` | loop | Pernas e braços alternados, saia abre na frente e atrás |
| `cast` | once | Braço direito aponta para o alvo e empurra; grimório em `grimoire_anchor` |
| `intro` | once | Só o Capitão: abre os braços no topo da torre |

O Capitão também balança a capa (`cape`) em todas.

### Bola de fogo
| Animação | Loop | Uso |
|---|---|---|
| `spin` | loop | Núcleo pulsa, duas cascas giram em sentidos opostos, 6 línguas tremem cada uma no seu ritmo, rastro em corrente chicoteia, brasas orbitam |

### Espadas (`sword_anim.js`)
Golpes e especiais são poses do jogador (playerAnimator); a espada só entra e sai. O `root` gira em volta da base do pomo: o renderer põe essa base no `sword_anchor` do grimório durante `summon` e `sheath`, e a espada cresce para fora das páginas a partir dela. O `summon` começa 0,35 s depois do `draw` do grimório; no fim a espada passa para a mão. Em `summon` e `sheath` o osso `sword` gira 90° em y (-90° na katana, que tem um fio só): a lâmina sai de perfil para o jogador, com o fio para a frente.

| Animação | Loop | Uso |
|---|---|---|
| `summon` | hold | Sai das páginas crescendo e sobe um pouco para ser pega |
| `sheath` | once | Desce e afunda de volta nas páginas |
| `ride` | loop | Só a katana: voo na espada, balançando sob o jogador |

## Poses do jogador (playerAnimator)
Rig `player_sword.js` (`kind: 'player'`): o `run.js` grava só o `.bbmodel` e `assets/grimoireknights/player_animation/player_sword.json`, sem geo nem textura.

- O playerAnimator lê o formato GeckoLib, mas sem Molang: loops viram quadros-chave numéricos (`loop` no `player_sword.js`, 12 por ciclo).
- Ossos: `body` (o jogador todo), `head`, `torso`, `right_arm`, `left_arm`, `right_leg`, `left_leg`, irmãos como no jogador vanilla.
- Parte sem quadro-chave volta para a pose padrão, não para a do vanilla; por isso andar e correr animam pernas e braços. `head` fica sem chave para seguir o olhar (conferir no jogo).
- Espada na mão: lâmina à frente com o fio para baixo (katana com a curva para cima), igual ao fim do saque. Quem orienta é o renderer do item na mão; as poses não giram a espada.
- Ainda não testado no jogo: sinais dos eixos e se o `head` sem chave segue a câmera.

| Animação | Loop | Uso |
|---|---|---|
| `walk` | loop 1 s | Andar com espada: lâmina à frente, baixa; braço livre e pernas no passo |
| `run` | loop 0,6 s | Correr: tronco inclinado, espada no ombro com a lâmina descendo pelas costas, braço livre bombeando |
