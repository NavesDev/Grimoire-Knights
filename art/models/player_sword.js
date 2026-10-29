// Player rig for playerAnimator poses while holding an anti-magic sword (right hand). Not a game model:
// run.js writes only the .bbmodel and player_animation/player_sword.json.
// playerAnimator has no Molang, so loops are sampled into numeric keyframes. Parts are siblings, as on the
// vanilla player; body moves the whole player. Rotations: +x swings an arm or leg forward, and tilts body
// back (pivot at the feet), so a forward lean is negative.
const K = load('./lib.js');

const part = (name, origin, from, to, mat = 'cloth') => ({ name, origin, cubes: [{ name: name + '_cube', from, to, paint: { north: name === 'head' ? 'face' : mat, all: name === 'head' ? 'skin' : mat } }] });

const spec = [{ name: 'body', origin: [0, 0, 0], children: [
  part('head', [0, 24, 0], [-4, 24, -4], [4, 32, 4]),
  part('torso', [0, 24, 0], [-4, 12, -2], [4, 24, 2]),
  part('right_arm', [-5, 22, 0], [-8, 12, -2], [-4, 24, 2]),
  part('left_arm', [5, 22, 0], [4, 12, -2], [8, 24, 2]),
  part('right_leg', [-1.9, 12, 0], [-3.9, 0, -2], [0.1, 12, 2], 'pants'),
  part('left_leg', [1.9, 12, 0], [-0.1, 0, -2], [3.9, 12, 2], 'pants')
] }];

const painter = (ctx, r, m) => {
  K.noiseFill(ctx, r, { skin: '#b08a6e', face: '#b08a6e', cloth: '#6a7a8c', pants: '#3d4656' }[m] || '#ff00ff', 0.05, 3);
  if (m === 'face') [[3, 8], [4, 8], [11, 8], [12, 8]].forEach(([x, y]) => K.px(ctx, r.u + x, r.v + y, '#2a2a40'));
};

// Samples one loop of length seconds: pose(phase) returns {bone: {channel: [x, y, z]}} for phase in 0..2π.
const STEPS = 12;
const loop = (length, pose) => {
  const bones = {};
  for (let i = 0; i <= STEPS; i++) {
    const t = Math.round(length * i / STEPS * 1000) / 1000;
    Object.entries(pose(2 * Math.PI * i / STEPS)).forEach(([bone, chs]) => Object.entries(chs).forEach(([ch, v]) =>
      ((bones[bone] ||= {})[ch] ||= []).push([t, v.map(n => Math.round(n * 100) / 100)])));
  }
  return { loop: 'loop', length, bones };
};

const animations = {
  // Walk: sword held low in front, blade forward; the free arm and the legs follow the step.
  walk: loop(1, p => ({
    body: { position: [0, -0.5 * Math.abs(Math.sin(p)), 0] },
    right_leg: { rotation: [25 * Math.sin(p), 0, 0] },
    left_leg: { rotation: [-25 * Math.sin(p), 0, 0] },
    left_arm: { rotation: [20 * Math.sin(p), 0, 0] },
    right_arm: { rotation: [18 + 4 * Math.sin(p), 0, -6] }
  })),
  // Run: leaning forward, sword resting on the shoulder with the blade down the back, free arm pumping.
  run: loop(0.6, p => ({
    body: { rotation: [-12, 0, 0], position: [0, -0.8 * Math.abs(Math.sin(p)), 0] },
    right_leg: { rotation: [50 * Math.sin(p), 0, 0] },
    left_leg: { rotation: [-50 * Math.sin(p), 0, 0] },
    left_arm: { rotation: [10 + 50 * Math.sin(p), 0, 0] },
    right_arm: { rotation: [-135 + 4 * Math.sin(2 * p), 0, -10] }
  }))
};

module.exports = { name: 'player_sword', group: 'player', kind: 'player', spec, animations, textures: { player_sword: painter } };
