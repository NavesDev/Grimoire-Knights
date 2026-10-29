// Floating grimoire. Cover variants per element and clover leaves (design: Folhas do trevo).
const K = load('./lib.js');

const LEAVES = {
  3: ['...XX...', '..XXXX..', '..XXXX..', 'XX.XX.XX', 'XXXXXXXX', '.XX..XX.', '....X...', '.....X..'],
  4: ['.XX..XX.', 'XXX..XXX', 'XXXXXXXX', '...XX...', 'XXXXXXXX', 'XXX..XXX', '.XX..XX.', '........'],
  5: ['...XX...', 'X..XX..X', 'XX.XX.XX', '.XXXXXX.', '..XXXX..', '.XX..XX.', 'XX....XX', '........']
};

const cover = (z1, z2, front) => {
  const zo = front ? [z2, z2 + 0.25] : [z1 - 0.25, z1];
  return [
    { name: 'cover', from: [0, 2.5, z1], to: [8, 13.5, z2], paint: { [front ? 'south' : 'north']: 'cover_face', all: 'cover_edge' } },
    { name: 'corner_top', from: [6.5, 12, zo[0]], to: [8, 13.5, zo[1]], mat: 'metal' },
    { name: 'corner_bottom', from: [6.5, 2.5, zo[0]], to: [8, 4, zo[1]], mat: 'metal' },
    { name: 'corner_spine_top', from: [0, 12, zo[0]], to: [1.5, 13.5, zo[1]], mat: 'metal' },
    { name: 'corner_spine_bottom', from: [0, 2.5, zo[0]], to: [1.5, 4, zo[1]], mat: 'metal' }
  ];
};

// Bones front_cover, back_cover and flip_page rotate on Y around the spine to open the book.
const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'book', origin: [0, 8, 0], cubes: [
  { name: 'spine', from: [-1, 2.5, -2], to: [0, 13.5, 2], mat: 'spine' },
  { name: 'spine_band_top', from: [-1.25, 10.5, -2], to: [-1, 11.5, 2], mat: 'metal' },
  { name: 'spine_band_bottom', from: [-1.25, 4.5, -2], to: [-1, 5.5, 2], mat: 'metal' }
], children: [
  { name: 'back_cover', origin: [0, 8, -1.5], cubes: [...cover(-2, -1, false),
    { name: 'pages_back', from: [0, 3, -1], to: [7.5, 13, 0], paint: { south: 'page', all: 'page_edge' } },
    { name: 'ribbon', from: [3, 0.5, -0.5], to: [3.5, 3, -0.5], mat: 'ribbon' }] },
  { name: 'front_cover', origin: [0, 8, 1.5], cubes: [...cover(1, 2, true),
    { name: 'emblem', from: [2, 5.5, 2], to: [6, 10.5, 2.5], paint: { south: 'emblem', all: 'metal' } },
    { name: 'pages_front', from: [0, 3, 0], to: [7.5, 13, 1], paint: { north: 'page', all: 'page_edge' } }] },
  { name: 'flip_page', origin: [0, 8, 0], cubes: [{ name: 'page', from: [0, 3, 0], to: [7.5, 13, 0], mat: 'page' }] }
] }] }];

