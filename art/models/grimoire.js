// Floating grimoire. Cover variants per element and clover leaves (design: Folhas do trevo).
const K = load('./lib.js');

// Clover drawn as a shape on the emblem face: heart-shaped leaves around the centre and a stem.
// Leaf count sets the layout: 3 (top, then sides), 4 (diagonals), 5 (evenly from the top).
const CLOVER = {
  3: { start: -90, len: 7.4, r: 3.4 },
  4: { start: -45, len: 7.6, r: 3 },
  5: { start: -90, len: 8, r: 2.5 }
};

const leafAxes = n => [...Array(n)].map((_, k) => {
  const a = (CLOVER[n].start + k * 360 / n) * Math.PI / 180;
  return [Math.cos(a), Math.sin(a)];
});

const cloverMask = (n, cx, cy) => {
  const { len, r } = CLOVER[n], off = r * 0.75;
  const leaves = leafAxes(n).map(([dx, dy]) => {
    const mid = [cx + dx * len, cy + dy * len], tip = [cx, cy], notch = [cx + dx * (len + r * 0.95), cy + dy * (len + r * 0.95)];
    const lobes = [-1, 1].map(sd => [mid[0] - dy * off * sd, mid[1] + dx * off * sd]);
    const base = [-1, 1].map(sd => [mid[0] - dy * (off + r * 0.7) * sd - dx * r * 0.5, mid[1] + dx * (off + r * 0.7) * sd - dy * r * 0.5]);
    return { lobes, tip, base, notch };
  });
  const side = (a, b, p) => (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0]);
  const inTri = (p, a, b, c) => { const s1 = side(a, b, p), s2 = side(b, c, p), s3 = side(c, a, p); return (s1 >= 0 && s2 >= 0 && s3 >= 0) || (s1 <= 0 && s2 <= 0 && s3 <= 0); };
  // Returns the leaf index under (x, y), n for the hub where the leaves meet, or -1.
  return (x, y) => {
    const p = [x + 0.5, y + 0.5];
    if ((p[0] - cx) ** 2 + (p[1] - cy) ** 2 <= 2.6) return n;
    for (let k = 0; k < n; k++) {
      const { lobes, tip, base, notch } = leaves[k];
      if ((p[0] - notch[0]) ** 2 + (p[1] - notch[1]) ** 2 <= (r * 0.5) ** 2) continue;
      if (lobes.some(([lx, ly]) => (p[0] - lx) ** 2 + (p[1] - ly) ** 2 <= r * r)) return k;
      if (inTri(p, tip, base[0], base[1]) || inTri(p, base[0], base[1], lobes[0]) || inTri(p, base[1], lobes[0], lobes[1])) return k;
    }
    return -1;
  };
};

const paintClover = (px, w, h, P) => {
  const n = P.leaves, cx = w / 2, cy = h / 2 - 2, leafAt = cloverMask(n, cx, cy);
  const { len } = CLOVER[n];
  // Stem: curves from the centre down to the lower right, under the leaves.
  for (let t = 0; t <= 1; t += 0.02) {
    const x = Math.round(cx + 1 + t * 5 + Math.sin(t * Math.PI) * 1.5), y = Math.round(cy + 2 + t * 13);
    [[0, 0], [1, 0]].forEach(([ox, oy]) => { if (leafAt(x + ox, y + oy) < 0) px(x + ox, y + oy, ox ? P.leafDark : P.leaf); });
  }
  const axes = leafAxes(n);
  for (let x = 0; x < w; x++) for (let y = 0; y < h; y++) {
    const k = leafAt(x, y);
    if (k < 0) continue;
    const edge = [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([ox, oy]) => leafAt(x + ox, y + oy) !== k);
    if (k === n) { px(x, y, P.core || P.leafDark); continue; }
    const [dx, dy] = axes[k], rx = x + 0.5 - cx, ry = y + 0.5 - cy, along = rx * dx + ry * dy, across = -rx * dy + ry * dx;
    let c = P.leaf;
    if (edge) c = P.leafEdge;
    else if (Math.abs(across) < 0.5 && along > 2 && along < len) c = P.leafDark;
    else if (rx + ry < -(along * 0.6)) c = P.leafLight;
    px(x, y, c);
  }
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
    { name: 'emblem', from: [2, 5.5, 2], to: [6, 10.5, 2.5], paint: { south: 'emblem', all: 'metal' }, hide: ['north'], res: 4 },
    { name: 'pages_front', from: [0, 3, 0], to: [7.5, 13, 1], paint: { north: 'page', all: 'page_edge' } }] },
  { name: 'flip_page', origin: [0, 8, 0], cubes: [{ name: 'page', from: [0, 3, 0], to: [7.5, 13, 0], mat: 'page' }] },
  // Where an anti-magic sword comes out of the open pages (draw animation; the book lies flat then).
  { name: 'sword_anchor', origin: [2, 8, 0] }
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
    for (let i = 0; i < w; i++) { px(i, 0, P.frame); px(i, 1, P.frame); px(i, h - 2, P.frame); px(i, h - 1, P.frame); }
    for (let j = 0; j < h; j++) { px(0, j, P.frame); px(1, j, P.frame); px(w - 2, j, P.frame); px(w - 1, j, P.frame); }
    for (let i = 2; i < w - 2; i++) { px(i, 2, K.shade(P.emblemBg, 0.7)); px(i, h - 3, K.shade(P.emblemBg, 0.7)); }
    for (let j = 2; j < h - 2; j++) { px(2, j, K.shade(P.emblemBg, 0.7)); px(w - 3, j, K.shade(P.emblemBg, 0.7)); }
    paintClover(px, w, h, P);
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

