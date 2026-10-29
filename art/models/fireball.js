// Fire magic 1 projectile. Flies toward north (-z); the trail points south.
const K = load('./lib.js');

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'ball', origin: [0, 4, 0], cubes: [
  { name: 'core', from: [-2, 2, -2], to: [2, 6, 2], mat: 'core' },
  { name: 'body_x', from: [-3, 2.5, -2.5], to: [3, 5.5, 2.5], mat: 'inner' },
  { name: 'body_y', from: [-2.5, 1, -2.5], to: [2.5, 7, 2.5], mat: 'inner' },
  { name: 'body_z', from: [-2.5, 2.5, -3], to: [2.5, 5.5, 3], mat: 'inner' }
], children: [
  { name: 'flames', origin: [0, 4, 0], cubes: [
    { name: 'shell', from: [-3.2, 1.2, -3.2], to: [3.2, 6.8, 3.2], origin: [0, 4, 0], rotation: [0, 45, 0], mat: 'outer' },
    { name: 'lick_left', from: [-4.5, 4, 0], to: [-2.5, 6, 4], origin: [0, 4, 0], rotation: [0, -15, 0], mat: 'tongue' },
    { name: 'lick_right', from: [2.5, 2, 0], to: [4.5, 4, 4], origin: [0, 4, 0], rotation: [0, 15, 0], mat: 'tongue' },
    { name: 'lick_top', from: [-1, 6.5, 0], to: [1, 8.5, 4], origin: [0, 4, 0], rotation: [-15, 0, 0], mat: 'tongue' }] },
  { name: 'trail', origin: [0, 4, 3], cubes: [
    { name: 'trail_1', from: [-2, 2, 3], to: [2, 6, 6.5], mat: 'tongue' },
    { name: 'trail_2', from: [-1.5, 2.5, 6.5], to: [1.5, 5.5, 9.5], mat: 'tongue' },
    { name: 'trail_3', from: [-1, 3, 9.5], to: [1, 5, 12], mat: 'tongue' },
    { name: 'trail_4', from: [-0.5, 3.5, 12], to: [0.5, 4.5, 14], mat: 'tongue' }] }
] }] }];

const pick = (rnd, arr) => arr[Math.floor(rnd() * arr.length)];

// Transparent pixels in the outer shell and tongues give the flickering voxel look (cutout render).
const painter = (ctx, r, m) => {
  const { u, v, w, h } = r, rnd = K.rng(97 + u * 7 + v * 13), px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    const cx = Math.abs(i - (w - 1) / 2) / (w / 2), cy = Math.abs(j - (h - 1) / 2) / (h / 2), d = Math.max(cx, cy);
    if (m === 'core') px(i, j, pick(rnd, ['#fffbe6', '#fff4b0', '#ffffff', '#ffe98a']));
    else if (m === 'inner') px(i, j, d < 0.5 ? pick(rnd, ['#ffe25c', '#fff19a', '#ffd23f']) : d < 0.8 ? pick(rnd, ['#ffb12e', '#ffc53d', '#ff9a1f']) : pick(rnd, ['#ff7a12', '#f2600c']));
    else if (m === 'outer') { if (rnd() < 0.55) continue; px(i, j, d > 0.75 ? pick(rnd, ['#c7290a', '#e0420c', '#a81e08']) : pick(rnd, ['#ff7a12', '#ff9a1f', '#ff5e0e'])); }
    else if (m === 'tongue') { if (d > 0.7 && rnd() < 0.45) continue; px(i, j, d < 0.4 ? pick(rnd, ['#ffc53d', '#ffb12e']) : pick(rnd, ['#ff7a12', '#f2600c', '#d93a0b'])); }
  }
};

module.exports = { name: 'fireball', group: 'ability', kind: 'entity', spec, textures: { fireball: painter } };