const painter = P => (ctx, r, m) => {
  const { u, v, w, h } = r, seed = P.seed || 7, px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  const fill = (c, a) => K.noiseFill(ctx, r, c, a, seed);
  const ink = K.shade(P.page, 0.72);
  if (m === 'cover_face') {
    fill(P.cover, P.wear || 0.1);
    for (let i = 0; i < w; i++) { px(i, 0, P.edge); px(i, h - 1, P.edge); }
    for (let j = 0; j < h; j++) { px(0, j, P.edge); px(w - 1, j, P.edge); }
    for (let i = 3; i < w - 3; i++) { px(i, 2, P.frame); px(i, h - 3, P.frame); }
    for (let j = 3; j < h - 3; j++) { px(2, j, P.frame); px(w - 3, j, P.frame); }
    for (let i = 5; i < w - 5; i += 2) { px(i, 4, K.shade(P.frame, 0.8)); px(i, h - 5, K.shade(P.frame, 0.8)); }
    [[3, 3], [w - 4, 3], [3, h - 4], [w - 4, h - 4]].forEach(([x, y]) => px(x, y, P.frame));
    if (P.scratch) {
      const rnd = K.rng(seed * 3 + u);
      for (let k = 0; k < P.scratch; k++) {
        const x = 1 + Math.floor(rnd() * (w - 3)), y = 1 + Math.floor(rnd() * (h - 3)), len = 1 + Math.floor(rnd() * 3);
        for (let s = 0; s < len; s++) px(Math.min(w - 2, x + s), Math.min(h - 2, y + s), K.shade(P.cover, 1.7));
      }
    }
  } else if (m === 'cover_edge') fill(P.edge, 0.1);
  else if (m === 'spine') { fill(P.cover, 0.12); for (let i = 0; i < w; i++) { px(i, 0, P.edge); px(i, h - 1, P.edge); } }
  else if (m === 'metal') { fill(P.frame, 0.2); px(0, 0, K.shade(P.frame, 1.35)); if (w > 1 && h > 1) px(w - 1, h - 1, K.shade(P.frame, 0.6)); }
  else if (m === 'emblem') {
    fill(P.emblemBg, 0.08);
    for (let i = 1; i < w - 1; i++) { px(i, 0, P.frame); px(i, h - 1, P.frame); }
    for (let j = 1; j < h - 1; j++) { px(0, j, P.frame); px(w - 1, j, P.frame); }
    const pat = LEAVES[P.leaves], ox = Math.floor((w - 8) / 2), oy = Math.floor((h - 8) / 2) + (P.leaves === 3 ? 0 : 1);
    pat.forEach((row, j) => [...row].forEach((ch, i) => { if (ch === 'X') px(ox + i, oy + j, (j < 2 || i < 2) ? K.shade(P.leaf, 1.2) : P.leaf); }));
  } else if (m === 'page') {
    K.noiseFill(ctx, r, P.page, 0.05, 7);
    const rnd = K.rng(11 + u + v);
    for (let j = 3; j < h - 2; j += 3) {
      let i = 2;
      while (i < w - 2) {
        const len = 1 + Math.floor(rnd() * 4);
        for (let k = 0; k < len && i < w - 2; k++, i++) px(i, j, ink);
        i += 1;
      }
    }
  } else if (m === 'page_edge') {
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, ((r.face === 'up' || r.face === 'down') ? (j % 2) : (i % 2)) ? P.pageShade : P.page);
  } else if (m === 'ribbon') fill(P.ribbon, 0.15);
  else fill('#ff00ff', 0);
};

const pages = { page: '#e9dcb6', pageShade: '#c8b78c' };
const goldPages = { page: '#efe3bd', pageShade: '#cdb98a' };
const palettes = {
  fire_3: { cover: '#7a1f1a', edge: '#3f0f0c', frame: '#a87a3c', emblemBg: '#561510', leaf: '#c99a4a', ...pages, ribbon: '#c23a22', leaves: 3 },
  wind_3: { cover: '#2e6a4e', edge: '#163626', frame: '#a87a3c', emblemBg: '#1f4a36', leaf: '#c99a4a', ...pages, ribbon: '#4fae7a', leaves: 3 },
  fire_4: { cover: '#7a1f1a', edge: '#3f0f0c', frame: '#e8c049', emblemBg: '#561510', leaf: '#ffe27a', ...goldPages, ribbon: '#e8c049', leaves: 4 },
  wind_4: { cover: '#2e6a4e', edge: '#163626', frame: '#e8c049', emblemBg: '#1f4a36', leaf: '#ffe27a', ...goldPages, ribbon: '#e8c049', leaves: 4 },
  anti_5: { cover: '#1d1b1b', edge: '#0b0a0a', frame: '#48403b', emblemBg: '#121010', leaf: '#5a2424', page: '#cfc3a0', pageShade: '#a8996f', ribbon: '#5a1414', leaves: 5, wear: 0.3, scratch: 14 }
};

module.exports = {
  name: 'grimoire', group: 'grimoire', kind: 'entity', spec,
  textures: Object.fromEntries(Object.entries(palettes).map(([k, p]) => ['grimoire_' + k, painter(p)]))
};
