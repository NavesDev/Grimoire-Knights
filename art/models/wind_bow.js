// Wind magic 3 (white gale bow, Yuno's Shippu no Hakkyu): a huge bow of wind modeled upright (the renderer lays it
// on its side, limbs left and right) in front of the player,
// shooting toward north (-z): seen from the side a ")" with the grip forward and the tips back toward the player
// (+z). Tangled see-through streams instead of a clean limb, broad gusts hooking at the tips, and six big arrows
// nocked at once, stacked along the string. Thin cubes placed with seeded noise, so every run is the same.
// The arrows only show while drawing; the code fires six wind_arrow entities from their spots (ARROWS below,
// mirrored in WindBowEntity) at the end of draw.
const K = load('./lib.js');
const W = load('./wind_painter.js');
const A = load('./wind_arrow.js');
const F = load('./fireball.js');

// Limbs from the grip at (0, Y, -D) up and down H to the tips at z 0.
const Y = 16, H = 28, D = 9, SEGS = 12, PULL = 4, DEG = 180 / Math.PI;
// Arrows: height off the grip, nocked at NOCK on the string; BIG times the wind_arrow size, as the fired ones.
const ARROWS = [-12.5, -7.5, -2.5, 2.5, 7.5, 12.5], NOCK = 1, BIG = 1.5;
const rnd = K.rng(1789);
const jit = (a, b) => a + (b - a) * rnd();
const r2 = n => Math.round(n * 100) / 100;

// Limb centerline from the grip (t = 0) to the tip (t = 1); side +1 (upper, +y) or -1 (lower).
const axis = (side, t) => [0, Y + side * H * t, -D * (1 - t * t)];
// Stroke of cubes in the side plane (y, z): each segment is a cube along y turned on x to follow the points;
// widths show from behind the bow (x), `thick` is the depth.
const strokeYZ = (name, pts, widths, thick) => pts.slice(1).map((p, k) => {
  const q = pts[k], dy = p[1] - q[1], dz = p[2] - q[2], len = Math.hypot(dy, dz) * 1.2, w = widths[k];
  const c = [(p[0] + q[0]) / 2, (p[1] + q[1]) / 2, (p[2] + q[2]) / 2];
  return { name: `${name}_${k + 1}`, from: [r2(c[0] - w / 2), r2(c[1] - len / 2), r2(c[2] - thick / 2)],
    to: [r2(c[0] + w / 2), r2(c[1] + len / 2), r2(c[2] + thick / 2)], origin: c.map(r2), rotation: [r2(Math.atan2(dz, dy) * DEG), 0, 0], mat: 'gust' };
});
// Three streams per limb, wavering sideways and back and forth around it, thick at the grip.
const streams = (side, name) => [0, 1, 2].flatMap(s => {
  const ax = s === 0 ? 0.5 : jit(1.6, 3), az = s === 0 ? 0.4 : jit(1.4, 2.6), f = jit(5, 9), ph = jit(0, 6);
  const pts = Array.from({ length: SEGS + 1 }, (_, k) => {
    const t = k / SEGS, [x, y, z] = axis(side, t), fade = Math.sin(Math.PI * Math.min(1, t * 1.15));
    return [x + ax * Math.sin(t * f + ph) * fade, y, z + az * Math.cos(t * f * 0.7 + ph) * fade];
  });
  return strokeYZ(`${name}_stream_${s + 1}`, pts, pts.slice(1).map((_, k) => (s === 0 ? 1.6 : 1.1) * (1.9 - 1 * k / SEGS) * jit(0.85, 1.15)), s === 0 ? 0.9 : jit(0.6, 0.9));
});
// Gust at the tip: a broad hook sweeping out and back toward the player.
const tip = (side, name) => {
  const [x, y, z] = axis(side, 1);
  return { name: `${name}_tip`, origin: [x, y, z], cubes: W.curlZY(`${name}_gust`, [x, y, z], side > 0 ? 25 : 155, side > 0 ? 28 : -28, 6, 4.5, 1.8) };
};
const limb = side => {
  const name = side > 0 ? 'upper' : 'lower';
  return { name: `${name}_limb`, origin: axis(side, 0), cubes: streams(side, name), children: [tip(side, name)] };
};
// String from tip to tip, straight at z 0.
const string = { name: 'string', origin: [0, Y, 0], cubes: [{ name: 'string_cube', from: [-0.15, Y - H, -0.15], to: [0.15, Y + H, 0.15], mat: 'gust' }] };
// Arrow k nocked on the string at its height.
const arrowAt = k => A.arrow(`arrow_${k + 1}`, Y + ARROWS[k], NOCK);

