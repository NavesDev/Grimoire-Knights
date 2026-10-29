// Order of the Eclipse mage. Hooded robe, face in shadow, element color on trims and eyes.
const K = load('./lib.js');

const arm = (side, extra = []) => {
  const s = side === 'right' ? -1 : 1, x0 = s < 0 ? -8 : 4, x1 = s < 0 ? -4 : 8;
  return { name: side + '_arm', origin: [s * 5, 22, 0], cubes: [
    { name: side + '_hand_arm', from: [x0, 12, -2], to: [x1, 24, 2], mat: 'arm' },
    { name: side + '_sleeve', from: [x0 - 0.25, 15, -2.25], to: [x1 + 0.25, 24.25, 2.25], mat: 'sleeve' },
    { name: side + '_cuff', from: [x0 - 0.5, 14, -2.5], to: [x1 + 0.5, 16, 2.5], mat: 'cuff' },
    ...extra.map(f => f(s, x0, x1))] };
};

const hood = [
  { name: 'hood_top', from: [-4.5, 32, -5], to: [4.5, 33, 4.5], mat: 'hood' },
  { name: 'hood_left', from: [4, 24, -5], to: [5, 33, 4.5], paint: { north: 'hood_rim', all: 'hood' } },
  { name: 'hood_right', from: [-5, 24, -5], to: [-4, 33, 4.5], paint: { north: 'hood_rim', all: 'hood' } },
  { name: 'hood_back', from: [-4.5, 23.5, 4], to: [4.5, 32.5, 5], mat: 'hood' },
  { name: 'hood_brim', from: [-4, 32, -5.5], to: [4, 33, -4.5], mat: 'hood_rim' }
];

const lowerBody = backZ => [
  { name: 'robe_front', origin: [0, 12, 0], cubes: [{ name: 'skirt_front', from: [-5, 1, -3], to: [5, 12, 0], paint: { north: 'skirt_front', all: 'skirt' } }] },
  { name: 'robe_back', origin: [0, 12, 0], cubes: [{ name: 'skirt_back', from: [-5, 0.5, 0], to: [5, 12, backZ], mat: 'skirt' }] },
  { name: 'right_leg', origin: [-2, 12, 0], cubes: [{ name: 'right_leg_cube', from: [-4, 0, -2], to: [0, 12, 2], mat: 'leg' }] },
  { name: 'left_leg', origin: [2, 12, 0], cubes: [{ name: 'left_leg_cube', from: [0, 0, -2], to: [4, 12, 2], mat: 'leg' }] }
];

const spec = [{ name: 'root', origin: [0, 0, 0], children: [
  { name: 'body', origin: [0, 24, 0], cubes: [
    { name: 'torso', from: [-4, 12, -2], to: [4, 24, 2], paint: { north: 'torso_front', all: 'robe' } },
    { name: 'capelet', from: [-5, 20.5, -3], to: [5, 24.5, 3], mat: 'capelet' },
    { name: 'belt', from: [-4.5, 11, -2.5], to: [4.5, 12.5, 2.5], paint: { north: 'belt_front', all: 'belt' } }
  ], children: [
    { name: 'head', origin: [0, 24, 0], cubes: [
      { name: 'face', from: [-4, 24, -4], to: [4, 32, 4], paint: { north: 'face', all: 'shadow' } }, ...hood],
    children: [{ name: 'hood_tip', origin: [0, 33, 2], rotation: [-20, 0, 0], cubes: [
      { name: 'tip_1', from: [-3, 33, -2], to: [3, 35, 4], mat: 'hood' },
      { name: 'tip_2', from: [-1.5, 35, 0], to: [1.5, 37, 4], mat: 'hood' },
      { name: 'tip_3', from: [-0.5, 37, 2], to: [0.5, 39, 4], mat: 'hood' }] }] },
    arm('right'), arm('left'),
    { name: 'grimoire_anchor', origin: [-7, 14, -6] }] },
  ...lowerBody(3)] }];

