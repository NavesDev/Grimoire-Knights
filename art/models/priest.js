// Village priest: old clergyman of the Clover Kingdom church. Black cassock with white collar, purple stole with gold
// three-leaf clovers, clover pendant, grey hair around a bald crown and a short beard.
const K = load('./lib.js');
const M = load('./order_mage.js');

const arm = side => {
  const s = side === 'right' ? -1 : 1, x0 = s < 0 ? -8 : 4, x1 = s < 0 ? -4 : 8;
  return { name: side + '_arm', origin: [s * 5, 22, 0], cubes: [
    { name: side + '_hand_arm', from: [x0, 12, -2], to: [x1, 24, 2], mat: 'arm' },
    { name: side + '_sleeve', from: [x0 - 0.25, 14.5, -2.25], to: [x1 + 0.25, 24.25, 2.25], mat: 'sleeve' }] };
};

const spec = [{ name: 'root', origin: [0, 0, 0], children: [
  { name: 'body', origin: [0, 24, 0], cubes: [
    { name: 'torso', from: [-4, 12, -2], to: [4, 24, 2], paint: { north: 'torso_front', all: 'cassock' } },
    { name: 'collar', from: [-3, 23, -2.5], to: [3, 24.5, 2.5], paint: { north: 'collar_front', all: 'collar' } },
    { name: 'stole_right', from: [-3.5, 11, -2.5], to: [-1.5, 23, -2], mat: 'stole' },
    { name: 'stole_left', from: [1.5, 11, -2.5], to: [3.5, 23, -2], mat: 'stole' },
    { name: 'sash', from: [-4.25, 13, -2.25], to: [4.25, 14.5, 2.25], mat: 'sash' }
  ], children: [
    { name: 'pendant', origin: [0, 19, -2.5], cubes: [
      { name: 'pendant_chain', from: [-0.25, 19.5, -2.6], to: [0.25, 23, -2.4], mat: 'gold' },
      { name: 'pendant_clover', from: [-1.5, 17, -2.75], to: [1.5, 20, -2.25], paint: { north: 'clover', all: 'gold' } }] },
    { name: 'head', origin: [0, 24, 0], cubes: [
      { name: 'face', from: [-4, 24, -4], to: [4, 32, 4], paint: { north: 'face', up: 'scalp', south: 'hair_back', all: 'face_side' } },
      { name: 'beard', from: [-2.5, 23.5, -4.5], to: [2.5, 25.5, -3.75], mat: 'beard' },
      { name: 'hair_right', from: [-4.5, 27, -3], to: [-3.75, 30.5, 4.25], mat: 'hair' },
      { name: 'hair_left', from: [3.75, 27, -3], to: [4.5, 30.5, 4.25], mat: 'hair' },
      { name: 'hair_back', from: [-4.5, 26.5, 3.75], to: [4.5, 30.5, 4.5], mat: 'hair' }] },
    arm('right'), arm('left')] },
  ...M.lowerBody(3)] }];

// Three-leaf clover, the church's sign.
const clover = (px, w, h, c, dark) => {
  const cx = (w - 1) / 2, cy = (h - 1) / 2;
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    const leaf = [[0, -1], [-1, 0.4], [1, 0.4]].some(([dx, dy]) => Math.hypot(i - cx - dx * w / 4, j - cy - dy * h / 4) <= w / 4);
    if (leaf) px(i, j, c);
    else if (j > cy + h / 4 && Math.abs(i - cx) < 0.6) px(i, j, dark);
  }
};

