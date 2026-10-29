// Anti-magic reverse blade (ref: Demon-Dweller Sword): central groove, teeth near the guard, pointed tip.
const K = load('./lib.js');
const S = load('./sword_painter.js');

const blade = (n, f, t) => ({ name: n, from: f, to: t, paint: { north: 'groove', south: 'groove', all: 'edge' } });
const teeth = [];
[6, 8.5, 11].forEach((y, i) => {
  teeth.push({ name: 'tooth_l' + i, from: [-3.25, y, -0.4], to: [-2.25, y + 1.25, 0.4], mat: 'edge' });
  teeth.push({ name: 'tooth_r' + i, from: [2.25, y, -0.4], to: [3.25, y + 1.25, 0.4], mat: 'edge' });
});

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'sword', origin: [0, 0, 0], cubes: [
  { name: 'grip', from: [-0.75, -7, -0.75], to: [0.75, 2.5, 0.75], mat: 'grip' },
  { name: 'pommel', from: [-1.25, -8.5, -1.25], to: [1.25, -7, 1.25], mat: 'metal' },
  { name: 'guard', from: [-4, 2.5, -1], to: [4, 3.75, 1], mat: 'metal' },
  { name: 'guard_hook_left', from: [-5, 1, -0.75], to: [-4, 3.75, 0.75], mat: 'metal' },
  { name: 'guard_hook_right', from: [4, 1, -0.75], to: [5, 3.75, 0.75], mat: 'metal' }
], children: [{ name: 'blade', origin: [0, 3.75, 0], cubes: [
  blade('blade_main', [-2.25, 3.75, -0.5], [2.25, 22, 0.5]),
  blade('tip_1', [-1.75, 22, -0.5], [1.75, 24, 0.5]),
  blade('tip_2', [-1.25, 24, -0.5], [1.25, 25.5, 0.5]),
  blade('tip_3', [-0.5, 25.5, -0.5], [0.5, 27, 0.5]),
  ...teeth] }] }] }];

const P = S.anti;
const painter = (ctx, r, m) => {
  if (m !== 'groove') return S.painter(P)(ctx, r, m);
  const { u, v, w, h } = r, rnd = K.rng(53 + u + v * 5), c = (w - 1) / 2;
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    const d = Math.abs(i - c);
    const col = (i === 0 || i === w - 1) ? P.edge : d < 0.75 ? P.blot : P.blade;
    const dark = (i !== 0 && i !== w - 1 && d >= 0.75 && d < 1.75) ? 0.8 : 1;
    K.px(ctx, u + i, v + j, K.shade(col, dark * (1 + (rnd() - 0.5) * 0.12)));
  }
};

module.exports = { name: 'reverse_blade', group: 'ability', kind: 'entity', spec, textures: { reverse_blade: painter } };
