// Magic torch block: wooden stick, iron cup, floating mana crystal with a sparse glow shell.
const K = load('./lib.js');

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'torch', origin: [0, 0, 0], cubes: [
  { name: 'stick', from: [-1, 0, -1], to: [1, 9, 1], mat: 'wood' },
  { name: 'cup', from: [-2, 9, -2], to: [2, 10.5, 2], mat: 'iron' },
  { name: 'prong_n', from: [-0.5, 10.5, -2], to: [0.5, 12, -1.5], mat: 'iron' },
  { name: 'prong_s', from: [-0.5, 10.5, 1.5], to: [0.5, 12, 2], mat: 'iron' },
  { name: 'prong_e', from: [1.5, 10.5, -0.5], to: [2, 12, 0.5], mat: 'iron' },
  { name: 'prong_w', from: [-2, 10.5, -0.5], to: [-1.5, 12, 0.5], mat: 'iron' }
], children: [
  { name: 'crystal', origin: [0, 13, 0], cubes: [
    { name: 'crystal_core', from: [-1, 11.5, -1], to: [1, 14.5, 1], origin: [0, 13, 0], rotation: [0, 45, 0], mat: 'crystal' },
    { name: 'crystal_tip', from: [-0.5, 14.5, -0.5], to: [0.5, 15.5, 0.5], origin: [0, 13, 0], rotation: [0, 45, 0], mat: 'crystalLight' }] },
  { name: 'glow', origin: [0, 13, 0], cubes: [{ name: 'glow_shell', from: [-1.75, 11, -1.75], to: [1.75, 15, 1.75], mat: 'glow' }] }] }] }];

const painter = (ctx, r, m) => {
  const { u, v, w, h } = r, rnd = K.rng(29 + u + v * 7), px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    let c;
    if (m === 'wood') c = K.shade('#6b4a2c', 1 + (i % 2 ? -0.08 : 0) + (rnd() - 0.5) * 0.1);
    else if (m === 'iron') c = K.shade('#4a4a52', 1 + (rnd() - 0.5) * 0.2);
    else if (m === 'crystal') c = j < 2 ? '#e6dcff' : (i + j) % 3 === 0 ? '#b49cff' : '#8d6dff';
    else if (m === 'crystalLight') c = '#f2edff';
    else if (m === 'glow') { if (rnd() < 0.7) continue; c = rnd() < 0.5 ? '#c6b4ff' : '#a98bff'; }
    else c = '#ff00ff';
    px(i, j, c);
  }
};

const sin = (speed, amp, phase = 0) => `math.sin(query.anim_time * ${speed}${phase ? ' + ' + phase : ''}) * ${amp}`;
const animations = { idle: { loop: 'loop', length: 3, bones: {
  crystal: { rotation: [0, 'query.anim_time * 120', 0], position: [0, sin(120, 0.4), 0] },
  glow: { scale: Array(3).fill(`1 + ${sin(240, 0.12)}`) }
} } };

module.exports = { name: 'magic_torch', group: 'building', kind: 'block', spec, animations, textures: { magic_torch: painter } };