const spec = [{ name: 'root', origin: [0, Y, 0], children: [{ name: 'bow', origin: [0, Y, -D], cubes: [
  { name: 'grip', from: [-1.2, Y - 2, -D - 1.2], to: [1.2, Y + 2, -D + 1.2], mat: 'gust' }
], children: [limb(1), limb(-1), string, ...ARROWS.map((_, k) => arrowAt(k))] }] }];

// The wind never sits still: the tip gusts sway in every animation.
const wave = F.wave;
const sway = Object.fromEntries(['upper', 'lower'].map((n, i) => [`${n}_tip`, { rotation: [wave(360, 5, i * 90), wave(720, 4), wave(540, 8, i * 120)] }]));
const arrows = f => Object.fromEntries(ARROWS.map((_, k) => [`arrow_${k + 1}`, f(k)]));
const hidden = arrows(() => ({ scale: [0, 0, 0] }));
const smooth = s => s * s * (3 - 2 * s);
const DRAW = [0, 0.1, 0.2, 0.3, 0.4, 0.5];

const animations = {
  // summon (0.3 s, hold): swirls in, spinning a half turn and growing; no arrows yet.
  summon: { loop: 'hold', length: 0.3, bones: { ...sway, ...hidden,
    root: { scale: [[0, [0.01, 0.01, 0.01]], [0.22, [1.1, 1.1, 1.1], 'catmullrom'], [0.3, [1, 1, 1], 'catmullrom']],
      rotation: [[0, [0, 180, 0]], [0.3, [0, 0, 0], 'catmullrom']] } } },
  // draw (0.5 s, hold): the six arrows appear one after another from the middle out and are pulled back
  // together; the tips flex toward the player.
  draw: { loop: 'hold', length: 0.5, bones: { ...sway,
    ...arrows(k => {
      const at = 0.04 + 0.05 * [2, 1, 0, 0, 1, 2][k];
      return { scale: [[0, [0, 0, 0]], [at, [0, 0, 0]], [at + 0.08, [BIG * 1.15, BIG * 1.15, BIG * 1.15], 'catmullrom'], [at + 0.14, [BIG, BIG, BIG], 'catmullrom']],
        position: DRAW.map(t => [t, [0, 0, r2(PULL * smooth(t / 0.5))]]) };
    }),
    upper_limb: { rotation: DRAW.map(t => [t, [r2(6 * smooth(t / 0.5)), 0, 0]]) },
    lower_limb: { rotation: DRAW.map(t => [t, [r2(-6 * smooth(t / 0.5)), 0, 0]]) } } },
  // release (0.6 s): the arrows leave at once (the code spawns the real ones), the tips snap forward past
  // rest and settle, then the bow blows apart.
  release: { loop: 'once', length: 0.6, bones: { ...sway, ...hidden,
    upper_limb: { rotation: [[0, [6, 0, 0]], [0.06, [-3, 0, 0], 'catmullrom'], [0.16, [1, 0, 0], 'catmullrom'], [0.24, [0, 0, 0], 'catmullrom']] },
    lower_limb: { rotation: [[0, [-6, 0, 0]], [0.06, [3, 0, 0], 'catmullrom'], [0.16, [-1, 0, 0], 'catmullrom'], [0.24, [0, 0, 0], 'catmullrom']] },
    root: { scale: [[0.3, [1, 1, 1]], [0.6, [1.4, 0.01, 1.4], 'catmullrom']] } } }
};

module.exports = { name: 'wind_bow', group: 'ability', kind: 'entity', spec, animations, ARROWS, textures: { wind_bow: W.painter } };
