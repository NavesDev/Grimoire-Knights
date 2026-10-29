// Anti-magic reverse blade (ref: Demon-Dweller Sword): deep central fuller, star-tiled base,
// ">" tip; very dark bronze hilt: guard bar with hooks toward the blade, wrapped grip, round pommel.
const K = load('./lib.js');
const S = load('./sword_painter.js');

const TILED = 7; // model units above the guard covered by the star tiles
const face = (n, f, t, m) => ({ name: n, from: f, to: t, paint: { north: m, south: m, all: 'edge' } });
const bronze = (n, f, t) => ({ name: n, from: f, to: t, mat: 'bronze' });

// Symmetric point: half-width shrinks one texture pixel per step.
const tip = Array.from({ length: 4 }, (_, k) => {
  const hw = 2 - k * 0.5, y = 22 + k * 1.25;
  const t = 0.5 - k * 0.0625; // thins toward the point
  return face('tip_' + (k + 1), [-hw, y, -t], [hw, y + 1.25, t], 'blade');
});

// Guard: thin bar with hooks rising from its ends toward the blade.
const hook = bronze('guard_hook_left', [-3.5, 3.25, -0.4], [-3, 6.25, 0.4]);
const hookTip = bronze('guard_hook_tip_left', [-3, 5.75, -0.35], [-2.5, 6.5, 0.35]);

// Round cartoon pommel: three crossed boxes approximate a ball.
const pommel = [
  bronze('pommel_x', [-1, -9.25, -0.75], [1, -7.25, 0.75]),
  bronze('pommel_z', [-0.75, -9.25, -1], [0.75, -7.25, 1]),
  bronze('pommel_y', [-0.75, -9.5, -0.75], [0.75, -7, 0.75])
];

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'sword', origin: [0, 0, 0], cubes: [
  { name: 'grip', from: [-0.6, -7.25, -0.6], to: [0.6, 3.25, 0.6], mat: 'grip' },
  ...pommel,
  bronze('guard', [-3, 3.25, -0.6], [3, 3.75, 0.6]),
  ...[hook, hookTip].flatMap(c => [c, K.mirrorCube(c)])
], children: [{ name: 'blade', origin: [0, 3.75, 0], cubes: [
  face('blade_left', [-2.5, 3.75, -0.5], [-0.5, 22, 0.5], 'tiled'),
  face('blade_right', [0.5, 3.75, -0.5], [2.5, 22, 0.5], 'tiled'),
  face('fuller', [-0.5, 3.75, -0.25], [0.5, 22, 0.25], 'ridge'),
  ...tip] }] }] }];

const P = { ...S.anti, bronze: '#352b1b', grip: '#241d12', gripDark: '#3a301e', tile: '#2c2d35' };

// Blade face with 4x4 px tiles and a dark four-point star on each corner near the guard.
const painter = (ctx, r, m) => {
  if (m !== 'tiled') return S.painter(P)(ctx, r, m);
  const { u, v, w, h } = r, rnd = K.rng(53 + u + v * 5), tiled = h - TILED * 2;
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    let col = S.patch(u + i, v + j, 5) > 0.58 ? P.blot : P.blade, k = 1 + (rnd() - 0.5) * 0.12;
    if (j >= tiled) {
      const y = j - tiled, x = i + (Math.floor(y / 4) % 2) * 2, cx = x % 4, cy = y % 4;
      col = P.tile;
      if (cx === 0 && cy === 0) col = P.ridge;
      else if ((cx === 0 && (cy === 1 || cy === 3)) || (cy === 0 && (cx === 1 || cx === 3))) col = P.blade;
    }
    K.px(ctx, u + i, v + j, K.shade(col, k));
  }
};

module.exports = { name: 'reverse_blade', group: 'ability', kind: 'entity', spec, textures: { reverse_blade: painter } };
