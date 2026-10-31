// Wind magic 2 (tornado fang): a white tornado lying on its side, flying toward north (-z) with the wide
// mouth forward. Drawn on flat planes so it stays round and airy: four planes crossing on the flight axis
// carry the funnel's swirling lines, and rings of broken arcs face forward. Inside, a 3D core of thin
// streaks winds around the axis for body. Upgrade parts, hidden by the
// renderer until unlocked: blades (mini wind blades inside) and stones (rocks pulled from the ground).
const K = load('./lib.js');
const W = load('./wind_painter.js');
const F = load('./fireball.js');

const Y = 8, TAIL = 11, MOUTH = -9, R = 8;
// Funnel radius along z: narrow tail (+z), wide mouth (-z).
const radius = z => 1 + (R - 1) * Math.pow((TAIL - z) / (TAIL - MOUTH), 1.2);
const RINGS = [8, 4, 0, -4, -8];

// Side view of the funnel: helix lines seen edge-on become waves between the walls, broken into dashes.
const funnel = seed => pen => {
  const rnd = K.rng(seed);
  for (let s = 0; s < 4; s++) {
    const ph = rnd() * 6.3, k = 0.28 + rnd() * 0.12;
    let dash = [];
    for (let z = TAIL; z >= MOUTH; z -= 0.35) {
      const r = radius(z), y = Y + r * Math.sin(z * k + ph) * 0.95;
      dash.push([z, y]);
      if (dash.length > 6 + rnd() * 10) {
        pen.gust(dash, 0.18 + 0.3 * (TAIL - z) / (TAIL - MOUTH), 0.05);
        dash = [];
        z -= 0.8 + rnd() * 1.6;
      }
    }
  }
  // Walls: wisps tracing the funnel's outline, broken up.
  [1, -1].forEach(side => {
    for (let z = TAIL - 1; z > MOUTH + 1; z -= 4 + rnd() * 2) {
      const pts = [];
      for (let t = 0; t < 2.5; t += 0.35) pts.push([z - t, Y + side * radius(z - t) * (1.02 - 0.06 * t)]);
      pen.gust(pts, 0.35, 0.05);
    }
  });
};

// A ring facing forward: broken arcs of wind around the axis, tapering at their ends, plus a smaller one.
const ring = (r, seed) => pen => {
  const rnd = K.rng(seed);
  [[0.92, 3], [0.6, 2]].forEach(([f, n]) => {
    for (let i = 0; i < n; i++) {
      const a0 = (i / n) * 360 + rnd() * 50, span = 70 + rnd() * 60, pts = [];
      for (let a = a0; a <= a0 + span; a += 6) pts.push([Math.cos(a * Math.PI / 180) * r * f, Y + Math.sin(a * Math.PI / 180) * r * f]);
      pen.gust(pts, 0.5 * f, 0.08);
    }
  });
};

// Mini wind blade: a small crescent, centered on its plane.
const crescent = (pen, r) => {
  const cx = (r.spec.from[0] + r.spec.to[0]) / 2, cy = (r.spec.from[1] + r.spec.to[1]) / 2;
  pen.blob([...Array.from({ length: 13 }, (_, i) => {
    const a = (-60 + i * 10) * Math.PI / 180; return [cx + Math.sin(a) * 2.2, cy + Math.cos(a) * 2.2 - 1];
  }), ...Array.from({ length: 13 }, (_, i) => {
    const a = (60 - i * 10) * Math.PI / 180; return [cx + Math.sin(a) * 1.6, cy + Math.cos(a) * 1.3 - 0.5];
  })], 0.3);
};

const shapes = { ...Object.fromEntries([0, 1, 2, 3].map(k => ['funnel_' + k, funnel(70 + k * 13)])),
  ...Object.fromEntries(RINGS.map((z, k) => ['ring_' + k, ring(radius(z), 300 + k * 7)])), crescent };