const leaf = c => ({ leaf: c, leafEdge: K.shade(c, 0.55), leafDark: K.shade(c, 0.75), leafLight: K.shade(c, 1.2) });
const pages = { page: '#e9dcb6', pageShade: '#c8b78c' };
const goldPages = { page: '#efe3bd', pageShade: '#cdb98a' };
const palettes = {
  fire_3: { cover: '#7a1f1a', edge: '#3f0f0c', frame: '#a87a3c', emblemBg: '#561510', ...leaf('#c99a4a'), ...pages, ribbon: '#c23a22', leaves: 3 },
  wind_3: { cover: '#2e6a4e', edge: '#163626', frame: '#a87a3c', emblemBg: '#1f4a36', ...leaf('#c99a4a'), ...pages, ribbon: '#4fae7a', leaves: 3 },
  fire_4: { cover: '#7a1f1a', edge: '#3f0f0c', frame: '#e8c049', emblemBg: '#561510', ...leaf('#ffe27a'), ...goldPages, ribbon: '#e8c049', leaves: 4 },
  wind_4: { cover: '#2e6a4e', edge: '#163626', frame: '#e8c049', emblemBg: '#1f4a36', ...leaf('#ffe27a'), ...goldPages, ribbon: '#e8c049', leaves: 4 },
  anti_5: { cover: '#1d1b1b', edge: '#0b0a0a', frame: '#48403b', emblemBg: '#3a1212', leaf: '#141111', leafEdge: '#a3261c', leafDark: '#4a1512', leafLight: '#2e2828', core: '#c0302a', page: '#cfc3a0', pageShade: '#a8996f', ribbon: '#5a1414', leaves: 5, wear: 0.3, scratch: 14 }
};

// float runs on root in its own controller; the others move book and its pages.
// open holds the open pose; flip turns one page while open; draw brings out an anti-magic sword;
// choose is the ceremony descent.
const OPEN = 75;
// The grimoire entity floats at the player's left, away from the sword hand (FLOAT_AT, model units from
// the player's feet, facing the player's yaw; +x is the player's left, -z forward) and the code keeps it
// there. draw moves the book by DRAW_AT, across to just in front of the right hand.
const FLOAT_AT = [12, 14, -3];
const DRAW_AT = [-18, -8, -7];
// At the altar the code flies the grimoire to ALTAR_LIFT above the altar's book_anchor, so the closed, upright
// book (ribbon included) clears the lectern; the altar animation lowers it while it lies down and lifts it before it stands up.
const ALTAR_LIFT = 6;
// flip_page turns around the middle of the spine, but the page blocks turn with their covers
// (pivots at z -1.5 and 1.5). So at angle a the page also moves to lie on the block it rests on,
// a hair above it: back block for a > 0, front block for a < 0.
const page = a => {
  const r = a * Math.PI / 180, pz = a >= 0 ? -1.5 : 1.5, n = Math.sign(a) || 1, e = 0.05 * Math.abs(a) / OPEN;
  const round = v => Math.round(v * 1000) / 1000 || 0;
  return { rotation: [0, round(a), 0], position: [round(-pz * Math.sin(r) + e * n * Math.sin(r)), 0, round(pz - pz * Math.cos(r) + e * n * Math.cos(r))] };
};
// Samples each phase [length, k => {bone: angle}] for k in 0..1 with smoothstep, so covers and page stay
// in step without clipping. flip_page also moves (page); book tilts on z; the rest turn on y.
const STEPS = 6;
// travel k (0..1) moves book from where it floats to DRAW_AT; it shares book with the tilt, on position.
// lower a (0..1) drops book by ALTAR_LIFT, from the hover point down onto the lectern; also on position.
const pose = (bone, a) => bone === 'flip_page' ? page(a) : bone === 'travel' ? { position: DRAW_AT.map(v => Math.round(v * a * 1000) / 1000) }
  : bone === 'lower' ? { position: [0, Math.round(-ALTAR_LIFT * a * 1000) / 1000, 0] }
  : { rotation: bone === 'book' ? [0, 0, a] : [0, a, 0] };
