// Squad banner block. Same two-texture scheme as the mantle: _cloth (tinted) and _trim (wood, gold, crest plate).
// cloth_upper and cloth_lower bones sway in the wind.
const K = load('./lib.js');
const Mantle = load('./squad_mantle.js');

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'banner', origin: [0, 0, 0], cubes: [
  { name: 'base', from: [-4, 0, -4], to: [4, 2, 4], mat: 'stoneB' },
  { name: 'pole', from: [-1, 2, -1], to: [1, 32, 1], mat: 'wood' },
  { name: 'finial', from: [-1.5, 32, -1.5], to: [1.5, 34, 1.5], mat: 'gold' },
  { name: 'finial_tip', from: [-0.5, 34, -0.5], to: [0.5, 36, 0.5], mat: 'gold' },
  { name: 'crossbar', from: [-8, 29, -1.75], to: [8, 30.5, -0.75], mat: 'wood' },
  { name: 'bar_cap_l', from: [-9, 28.75, -2], to: [-8, 30.75, -0.5], mat: 'gold' },
  { name: 'bar_cap_r', from: [8, 28.75, -2], to: [9, 30.75, -0.5], mat: 'gold' }
], children: [{ name: 'cloth_upper', origin: [0, 29, -1.5], cubes: [
  { name: 'cloth_top', from: [-7, 17, -1.75], to: [7, 29, -1.25], paint: { north: 'cloth_front', south: 'cloth_back', all: 'cloth' } }
], children: [{ name: 'cloth_lower', origin: [0, 17, -1.5], cubes: [
  { name: 'cloth_mid', from: [-7, 11, -1.75], to: [7, 17, -1.25], paint: { north: 'cloth_hem_front', south: 'cloth_hem_front', all: 'cloth' } },
  { name: 'tail_left', from: [1.5, 7, -1.75], to: [7, 11, -1.25], paint: { north: 'tail', south: 'tail', all: 'cloth' } },
  { name: 'tail_right', from: [-7, 7, -1.75], to: [-1.5, 11, -1.25], paint: { north: 'tail', south: 'tail', all: 'cloth' } }] }] }] }] }];

const SOLID = { wood: '#5a3b24', stoneB: '#7d7873' };

const painter = (layer, P) => (ctx, r, m) => {
  const { u, v, w, h } = r, rnd = K.rng(37 + u + v * 5), px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  const cloth = layer !== 'trim', trim = layer !== 'cloth';
  if (m in SOLID || m === 'gold') {
    if (!trim) return;
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, K.shade(m === 'gold' ? P.gold : SOLID[m], 1 + (rnd() - 0.5) * 0.18));
    return;
  }
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    let t = false, f = null;
    if (m === 'cloth_front' || m === 'cloth_back') {
      t = i < 1 || i >= w - 1 || j < 1;
      const d = Math.hypot(i - (w - 1) / 2, j - 12);
      if (d <= 6.5 && d >= 5.3) t = true; else if (d < 5.3) f = P.field;
    }
    if (m === 'cloth_hem_front') t = i < 1 || i >= w - 1;
    if (m === 'tail') t = i < 1 || i >= w - 1 || j >= h - 1;
    if (m === 'cloth') t = true;
    if (f) { if (trim) px(i, j, f); else px(i, j, K.shade(P.cloth, 1 + (rnd() - 0.5) * 0.06)); continue; }
    if (t) { if (trim) px(i, j, K.shade(P.gold, 1 + (rnd() - 0.5) * 0.15)); else px(i, j, K.shade(P.cloth, 0.9)); continue; }
    if (cloth) px(i, j, K.shade(P.cloth, (i % 5 === 0 ? 0.92 : 1) * (1 + (rnd() - 0.5) * 0.06)));
  }
};

const P = Mantle.palettes;
module.exports = {
  name: 'squad_banner', group: 'building', kind: 'block', spec,
  textures: { squad_banner_cloth: painter('cloth', P.tintable), squad_banner_trim: painter('trim', P.tintable) },
  preview: painter('both', P.preview)
};
