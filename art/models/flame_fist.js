// Fire magic 2 (flame arm): a giant hand of fire that grabs the first target and slams it into the ground.
// Flies toward north (-z) with the hand open and turned on its side (thumb up, palm to the left), so the
// fingers close sideways around the target; the forearm and its flames trail south. Same fire materials as
// the fireball (cutout render). Rest pose is the open hand; grab closes it into a fist.
const K = load('./lib.js');
const F = load('./fireball.js');

const C = [0, 6, 0];
const FINGERS = [-4.5, -2.25, 0, 2.25];
// Finger and thumb rotations, open (rest) and the change that closes them into a fist. A negative x turn
// curls a finger down toward the palm.
const OPEN = k => [-30, (k - 1.5) * -10, 0];
const CLOSE = [-70, 0, 0];
const THUMB_OPEN = [0, 25, 0], THUMB_CLOSE = [0, -95, 0];
const finger = (x, k) => ({ name: 'finger_' + (k + 1), origin: [x + 1, 8.5, -5], rotation: OPEN(k), cubes: [
  { name: 'finger_cube_' + (k + 1), from: [x + 0.1, 7.5, -11.5], to: [x + 1.9, 9.5, -4], mat: 'inner' }] });
// Flames licking back from the forearm: [x, y, length, tilt].
const TONGUES = [[-4, 9, 7, -20], [4, 9, 6, 20], [-4.5, 3, 6, -25], [4.5, 3, 7, 25], [0, 10.5, 8, 0], [0, 1.5, 6, 0]];
const tongue = ([x, y, len, tilt], k) => ({ name: 'tongue_' + (k + 1), origin: [x, y, 6], rotation: [y > C[1] ? 12 : -12, tilt, 0],
  cubes: [{ name: 'tongue_cube_' + (k + 1), from: [x - 1.25, y - 1.25, 6], to: [x + 1.25, y + 1.25, 6 + len], mat: 'tongue' }] });
const TRAIL = [[6, 13, 4], [4.5, 17, 4], [3, 21, 3.5], [1.5, 24.5, 3]];
const trail = i => {
  const [w, z, len] = TRAIL[i];
  return { name: 'trail_' + (i + 1), origin: [0, C[1], z],
    cubes: [{ name: 'trail_cube_' + (i + 1), from: [-w / 2, C[1] - w / 2, z], to: [w / 2, C[1] + w / 2, z + len], mat: 'tongue' }],
    children: i + 1 < TRAIL.length ? [trail(i + 1)] : [] };
};

// side turns the hand a quarter on the flight axis (thumb up); fist above it stays free for the slam's pitch.
const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'fist', origin: C, children: [{ name: 'side', origin: C, rotation: [0, 0, -90], children: [
  { name: 'hand', origin: C, cubes: [
    { name: 'palm', from: [-5.5, 1.5, -4], to: [5.5, 10.5, 4], mat: 'inner' },
    { name: 'thumb_base', from: [-7, 2, -4], to: [-5, 6, 1], mat: 'inner' }],
    children: [...FINGERS.map(finger), { name: 'thumb', origin: [-6, 4, -3.5], rotation: THUMB_OPEN, cubes: [
      { name: 'thumb_cube', from: [-7, 3, -9.5], to: [-5, 5, -3.5], mat: 'inner' }] }] },
  { name: 'shell', origin: C, cubes: [{ name: 'shell_cube', from: [-6.5, 0.5, -3.5], to: [6.5, 11.5, 5], inflate: 0.25, mat: 'outer' }] },
  { name: 'forearm', origin: [0, C[1], 4], cubes: [
    { name: 'wrist', from: [-4, 2.5, 4], to: [4, 9.5, 8], mat: 'inner' },
    { name: 'arm', from: [-3.5, 3, 8], to: [3.5, 9, 13], mat: 'tongue' }],
    children: [{ name: 'tongues', origin: [0, C[1], 6], children: TONGUES.map(tongue) }, trail(0)] }
] }] }] }];

