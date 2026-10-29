// Anti-magic demon form: overlay on the player model. Bones prefixed s1_..s4_ belong to awakening stages
// (arm, half body, full form, union); the renderer shows every bone up to the current stage.
// Stages 1-3 are plain black anti-magic: 1 the right arm with tendrils over the chest, 2 the right horn
// and a small right wing, 3 the left arm with its own tendrils and both full wings (torso and legs still bare). Stage 4 (union)
// covers torso and legs and adds the left horn, headband, red marks (chest, arms front and back, shins),
// pauldrons, claws and tail. The face stays visible.
const K = load('./lib.js');

// Box on one side of the body: x1..x2 are distances from the center, s is -1 (right) or 1 (left).
const side = (s, name, x1, x2, y1, y2, z1, z2, extra) => ({
  name, from: [Math.min(s * x1, s * x2), y1, z1], to: [Math.max(s * x1, s * x2), y2, z2], ...extra
});
const sideName = s => (s < 0 ? 'right' : 'left');
// Thin shell over a box carrying a red union mark on the given faces (transparent elsewhere).
const marks = (name, from, to, faces) => ({ name, from: from.map(v => v - 0.06), to: to.map(v => v + 0.06),
  paint: Object.fromEntries(faces.map(f => [f, 'thorns'])), hide: K.FACES.filter(f => !faces.includes(f)) });

const arm = { name: 'right_arm_plate', from: [-8.25, 11.75, -2.25], to: [-3.75, 24.25, 2.25], mat: 'black' };
const armUnion = [
  marks('right_arm_marks', arm.from, arm.to, ['north', 'south']),
  // Pauldron with a raised red four-point star on the outer side: two bars and a diamond in the middle.
  { name: 'right_pauldron', from: [-8.75, 21, -2.75], to: [-3.5, 25, 2.75], mat: 'black' },
  { name: 'right_star_v', from: [-9.1, 21.25, -0.3], to: [-8.75, 24.75, 0.3], mat: 'relief' },
  { name: 'right_star_h', from: [-9.1, 22.7, -2.5], to: [-8.75, 23.3, 2.5], mat: 'relief' },
  { name: 'right_star_core', from: [-9.25, 22.35, -0.65], to: [-8.75, 23.65, 0.65], origin: [-9, 23, 0], rotation: [45, 0, 0], mat: 'relief' },
  { name: 'right_elbow_spike', from: [-8.75, 17, 2], to: [-7.25, 18.5, 5], origin: [-8, 18, 2.5], rotation: [25, 0, 0], mat: 'spike' },
  ...[0, 1, 2, 3].map(k => ({ name: 'right_claw_' + k, from: [-8.25 + k * 1.25, 9.75, -2.5], to: [-7.5 + k * 1.25, 11.75, -1.5], origin: [-8 + k * 1.25, 11.75, -2], rotation: [-20, 0, 0], mat: 'claw' }))
];
const toLeft = c => K.mirrorCube(c, 'right', 'left');

// Ram horn curling back and down: a chain of bones laid straight up at rest, each one bent from the last.
const horn = (s, bone) => {
  const seg = (k, x1, x2, y1, y2, z1, z2, mat) => side(s, `horn_${sideName(s)}_${k}`, x1, x2, y1, y2, z1, z2, { mat });
  return { name: bone, origin: [s * 4.5, 29.5, 0.5], rotation: [0, 0, -s * 30], cubes: [seg(0, 3.8, 5.4, 29.5, 32.5, -0.4, 1.4, 'horn')], children: [
    { name: `horn_${sideName(s)}_mid`, origin: [s * 4.6, 32.5, 0.5], rotation: [55, 0, 0], cubes: [seg(1, 3.95, 5.25, 32.5, 35, -0.25, 1.25, 'horn')], children: [
      { name: `horn_${sideName(s)}_end`, origin: [s * 4.6, 35, 0.5], rotation: [60, 0, 0], cubes: [seg(2, 4.1, 5.1, 35, 37, -0.1, 1.1, 'horn')], children: [
        { name: `horn_${sideName(s)}_tip`, origin: [s * 4.6, 37, 0.5], rotation: [60, 0, -s * 10], cubes: [seg(3, 4.2, 5, 37, 38.75, 0.1, 0.9, 'hornTip')] }] }] }] };
};

