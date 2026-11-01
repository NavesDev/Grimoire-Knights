// Wind magic 3 (white gale bow, Yuno's Shippu no Hakkyu): a huge bow of wind standing up and arching over the
// player like a gate, shooting toward north (-z): tangled see-through streams instead of a clean arc, broad gusts
// hooking at the tips, and six big arrows nocked at once in a fan inside the arch. Thin cubes placed with seeded
// noise, so every run is the same. Grip on top and forward (-z), tips low at the sides and toward the player (+z).
// The arrows only show while drawing; the code fires six wind_arrow entities from their spots (ARROWS below,
// mirrored in WindBowEntity) at the end of draw.
const K = load('./lib.js');
const W = load('./wind_painter.js');
const A = load('./wind_arrow.js');
const F = load('./fireball.js');

// Arch around (0, Y): radius R, squashed SQUASH on y, from the grip (0°, top) to the tips (±SPAN°); the grip is
// D forward of the tips.
const Y = 16, R = 34, SQUASH = 0.85, SPAN = 110, D = 10, SEGS = 14, PULL = 4, DEG = 180 / Math.PI;
// Arrows: angle on the arch and how far in from it (share of R); nocked at NOCK, pointing -z.
// The arrows show BIG times the wind_arrow size, as the fired ones (WindBowEntity).
const ARROWS = [-82, -50, -18, 18, 50, 82], INNER = 0.78, NOCK = 4, BIG = 1.5;
const rnd = K.rng(1789);
const jit = (a, b) => a + (b - a) * rnd();
const r2 = n => Math.round(n * 100) / 100;

// Arch centerline at t from the grip (0) to a tip (1); side +1 (right, +x) or -1 (left).
const axis = (side, t) => {
  const a = side * SPAN * t / DEG;
  return [R * Math.sin(a), Y + R * SQUASH * Math.cos(a), -D * (1 - t * t)];
};
// Stroke of cubes in the vertical plane (x, y): each segment is a cube along x turned on z to follow the
// points; widths show on the face toward the player, `thick` is the depth.
const strokeXY = (name, pts, widths, thick) => pts.slice(1).map((p, k) => {
  const q = pts[k], dx = p[0] - q[0], dy = p[1] - q[1], len = Math.hypot(dx, dy) * 1.2, w = widths[k];
  const c = [(p[0] + q[0]) / 2, (p[1] + q[1]) / 2, (p[2] + q[2]) / 2];
  return { name: `${name}_${k + 1}`, from: [r2(c[0] - len / 2), r2(c[1] - w / 2), r2(c[2] - thick / 2)],
    to: [r2(c[0] + len / 2), r2(c[1] + w / 2), r2(c[2] + thick / 2)], origin: c.map(r2), rotation: [0, 0, r2(Math.atan2(dy, dx) * DEG)], mat: 'gust' };
});
// Three streams per limb, wavering in and out of the arch and back and forth, thick at the grip.
const streams = (side, name) => [0, 1, 2].flatMap(s => {
  const ar = s === 0 ? 0.6 : jit(2, 3.6), az = s === 0 ? 0.4 : jit(1.4, 2.6), f = jit(5, 9), ph = jit(0, 6);
  const pts = Array.from({ length: SEGS + 1 }, (_, k) => {
    const t = k / SEGS, [x, y, z] = axis(side, t), fade = Math.sin(Math.PI * Math.min(1, t * 1.1));
    const out = 1 + ar * Math.sin(t * f + ph) * fade / R;
    return [x * out, Y + (y - Y) * out, z + az * Math.cos(t * f * 0.7 + ph) * fade];
  });
  return strokeXY(`${name}_stream_${s + 1}`, pts, pts.slice(1).map((_, k) => (s === 0 ? 1.8 : 1.2) * (2 - 1.1 * k / SEGS) * jit(0.85, 1.15)), s === 0 ? 0.9 : jit(0.6, 0.9));
});
// Gust at the tip: a broad hook sweeping out and back up.
const tip = (side, name) => {
  const [x, y, z] = axis(side, 1);
  return { name: `${name}_tip`, origin: [x, y, z], cubes: W.curlXY(`${name}_gust`, [x, y, z], side * 160, side * 26, 6, 5, 2, 'gust', 1.2) };
};
const limb = side => {
  const name = side > 0 ? 'right' : 'left';
  return { name: `${name}_limb`, origin: axis(side, 0), cubes: streams(side, name), children: [tip(side, name)] };
};
// Arrow k nocked inside the arch, built at x 0 by wind_arrow and moved over.
const arrowAt = k => {
  const a = ARROWS[k] / DEG, x = r2(R * INNER * Math.sin(a)), y = r2(Y + R * SQUASH * INNER * Math.cos(a));
  const built = A.arrow(`arrow_${k + 1}`, y, NOCK);
  const move = v => [r2(v[0] + x), v[1], v[2]];
  return { ...built, origin: move(built.origin), cubes: built.cubes.map(c => ({ ...c, from: move(c.from), to: move(c.to), origin: c.origin && move(c.origin) })) };
};

