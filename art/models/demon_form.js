// Anti-magic demon form: overlay on the player model. Bones prefixed s1_..s4_ belong to awakening stages
// (arm, half body, full form, union); the renderer shows every bone up to the current stage.
const K = load('./lib.js');

const armParts = [
  { name: 'right_forearm', from: [-8.75, 11.5, -2.75], to: [-3.25, 19, 2.75], mat: 'demon' },
  { name: 'right_upperarm', from: [-8.4, 19, -2.4], to: [-3.6, 24.4, 2.4], mat: 'demon' },
  { name: 'right_elbow_spike', from: [-8.75, 17, 2], to: [-7.25, 18.5, 5], origin: [-8, 18, 2.5], rotation: [25, 0, 0], mat: 'spike' },
  { name: 'right_shoulder_spike', from: [-8, 24, -1], to: [-6, 27, 1], origin: [-7, 24, 0], rotation: [0, 0, 20], mat: 'spike' },
  ...[0, 1, 2, 3].map(k => ({ name: 'right_claw_' + k, from: [-8.25 + k * 1.25, 9.5, -2.5], to: [-7.5 + k * 1.25, 11.5, -1.5], origin: [-8 + k * 1.25, 11.5, -2], rotation: [-20, 0, 0], mat: 'claw' }))
];
const toLeft = c => K.mirrorCube(c, 'right', 'left');

const horn = (side, s) => ({
  base: { name: `horn_${side}_base`, from: [s < 0 ? -4 : 2, 31.5, -1.5], to: [s < 0 ? -2 : 4, 34, 0.5], origin: [s * 3, 32, 0], rotation: [0, 0, -s * 15], mat: 'horn' },
  tip: { name: `horn_${side}_tip`, from: [s < 0 ? -4.5 : 3, 34, -1], to: [s < 0 ? -3 : 4.5, 37, 0], origin: [s * 3, 32, 0], rotation: [0, 0, -s * 25], mat: 'hornTip' }
});
const hr = horn('right', -1), hl = horn('left', 1);

const spec = [{ name: 'root', origin: [0, 0, 0], children: [
  { name: 'body', origin: [0, 24, 0], children: [
    { name: 's2_body_right', origin: [0, 24, 0], cubes: [
      { name: 'right_chest', from: [-4.4, 12, -2.4], to: [0, 24.4, 2.4], mat: 'demon' },
      { name: 'right_cloak', from: [-8, 6, 2.4], to: [0, 24, 3], paint: { south: 'cloak', north: 'cloak', all: 'demon' } }] },
    { name: 's3_body_left', origin: [0, 24, 0], cubes: [
      { name: 'left_chest', from: [0, 12, -2.4], to: [4.4, 24.4, 2.4], mat: 'demon' },
      { name: 'left_cloak', from: [0, 6, 2.4], to: [8, 24, 3], paint: { south: 'cloak', north: 'cloak', all: 'demon' } }] },
    { name: 's4_back_spikes', origin: [0, 24, 0], cubes: [0, 1, 2].map(k => (
      { name: 'back_spike_' + k, from: [-0.75, 21 - k * 4, 2.5], to: [0.75, 23 - k * 4, 5.5], origin: [0, 22 - k * 4, 3], rotation: [35, 0, 0], mat: 'spike' })) }] },
  { name: 'head', origin: [0, 24, 0], children: [
    { name: 's2_horn_right', origin: [-3, 32, 0], cubes: [hr.base, hr.tip] },
    { name: 's4_horn_left', origin: [3, 32, 0], cubes: [hl.base, hl.tip] },
    { name: 's4_mask', origin: [0, 24, 0], cubes: [
      { name: 'mask', from: [-4.5, 23.75, -4.5], to: [4.5, 32.25, 4.5], paint: { north: 'mask', all: 'demon' } },
      ...[-3, -1, 1, 3].map((x, k) => ({ name: 'mane_' + k, from: [x - 1, 31.5, -1 + k % 2], to: [x + 1, 35 - (k % 2), 3 + k % 2], origin: [x, 32, 2], rotation: [-30, 0, x * 4], mat: 'spike' }))] }] },
  { name: 'right_arm', origin: [-5, 22, 0], children: [{ name: 's1_arm_right', origin: [-5, 22, 0], cubes: armParts }] },
  { name: 'left_arm', origin: [5, 22, 0], children: [{ name: 's3_arm_left', origin: [5, 22, 0], cubes: armParts.map(toLeft) }] },
  { name: 'right_leg', origin: [-2, 12, 0], children: [{ name: 's3_leg_right', origin: [-2, 12, 0], cubes: [
    { name: 'right_leg_plate', from: [-4.4, -0.25, -2.4], to: [0.1, 12.25, 2.4], mat: 'demon' },
    { name: 'right_knee_spike', from: [-3, 6, -3.5], to: [-1, 8, -2.4], mat: 'spike' }] }] },
  { name: 'left_leg', origin: [2, 12, 0], children: [{ name: 's3_leg_left', origin: [2, 12, 0], cubes: [
    { name: 'left_leg_plate', from: [-0.1, -0.25, -2.4], to: [4.4, 12.25, 2.4], mat: 'demon' },
    { name: 'left_knee_spike', from: [1, 6, -3.5], to: [3, 8, -2.4], mat: 'spike' }] }] }] }];

