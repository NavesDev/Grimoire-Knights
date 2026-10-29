// Grimoire Altar block: castle stone, rune circle, candles, lectern. The grimoire floats at book_anchor.
const K = load('./lib.js');

// Candle flame like vanilla fire: two crossed planes with a teardrop cut out of the texture (cutout), in
// flicker_<n>, plus a smaller, brighter crossed pair in flame_core_<n> that pulses out of phase.
const candle = (n, x, z, hgt) => {
  const y = 3 + hgt, cross = (name, w, h, y0, mat) => [45, -45].map((a, k) => ({ name: `${name}_${k ? 'b' : 'a'}_${n}`,
    from: [x - w / 2, y0, z], to: [x + w / 2, y0 + h, z], origin: [x, y0, z], rotation: [0, a, 0], mat, res: 8 }));
  return { name: 'candle_' + n, origin: [x, 3, z], cubes: [
    { name: 'wax_' + n, from: [x - 0.75, 3, z - 0.75], to: [x + 0.75, y, z + 0.75], mat: 'wax' },
    { name: 'wick_' + n, from: [x - 0.125, y, z - 0.125], to: [x + 0.125, y + 0.5, z + 0.125], mat: 'wick' }],
  children: [{ name: 'flicker_' + n, origin: [x, y + 0.25, z], cubes: cross('flame', 1.25, 2.25, y + 0.25, 'flame'),
    children: [{ name: 'flame_core_' + n, origin: [x, y + 0.25, z], cubes: cross('core', 0.6, 1.2, y + 0.3, 'flame_core') }] }] };
};

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'altar', origin: [0, 0, 0], cubes: [
  { name: 'plinth', from: [-8, 0, -8], to: [8, 3, 8], paint: { up: 'stone_top', all: 'stone' } },
  { name: 'step', from: [-6, 3, -6], to: [6, 5, 6], paint: { up: 'circle', all: 'stone' } },
  { name: 'column', from: [-3, 5, -3], to: [3, 13, 3], mat: 'column' },
  { name: 'band_low', from: [-3.5, 5, -3.5], to: [3.5, 6, 3.5], mat: 'gold' },
  { name: 'band_high', from: [-3.5, 12, -3.5], to: [3.5, 13, 3.5], mat: 'gold' },
  { name: 'capital', from: [-4.5, 13, -4.5], to: [4.5, 14.5, 4.5], mat: 'stone' }
], children: [
  { name: 'lectern', origin: [0, 14.5, 0], rotation: [-15, 0, 0], cubes: [
    { name: 'lectern_slab', from: [-6, 14.5, -4.5], to: [6, 16, 4.5], paint: { up: 'lectern_top', all: 'wood' } },
    { name: 'lectern_lip', from: [-5, 16, -5], to: [5, 17, -4], mat: 'gold' }] },
  candle('nw', -6.5, -6.5, 3.5), candle('ne', 6.5, -6.5, 2.5), candle('sw', -6.5, 6.5, 2.5), candle('se', 6.5, 6.5, 3.5),
  { name: 'book_anchor', origin: [0, 19, 0] }] }] }];

const palette = { stone: '#8a8580', mortar: '#5a5652', rune: '#a98bff', gold: '#c9a24a', wood: '#5a3b24', cloth: '#7a1f2a', wax: '#e8dcb4' };