const channelBone = bone => bone === 'travel' || bone === 'lower' ? 'book' : bone;
const phases = list => {
  const bones = {};
  let t0 = 0;
  list.forEach(([length, angles]) => {
    for (let i = 0; i <= STEPS; i++) {
      const s = i / STEPS, t = Math.round((t0 + length * s) * 1000) / 1000, k = s * s * (3 - 2 * s);
      Object.entries(angles(k)).forEach(([bone, a]) => Object.entries(pose(bone, a)).forEach(([ch, v]) => {
        const keys = ((bones[channelBone(bone)] ||= {})[ch] ||= []);
        if (!keys.length || keys[keys.length - 1][0] !== t) keys.push([t, v]);
      }));
    }
    t0 += length;
  });
  return bones;
};
const sampled = (length, angles) => phases([[length, angles]]);
const covers = k => ({ back_cover: OPEN * k, front_cover: -OPEN * k, flip_page: OPEN * k });
const opened = Object.fromEntries(Object.entries(covers(1)).map(([b, a]) => [b, pose(b, a)]));
const swing = (from, to) => sampled(0.5, k => covers(from + (to - from) * k));
const animations = {
  float: { loop: 'loop', length: 3, bones: { root: {
    position: [0, 'math.sin(query.anim_time * 120) * 0.6', 0],
    rotation: ['math.sin(query.anim_time * 120 + 90) * 2', 0, 'math.sin(query.anim_time * 60) * 3']
  } } },
  open: { loop: 'hold', length: 0.5, bones: swing(0, 1) },
  close: { loop: 'once', length: 0.5, bones: swing(1, 0) },
  flip: { loop: 'once', length: 0.6, bones: { ...opened, ...sampled(0.6, k => ({ flip_page: OPEN - 2 * OPEN * k })) } },
  // Anti-magic: from where it floats at the player's left, the book flies in front of the right hand, lies flat
  // with the pages up and opens; pages riffle while the sword comes out at sword_anchor (sword summon starts
  // at 0.35 s and is grabbed around 1.05 s); then it closes, stands up and flies back.
  draw: { loop: 'once', length: 1.5, bones: phases([
    [0.35, k => ({ ...covers(k), book: 90 * k, travel: k })],
    [0.4, k => ({ flip_page: OPEN - 2 * OPEN * k })],
    [0.3, () => ({})],
    [0.45, k => ({ back_cover: OPEN * (1 - k), front_cover: -OPEN * (1 - k), flip_page: -OPEN * (1 - k), book: 90 * (1 - k), travel: 1 - k })]
  ]) },
  // At the altar (synced with the altar's activate, 1.5 s): the code has flown the grimoire to ALTAR_LIFT above
  // the altar's book_anchor, turned to the reader and tilted like the lectern; it lies down onto the lectern
  // while opening, riffles forward, back and forward, then rises, closes and stands up for the flight back.
  altar: { loop: 'once', length: 1.5, bones: phases([
    [0.3, k => ({ ...covers(k), book: 90 * k, lower: k })],
    [0.3, k => ({ flip_page: OPEN - 2 * OPEN * k })],
    [0.3, k => ({ flip_page: -OPEN + 2 * OPEN * k })],
    [0.3, k => ({ flip_page: OPEN - 2 * OPEN * k })],
    [0.3, k => ({ back_cover: OPEN * (1 - k), front_cover: -OPEN * (1 - k), flip_page: -OPEN * (1 - k), book: 90 * (1 - k), lower: 1 - k })]
  ]) },
  choose: { loop: 'once', length: 2, bones: { book: {
    position: [[0, [0, 12, 0], 'catmullrom'], [1.6, [0, -0.6, 0], 'catmullrom'], [2, [0, 0, 0], 'catmullrom']],
    rotation: [[0, [0, 720, 0], 'catmullrom'], [1.6, [0, 0, 0], 'catmullrom']]
  } } }
};

module.exports = {
  name: 'grimoire', group: 'grimoire', kind: 'entity', spec, animations, FLOAT_AT, ALTAR_LIFT,
  textures: Object.fromEntries(Object.entries(palettes).map(([k, p]) => ['grimoire_' + k, painter(p)]))
};
