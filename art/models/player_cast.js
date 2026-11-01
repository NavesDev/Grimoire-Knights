// Player poses when casting a spell (Z, X, C) and dashing, for playerAnimator. Same rig as player_sword.js.
// Cast poses key only the torso and arms: code turns the missing parts off, so the legs
// keep walking and the head keeps following the look. +x swings an arm forward (90 = pointing ahead);
// on the right arm +y swings it in toward the chest and +z lifts it out to the side (mirrored on the left): -z
// sank the arm into the body in game. Blockbench shows y and z the other way (the game mirrors it, see player_ride's gameAxes).
const P = load('./player_sword.js');
const zero = [0, 0, 0];
const REST = { torso: zero, right_arm: zero, left_arm: zero };
const none = () => 0;
const pose = (length, keys) => P.acted(length, keys, none, 1);

const animations = {
  // Z (fireball, crescent blade): right hand thrown forward, palm out; the spell leaves at 0.15 s.
  cast_z: pose(0.6, [
    [0, REST],
    [0.08, { torso: [0, 8, 0], right_arm: [40, -10, -15], left_arm: [-10, 0, 8] }],
    [0.15, { torso: [0, -12, 0], right_arm: [95, 8, 0], left_arm: [-15, 0, 10] }],
    [0.35, { torso: [0, -10, 0], right_arm: [88, 6, 0], left_arm: [-12, 0, 8] }],
    [0.6, REST]
  ]),
  // X fire (flame arm): pulls the arm back, then thrusts the open hand; the hand flies at 0.25 s. The arm
  // stays reaching out until the hand grabs (code holds the last frame).
  cast_x_fire: { ...pose(0.45, [
    [0, REST],
    [0.18, { torso: [0, 25, 0], right_arm: [-35, 0, -20], left_arm: [60, -20, 0] }],
    [0.25, { torso: [0, -25, 0], right_arm: [100, 10, 0], left_arm: [-25, 0, 12] }],
    [0.45, { torso: [0, -20, 0], right_arm: [92, 8, 0], left_arm: [-20, 0, 10] }]
  ]), loop: 'hold' },
  // X fire, on the grab: closes the fist, hauls it overhead and yanks it down, in step with the hand's slam
  // (lift 0-0.3 s, impact at 0.45 s).
  cast_x_fire_slam: pose(0.8, [
    [0, { torso: [0, -20, 0], right_arm: [92, 8, 0], left_arm: [-20, 0, 10] }],
    [0.3, { torso: [8, -5, 0], right_arm: [165, 5, 0], left_arm: [-10, 0, 15] }],
    [0.45, { torso: [-15, -10, 0], right_arm: [45, 10, 0], left_arm: [20, 0, 10] }],
    [0.6, { torso: [-12, -8, 0], right_arm: [40, 8, 0], left_arm: [15, 0, 8] }],
    [0.8, REST]
  ]),
  // X fire, when the hand explodes without grabbing anyone: the held arm drops back to rest.
  cast_x_fire_release: pose(0.3, [
    [0, { torso: [0, -20, 0], right_arm: [92, 8, 0], left_arm: [-20, 0, 10] }],
    [0.3, REST]
  ]),
  // X wind (tornado fang): both hands gathered at the chest, then pushed forward; the tornado leaves at 0.3 s.
  cast_x_wind: pose(0.8, [
    [0, REST],
    [0.2, { torso: [6, 0, 0], right_arm: [55, 30, 0], left_arm: [55, -30, 0] }],
    [0.3, { torso: [-6, 0, 0], right_arm: [95, 12, 0], left_arm: [95, -12, 0] }],
    [0.55, { torso: [-5, 0, 0], right_arm: [90, 10, 0], left_arm: [90, -10, 0] }],
    [0.8, REST]
  ]),
  // C fire (ember burst): arms crossed in front, gathering, then flung open; the burst goes off at 0.45 s.
  cast_c_fire: pose(1, [
    [0, REST],
    [0.3, { torso: [8, 0, 0], right_arm: [70, 35, 0], left_arm: [70, -35, 0] }],
    [0.45, { torso: [-8, 0, 0], right_arm: [25, 0, -80], left_arm: [25, 0, 80] }],
    [0.75, { torso: [-6, 0, 0], right_arm: [20, 0, -75], left_arm: [20, 0, 75] }],
    [1, REST]
  ]),
  // C wind (white gale bow): left arm holds the upright bow ahead, right hand draws the six arrows to the chin and
  // lets go, in step with the bow's summon (0-0.3 s), draw (0.3-0.8 s) and release (from 0.8 s).
  cast_c_wind: pose(1.4, [
    [0, REST],
    [0.3, { torso: [0, -20, 0], left_arm: [90, 15, 0], right_arm: [90, -5, 0] }],
    [0.8, { torso: [0, -30, 0], left_arm: [90, 25, 0], right_arm: [95, 40, 0] }],
    [0.9, { torso: [0, -28, 0], left_arm: [90, 22, 0], right_arm: [80, -10, -25] }],
    [1.15, { torso: [0, -20, 0], left_arm: [80, 15, 0], right_arm: [60, -10, -20] }],
    [1.4, REST]
  ])
};

