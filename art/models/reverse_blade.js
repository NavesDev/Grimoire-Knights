// Anti-magic reverse blade (ref: Demon-Dweller Sword): long straight blade with a black fuller ending in a
// four-point star, two rows of stars near the guard, long ">" tip; very dark bronze hilt: wide guard with
// hexagonal ends and a round cross boss, spiral-wrapped grip, teardrop pommel.
const K = load('./lib.js');
const S = load('./sword_painter.js');
// root pivots at the pommel base: the sword grows out of the grimoire pages from there (sword_anim.js).
const BASE = -8.5;
const A = load('./sword_anim.js');

const BLADE_TOP = 27.75;
const bronze = (n, f, t, m = 'bronze') => ({ name: n, from: f, to: t, mat: m });

// Symmetric point: half-width shrinks one texture pixel per step, thickness tapers.
const tip = Array.from({ length: 4 }, (_, k) => {
  const hw = 2 - k * 0.5, y = BLADE_TOP + k * 1.5, t = 0.5 - k * 0.0625;
  return { name: 'tip_' + (k + 1), from: [-hw, y, -t], to: [hw, y + 1.5, t], paint: { north: 'blade', south: 'blade', all: 'edge' } };
});

const guardEnd = bronze('guard_end_left', [-4.5, 2.25, -0.75], [-3.5, 4.5, 0.75]);
const guardCap = bronze('guard_cap_left', [-5, 2.75, -0.5], [-4.5, 4, 0.5]);

// Teardrop pommel: three crossed boxes for the ball, a small point below.
const pommel = [
  bronze('pommel_x', [-1, -7.5, -0.75], [1, -5.5, 0.75]),
  bronze('pommel_z', [-0.75, -7.5, -1], [0.75, -5.5, 1]),
  bronze('pommel_y', [-0.75, -7.75, -0.75], [0.75, -5.25, 0.75]),
  bronze('pommel_point', [-0.4, -8.5, -0.4], [0.4, -7.75, 0.4])
];

const spec = [{ name: 'root', origin: [0, BASE, 0], children: [{ name: 'sword', origin: [0, 0, 0], cubes: [
  bronze('grip', [-0.6, -5.5, -0.6], [0.6, 2.5, 0.6], 'wrap'),
  ...pommel,
  bronze('guard', [-3.5, 2.5, -0.75], [3.5, 3.75, 0.75]),
  ...[guardEnd, guardCap].flatMap(c => [c, K.mirrorCube(c)]),
  { name: 'guard_boss', from: [-1, 1.75, -1.1], to: [1, 4.25, 1.1], paint: { north: 'boss', south: 'boss', all: 'bronze' } },
  bronze('guard_boss_round', [-0.75, 1.5, -1], [0.75, 4.5, 1])
], children: [{ name: 'blade', origin: [0, 3.75, 0], cubes: [
  { name: 'blade_main', from: [-2.5, 3.75, -0.5], to: [2.5, BLADE_TOP, 0.5], paint: { north: 'rblade', south: 'rblade', all: 'edge' } },
  ...tip] }] }] }];

const P = { ...S.anti, blade: '#27272e', blot: '#36373f', bronze: '#352b1b', bronzeLight: '#5a4a2c', wrap: '#120f0b' };

// Four-point star centred on (cx, cy) in pixel coordinates; arms thin toward their ends.
const inStar = (i, j, cx, cy, r) => {
  const dx = Math.abs(i + 0.5 - cx), dy = Math.abs(j + 0.5 - cy);
  return dx <= 1.2 * (1 - dy / r) || dy <= 1.2 * (1 - dx / r);
};
// Small star on one pixel column: 3 wide, 5 tall.
const inSmallStar = (i, j, ci, cj) => {
  const di = Math.abs(i - ci), dj = Math.abs(j - cj);
  return (di === 0 && dj <= 2) || (dj === 0 && di === 1);
};

const painter = (ctx, r, m) => {
  const { u, v, w, h } = r, rnd = K.rng(53 + u + v * 5), grain = () => 1 + (rnd() - 0.5) * 0.12;
  const put = (i, j, c, k = grain()) => K.px(ctx, u + i, v + j, K.shade(c, k));
  if (m === 'rblade') {
    // Face row 0 is the top of the blade. Fuller runs up the middle and ends in a star.
    const mid = w / 2, starY = 6, stars = [6, 12, 18].map(d => h - d);
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
      let c = S.patch(u + i, v + j, 5) > 0.58 ? P.blot : P.blade;
      if (i === 0 || i === w - 1) c = P.edge;
      const fuller = Math.abs(i + 0.5 - mid) < 1 && j >= starY;
      const star = inStar(i, j, mid, starY, 5) || stars.some(y => inSmallStar(i, j, 2, y) || inSmallStar(i, j, w - 3, y));
      put(i, j, fuller || star ? P.ridge : c);
    }
  } else if (m === 'wrap') {
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) put(i, j, (i + j) % 3 === 0 ? P.wrap : P.bronze);
  } else if (m === 'boss') {
    // Cross in lighter bronze on the guard's round boss.
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
      const cross = Math.abs(i + 0.5 - w / 2) < 1 || Math.abs(j + 0.5 - h / 2) < 1;
      put(i, j, cross ? P.bronzeLight : P.bronze);
    }
  } else S.painter(P)(ctx, r, m);
};

module.exports = { name: 'reverse_blade', group: 'ability', kind: 'entity', spec, animations: A.animations(), textures: { reverse_blade: painter } };
