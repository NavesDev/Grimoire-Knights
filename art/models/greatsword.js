// Anti-magic greatsword (ref: Demon-Slayer Sword). Grip point at the origin, blade up.
const S = load('./sword_painter.js');

const blade = (n, f, t) => ({ name: n, from: f, to: t, paint: { north: 'blade', south: 'blade', all: 'edge' } });

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'sword', origin: [0, 0, 0], cubes: [
  { name: 'grip', from: [-0.75, -8, -0.75], to: [0.75, 3, 0.75], mat: 'grip' },
  { name: 'pommel', from: [-1.25, -10, -1.25], to: [1.25, -8, 1.25], mat: 'metal' },
  { name: 'pommel_ring', from: [-0.5, -11, -0.5], to: [0.5, -10, 0.5], mat: 'metal' },
  { name: 'guard', from: [-5.5, 3, -1.25], to: [5.5, 4.5, 1.25], mat: 'metal' },
  { name: 'guard_tip_left', from: [-6.5, 2.5, -1], to: [-5.5, 4.5, 1], mat: 'metal' },
  { name: 'guard_tip_right', from: [5.5, 2.5, -1], to: [6.5, 4.5, 1], mat: 'metal' }
], children: [{ name: 'blade', origin: [0, 4.5, 0], cubes: [
  { name: 'ricasso', from: [-3, 4.5, -1], to: [3, 7, 1], mat: 'metal' },
  blade('blade_main', [-4, 7, -0.75], [4, 33, 0.75]),
  { name: 'spine_ridge', from: [-0.5, 7, -1], to: [0.5, 30, 1], mat: 'ridge' },
  blade('tip_1', [-4, 33, -0.75], [2.5, 35.5, 0.75]),
  blade('tip_2', [-4, 35.5, -0.75], [0.5, 37.5, 0.75]),
  blade('tip_3', [-4, 37.5, -0.75], [-1.5, 38.5, 0.75])] }] }] }];

module.exports = { name: 'greatsword', group: 'ability', kind: 'entity', spec, textures: { greatsword: S.painter(S.anti) } };
