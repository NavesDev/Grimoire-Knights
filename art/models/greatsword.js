// Anti-magic greatsword (ref: Demon-Slayer Sword). Grip point at the origin, blade up.
// Hilt is plain tarnished bronze: thin cross with pointed ends, a langet over the blade, flared pommel.
const S = load('./sword_painter.js');
// root pivots at the pommel base: the sword grows out of the grimoire pages from there (sword_anim.js).
const BASE = -10;
const A = load('./sword_anim.js');

const blade = (n, f, t, face = 'blade') => ({ name: n, from: f, to: t, paint: { north: face, south: face, all: 'edge' } });
const bronze = (n, f, t) => ({ name: n, from: f, to: t, mat: 'bronze' });

// Symmetric point (">" seen side-on): half-width shrinks one texture pixel per step.
const tip = Array.from({ length: 7 }, (_, k) => {
  const hw = 3.5 - k * 0.5, y = 33 + k * 1.25;
  return blade('tip_' + (k + 1), [-hw, y, -0.75], [hw, y + 1.25, 0.75]);
});

// Langet: guard arm lying flat on one face of the blade, ending in a point.
const langet = (side, z0, z1) => [
  bronze('langet_' + side, [-0.5, 4.5, z0], [0.5, 10, z1]),
  bronze('langet_tip_' + side, [-0.25, 10, z0], [0.25, 11.5, z1])
];

const spec = [{ name: 'root', origin: [0, BASE, 0], children: [{ name: 'sword', origin: [0, 0, 0], cubes: [
  bronze('grip', [-0.5, -8, -0.5], [0.5, 3, 0.5]),
  bronze('pommel_neck', [-0.75, -9, -0.75], [0.75, -8, 0.75]),
  bronze('pommel_cap', [-1.25, -10, -1.25], [1.25, -9, 1.25]),
  bronze('guard', [-5, 3, -0.75], [5, 4.5, 0.75]),
  bronze('guard_tip_left', [-6, 3.25, -0.5], [-5, 4.25, 0.5]),
  bronze('guard_point_left', [-6.75, 3.5, -0.25], [-6, 4, 0.25]),
  bronze('guard_tip_right', [5, 3.25, -0.5], [6, 4.25, 0.5]),
  bronze('guard_point_right', [6, 3.5, -0.25], [6.75, 4, 0.25])
], children: [{ name: 'blade', origin: [0, 4.5, 0], cubes: [
  blade('blade_main', [-4, 4.5, -0.75], [4, 33, 0.75], 'bladeRidge'),
  ...tip,
  ...langet('front', -1.25, -0.75),
  ...langet('back', 0.75, 1.25)] }] }] }];

module.exports = { name: 'greatsword', group: 'ability', kind: 'entity', spec, animations: { ...A.animations(), ride: A.ride }, textures: { greatsword: S.painter(S.anti) } };
