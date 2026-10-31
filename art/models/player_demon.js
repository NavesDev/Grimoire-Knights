// Player poses while flying in demon form (from stage 2), for playerAnimator. Same rig as player_sword.js;
// each pairs with the demon form animation of the same name (fly_<dir>, awaken, revert). Sampled, no Molang.
// Whole-body tilts are on body (pivot at the feet): -x leans forward, -z leans to the player's left.
const P = load('./player_sword.js');
const K = load('./lib.js');
const D = load('./demon_form.js');
const { sin } = Math;

const zero = [0, 0, 0];
const acted = P.acted; // its tremor is the body fighting the anti-magic
const STAND = { body: zero, head: zero, right_arm: zero, left_arm: zero, right_leg: zero, left_leg: zero };
const L = D.animations.awaken.length;
// Poses below are timed on a 3.35 s take; stretch them to the demon form's awaken length.
const paced = (poses, shake) => [poses.map(([t, p]) => [t * L / 3.35, p]), t => shake(t * 3.35 / L)];

const animations = {
  // Hovering: upright, legs dangling, arms loose; rises and sinks with each beat (two per second).
  hover: P.loop(1, p => ({
    body: { rotation: [-3, 0, 0], position: [0, 0.8 * sin(2 * p), 0] },
    right_leg: { rotation: [8 * sin(p), 0, 3] }, left_leg: { rotation: [-8 * sin(p), 0, -3] },
    right_arm: { rotation: [5 * sin(p + 1), 0, 12 + 4 * sin(2 * p)] }, left_arm: { rotation: [-5 * sin(p + 1), 0, -12 - 4 * sin(2 * p)] }
  })),
  // Forward: leaning into the flight, legs trailing, arms swept back, head up to look ahead.
  forward: P.loop(0.8, p => ({
    body: { rotation: [-35, 0, 0], position: [0, 0.5 * sin(p), 0] },
    head: { rotation: [25, 0, 0] },
    right_leg: { rotation: [-15 + 5 * sin(p), 0, 2] }, left_leg: { rotation: [-15 - 5 * sin(p), 0, -2] },
    right_arm: { rotation: [-25 + 4 * sin(p), 0, 15] }, left_arm: { rotation: [-25 - 4 * sin(p), 0, -15] }
  })),
  // Backward: leaning back, legs and arms forward as if braking.
  backward: P.loop(1, p => ({
    body: { rotation: [15, 0, 0], position: [0, 0.6 * sin(2 * p), 0] },
    head: { rotation: [-10, 0, 0] },
    right_leg: { rotation: [20 + 6 * sin(p), 0, 3] }, left_leg: { rotation: [15 - 6 * sin(p), 0, -3] },
    right_arm: { rotation: [30 + 5 * sin(p), 0, 15] }, left_arm: { rotation: [30 - 5 * sin(p), 0, -15] }
  })),
  // Sideways: banking into the turn, the outer arm out for balance.
  left: P.loop(0.8, p => ({
    body: { rotation: [-10, 0, -20], position: [0, 0.5 * sin(p), 0] },
    head: { rotation: [0, 0, 15] },
    right_leg: { rotation: [-5 + 5 * sin(p), 0, 8] }, left_leg: { rotation: [-5 - 5 * sin(p), 0, 2] },
    right_arm: { rotation: [-10, 0, 35 + 4 * sin(p)] }, left_arm: { rotation: [-10, 0, -10] }
  })),
  right: P.loop(0.8, p => ({
    body: { rotation: [-10, 0, 20], position: [0, 0.5 * sin(p), 0] },
    head: { rotation: [0, 0, -15] },
    right_leg: { rotation: [-5 + 5 * sin(p), 0, -2] }, left_leg: { rotation: [-5 - 5 * sin(p), 0, -8] },
    right_arm: { rotation: [-10, 0, 10] }, left_arm: { rotation: [-10, 0, -35 - 4 * sin(p)] }
  })),
  // Dash (fast low pass): body flat, right fist forward, left arm along the body, legs together trailing.
  dash: P.loop(0.4, p => ({
    body: { rotation: [-80, 0, 2 * sin(p)], position: [0, 0, 0] },
    head: { rotation: [60, 0, 0] },
    right_arm: { rotation: [170, 0, -5] }, left_arm: { rotation: [-15, 0, -8] },
    right_leg: { rotation: [-5 + 3 * sin(p), 0, 1] }, left_leg: { rotation: [-5 - 3 * sin(p), 0, -1] }
  }))
};

