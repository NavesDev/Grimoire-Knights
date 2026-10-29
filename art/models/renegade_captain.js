// Renegade captain boss. Same rig as the order mage plus mask, crown, pauldrons and long cape.
// The renderer scales it up; the geometry stays player sized.
const K = load('./lib.js');
const M = load('./order_mage.js');

const pauldron = (s, x0, x1) => ({ name: (s < 0 ? 'right' : 'left') + '_pauldron', from: [x0 - 0.75, 22, -3], to: [x1 + 0.75, 25.5, 3], mat: 'pauldron' });
const spike = (s, x0, x1) => ({ name: (s < 0 ? 'right' : 'left') + '_pauldron_spike', from: [s < 0 ? x0 : x1 - 1.5, 25.5, -1], to: [s < 0 ? x0 + 1.5 : x1, 27.5, 1], mat: 'gold' });

const spec = [{ name: 'root', origin: [0, 0, 0], children: [
  { name: 'body', origin: [0, 24, 0], cubes: [
    { name: 'torso', from: [-4, 12, -2], to: [4, 24, 2], paint: { north: 'torso_front', all: 'robe' } },
    { name: 'collar', from: [-4.5, 23, -2.75], to: [4.5, 26, 2.75], mat: 'collar' },
    { name: 'belt', from: [-4.5, 11, -2.5], to: [4.5, 12.5, 2.5], paint: { north: 'belt_front', all: 'belt' } }
  ], children: [
    { name: 'head', origin: [0, 24, 0], cubes: [
      { name: 'face', from: [-4, 24, -4], to: [4, 32, 4], paint: { north: 'mask', all: 'shadow' } }, ...M.hood],
    children: [{ name: 'crown', origin: [0, 33, 0], cubes: [
      { name: 'crown_band', from: [-4.5, 33, -5], to: [4.5, 34, -4], mat: 'gold' },
      { name: 'crown_spike_mid', from: [-0.75, 34, -5], to: [0.75, 37, -4], mat: 'gold' },
      { name: 'crown_spike_l', from: [2.5, 34, -5], to: [3.5, 35.5, -4], mat: 'gold' },
      { name: 'crown_spike_r', from: [-3.5, 34, -5], to: [-2.5, 35.5, -4], mat: 'gold' },
      { name: 'crown_gem', from: [-0.5, 33.25, -5.25], to: [0.5, 34.75, -4.75], mat: 'gem' }] }] },
    M.arm('right', [pauldron, spike]), M.arm('left', [pauldron, spike]),
    { name: 'cape', origin: [0, 24, 3], cubes: [{ name: 'cape_cloth', from: [-5.5, 0.5, 2.75], to: [5.5, 24, 3.75], paint: { north: 'cape_inner', all: 'cape' } }] },
    { name: 'grimoire_anchor', origin: [-7, 14, -6] }] },
  ...M.lowerBody(2.75)] }];

const painter = P => (ctx, r, m) => {
  const { u, v, w, h } = r, px = (x, y, c) => K.px(ctx, u + x, v + y, c), fill = (c, a) => K.noiseFill(ctx, r, c, a, 9);
  if (m === 'mask') {
    fill(P.mask, 0.06);
    for (let i = 0; i < w; i++) px(i, 0, P.shadow);
    [0, 1, w - 2, w - 1].forEach(i => { for (let j = 0; j < h; j++) px(i, j, P.shadow); });
    for (let i = 3; i < 7; i++) { px(i, 6, P.trim); px(i, 9, P.trim); }
    for (let i = 9; i < 13; i++) { px(i, 6, P.trim); px(i, 9, P.trim); }
    [[3, 7], [3, 8], [6, 7], [6, 8], [9, 7], [9, 8], [12, 7], [12, 8]].forEach(([x, y]) => px(x, y, P.trim));
    [[4, 7], [5, 7], [4, 8], [5, 8], [10, 7], [11, 7], [10, 8], [11, 8]].forEach(([x, y]) => px(x, y, P.shadow));
    px(5, 7, P.eye); px(10, 7, P.eye);
    for (let j = 2; j < 5; j++) { px(7, j, P.trim); px(8, j, P.trim); }
    for (let j = 11; j < 14; j++) { px(7, j, P.trim2); px(8, j, P.trim2); }
    return;
  }
  if (m === 'collar') { fill(P.robeDark, 0.08); for (let i = 0; i < w; i++) { px(i, 0, P.trim); if (i % 3 === 0) px(i, 1, P.trim); } return; }
  if (m === 'pauldron') { fill(P.robeDark, 0.1); for (let i = 0; i < w; i++) { px(i, 0, P.trim); px(i, h - 1, P.trim); } return; }
  if (m === 'gold') { fill(P.trim2, 0.15); px(0, 0, '#fff2b0'); return; }
  if (m === 'gem') { fill(P.eye, 0.1); px(0, 0, '#ffffff'); return; }
  if (m === 'cape') { fill(P.robe, 0.08); for (let i = 0; i < w; i++) { px(i, h - 1, P.trim); px(i, h - 2, P.accent); } return; }
  if (m === 'cape_inner') { fill(P.accent, 0.1); for (let i = 0; i < w; i++) px(i, h - 1, P.trim); return; }
  M.painter(P)(ctx, r, m);
};

const fire = { ...M.palettes.fire, robe: '#1f1a2e', robeDark: '#151122', mask: '#e8e0cc' };
const wind = { ...fire, accent: '#2f8456', eye: '#8affc0' };

module.exports = {
  name: 'renegade_captain', group: 'enemy', kind: 'entity', spec,
  textures: { renegade_captain_fire: painter(fire), renegade_captain_wind: painter(wind) }
};
