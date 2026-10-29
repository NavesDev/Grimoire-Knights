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

## Regras
- Mudança de modelo vai no `.js`. Editar `.geo.json`, `.png` ou `.bbmodel` à mão se perde na próxima geração.
- Variante nova (elemento, patente) é uma paleta nova no `.js` do modelo.
- Modelo novo: criar `art/models/<modelo>.js` e incluir em `MODELS` no `run.js`.
- Todos os modelos usam 2 pixels de textura por unidade (`DENSITY` no `run.js`).
- A geração é determinística: rodar de novo sem mudar o script dá os mesmos arquivos.
- Um agente por vez no Blockbench: a geração abre e fecha projetos no app.

## Como gerar
Com o Blockbench aberto e o servidor MCP do Blockbench ligado, rodar pelo `risky_eval` (lista vazia gera todos):

```js
(()=>{const fs=require('fs'),path=require('path');const cache={};const load=f=>{if(cache[f])return cache[f];const m={exports:{}};new Function('module','exports','load','fs','path','__dirname',fs.readFileSync(f,'utf8'))(m,m.exports,p=>load(path.resolve(path.dirname(f),p)),fs,path,path.dirname(f));return cache[f]=m.exports;};return load('/home/naves/Projetos/Grimoire-Knights/art/models/run.js')(['grimoire'])})()
```

O Blockbench não carrega arquivos com `require`; o trecho acima lê cada `.js` e entrega `load`, `fs`, `path` e `__dirname`.

## Convenções que o código precisa saber
- **Grimório:** ossos `front_cover`, `back_cover` e `flip_page` giram no eixo Y em volta da lombada para abrir. Texturas `grimoire_<elemento>_<folhas>`.
- **Manto e estandarte:** duas texturas desenhadas uma sobre a outra. `_cloth` é cinza claro, tingida com a cor do esquadrão; `_trim` tem borda, madeira, ouro e a placa do brasão. O osso `tail` do manto aparece a partir de Cavaleiro.
- **Forma demônio:** camada sobre o jogador. Ossos `s1_` a `s4_` são os estágios (braço, meio corpo, forma completa, união); o renderer mostra todos até o estágio atual.
- **Capitão renegado:** mesmo esqueleto do mago; o tamanho maior vem da escala no renderer.
- **Âncoras:** `grimoire_anchor` (magos) e `book_anchor` (altar) marcam onde o grimório flutua.

## Ponto em aberto
Animações ainda não existem. Como `run.js` reescreve o `.bbmodel`, a geração precisa preservar as animações antes de elas serem criadas no Blockbench.
