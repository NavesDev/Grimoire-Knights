// Anti-magic katana (ref: Demon-Slasher). Curve comes from nested blade segments rotated 3 degrees each.
const K = load('./lib.js');
const S = load('./sword_painter.js');

const segment = (i, y0, len, next) => ({
  name: 'blade_' + i, origin: [0, y0, 0], rotation: [0, 0, i === 1 ? 0 : -3],
  cubes: [{ name: 'blade_seg_' + i, from: [-0.75, y0, -0.25], to: [1, y0 + len, 0.25], paint: { north: 'kblade', south: 'kblade', all: 'edge' } }],
  children: next ? [next] : []
});
const tip = { name: 'kissaki', origin: [0, 31, 0], rotation: [0, 0, -4], cubes: [
  { name: 'tip_a', from: [-0.75, 31, -0.25], to: [0.75, 32.5, 0.25], paint: { north: 'kblade', south: 'kblade', all: 'edge' } },
  { name: 'tip_b', from: [-0.75, 32.5, -0.25], to: [0, 33.5, 0.25], paint: { north: 'kblade', south: 'kblade', all: 'edge' } }] };

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'sword', origin: [0, 0, 0], cubes: [
  { name: 'tsuka', from: [-0.75, -8, -0.75], to: [0.75, 2, 0.75], mat: 'tsuka' },
  { name: 'kashira', from: [-1, -9, -1], to: [1, -8, 1], mat: 'metal' },
  { name: 'tsuba_a', from: [-2.25, 2, -1.5], to: [2.25, 2.75, 1.5], mat: 'metal' },
  { name: 'tsuba_b', from: [-1.5, 2, -2.25], to: [1.5, 2.75, 2.25], mat: 'metal' },
  { name: 'habaki', from: [-1, 2.75, -0.5], to: [1.25, 4, 0.5], mat: 'metal' }
], children: [segment(1, 4, 7, segment(2, 11, 7, segment(3, 18, 7, segment(4, 25, 6, tip))))] }] }];

const P = { blade: '#222228', bevel: '#2c2c33', blot: '#18161a', edge: '#6c6c78', ridge: '#30303a', metal: '#3a3431', grip: '#1e1a1a', gripDark: '#5a1d1d', chip: 0.3 };

// Single-edged blade: only the outer column (cutting edge) is light and chipped.
const painter = (ctx, r, m) => {
  if (m !== 'kblade') return S.painter(P)(ctx, r, m);
  const { u, v, w, h } = r, rnd = K.rng(41 + u + v * 3);
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    const edge = i === w - 1;
    if (edge && rnd() < P.chip) continue;
    K.px(ctx, u + i, v + j, edge ? P.edge : (i === w - 2 && rnd() < 0.6) ? K.shade(P.edge, 0.8) : K.shade(P.blade, 1 + (rnd() - 0.5) * 0.15));
  }
};

module.exports = { name: 'katana', group: 'ability', kind: 'entity', spec, textures: { katana: painter } };