const painter = P => (ctx, r, m) => {
  const { u, v, w, h } = r, px = (x, y, c) => K.px(ctx, u + x, v + y, c), fill = (c, a) => K.noiseFill(ctx, r, c, a, 7);
  const hline = (y, c) => { for (let i = 0; i < w; i++) px(i, y, c); };
  const vline = (x, c, y0 = 0, y1 = h) => { for (let j = y0; j < y1; j++) px(x, j, c); };
  switch (m) {
    case 'cassock': case 'sleeve': fill(P.cassock, 0.06); break;
    case 'torso_front': fill(P.cassock, 0.06); for (let j = 4; j < h; j += 3) { px(7, j, P.button); px(8, j, P.button); } break;
    case 'collar': fill(P.cassock, 0.06); hline(h - 1, P.white); break;
    case 'collar_front': fill(P.cassock, 0.06); for (let i = 4; i < w - 4; i++) for (let j = 0; j < h; j++) px(i, j, P.white); break;
    case 'stole': fill(P.stole, 0.08); vline(0, P.gold); vline(w - 1, P.gold); hline(h - 1, P.gold);
      // small clover near the end
      [[1, h - 7], [2, h - 7], [0, h - 6], [1, h - 6], [2, h - 6], [3, h - 6], [1, h - 5], [2, h - 5], [1, h - 4]].forEach(([x, y]) => px(x, y, P.gold)); break;
    case 'sash': fill(P.stole, 0.08); hline(0, P.gold); hline(h - 1, P.gold); break;
    case 'gold': fill(P.gold, 0.1); break;
    case 'clover': fill(P.gold, 0.05); clover(px, w, h, P.green, K.shade(P.gold, 0.6)); break;
    case 'face': {
      for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, K.shade(P.skin, 1 + ((i * 5 + j * 3) % 5 - 2) * 0.015));
      // grey brows, kind eyes, wrinkles
      [3, 4, 5, 10, 11, 12].forEach(x => px(x, 6, P.hair));
      [[4, 8], [11, 8]].forEach(([x, y]) => { px(x, y, P.eye); px(x + 1, y, P.white); });
      [[3, 9], [12, 9], [6, 11], [9, 11]].forEach(([x, y]) => px(x, y, K.shade(P.skin, 0.85)));
      for (let x = 7; x < 9; x++) { px(x, 9, K.shade(P.skin, 0.9)); px(x, 10, K.shade(P.skin, 0.8)); }
      for (let x = 5; x < 11; x++) px(x, 12, P.hair);
      break;
    }
    case 'face_side': for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, K.shade(P.skin, 0.95)); break;
    case 'scalp': for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, (i < 2 || i >= w - 2 || j >= h - 2) ? P.hair : K.shade(P.skin, 1.05)); break;
    case 'hair': case 'hair_back': fill(P.hair, 0.1); break;
    case 'beard': fill(P.hair, 0.12); for (let i = 3; i < w - 3; i++) px(i, 0, K.shade(P.skin, 0.7)); break;
    case 'arm': for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, j >= h - 5 ? K.shade(P.skin, 1 + ((i * 7 + j * 3) % 5 - 2) * 0.02) : P.cassock); break;
    case 'skirt': fill(P.cassock, 0.06); hline(h - 1, P.stole); break;
    case 'skirt_front': fill(P.cassock, 0.06); hline(h - 1, P.stole); vline(Math.floor(w / 2) - 1, K.shade(P.cassock, 0.7)); break;
    case 'leg': for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, j >= h - 4 ? P.shoe : P.cassock); break;
    default: fill('#ff00ff', 0);
  }
};

const sin = M.sin;
const animations = {
  ...M.humanoid(),
  // Right-click: raises the right hand and traces a blessing, then rests it on the pendant.
  bless: { loop: 'once', length: 1.4, bones: {
    right_arm: { rotation: [[0, [0, 0, 0]], [0.3, [110, 0, -10], 'catmullrom'], [0.55, [110, 0, 10], 'catmullrom'], [0.8, [125, 0, 0], 'catmullrom'], [1.0, [95, 0, 0], 'catmullrom'], [1.4, [0, 0, 0], 'catmullrom']] },
    head: { rotation: [[0, [0, 0, 0]], [0.3, [-8, 0, 0], 'catmullrom'], [1.0, [10, 0, 0], 'catmullrom'], [1.4, [0, 0, 0], 'catmullrom']] },
    body: { rotation: [[0, [0, 0, 0]], [1.0, [5, 0, 0], 'catmullrom'], [1.4, [0, 0, 0], 'catmullrom']] } } },
};
animations.idle.bones.pendant = { rotation: [sin(90, 4, 30), 0, 0] };

const palette = { cassock: '#1b1a20', white: '#ecebe4', button: '#3a3844', stole: '#5b2a7a', gold: '#d4b04c', green: '#3f9a4a',
  skin: '#d9ad8a', hair: '#b9b6b0', eye: '#3a2a20', shoe: '#2b2018' };

module.exports = { name: 'priest', group: 'npc', kind: 'entity', spec, animations, textures: { priest: painter(palette) } };