// Eclipse insignia: gold ring, crescent and rays.
const eclipse = (px, w, h, P, cx, cy) => {
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    const d = Math.hypot(i - cx, j - cy), d2 = Math.hypot(i - cx - 1.3, j - cy + 0.8);
    if (d <= 4.2 && d >= 3.0) px(i, j, P.trim);
    else if (d < 3.0 && d2 > 2.4) px(i, j, P.trim2);
    else if (d < 3.0) px(i, j, P.robeDark);
    if (d > 4.6 && d < 5.8 && (Math.round(Math.atan2(j - cy, i - cx) * 8 / Math.PI) % 2) === 0) px(i, j, P.trim);
  }
};

const painter = P => (ctx, r, m) => {
  const { u, v, w, h } = r, px = (x, y, c) => K.px(ctx, u + x, v + y, c), fill = (c, a) => K.noiseFill(ctx, r, c, a, 5);
  const hline = (y, c) => { for (let i = 0; i < w; i++) px(i, y, c); };
  const vline = (x, c, y0 = 0, y1 = h) => { for (let j = y0; j < y1; j++) px(x, j, c); };
  switch (m) {
    case 'robe': case 'hood': case 'sleeve': fill(P.robe, 0.08); break;
    case 'torso_front': fill(P.robe, 0.08); for (let j = 19; j < h; j++) { px(7, j, P.trim); px(8, j, P.trim); } eclipse(px, w, h, P, 7.5, 14); break;
    case 'capelet': fill(P.robeDark, 0.08); hline(h - 1, P.accent); hline(h - 2, P.trim); break;
    case 'belt': fill(P.leather, 0.1); break;
    case 'belt_front': fill(P.leather, 0.1); for (let i = 7; i < 11; i++) for (let j = 0; j < h; j++) px(i, j, P.trim); px(8, 1, P.leather); px(9, 1, P.leather); break;
    case 'face': fill(P.shadow, 0.15); [[4, 7], [5, 7], [11, 7], [12, 7]].forEach(([x, y]) => px(x, y, P.eye)); [[4, 8], [12, 8]].forEach(([x, y]) => px(x, y, K.shade(P.eye, 0.7))); break;
    case 'shadow': fill(P.shadow, 0.1); break;
    case 'hood_rim': fill(P.robe, 0.08); for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) if (i === 0 || i === w - 1 || j === 0 || j === h - 1) px(i, j, P.trim); break;
    case 'skirt': fill(P.robe, 0.08); hline(h - 1, P.trim); hline(h - 2, P.accent); hline(h - 3, P.accent); break;
    case 'skirt_front': fill(P.robe, 0.08); hline(h - 1, P.trim); hline(h - 2, P.accent); hline(h - 3, P.accent); vline(Math.floor(w / 2) - 1, P.trim); vline(Math.floor(w / 2), P.trim); break;
    case 'arm': for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, j >= h - 5 ? K.shade(P.skin, 1 + ((i * 7 + j * 3) % 5 - 2) * 0.02) : P.robeDark); break;
    case 'cuff': fill(P.accent, 0.08); hline(0, P.trim); hline(h - 1, P.trim); break;
    case 'leg': for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, j >= h - 6 ? K.shade(P.boot, 1 + ((i + j) % 3 - 1) * 0.06) : P.pants); break;
    default: fill('#ff00ff', 0);
  }
};

const fire = { robe: '#2b2340', robeDark: '#1d1730', trim: '#c9a24a', trim2: '#e8c76a', accent: '#a3301c', leather: '#4a3020', shadow: '#120e18', eye: '#ff8a2a', skin: '#c9a07c', boot: '#2e211a', pants: '#1f1a26' };
const wind = { ...fire, accent: '#2f8456', eye: '#8affc0' };

module.exports = {
  name: 'order_mage', group: 'enemy', kind: 'entity', spec,
  textures: { order_mage_fire: painter(fire), order_mage_wind: painter(wind) },
  // Shared with renegade_captain.
  arm, hood, lowerBody, painter, palettes: { fire, wind }
};
