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

// Stage bones pivot where the anti-magic starts spreading from (see the awaken animation): arms from the
// hand, tendrils from their shoulder, the union from the shoulders down, legs from the hip.
const spec = [{ name: 'root', origin: [0, 0, 0], children: [
  { name: 'body', origin: [0, 24, 0], children: [
    { name: 's1_tendrils', origin: [-4.5, 24.5, 0], cubes: [
      { name: 'tendrils_front', from: [-4.5, 14, -2.5], to: [-0.5, 24.5, -2.5], paint: { north: 'tendril' }, hide: ['south'] },
      { name: 'tendrils_back', from: [-4.5, 14, 2.5], to: [-0.5, 24.5, 2.5], paint: { south: 'tendril' }, hide: ['north'] }] },
    wing(-1, 's2_wing_small', 0.45, 3.1),
    { name: 's3_tendrils', origin: [4.5, 24.5, 0], cubes: [
      { name: 'tendrils_left_front', from: [0.5, 14, -2.5], to: [4.5, 24.5, -2.5], paint: { north: 'tendrilLeft' }, hide: ['south'] },
      { name: 'tendrils_left_back', from: [0.5, 14, 2.5], to: [4.5, 24.5, 2.5], paint: { south: 'tendrilLeft' }, hide: ['north'] }] },
    wing(1, 's3_wing_left'),
    wing(-1, 's3_wing_right'),
    { name: 's4_union', origin: [0, 24.5, 0], cubes: [chestRight, chestLeft,
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
  { name: 'right_arm', origin: [-5, 22, 0], children: [{ name: 's1_arm_right', origin: [-6, 11.75, 0], cubes: [arm],
    children: [{ name: 's4_arm_right_union', origin: [-5, 25, 0], cubes: armUnion }] }] },
  { name: 'left_arm', origin: [5, 22, 0], children: [{ name: 's3_arm_left', origin: [6, 11.75, 0],
    cubes: [toLeft(arm)],
    children: [{ name: 's4_arm_left_union', origin: [5, 25, 0], cubes: armUnion.map(toLeft) }] }] },
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

// Body, head, arms and legs follow the player's pose in the renderer; these animate only the extras.
// Wings rise from the back: turning a wing on y swings its tip back (-y for the left wing, +y for the right).
// The player's own flight poses are in player_demon.js; each fly_<dir> here is the matching wing beat.
const sin = (speed, amp, phase = 0) => `math.sin(query.anim_time * ${speed}${phase ? ' + ' + phase : ''}) * ${amp}`;
const WINGS = [['s2_wing_small', -1], ['s3_wing_left', 1], ['s3_wing_right', -1]];
// base: degrees swept back; amp per side (left, right) so a turn can beat the outer wing harder.
const wings = (speed, base, ampL, ampR = ampL) => Object.assign({}, ...WINGS.map(([w, s]) => {
  const amp = s > 0 ? ampL : ampR;
  return { [w]: { rotation: [0, `${-s * base} + ${sin(speed, -s * amp)}`, 0] },
    [w + '_outer']: { rotation: [0, sin(speed, -s * amp * 0.8, -50), 0] } };
}));
const tailSway = (speed, lift, amp = 1) => ({
  s4_tail: { rotation: [`${lift} + ${sin(speed, 5 * amp)}`, sin(speed, 14 * amp), 0] },
  tail_mid: { rotation: [sin(speed, 8 * amp, -40), sin(speed, 18 * amp, -40), 0] },
  tail_end: { rotation: [sin(speed, 10 * amp, -80), sin(speed, 26 * amp, -80), 0] } });

// Anti-magic consuming the player: each stage bone grows from its pivot (see the spec), one after another.
// [bone, start, end, axes]: axes 'y' stretches only along y (black spreading over a limb or the chest),
// 'xy' spreads across, 'all' grows a part (horns, crown, tail segments). Times are within the stage.
const STAGES = {
  1: [['s1_arm_right', 0, 0.6, 'y'], ['s1_tendrils', 0.4, 1, 'xy']],
  2: [['s2_horn_right', 0, 0.25, 'all'], ['horn_right_mid', 0.15, 0.4, 'all'], ['horn_right_end', 0.3, 0.55, 'all'],
    ['horn_right_tip', 0.45, 0.65, 'all'], ['s2_wing_small', 0.2, 0.7, 'all']],
  3: [['s3_arm_left', 0, 0.6, 'y'], ['s3_tendrils', 0.4, 1, 'xy'], ['s3_wing_left', 0.3, 0.8, 'all'], ['s3_wing_right', 0.3, 0.8, 'all']],
  4: [['s4_arm_right_union', 0, 0.5, 'y'], ['s4_arm_left_union', 0, 0.5, 'y'], ['s4_union', 0.2, 0.8, 'y'],
    ['s4_leg_right', 0.5, 1, 'y'], ['s4_leg_left', 0.5, 1, 'y'], ['s4_crown', 0.6, 0.9, 'all'],
    ['s4_horn_left', 0.7, 0.95, 'all'], ['horn_left_mid', 0.85, 1.1, 'all'], ['horn_left_end', 1, 1.25, 'all'], ['horn_left_tip', 1.15, 1.35, 'all'],
    ['s4_tail', 0.8, 1.05, 'all'], ['tail_mid', 0.95, 1.2, 'all'], ['tail_end', 1.1, 1.35, 'all']]
};
// Wings unfold as they grow: the outer part opens after the inner one.
const UNFOLD = { s2_wing_small: -1, s3_wing_left: 1, s3_wing_right: -1 };
const PACE = 1.25; // slows the whole transformation so the spreading reads
const STAGE_START = { 1: 0, 2: 0.6 * PACE, 3: 1.1 * PACE, 4: 1.7 * PACE };
const r2 = n => Math.round(n * 100) / 100;
const dedupe = keys => keys.sort((p, q) => p[0] - q[0]).filter((k, i, all) => i === all.length - 1 || all[i + 1][0] !== k[0]);
// Growth comes in surges, like something alive taking over: it pushes, falls back a little, pushes again,
// overshoots and settles. Each piece gets its own small offsets (seeded by its name) so no two move alike,
// and a limb thickens while the black spreads over it.
const SURGES = [[0, 0], [0.18, 0.32], [0.3, 0.25], [0.52, 0.62], [0.6, 0.55], [0.82, 1.08], [0.92, 0.97], [1, 1]];
const seed = name => [...name].reduce((h, c) => (h * 31 + c.charCodeAt(0)) >>> 0, 7);
const grow = (axes, g) => {
  const thick = axes === 'y' ? 1 + 0.12 * Math.sin(Math.PI * Math.min(1, Math.max(0, g))) : 1;
  return axes === 'y' ? [thick, g, thick] : axes === 'xy' ? [g, g, 1] : [g, g, g];
};
// Keys for the given stages, each shifted by t0of(stage); reverse plays them backwards (receding).
const consume = (stages, t0of, reverse, length) => {
  const bones = {}, at = t => reverse ? r2(length - t) : r2(t), tiny = 0.01;
  stages.forEach(n => STAGES[n].forEach(([b, a, z, axes]) => {
    if (SPREAD[b]) return; // skin pieces spread in the texture instead (see spreadTextures)
    const t0 = t0of(n) + a * PACE, t1 = t0of(n) + z * PACE, rnd = K.rng(seed(b));
    // Fully hidden until it starts (a y-only squash would still show a flat sheet).
    const keys = [[0, [tiny, tiny, tiny]], [Math.max(0, t0 - 0.01), [tiny, tiny, tiny]]];
    SURGES.forEach(([f, g], i) => {
      const jf = i && i < SURGES.length - 1 ? (rnd() - 0.5) * 0.08 : 0, jg = i && i < SURGES.length - 1 ? (rnd() - 0.5) * 0.1 : 0;
      keys.push([t0 + (t1 - t0) * (f + jf), grow(axes, Math.max(tiny, g + jg))]);
    });
    keys.push([length, grow(axes, 1)]);
    bones[b] = { scale: dedupe(keys.map(([t, v]) => [at(t), v.map(r2), 'catmullrom'])) };
    // Wings burst open past their rest pose and settle back.
    if (UNFOLD[b]) {
      const s = UNFOLD[b], fold = [[0, 70], [t1, 70], [t1 + 0.15, -18], [t1 + 0.3, 6], [t1 + 0.42, 0], [length, 0]];
      bones[b + '_outer'] = { rotation: dedupe(fold.map(([t, a]) => [at(t), [0, r2(-s * a), 0], 'catmullrom'])) };
    }
  }));
  return bones;
};
const stageEnd = n => Math.max(...STAGES[n].map(([, , z]) => z)) * PACE + 0.3;
const ALL = [1, 2, 3, 4], AWAKEN = STAGE_START[4] + stageEnd(4);

// Skin pieces (plates, tendrils, marks) don't scale: the black spreads over them in the texture. Every texel
// gets the time it turns, from its distance to where the piece starts (hand, shoulders, hip) plus blotchy
// noise and a few early specks of pigment; it shows blood red first, darkens, then takes its own colour.
// SPREAD: bone -> [direction, start y, length]. The renderer swaps demon_form_spread/<anim>_<frame> at FPS.
const SPREAD = {
  s1_arm_right: ['up', 11.75, 12.5], s3_arm_left: ['up', 11.75, 12.5],
  s1_tendrils: ['down', 24.5, 10.5], s3_tendrils: ['down', 24.5, 10.5],
  s4_union: ['down', 24.5, 12.5], s4_arm_right_union: ['down', 25, 13.5], s4_arm_left_union: ['down', 25, 13.5],
  s4_leg_right: ['down', 12.25, 12.5], s4_leg_left: ['down', 12.25, 12.5]
};
const FPS = 8;
const boneOf = {};
const index = (g, bone) => { (g.cubes || []).forEach(c => { boneOf[c.name] = bone; }); (g.children || []).forEach(ch => index(ch, ch.name)); };
spec.forEach(g => index(g, g.name));
// Smooth value noise over texture coordinates (blobs about CELL texels wide), and a per-texel hash.
const CELL = 4, speck = hash;
const noise = (x, y) => {
  const gx = Math.floor(x / CELL), gy = Math.floor(y / CELL), fx = x / CELL - gx, fy = y / CELL - gy;
  const s = v => v * v * (3 - 2 * v), a = speck(gx, gy), b = speck(gx + 1, gy), c = speck(gx, gy + 1), d = speck(gx + 1, gy + 1);
  return a + (b - a) * s(fx) + (c - a) * s(fy) + (a - b - c + d) * s(fx) * s(fy);
};
const texelY = (r, j) => {
  const { from, to } = r.spec;
  return r.face === 'up' ? to[1] : r.face === 'down' ? from[1] : to[1] - (j + 0.5) / r.h * (to[1] - from[1]);
};
// window(bone) -> [t0, t1] when that bone spreads in the animation, or null if it is already whole.
const spreadPainter = (T, window) => (ctx, r, m) => {
  painter(ctx, r, m);
  const bone = boneOf[r.spec.name], rule = SPREAD[bone], win = rule && window(bone);
  if (!win) return;
  const [dir, y0, len] = rule, [t0, t1] = win, img = ctx.getImageData(r.u, r.v, r.w, r.h), d = img.data;
  for (let i = 0; i < r.w; i++) for (let j = 0; j < r.h; j++) {
    const k = (j * r.w + i) * 4;
    if (!d[k + 3]) continue;
    const p = Math.min(1, Math.max(0, (dir === 'up' ? texelY(r, j) - y0 : y0 - texelY(r, j)) / len));
    let f = 0.8 * p + 0.45 * (noise(r.u + i, r.v + j) - 0.5);
    if (speck(r.u + i + 0.5, r.v + j + 0.5) < 0.05) f -= 0.35;
    const age = T - (t0 + (t1 - t0) * Math.min(1, Math.max(0, f)));
    if (age < 0) d[k + 3] = 0;
    else if (age < 0.12) [d[k], d[k + 1], d[k + 2]] = [122, 13, 24];
    else if (age < 0.25) [d[k], d[k + 1], d[k + 2]] = [42, 5, 8];
  }
  ctx.putImageData(img, r.u, r.v);
};
const r1 = n => Math.round(n * 1000) / 1000;
const frames = (name, length, window) => Object.fromEntries([...Array(Math.ceil(length * FPS) + 1)].map((_, f) =>
  [`demon_form_spread/${name}_${String(f).padStart(2, '0')}`, spreadPainter(r1(f / FPS), window)]));
const windowIn = (stages, t0of) => bone => {
  for (const n of stages) {
    const e = STAGES[n].find(([b]) => b === bone);
    if (e) return [t0of(n) + e[1] * PACE, t0of(n) + e[2] * PACE];
  }
  return null;
};
// revert plays the awaken frames backwards; transform_2 has no skin pieces.
const spreadTextures = {
  ...frames('awaken', r2(AWAKEN), windowIn(ALL, n => STAGE_START[n])),
  ...Object.assign({}, ...[1, 3, 4].map(n => frames('transform_' + n, r2(stageEnd(n)), windowIn([n], () => 0))))
};
const animations = {
  // idle only rotates, so it can run under awaken/revert/transform, which only scale.
  idle: { loop: 'loop', length: 3, bones: { ...wings(120, 0, 5), ...tailSway(120, 0, 1.6) } },
  // Awakening on (V): every stage the player has reaches over them in order; stages above theirs are hidden.
  awaken: { loop: 'once', length: r2(AWAKEN), bones: consume(ALL, n => STAGE_START[n], false, r2(AWAKEN)) },
  // Awakening over: the same, in reverse, the last stage receding first.
  revert: { loop: 'once', length: r2(AWAKEN), bones: consume(ALL, n => STAGE_START[n], true, r2(AWAKEN)) },
  // Reaching a new stage (after a demon quest): only that stage spreads.
  ...Object.fromEntries(ALL.map(n => ['transform_' + n, { loop: 'once', length: r2(stageEnd(n)), bones: consume([n], () => 0, false, r2(stageEnd(n))) }])),
  // Flight (from stage 2), matching the player's poses. dash is the fast low pass: wings swept back, quick flutter.
  fly_hover: { loop: 'loop', length: 1, bones: { ...wings(720, 0, 30), ...tailSway(360, -10) } },
  fly_forward: { loop: 'loop', length: 0.8, bones: { ...wings(450, 20, 25), ...tailSway(450, -30, 0.7) } },
  fly_backward: { loop: 'loop', length: 1, bones: { ...wings(720, -10, 30), ...tailSway(360, 10) } },
  fly_left: { loop: 'loop', length: 0.8, bones: { ...wings(450, 15, 15, 35), ...tailSway(450, -25, 0.7) } },
  fly_right: { loop: 'loop', length: 0.8, bones: { ...wings(450, 15, 35, 15), ...tailSway(450, -25, 0.7) } },
  fly_dash: { loop: 'loop', length: 0.4, bones: { ...wings(900, 60, 6), ...tailSway(900, -45, 0.3) } }
};

module.exports = { name: 'demon_form', group: 'awakening', kind: 'entity', spec, animations, STAGES, FPS, textures: { demon_form: painter, ...spreadTextures } };