// Also used by mage_tower_core for stone, runes and gold.
const painter = P => (ctx, r, m) => {
  const { u, v, w, h } = r, rnd = K.rng(71 + u * 5 + v * 3), px = (x, y, c) => K.px(ctx, u + x, v + y, c), n = (c, a) => K.shade(c, 1 + (rnd() - 0.5) * a);
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    let c;
    if (m === 'stone' || m === 'column') { const off = (Math.floor(j / 4) % 2) * 4; c = (j % 4 === 3 || (i + off) % 8 === 7) ? P.mortar : n(P.stone, 0.18); }
    else if (m === 'stone_top') c = (i % 8 === 7 || j % 8 === 7) ? P.mortar : n(P.stone, 0.15);
    else if (m === 'circle') {
      const cx = (w - 1) / 2, cy = (h - 1) / 2, d = Math.hypot(i - cx, j - cy);
      c = (Math.abs(d - 10.5) < 0.7 || Math.abs(d - 8) < 0.6) ? P.rune
        : (d < 8 && d > 6 && (Math.round(Math.atan2(j - cy, i - cx) * 12 / Math.PI) % 3 === 0)) ? K.shade(P.rune, 0.7) : n(P.stone, 0.15);
    }
    else if (m === 'gold') c = n(P.gold, 0.2);
    else if (m === 'wood') c = (j % 3 === 2) ? K.shade(P.wood, 0.75) : n(P.wood, 0.12);
    else if (m === 'lectern_top') c = (i >= 8 && i < w - 8) ? n(P.cloth, 0.1) : (i === 7 || i === w - 8) ? P.gold : n(P.wood, 0.12);
    else if (m === 'wax') c = j < 2 ? '#f7efd6' : n(P.wax, 0.08);
    else if (m === 'wick') c = '#2a2420';
    // Flame (cutout): teardrop with a round base, widest a third of the way up; blue root, white heart, orange then red edge.
    else if (m === 'flame' || m === 'flame_core') {
      const v = 1 - (j + 0.5) / h, u = Math.abs(i + 0.5 - w / 2) / (w / 2);
      const width = v < 0.3 ? Math.sqrt(0.35 + 0.65 * v / 0.3) : Math.pow((1 - v) / 0.7, 0.8);
      if (u > width) continue;
      const d = u / width;
      if (m === 'flame_core') c = d < 0.5 ? '#ffffff' : '#fff1a0';
      else c = v < 0.12 ? (d < 0.6 ? '#7ab4ff' : '#3f6fd0') : d > 0.75 ? (v > 0.6 ? '#d93a12' : '#f2600c') : d > 0.4 ? '#ffa62e' : '#ffe27a';
    }
    else c = '#ff00ff';
    px(i, j, c);
  }
  if (m === 'column') for (let j = 3; j < h - 3; j += 4) {
    const x = Math.floor(w / 2);
    px(x, j, P.rune); px(x - 1, j + 1, P.rune); px(x + 1, j + 1, P.rune);
  }
};

const sin = (speed, amp, phase = 0) => `math.sin(query.anim_time * ${speed}${phase ? ' + ' + phase : ''}) * ${amp}`;
const CANDLES = ['nw', 'ne', 'sw', 'se'];
// Each candle has its own phase; the core pulses against the outer flame. Whole turns per loop.
const flame = (n, k, s = 1) => ({
  ['flicker_' + n]: { rotation: [sin(360, 4, k * 90), 0, sin(720, 4, k * 90 + 40)], scale: [`${s} + ${sin(1080, 0.08, k * 131)}`, `${s} + ${sin(720, 0.15, k * 97)}`, `${s} + ${sin(1080, 0.08, k * 131)}`] },
  ['flame_core_' + n]: { scale: [`1 + ${sin(1440, 0.12, k * 61 + 180)}`, `1 + ${sin(1080, 0.2, k * 73 + 180)}`, `1 + ${sin(1440, 0.12, k * 61 + 180)}`] }
});
const flames = s => Object.assign({}, ...CANDLES.map((n, k) => flame(n, k, s)));
// idle: flames flicker; activate (evolve, reset, summon): flames flare up and the lectern shudders.
const animations = {
  idle: { loop: 'loop', length: 1, bones: flames(1) },
  activate: { loop: 'once', length: 1.5, bones: {
    ...flames(1),
    ...Object.fromEntries(CANDLES.map(n => ['flicker_' + n, { ...flame(n, 0)['flicker_' + n], scale: [[0, [1, 1, 1]], [0.3, [1.5, 1.9, 1.5], 'catmullrom'], [1.1, [1.35, 1.6, 1.35], 'catmullrom'], [1.5, [1, 1, 1], 'catmullrom']] }])),
    lectern: { rotation: [[0, [0, 0, 0]], [0.15, [2, 0, 1.5]], [0.3, [-1.5, 0, -1.5]], [0.45, [1, 0, 1]], [0.6, [0, 0, 0]]] }
  } }
};

module.exports = { name: 'grimoire_altar', group: 'building', kind: 'block', spec, animations, textures: { grimoire_altar: painter(palette) }, painter, palette };
