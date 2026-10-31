// Wind magic 3 (white gale bow): a huge bow of wind lying almost flat in front of the player, shooting
// toward north (-z), like Yuno's: tangled see-through streams instead of a clean arc, broad gusts hooking at
// the tips. Thin cubes placed with seeded noise, so every run is the same.
// Grip forward, tips and string toward the player (+z); the nocked arrow shows while drawing.
const K = load('./lib.js');
const W = load('./wind_painter.js');
const A = load('./wind_arrow.js');
const F = load('./fireball.js');

const Y = 16, H = 20, D = 7, SEGS = 9, PULL = 10, BEND = 8, DEG = 180 / Math.PI;
const rnd = K.rng(1789);
const jit = (a, b) => a + (b - a) * rnd();

// Limb centerline from the grip (t = 0) to the tip (t = 1); side is +1 (right, +x) or -1 (left).
const axis = (side, t) => [side * H * t, Y, -D * (1 - t * t)];
// Three streams per limb, wavering around the centerline with their own noise, thick at the grip.
const streams = (side, name) => [0, 1, 2].flatMap(s => {
  const ay = s === 0 ? 0.5 : jit(1.6, 3), az = s === 0 ? 0.4 : jit(1.4, 2.6), f = jit(5, 9), ph = jit(0, 6);
  const pts = Array.from({ length: SEGS + 1 }, (_, k) => {
    const t = k / SEGS, [x, y, z] = axis(side, t), fade = Math.sin(Math.PI * Math.min(1, t * 1.15));
    return [x, y + ay * Math.sin(t * f + ph) * fade, z + az * Math.cos(t * f * 0.7 + ph) * fade];
  });
  return W.stroke(`${name}_stream_${s + 1}`, pts, pts.slice(1).map((_, k) => (s === 0 ? 1.5 : 1.1) * (1.9 - 1 * k / SEGS) * jit(0.85, 1.15)), 'gust', s === 0 ? 0.9 : jit(0.7, 0.9));
});
// Gust at the tip: a broad hook sweeping out and back.
const tip = (side, name) => {
  const [x, y, z] = axis(side, 1);
  return { name: `${name}_tip`, origin: [x, y, z], cubes: [
    ...W.curlXY(`${name}_gust`, [x, y, z], side * 30, -side * 22, 6, 4.5, 1.8, 'gust', 1.2)
  ] };
};
// String half from the tip to the middle, along x; it turns on the tip to meet the nock.
const string = (side, name) => {
  const [x, y, z] = axis(side, 1);
  return { name: `${name}_string`, origin: [x, y, z],
    cubes: [{ name: `${name}_string_cube`, from: [side > 0 ? 0 : x, y - 0.15, z - 0.15], to: [side > 0 ? x : 0, y + 0.15, z + 0.15], mat: 'gust' }] };
};
const limb = side => {
  const name = side > 0 ? 'right' : 'left';
  return { name: `${name}_limb`, origin: [0, Y, -D], cubes: streams(side, name),
    children: [tip(side, name), string(side, name)] };
};

const spec = [{ name: 'root', origin: [0, Y, 0], children: [{ name: 'bow', origin: [0, Y, -D], cubes: [
  { name: 'grip', from: [-1.2, Y - 1, -D - 1], to: [1.2, Y + 1, -D + 1], mat: 'gust' }
], children: [
  limb(1), limb(-1),
  A.arrow('nocked_arrow', Y, 0)
] }] }];

// Pose for pull p (nock p back from rest) and bend b (degrees, tips toward the player). A +y turn moves the
// x axis toward -z. Each string half turns on its tip so its inner end meets the nock, stretched to fit.
const r2 = n => Math.round(n * 100) / 100;
const rig = (p, b) => {
  const out = {};
  [1, -1].forEach(side => {
    const name = side > 0 ? 'right' : 'left', r = -side * b, rr = r / DEG;
    const vx = side * H, vz = D, tx = vx * Math.cos(rr) + vz * Math.sin(rr), tz = -D - vx * Math.sin(rr) + vz * Math.cos(rr);
    const dx = -tx, dz = p - tz, len = Math.hypot(dx, dz);
    const w = (side > 0 ? Math.atan2(dz, -dx) : Math.atan2(-dz, dx)) * DEG;
    out[`${name}_limb`] = [0, r2(r), 0];
    out[`${name}_string`] = [0, r2(w - r), 0];
    out[`${name}_stretch`] = [r2(len / H), 1, 1];
  });
  out.nock = [0, 0, r2(p)];
  return out;
};
const smooth = s => s * s * (3 - 2 * s);
// keys: [[time, pull, bend]] -> bone channels.
const timeline = keys => {
  const bones = {}, add = (bone, ch, t, v) => (((bones[bone] ||= {})[ch] ||= []).push([t, v]));
  keys.forEach(([t, p, b]) => {
    const r = rig(p, b);
    ['right', 'left'].forEach(n => {
      add(`${n}_limb`, 'rotation', t, r[`${n}_limb`]);
      add(`${n}_string`, 'rotation', t, r[`${n}_string`]);
      add(`${n}_string`, 'scale', t, r[`${n}_stretch`]);
    });
    add('nocked_arrow', 'position', t, r.nock);
  });
  return bones;
};
// The wind never sits still: the tip gusts sway in every animation.
const wave = F.wave;
const sway = Object.fromEntries(['right', 'left'].map((n, i) => [`${n}_tip`, { rotation: [wave(360, 5, i * 90), wave(720, 4), wave(540, 8, i * 120)] }]));

const DRAW = [0, 0.1, 0.2, 0.3, 0.4, 0.5];
const animations = {
  // summon (0.3 s, hold): swirls in, spinning a half turn and growing.
  summon: { loop: 'hold', length: 0.3, bones: { ...sway,
    root: { scale: [[0, [0.01, 0.01, 0.01]], [0.22, [1.1, 1.1, 1.1], 'catmullrom'], [0.3, [1, 1, 1], 'catmullrom']],
      rotation: [[0, [0, 180, 0]], [0.3, [0, 0, 0], 'catmullrom']] },
    nocked_arrow: { scale: [0, 0, 0] } } },
  // draw (0.5 s, hold): the arrow appears on the string and is pulled back; the limbs bend toward the player.
  draw: { loop: 'hold', length: 0.5, bones: { ...sway, ...timeline(DRAW.map(t => [t, PULL * smooth(t / 0.5), BEND * smooth(t / 0.5)])),
    nocked_arrow: { ...timeline(DRAW.map(t => [t, PULL * smooth(t / 0.5), 0])).nocked_arrow,
      scale: [[0, [0, 0, 0]], [0.1, [1, 1, 1], 'catmullrom']] } } },
  // release (0.6 s): the string snaps forward past rest and settles, the arrow leaves (the code spawns the
  // real ones), then the bow blows apart.
  release: { loop: 'once', length: 0.6, bones: { ...sway, ...timeline([[0, PULL, BEND], [0.06, -1.5, -2], [0.14, 0.6, 0.8], [0.2, 0, 0]]),
    nocked_arrow: { scale: [0, 0, 0] },
    root: { scale: [[0.3, [1, 1, 1]], [0.6, [1.4, 0.01, 1.4], 'catmullrom']] } } }
};

module.exports = { name: 'wind_bow', group: 'ability', kind: 'entity', spec, animations, textures: { wind_bow: W.painter } };