const wave = F.wave;
const C3 = 'catmullrom';
const closed = Object.fromEntries([...FINGERS.map((_, k) => ['finger_' + (k + 1), { rotation: CLOSE }]), ['thumb', { rotation: THUMB_CLOSE }]]);
const scaled = (v, f) => v.map(n => Math.round(n * f * 100) / 100);
const animations = {
  // fly (1 s loop): the open hand swims through the air: it rolls and yaws a little, fingers ripple one
  // after another, the forearm and trail lag behind the hand.
  fly: { loop: 'loop', length: 1, bones: {
    fist: { rotation: [wave(360, 4), wave(360, 6, 90), wave(360, 8, 45)] },
    ...Object.fromEntries(FINGERS.map((_, k) => ['finger_' + (k + 1), { rotation: [wave(360, 12, k * 60), 0, 0] }])),
    thumb: { rotation: [0, wave(360, 10, 240), 0] },
    hand: { scale: Array(3).fill(`1 + ${wave(720, 0.04)}`) },
    shell: { rotation: [0, 0, wave(360, 6)], scale: Array(3).fill(`1 + ${wave(1440, 0.05, 45)}`) },
    forearm: { rotation: [wave(360, 7, -60), wave(360, 8, 30), 0] },
    ...Object.fromEntries(TONGUES.map((_, k) => ['tongue_' + (k + 1), { scale: [
      `1 + ${wave(1080, 0.15, k * 97)}`, `1 + ${wave(1080, 0.15, k * 97)}`, `1 + ${wave(1440, 0.35, k * 131)}`] }])),
    ...Object.fromEntries(TRAIL.map((_, i) => ['trail_' + (i + 1), { rotation: [wave(360, 12, i * 50 - 60), wave(360, 12, i * 50 + 30), 0] }]))
  } },
  // grab (0.25 s, hold): the hand lunges a little, the fingers snap shut one after another past the fist
  // and settle, the thumb locks last; the forearm jerks with the squeeze.
  grab: { loop: 'hold', length: 0.25, bones: {
    ...Object.fromEntries(FINGERS.map((_, k) => ['finger_' + (k + 1), { rotation: [
      [0, [0, 0, 0]], [0.04 + k * 0.02, [-15, 0, 0], C3], [0.12 + k * 0.02, scaled(CLOSE, 1.18), C3], [0.25, CLOSE, C3]] }])),
    thumb: { rotation: [[0, [0, 0, 0]], [0.12, [0, 10, 0], C3], [0.2, scaled(THUMB_CLOSE, 1.08), C3], [0.25, THUMB_CLOSE, C3]] },
    fist: { position: [[0, [0, 0, 0]], [0.08, [0, 0, -1.5], C3], [0.25, [0, 0, 0], C3]], rotation: [[0, [0, 0, 0]], [0.1, [0, 0, 8], C3], [0.25, [0, 0, 0], C3]] },
    hand: { scale: [[0, [1, 1, 1]], [0.14, [1.12, 0.9, 1.08], C3], [0.25, [1, 1, 1], C3]] },
    forearm: { rotation: [[0, [0, 0, 0]], [0.1, [-8, 0, 0], C3], [0.25, [0, 0, 0], C3]] }
  } },
  // slam (0.45 s): code lifts the fist and the target 2 blocks (0-0.3 s) and drives them into the ground
  // (0.3-0.45 s). The fist dips to wind up, rises leaning back and rolling, whips down past the target and
  // squashes on the impact; the forearm and trail follow late.
  slam: { loop: 'once', length: 0.45, bones: { ...closed,
    fist: {
      rotation: [[0, [0, 0, 0]], [0.07, [-10, 0, 4], C3], [0.28, [38, 0, -12], C3], [0.33, [30, 0, -8], C3], [0.41, [-72, 0, 6], C3], [0.45, [-62, 0, 0], C3]],
      scale: [[0, [1, 1, 1]], [0.3, [1, 1.06, 1], C3], [0.38, [0.92, 1.12, 0.92], C3], [0.42, [1.28, 0.72, 1.28], C3], [0.45, [1.2, 0.8, 1.2], C3]] },
    forearm: { rotation: [[0, [0, 0, 0]], [0.15, [12, 0, 0], C3], [0.32, [-20, 0, 0], C3], [0.45, [25, 0, 0], C3]] },
    ...Object.fromEntries(TRAIL.map((_, i) => ['trail_' + (i + 1), { rotation: [[0, [0, 0, 0]], [0.2 + i * 0.03, [10 + i * 4, 0, 0], C3], [0.36 + i * 0.02, [-18 - i * 6, 0, 0], C3], [0.45, [20, 0, 0], C3]] }]))
  } },
  // impact (0.3 s): after the slam, or when the fist hits a block: squashes, the wrist kicks, and the fist
  // flares out to nothing while the explosion VFX takes over.
  impact: { loop: 'once', length: 0.3, bones: { ...closed,
    fist: { scale: [[0, [1, 1, 1]], [0.06, [1.3, 1.3, 0.55], C3], [0.3, [0.01, 0.01, 0.01], C3]], rotation: [[0, [0, 0, 0]], [0.08, [0, 0, -10], C3], [0.3, [0, 0, 15], C3]] },
    forearm: { scale: [[0, [1, 1, 1]], [0.12, [0.6, 0.6, 0.3], C3]] }
  } }
};

module.exports = { name: 'flame_fist', group: 'ability', kind: 'entity', spec, animations, textures: { flame_fist: F.painter } };