// Wings are flat planes whose silhouette is cut out of the texture (see wingShape). The shape is
// defined in rest space, so the inner and outer planes join; the outer plane folds back on a child bone.
// k scales the whole wing around its root (stage 2 grows a small one, hidden inside the full wing later).
const WING = { a0: 1.5, a1: 26, y0: 6, y1: 46, fold: 13, pivotY: 25 };
const wingPlane = (s, name, a1, a2, y1, y2, z, k) =>
  side(s, name, a1, a2, y1, y2, z, z, { paint: { north: 'wing', south: 'wing' }, wing: { k }, res: 2 });
const wing = (s, bone, k = 1, z = 3) => {
  const sc = (a, y) => [WING.a0 + (a - WING.a0) * k, WING.pivotY + (y - WING.pivotY) * k];
  const [a0, y0] = sc(WING.a0, WING.y0), [af, y1] = sc(WING.fold, WING.y1), [a1] = sc(WING.a1, 0);
  return { name: bone, origin: [s * 1.5, 25, z], rotation: [15, -s * 15, s * 5], cubes: [
    wingPlane(s, `${bone}_inner`, a0, af, y0, y1, z, k)], children: [
    { name: `${bone}_outer`, origin: [s * af, 25, z], rotation: [0, -s * 15, 0], cubes: [wingPlane(s, `${bone}_tip`, af, a1, y0, y1, z, k)] }] };
};

// Tail from the lower back, laid straight down at rest and bent back by each bone.
const tail = { name: 's4_tail', origin: [0, 13, 3], rotation: [-50, 0, 0], cubes: [
  { name: 'tail_0', from: [-0.6, 7, 2.4], to: [0.6, 13, 3.6], mat: 'tail' }], children: [
  { name: 'tail_mid', origin: [0, 7, 3], rotation: [-25, 0, 0], cubes: [{ name: 'tail_1', from: [-0.5, 1, 2.5], to: [0.5, 7, 3.5], mat: 'tail' }], children: [
    { name: 'tail_end', origin: [0, 1, 3], rotation: [30, 0, 15], cubes: [
      { name: 'tail_2', from: [-0.4, -4, 2.6], to: [0.4, 1, 3.4], mat: 'tail' },
      { name: 'tail_spade', from: [-2, -8, 2.75], to: [2, -4, 3.25], paint: { north: 'spade', south: 'spade' }, hide: ['east', 'west', 'up', 'down'] }] }] }] };

const chestRight = { name: 'right_chest', from: [-4.4, 12, -2.4], to: [0, 24.4, 2.4], mat: 'black' };
const chestLeft = { name: 'left_chest', from: [0, 12, -2.4], to: [4.4, 24.4, 2.4], mat: 'black' };
const legRight = { name: 'right_leg_plate', from: [-4.4, -0.25, -2.4], to: [0.1, 12.25, 2.4], mat: 'black' };
const legLeft = { name: 'left_leg_plate', from: [-0.1, -0.25, -2.4], to: [4.4, 12.25, 2.4], mat: 'black' };