// Awakening, in step with the demon form's awaken: doubles over clutching the arm as the black climbs it,
// shakes, throws the arms out and roars when the wings burst, doubles over again as the union spreads down,
// then straightens up. Stages above the player's are hidden but the pose plays the same.
animations.awaken = acted(L, ...paced([
  [0, STAND],
  [0.3, { body: [-12, 0, 0], head: [-25, 0, 0], right_arm: [60, 0, 25], left_arm: [55, 0, -30], right_leg: [0, 0, 4], left_leg: [0, 0, -4] }],
  [0.9, { body: [-14, 0, 3], head: [-15, 20, 0], right_arm: [70, 0, 20], left_arm: [60, 0, -25], right_leg: [0, 0, 5], left_leg: [0, 0, -5] }],
  [1.7, { body: [-10, 0, -2], head: [-20, -10, 0], right_arm: [45, 0, 10], left_arm: [40, 0, -10], right_leg: [0, 0, 6], left_leg: [0, 0, -6] }],
  [2.0, { body: [8, 0, 0], head: [28, 0, 0], right_arm: [-10, 0, -65], left_arm: [-10, 0, 65], right_leg: [0, 0, 8], left_leg: [0, 0, -8] }],
  [2.4, { body: [-15, 0, 0], head: [-20, 0, 0], right_arm: [30, 0, -15], left_arm: [30, 0, 15], right_leg: [0, 0, 8], left_leg: [0, 0, -8] }],
  [2.9, { body: [-6, 0, 0], head: [-5, 0, 0], right_arm: [15, 0, -10], left_arm: [15, 0, 10], right_leg: [0, 0, 6], left_leg: [0, 0, -6] }],
  [3.35, { body: [3, 0, 0], head: [8, 0, 0], right_arm: [0, 0, -12], left_arm: [0, 0, 12], right_leg: [0, 0, 5], left_leg: [0, 0, -5] }]
], t => t < 0.1 ? 0 : t < 1.9 ? 2.5 : t < 2.05 ? 0.5 : t < 2.9 ? 3 : 0.6), 911);
// Awakening over: the power drains, the player sags, exhausted, and recovers at the very end.
animations.revert = acted(L, ...paced([
  [0, { body: [3, 0, 0], head: [8, 0, 0], right_arm: [0, 0, -12], left_arm: [0, 0, 12], right_leg: [0, 0, 5], left_leg: [0, 0, -5] }],
  [0.6, { body: [-4, 0, 2], head: [-10, 10, 0], right_arm: [10, 0, -6], left_arm: [10, 0, 6], right_leg: [0, 0, 4], left_leg: [0, 0, -4] }],
  [2.2, { body: [-12, 0, -3], head: [-30, -8, 0], right_arm: [15, 0, 4], left_arm: [12, 0, -4], right_leg: [0, 0, 6], left_leg: [0, 0, -2] }],
  [3.0, { body: [-14, 0, -3], head: [-32, -8, 0], right_arm: [18, 0, 5], left_arm: [15, 0, -5], right_leg: [0, 0, 6], left_leg: [0, 0, -2] }],
  [3.35, STAND]
], t => t < 2.4 ? 1.2 * (1 - t / 2.4) : 0), 313);

module.exports = { name: 'player_demon', group: 'player', kind: 'player', spec: P.spec, animations, textures: P.textures };