// Dash (0.3 s, the dash's own duration): a quick lunge, not a flight: slight lean, legs in a long stride, arms
// swinging opposite to the legs like a sprint step.
const dashPose = peak => P.acted(0.3, [
  [0, { body: zero, right_arm: zero, left_arm: zero, right_leg: zero, left_leg: zero }],
  [0.06, peak],
  [0.22, Object.fromEntries(Object.entries(peak).map(([k, v]) => [k, v.map(n => n * 0.85)]))],
  [0.3, { body: zero, right_arm: zero, left_arm: zero, right_leg: zero, left_leg: zero }]
], none, 1);
animations.dash = dashPose({ body: [-8, 0, 0], right_arm: [-40, 0, 6], left_arm: [40, 0, -6], right_leg: [30, 0, 0], left_leg: [-25, 0, 0] });
// Back: leans away, arms thrown forward for balance, feet leading. Sides: lean into it, the leading arm and leg out,
// the other arm tucked.
animations.dash_back = dashPose({ body: [12, 0, 0], right_arm: [45, 0, 12], left_arm: [45, 0, -12], right_leg: [-30, 0, 0], left_leg: [20, 0, 0] });
animations.dash_right = dashPose({ body: [-4, 0, -12], right_arm: [0, 0, 55], left_arm: [-15, 0, 10], right_leg: [0, 0, 20], left_leg: [0, 0, -4] });
animations.dash_left = dashPose({ body: [-4, 0, 12], right_arm: [-15, 0, -10], left_arm: [0, 0, -55], right_leg: [0, 0, 4], left_leg: [0, 0, -20] });

// Roll (0.5 s, the roll's own push): a forward somersault on landing, tucked tight (knees to the chest, arms hugging
// them), one full turn of the whole body eased over the whole roll, then it opens up standing. Keys on every sample,
// so the per-key easing doesn't stall the turn at each key.
const tuck = { right_arm: [70, 25, 0], left_arm: [70, -25, 0], right_leg: [100, 0, 0], left_leg: [100, 0, 0] };
const open = { right_arm: zero, left_arm: zero, right_leg: zero, left_leg: zero };
animations.roll = P.acted(0.5, Array.from({ length: 11 }, (_, i) => {
  const t = i * 0.05, s = Math.min(1, t / 0.45), turn = -360 * s * s * (3 - 2 * s);
  const limbs = t < 0.1 ? Object.fromEntries(Object.entries(tuck).map(([k, v]) => [k, v.map(n => n * t / 0.1)]))
    : t < 0.35 ? tuck : Object.fromEntries(Object.entries(tuck).map(([k, v]) => [k, v.map(n => n * Math.max(0, (0.5 - t) / 0.15))]));
  return [Math.round(t * 100) / 100, { body: [turn, 0, 0], ...(t >= 0.5 ? open : limbs) }];
}), none, 1);

module.exports = { name: 'player_cast', group: 'player', kind: 'player', spec: P.spec, animations, textures: P.textures };