const spec = [{ name: 'root', origin: [0, 0, 0], children: [
  { name: 'body', origin: [0, 24, 0], children: [
    { name: 's1_tendrils', origin: [0, 24, 0], cubes: [
      { name: 'tendrils_front', from: [-4.5, 14, -2.5], to: [-0.5, 24.5, -2.5], paint: { north: 'tendril' }, hide: ['south'] },
      { name: 'tendrils_back', from: [-4.5, 14, 2.5], to: [-0.5, 24.5, 2.5], paint: { south: 'tendril' }, hide: ['north'] }] },
    wing(-1, 's2_wing_small', 0.45, 3.1),
    { name: 's3_tendrils', origin: [0, 24, 0], cubes: [
      { name: 'tendrils_left_front', from: [0.5, 14, -2.5], to: [4.5, 24.5, -2.5], paint: { north: 'tendrilLeft' }, hide: ['south'] },
      { name: 'tendrils_left_back', from: [0.5, 14, 2.5], to: [4.5, 24.5, 2.5], paint: { south: 'tendrilLeft' }, hide: ['north'] }] },
    wing(1, 's3_wing_left'),
    wing(-1, 's3_wing_right'),
    { name: 's4_union', origin: [0, 24, 0], cubes: [chestRight, chestLeft,
      { name: 'chest_mark', from: [-4.5, 12.5, -2.6], to: [4.5, 24.5, -2.5], paint: { north: 'emblem' }, hide: ['east', 'west', 'up', 'down', 'south'], res: 3 }],
      children: [tail] }] },
  { name: 'head', origin: [0, 24, 0], children: [
    horn(-1, 's2_horn_right'),
    horn(1, 's4_horn_left'),
    { name: 's4_crown', origin: [0, 24, 0], cubes: [
      // Black headband round the head; on the front the red thorned mark sits inside it, outlined in black
      // (shape cut out of the texture).
      { name: 'headband', from: [-4.4, 28.25, -4.4], to: [4.4, 32.25, -4.4], paint: { north: 'headbandFront' }, hide: ['south'], res: 3 },
      { name: 'headband_ring', from: [-4.35, 28.85, -4.3], to: [4.35, 30.65, 4.35], mat: 'headband', hide: ['north', 'up', 'down'] },
      { name: 'face_marks', from: [-4, 24, -4.3], to: [4, 32, -4.3], paint: { north: 'face' }, hide: ['south'] }] }] },
  { name: 'right_arm', origin: [-5, 22, 0], children: [{ name: 's1_arm_right', origin: [-5, 22, 0], cubes: [arm],
    children: [{ name: 's4_arm_right_union', origin: [-5, 22, 0], cubes: armUnion }] }] },
  { name: 'left_arm', origin: [5, 22, 0], children: [{ name: 's3_arm_left', origin: [5, 22, 0],
    cubes: [toLeft(arm)],
    children: [{ name: 's4_arm_left_union', origin: [5, 22, 0], cubes: armUnion.map(toLeft) }] }] },
  { name: 'right_leg', origin: [-2, 12, 0], children: [{ name: 's4_leg_right', origin: [-2, 12, 0],
    cubes: [legRight, marks('right_shin_marks', legRight.from, [legRight.to[0], 6, legRight.to[2]], ['north'])] }] },
  { name: 'left_leg', origin: [2, 12, 0], children: [{ name: 's4_leg_left', origin: [2, 12, 0],
    cubes: [legLeft, marks('left_shin_marks', legLeft.from, [legLeft.to[0], 6, legLeft.to[2]], ['north'])] }] }] }];

const B = '#09080b', R = '#d0101c';

const inPoly = (x, y, pts) => {
  let inside = false;
  for (let a = 0, b = pts.length - 1; a < pts.length; b = a++) {
    const [xa, ya] = pts[a], [xb, yb] = pts[b];
    if ((ya > y) !== (yb > y) && x < (xb - xa) * (y - ya) / (yb - ya) + xa) inside = !inside;
  }
  return inside;
};


// Deterministic hash of a point, 0..1.
const hash = (x, y = 0) => { const t = Math.sin(x * 127.1 + y * 311.7) * 43758.5453; return t - Math.floor(t); };

// Anti-magic wing silhouette in rest space: a = distance from the spine, y = height. A torn black
// flame: leading edge rises to a sharp tip; trailing edge breaks into tongues of uneven length.
function wingShape(a, y) {
  const t = (a - WING.a0) / (WING.a1 - WING.a0);
  if (t < 0 || t > 1) return false;
  const top = 29 + 16 * Math.pow(t, 1.1) - (t > 0.88 ? 6 * Math.pow((t - 0.88) / 0.12, 2) : 0) + (hash(Math.floor(a * 4), 3) - 0.5) * 0.5;
  let bottom = 13 + 30 * Math.pow(t, 1.7) + (hash(Math.floor(a * 4)) - 0.5) * 0.8;
  for (let k = 0; k < 10; k++) {
    const p = (k + 0.3 + hash(k, 7) * 0.5) / 10, len = 2 + hash(k, 9) * 6 * (1 - 0.5 * t), wd = 0.025 + hash(k, 11) * 0.03;
    bottom -= len * Math.max(0, 1 - Math.abs(t - p) / wd);
  }
  if (y > top || y < bottom) return false;
  return !(y < bottom + 3 && hash(Math.floor(a * 3), Math.floor(y * 3)) < 0.05);
}