// 3D core: thin streaks along helix strands inside the funnel, with gaps.
const coreStreaks = (() => {
  const rnd = K.rng(4242), out = [];
  for (let s = 0; s < 4; s++) {
    const turns = 2 + rnd(), steps = 30, phase = s * 90 + rnd() * 40;
    for (let i = 0; i < steps; i++) {
      if (rnd() < 0.5) continue;
      const u = i / steps, z = TAIL + (MOUTH - TAIL) * u, r = radius(z) * (0.65 + rnd() * 0.2), th = phase + u * turns * 360;
      const rad = th * Math.PI / 180, x = r * Math.cos(rad), y = Y + r * Math.sin(rad);
      const len = (2 * Math.PI * r * turns / steps) * (1.3 + rnd() * 0.8), w = 0.15 + rnd() * 0.15;
      out.push({ name: `core_${s + 1}_${i + 1}`, from: [x - w / 2, y - len / 2, z - 0.15], to: [x + w / 2, y + len / 2, z + 0.15],
        origin: [x, y, z], rotation: [0, 0, th], mat: rnd() < 0.7 ? 'edge' : 'mist' });
    }
  }
  return out;
})();

const BLADES = [[0, 2], [90, -2], [180, -6], [270, 4]];
const STONES = [[30, 5, 1.2], [110, -1, 0.9], [200, 2.5, 1.4], [290, -5, 1], [340, 7, 0.8], [160, -6, 1.1]];

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'tornado', origin: [0, Y, 0], children: [
  { name: 'core', origin: [0, Y, 0], cubes: coreStreaks },
  { name: 'funnel', origin: [0, Y, 0], children: [0, 1, 2, 3].map(k => ({ name: 'funnel_' + (k + 1), origin: [0, Y, 0], rotation: [0, 0, k * 45],
    cubes: [W.plane('funnel_plane_' + (k + 1), 'yz', MOUTH - 1, TAIL + 1, Y - R - 1, Y + R + 1, 0, 'funnel_' + k)] })) },
  ...RINGS.map((z, k) => {
    const r = radius(z) + 1;
    return { name: 'ring_' + (k + 1), origin: [0, Y, z], cubes: [W.plane('ring_plane_' + (k + 1), 'xy', -r, r, Y - r, Y + r, z, 'ring_' + k)] };
  }),
  { name: 'blades', origin: [0, Y, 0], cubes: BLADES.map(([deg, z], k) => {
    const a = deg * Math.PI / 180, x = Math.cos(a) * 3, y = Math.sin(a) * 3;
    return { ...W.plane('mini_blade_' + (k + 1), 'xy', x - 2.5, x + 2.5, Y + y - 2.5, Y + y + 2.5, z, 'crescent'), origin: [x, Y + y, z], rotation: [0, 0, deg] };
  }) },
  { name: 'stones', origin: [0, Y, 0], cubes: STONES.map(([deg, z, s], k) => {
    const a = deg * Math.PI / 180, x = Math.cos(a) * 5, y = Y + Math.sin(a) * 5;
    return { name: 'stone_' + (k + 1), from: [x - s, y - s, z - s], to: [x + s, y + s, z + s], origin: [x, y, z], rotation: [deg, deg / 2, 0], mat: 'stone' };
  }) }
] }] }];

const wave = F.wave;
// The funnel planes turn slowly; each ring turns at its own whole-number speed so the 1 s loop is seamless.
const spinning = (turns, pulse) => ({
  funnel: { rotation: [0, 0, `query.anim_time * ${-360 * turns}`] },
  core: { rotation: [0, 0, `query.anim_time * ${-720 * turns}`] },
  ...Object.fromEntries(RINGS.map((_, k) => ['ring_' + (k + 1), { rotation: [0, 0, `query.anim_time * ${-360 * turns * (2 + (k % 3))}`],
    ...(pulse ? { scale: [`1 + ${wave(360 * turns, pulse, k * 60)}`, `1 + ${wave(360 * turns, pulse, k * 60)}`, 1] } : {}) }])),
  blades: { rotation: [0, 0, `query.anim_time * ${-720 * turns}`] },
  stones: { rotation: [wave(360, 4), 0, `query.anim_time * ${-360 * turns}`] } });

const animations = {
  // spin (1 s loop): flying.
  spin: { loop: 'loop', length: 1, bones: spinning(1, 0) },
  // hold (0.5 s loop): a target is trapped inside; spins twice as fast and the rings squeeze in and out.
  hold: { loop: 'loop', length: 0.5, bones: spinning(2, 0.08) }
};

module.exports = { name: 'tornado', group: 'ability', kind: 'entity', spec, animations, textures: { tornado: W.planePainter(shapes) } };