const B = '#141117', R = '#c0141f', RD = '#7a0d14';

const painter = (ctx, r, m) => {
  const { u, v, w, h } = r, px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  if (m === 'mask') {
    const rnd = K.rng(7 + u);
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, K.shade(B, 1 + (rnd() - 0.5) * 0.18));
    // Slanted red eyes, cheek marks and forehead mark.
    [[2, 6], [3, 6], [4, 7], [5, 7], [6, 8], [13, 6], [12, 6], [11, 7], [10, 7], [9, 8], [3, 7], [4, 8], [5, 8], [12, 7], [11, 8], [10, 8]].forEach(([x, y]) => px(x, y, R));
    px(4, 8, '#ffe0e0'); px(11, 8, '#ffe0e0');
    for (let j = 10; j < 14; j++) { px(3, j, R); px(12, j, R); }
    px(4, 13, R); px(11, 13, R);
    for (let j = 1; j < 5; j++) { px(7, j, R); px(8, j, R); }
    return;
  }
  const rnd = K.rng(101 + u * 13 + v * 7), n = (c, a = 0.12) => K.shade(c, 1 + (rnd() - 0.5) * a);
  if (m === 'demon' || m === 'cloak') {
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
      if (m === 'cloak' && j > h - 6 && rnd() < (j - (h - 6)) / 6) continue; // tattered hem
      px(i, j, n(B, 0.18));
    }
    if (m === 'demon') {
      // Red veins: short random walks going down.
      const veins = Math.max(1, Math.round(w * h / 90));
      for (let k = 0; k < veins; k++) {
        let x = Math.floor(rnd() * w), y = Math.floor(rnd() * h);
        const len = 4 + Math.floor(rnd() * 10);
        for (let s = 0; s < len; s++) {
          px(x, y, s % 3 === 0 ? RD : R);
          x += rnd() < 0.5 ? -1 : 1;
          y += rnd() < 0.7 ? 1 : 0;
          if (x < 0 || x >= w || y >= h) break;
        }
      }
    }
    return;
  }
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    let c;
    if (m === 'spike') c = j < 2 ? n(R, 0.1) : n('#1c181c', 0.15);
    else if (m === 'claw') c = j >= h - 1 ? '#e0dcd4' : n('#1a1618', 0.1);
    else if (m === 'horn') c = n('#1c181c', 0.15);
    else if (m === 'hornTip') c = j < 2 ? n(R, 0.1) : n('#2a1418', 0.15);
    else c = '#ff00ff';
    px(i, j, c);
  }
};

module.exports = { name: 'demon_form', group: 'awakening', kind: 'entity', spec, textures: { demon_form: painter } };