// Red mark of the union headband (x -5..5, y 27.5..31.5): band tapering to points at the sides,
// a diamond in the middle, thorns up and down along the band.
function tiaraShape(x, y) {
  const ax = Math.abs(x);
  if (ax > 5) return false;
  if (Math.abs(y - 29) < 0.15 + 0.5 * (1 - ax / 5.2)) return true;
  if (ax * 1.1 + Math.abs(y - 29.3) * 0.75 < 1.3) return true;
  if (y > 29 && (Math.abs(ax - 1.9) < (30.6 - y) * 0.45 || Math.abs(ax - 3.4) < (30.1 - y) * 0.45)) return true;
  return y < 29 && Math.abs(ax - 2.65) < (y - 27.9) * 0.4;
}

// Black anti-magic tendrils creeping from a shoulder over the chest, as 0.5-unit cells, laid out from the
// right shoulder; the left side reads them mirrored (its own seed so the sides differ).
const tendrils = seed => {
  const set = new Set(), rnd = K.rng(seed);
  const walk = (x, y, steps, width) => {
    for (let s = 0; s < steps; s++) {
      set.add(`${Math.floor(x * 2)},${Math.floor(y * 2)}`);
      if (width > 1 && s < steps / 2) set.add(`${Math.floor(x * 2)},${Math.floor(y * 2) + 1}`);
      if (rnd() < 0.18 && steps - s > 3) walk(x, y, Math.floor((steps - s) * 0.6), 1);
      const dy = rnd() < 0.45 ? -0.5 : rnd() < 0.5 ? 0.5 : 0;
      if (dy) set.add(`${Math.floor(x * 2)},${Math.floor((y + dy) * 2)}`); // keep the line connected
      x += 0.5; y += dy;
    }
  };
  [24, 22.5, 21, 19.5, 17].forEach((y, k) => walk(-4.5, y, 6 + Math.floor(rnd() * 6) + (k < 3 ? 3 : 0), 2));
  return set;
};
const TENDRILS = { tendril: tendrils(4242), tendrilLeft: tendrils(777) };

