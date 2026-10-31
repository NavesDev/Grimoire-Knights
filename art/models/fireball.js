// Fire magic 1 projectile. Flies toward north (-z); the trail points south.
const K = load('./lib.js');

// Bones: core (pulses), two shells spinning opposite ways, six tongues swept back that flicker on their
// own phase, a trail chain that whips, and embers orbiting the ball.
const C = [0, 4, 0];
const TONGUES = [7, 5, 8, 5, 7, 4];
const tongue = (len, k) => {
  const a = k * Math.PI / 3, x = Math.cos(a) * 3.4, y = C[1] + Math.sin(a) * 3.4;
  return { name: 'tongue_' + (k + 1), origin: [x, y, 1], rotation: [-15 * Math.sin(a), 15 * Math.cos(a), 0],
    cubes: [{ name: 'tongue_cube_' + (k + 1), from: [x - 1, y - 1, 1], to: [x + 1, y + 1, 1 + len], mat: 'tongue' }] };
};
const TRAIL = [[4, 3, 3.5], [3, 6.5, 3], [2, 9.5, 2.5], [1, 12, 2]];
const trail = i => {
  const [w, z, len] = TRAIL[i];
  return { name: 'trail_' + (i + 1), origin: [0, C[1], z],
    cubes: [{ name: 'trail_cube_' + (i + 1), from: [-w / 2, C[1] - w / 2, z], to: [w / 2, C[1] + w / 2, z + len], mat: 'tongue' }],
    children: i + 1 < TRAIL.length ? [trail(i + 1)] : [] };
};
const EMBERS = [[45, 0], [135, 2.5], [225, 1], [315, 4]];

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'ball', origin: C, children: [
  { name: 'core', origin: C, cubes: [
    { name: 'core_cube', from: [-2, 2, -2], to: [2, 6, 2], mat: 'core' },
    { name: 'body_x', from: [-3, 2.5, -2.5], to: [3, 5.5, 2.5], mat: 'inner' },
    { name: 'body_y', from: [-2.5, 1, -2.5], to: [2.5, 7, 2.5], mat: 'inner' },
    { name: 'body_z', from: [-2.5, 2.5, -3], to: [2.5, 5.5, 3], mat: 'inner' }] },
  { name: 'shell_a', origin: C, cubes: [{ name: 'shell_a_cube', from: [-3.2, 0.8, -3.2], to: [3.2, 7.2, 3.2], origin: C, rotation: [0, 45, 0], mat: 'outer' }] },
  { name: 'shell_b', origin: C, cubes: [{ name: 'shell_b_cube', from: [-3.6, 0.4, -3.6], to: [3.6, 7.6, 3.6], origin: C, rotation: [45, 0, 0], mat: 'outer' }] },
  { name: 'tongues', origin: C, children: TONGUES.map(tongue) },
  trail(0),
  { name: 'embers', origin: C, cubes: EMBERS.map(([deg, z], k) => {
    const a = deg * Math.PI / 180, x = Math.cos(a) * 5.5, y = C[1] + Math.sin(a) * 5.5;
    return { name: 'ember_' + (k + 1), from: [x - 0.5, y - 0.5, z - 0.5], to: [x + 0.5, y + 0.5, z + 0.5], mat: 'inner' };
  }) }
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

// spin (1 s loop; every frequency is a whole number of turns per second so it loops cleanly).
const wave = (speed, amp, phase = 0) => `math.sin(query.anim_time * ${speed} + ${phase}) * ${amp}`;
const animations = { spin: { loop: 'loop', length: 1, bones: {
  core: { scale: Array(3).fill(`1 + ${wave(1440, 0.07)}`) },
  shell_a: { rotation: [0, 0, 'query.anim_time * 360'] },
  shell_b: { rotation: [0, 0, 'query.anim_time * -720'] },
  tongues: { rotation: [0, 0, 'query.anim_time * 360'] },
  ...Object.fromEntries(TONGUES.map((_, k) => ['tongue_' + (k + 1), { scale: [
    `1 + ${wave(1080, 0.15, k * 97)}`, `1 + ${wave(1080, 0.15, k * 97)}`, `1 + ${wave(1440, 0.35, k * 131)}`] }])),
  ...Object.fromEntries(TRAIL.map((_, i) => ['trail_' + (i + 1), { rotation: [wave(720, 8, i * 60), wave(720, 8, i * 60 + 90), 0] }])),
  embers: { rotation: [wave(360, 10), wave(360, 10, 90), 'query.anim_time * 720'] }
} } };

module.exports = { name: 'fireball', group: 'ability', kind: 'entity', spec, animations, textures: { fireball: painter }, painter, wave };
