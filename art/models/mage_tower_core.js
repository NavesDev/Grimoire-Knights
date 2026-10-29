// Mage Tower central block: stone base with rune circle, four pillars, floating mana orb and two rings.
const K = load('./lib.js');
const A = load('./grimoire_altar.js');

const Y = 15;
const pillar = (n, x, z) => ({ name: 'pillar_' + n, from: [x - 1, 4, z - 1], to: [x + 1, 11, z + 1], mat: 'column' });
const cap = (n, x, z) => ({ name: 'cap_' + n, from: [x, 11, z], to: [x + 3, 12, z + 3], mat: 'gold' });
// Square ring of four bars; animate by rotating the bone.
const ring = (name, rot, r) => ({ name, origin: [0, Y, 0], rotation: rot, cubes: [
  { name: name + '_n', from: [-r, Y - 0.5, -r], to: [r, Y + 0.5, -r + 1], mat: 'gold' },
  { name: name + '_s', from: [-r, Y - 0.5, r - 1], to: [r, Y + 0.5, r], mat: 'gold' },
  { name: name + '_e', from: [r - 1, Y - 0.5, -r + 1], to: [r, Y + 0.5, r - 1], mat: 'gold' },
  { name: name + '_w', from: [-r, Y - 0.5, -r + 1], to: [-r + 1, Y + 0.5, r - 1], mat: 'gold' }] });

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'core_block', origin: [0, 0, 0], cubes: [
  { name: 'base', from: [-8, 0, -8], to: [8, 4, 8], paint: { up: 'circle', all: 'stone' } },
  pillar('nw', -6, -6), pillar('ne', 6, -6), pillar('sw', -6, 6), pillar('se', 6, 6),
  cap('nw', -7.5, -7.5), cap('ne', 4.5, -7.5), cap('sw', -7.5, 4.5), cap('se', 4.5, 4.5)
], children: [
  { name: 'orb', origin: [0, Y, 0], cubes: [
    { name: 'orb_x', from: [-3, Y - 2, -2], to: [3, Y + 2, 2], mat: 'orb' },
    { name: 'orb_y', from: [-2, Y - 3, -2], to: [2, Y + 3, 2], mat: 'orb' },
    { name: 'orb_z', from: [-2, Y - 2, -3], to: [2, Y + 2, 3], mat: 'orb' }] },
  ring('ring_a', [0, 0, 25], 4.5), ring('ring_b', [25, 0, 0], 3.75)] }] }];

const painter = (ctx, r, m) => {
  if (['stone', 'circle', 'column', 'gold'].includes(m)) return A.painter(A.palette)(ctx, r, m);
  const { u, v, w, h } = r, rnd = K.rng(61 + u + v * 3);
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++)
    K.px(ctx, u + i, v + j, ['#7b5cff', '#9d7bff', '#b9a0ff', '#6a48f0'][Math.floor(rnd() * 4)]);
};

module.exports = { name: 'mage_tower_core', group: 'building', kind: 'block', spec, textures: { mage_tower_core: painter } };
