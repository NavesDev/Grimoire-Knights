// Squad mantle (Curios back slot). Two textures drawn on top of each other by the renderer:
// _cloth is light grey and tinted with the squad color; _trim holds the gold trim and crest plate.
// The tail bone only shows from rank Knight up.
const K = load('./lib.js');

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'body', origin: [0, 24, 0], children: [{ name: 'mantle', origin: [0, 24, 0], cubes: [
  { name: 'front', from: [-8.5, 17, -3], to: [8.5, 24.5, -2.25], origin: [0, 24.5, -2.6], rotation: [10, 0, 0], paint: { north: 'front', all: 'cloth' } },
  { name: 'back', from: [-8.5, 15, 2.25], to: [8.5, 24.5, 3], origin: [0, 24.5, 2.6], rotation: [-8, 0, 0], paint: { south: 'back', all: 'cloth' } },
  { name: 'side_left', from: [8, 17, -3], to: [8.75, 24.5, 3], origin: [8.4, 24.5, 0], rotation: [0, 0, -10], paint: { east: 'side', all: 'cloth' } },
  { name: 'side_right', from: [-8.75, 17, -3], to: [-8, 24.5, 3], origin: [-8.4, 24.5, 0], rotation: [0, 0, 10], paint: { west: 'side', all: 'cloth' } },
  { name: 'top_left', from: [4, 24, -3], to: [8.75, 24.75, 3], mat: 'cloth' },
  { name: 'top_right', from: [-8.75, 24, -3], to: [-4, 24.75, 3], mat: 'cloth' },
  { name: 'top_front', from: [-4, 24, -3], to: [4, 24.75, -2.25], mat: 'cloth' },
  { name: 'top_back', from: [-4, 24, 2.25], to: [4, 24.75, 3], mat: 'cloth' },
  { name: 'clasp', from: [-1, 22.5, -3.5], to: [1, 24, -3], origin: [0, 24.5, -2.6], rotation: [10, 0, 0], mat: 'gold' }
], children: [
  { name: 'hood', origin: [0, 24, 3], cubes: [{ name: 'hood_down', from: [-4, 21, 3], to: [4, 25.5, 5.5], paint: { up: 'hood_top', all: 'hood' } }] },
  { name: 'tail', origin: [0, 15, 2.75], cubes: [{ name: 'tail_cloth', from: [-6, 7, 2.4], to: [6, 15, 3], paint: { south: 'tail', all: 'cloth' } }] }] }] }] }];

// layer: 'cloth', 'trim' or 'both' (preview in Blockbench).
const painter = (layer, P) => (ctx, r, m) => {
  const { u, v, w, h } = r, rnd = K.rng(19 + u * 3 + v), px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  const cloth = layer !== 'trim', trim = layer !== 'cloth';
  const isTrim = (i, j) => {
    if (r.face === 'down' || m === 'gold') return true;
    if (['front', 'back', 'side', 'tail'].includes(m)) return j >= h - 2 || (m === 'front' && (i < 1 || i >= w - 1));
    if (m === 'hood') return j === 0;
    if (m === 'hood_top') return j === h - 1;
    return false;
  };
  // Crest plate on the wearer's left chest; the crest symbol is drawn at runtime.
  const crest = (i, j) => {
    if (m !== 'front') return null;
    const d = Math.hypot(i - 8.5, j - 6.5);
    return d <= 4.6 && d >= 3.6 ? 'ring' : d < 3.6 ? 'field' : null;
  };
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    const e = crest(i, j);
    if (e) {
      if (trim) px(i, j, e === 'ring' ? K.shade(P.gold, 1 + (rnd() - 0.5) * 0.2) : P.field);
      else px(i, j, K.shade(P.cloth, 1 + (rnd() - 0.5) * 0.08));
      continue;
    }
    if (isTrim(i, j)) {
      if (trim) px(i, j, K.shade(m === 'gold' ? P.gold : P.trim, 1 + (rnd() - 0.5) * 0.15));
      else px(i, j, K.shade(P.cloth, 0.9));
      continue;
    }
    if (cloth) {
      const fold = (m === 'back' || m === 'tail' || m === 'front') && (i % 6 === 0) ? 0.9 : 1;
      px(i, j, K.shade(P.cloth, fold * (1 + (rnd() - 0.5) * 0.08)));
    }
  }
};

const tintable = { cloth: '#d8d8d8', gold: '#d4ad4a', trim: '#d4ad4a', field: '#2a2a2a' };
const preview = { cloth: '#26222a', gold: '#d4ad4a', trim: '#d4ad4a', field: '#1a1a1a' };

module.exports = {
  name: 'squad_mantle', group: 'cosmetic', kind: 'entity', spec,
  textures: { squad_mantle_cloth: painter('cloth', tintable), squad_mantle_trim: painter('trim', tintable) },
  preview: painter('both', preview), painter, palettes: { tintable, preview }
};