const painter = (ctx, r, m) => {
  const { u, v, w, h } = r, px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  const rnd = K.rng(101 + u * 13 + v * 7), n = (c, a = 0.12) => K.shade(c, 1 + (rnd() - 0.5) * a);
  const isSide = !['up', 'down'].includes(r.face);
  if (m === 'black') {
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) px(i, j, n(B, 0.1));
    return;
  }
  if (m === 'thorns') {
    // One red stroke down the middle with short spikes alternating to each side, tapered at both ends.
    const x = Math.floor(w / 2), y0 = 1, y1 = h - 2;
    for (let y = y0; y <= y1; y++) {
      px(x, y, n(R, 0.1));
      const k = y - y0;
      if (k > 1 && y < y1 - 1 && k % 4 === 2) {
        const d = (k / 4) % 2 < 1 ? -1 : 1;
        px(x + d, y, n(R, 0.1)); px(x + 2 * d, y - 1, n(R, 0.1));
      }
    }
    return;
  }
  if (m === 'wing' || m === 'tendril' || m === 'tendrilLeft' || m === 'headbandFront') {
    // Both read the shape in model space, so front and back faces and neighbour planes line up.
    const c = r.spec, sx = (c.to[0] - c.from[0]) / w, sy = (c.to[1] - c.from[1]) / h;
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
      const x = r.face === 'north' ? c.to[0] - (i + 0.5) * sx : c.from[0] + (i + 0.5) * sx, y = c.to[1] - (j + 0.5) * sy;
      if (TENDRILS[m]) { if (TENDRILS[m].has(`${Math.floor((m === 'tendrilLeft' ? -x : x) * 2)},${Math.floor(y * 2)}`)) px(i, j, n(B, 0.15)); continue; }
      if (m === 'headbandFront') {
        const red = (dx, dy) => tiaraShape((x + dx) * 5 / 4.4, y + dy - 0.75); // raised clear of the eyes
        if (red(0, 0)) px(i, j, n(y > 30.15 ? '#e8202a' : R, 0.12));
        else if (Math.abs(y - 29.75) < 0.9 || [[0.35, 0], [-0.35, 0], [0, 0.35], [0, -0.35], [0.25, 0.25], [-0.25, 0.25], [0.25, -0.25], [-0.25, -0.25]].some(([dx, dy]) => red(dx, dy))) px(i, j, n('#0b0a0c', 0.12));
        continue;
      }
      const k = c.wing.k;
      if (wingShape(WING.a0 + (Math.abs(x) - WING.a0) / k, WING.pivotY + (y - WING.pivotY) / k)) px(i, j, n(B, 0.1));
    }
    return;
  }
  if (m === 'emblem') {
    // Red devil mark: wings sweeping up to both shoulders with small side points, a <> diamond running
    // down to a point at the belly, black six-point star in the middle; rest transparent.
    const pts = [[0.5, 0.12], [0.62, 0.08], [0.95, 0], [0.82, 0.18], [0.72, 0.3], [0.86, 0.4], [0.64, 0.42], [0.6, 0.5], [0.5, 0.8],
      [0.4, 0.5], [0.36, 0.42], [0.14, 0.4], [0.28, 0.3], [0.18, 0.18], [0.05, 0], [0.38, 0.08]].map(([x, y]) => [x * w, y * h]);
    const cx = w / 2, cy = h * 0.3, rs = w * 0.07;
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
      if (!inPoly(i + 0.5, j + 0.5, pts)) continue;
      const dx = i + 0.5 - cx, dy = j + 0.5 - cy, d = Math.hypot(dx, dy), ang = Math.atan2(dy, dx);
      const star = d < rs && (d < 1 || Math.abs(Math.sin(3 * ang)) < 0.9 / Math.max(d, 1));
      px(i, j, star ? B : n(j < h * 0.12 ? '#e8202a' : R, 0.1));
    }
    return;
  }
  if (m === 'face') {
    // Black streaks down the cheeks from under the tiara, clear of the eyes; rest transparent.
    [[0, 7, 13], [1, 7, 11], [15, 7, 13], [14, 7, 11], [3, 10, 12], [12, 10, 12], [7, 7, 8], [8, 7, 8]].forEach(([x, y1, y2]) => {
      for (let y = y1; y <= y2; y++) px(x * w / 16, y * h / 16, n('#0c0a0c', 0.1));
    });
    return;
  }
  if (m === 'spade') {
    for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
      const half = j < h / 2 ? (j + 1) / (h / 2) : (h - j) / (h / 2);
      if (Math.abs(i + 0.5 - w / 2) <= half * w / 2) px(i, j, n(B, 0.15));
    }
    return;
  }
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    let c;
    if (m === 'spike') c = j < 2 ? n(R, 0.1) : n('#1c181c', 0.15);
    else if (m === 'claw') c = n(B, 0.1);
    else if (m === 'horn') c = j % 3 === 0 ? n('#2c282c', 0.1) : n('#121012', 0.15);
    else if (m === 'hornTip') c = n('#0a090a', 0.1);
    else if (m === 'headband') c = n('#0b0a0c', 0.12);
    else if (m === 'relief') c = n(r.face === 'up' ? '#ff3a40' : R, 0.1);
    else if (m === 'tail') c = n(B, 0.1);
    else c = '#ff00ff';
    px(i, j, c);
  }
};

module.exports = { name: 'demon_form', group: 'awakening', kind: 'entity', spec, textures: { demon_form: painter } };