const spec = [{ name: 'root', origin: [0, Y, 0], children: [{ name: 'bow', origin: [0, Y, 0], cubes: [
  { name: 'grip', from: [-1.5, Y + R * SQUASH - 1.5, -D - 1], to: [1.5, Y + R * SQUASH + 1.5, -D + 1], mat: 'gust' }
], children: [limb(1), limb(-1), ...ARROWS.map((_, k) => arrowAt(k))] }] }];

// The wind never sits still: the tip gusts sway in every animation.
const wave = F.wave;
const sway = Object.fromEntries(['right', 'left'].map((n, i) => [`${n}_tip`, { rotation: [wave(360, 5, i * 90), wave(720, 4), wave(540, 8, i * 120)] }]));
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
  // together; the limbs flex toward the player.
  draw: { loop: 'hold', length: 0.5, bones: { ...sway,
    ...arrows(k => {
      const at = 0.04 + 0.05 * [2, 1, 0, 0, 1, 2][k];
      return { scale: [[0, [0, 0, 0]], [at, [0, 0, 0]], [at + 0.08, [BIG * 1.15, BIG * 1.15, BIG * 1.15], 'catmullrom'], [at + 0.14, [BIG, BIG, BIG], 'catmullrom']],
        position: DRAW.map(t => [t, [0, 0, r2(PULL * smooth(t / 0.5))]]) };
    }),
    right_limb: { rotation: DRAW.map(t => [t, [r2(-6 * smooth(t / 0.5)), 0, 0]]) },
    left_limb: { rotation: DRAW.map(t => [t, [r2(-6 * smooth(t / 0.5)), 0, 0]]) } } },
  // release (0.6 s): the arrows leave at once (the code spawns the real ones), the limbs snap forward past
  // rest and settle, then the bow blows apart.
  release: { loop: 'once', length: 0.6, bones: { ...sway, ...hidden,
    right_limb: { rotation: [[0, [-6, 0, 0]], [0.06, [3, 0, 0], 'catmullrom'], [0.16, [-1, 0, 0], 'catmullrom'], [0.24, [0, 0, 0], 'catmullrom']] },
    left_limb: { rotation: [[0, [-6, 0, 0]], [0.06, [3, 0, 0], 'catmullrom'], [0.16, [-1, 0, 0], 'catmullrom'], [0.24, [0, 0, 0], 'catmullrom']] },
    root: { scale: [[0.3, [1, 1, 1]], [0.6, [1.4, 0.01, 1.4], 'catmullrom']] } } }
};

module.exports = { name: 'wind_bow', group: 'ability', kind: 'entity', spec, animations, ARROWS, textures: { wind_bow: W.painter } };
