// Player poses while flying on the broom or on the anti-magic greatsword, for playerAnimator. Both are ridden
// standing, like a flying skateboard: body turned sideways on the board (right foot toward the nose), head
// turned to look where it goes, arms out along the board. Same rig as player_sword.js; sampled, no Molang.
// The board flies nose first (-z); the player faces +x, so the toes point right of the flight and the heels
// left. Each pose bobs and rolls with its prop (broom.js hover/fly, sword_anim.js ride) so the feet stay on
// it; the renderer puts the player on the prop and tilts both while steering.
const P = load('./player_sword.js');
const { sin, cos, PI } = Math;
const RAD = PI / 180;

// With body turned 90° on y, its x and z rotations both roll around the flight axis (gimbal lock), so the
// lean is built in world axes and turned back into the rig's ZYX euler: pitch leans toward the nose (+) or
// tail (-), roll toward the toes (+) or heels (-).
const M = {
  x: a => new THREE.Matrix4().makeRotationX(a * RAD),
  y: a => new THREE.Matrix4().makeRotationY(a * RAD),
  z: a => new THREE.Matrix4().makeRotationZ(a * RAD)
};
const euler = m => {
  const e = new THREE.Euler().setFromRotationMatrix(m, 'ZYX');
  return [e.x / RAD, e.y / RAD, e.z / RAD];
};
const TURN = -90, SPLAY = 12, LEG = 12, HIP = 1.9;

// The legs have no knees: leaning toward the nose or tail drops one hip, so each leg swings along the board
// (front leg toward the nose, back leg toward the tail, at least SPLAY) just enough to keep both feet on the
// same floor. Returns the leg rotations (local to body) and the floor height under the body pivot.
const legs = (body, roll) => {
  const hip = x => new THREE.Vector3(x, LEG, 0).applyMatrix4(body).y;
  const world = (a, nose) => M.z(-roll).multiply(M.x(nose ? a : -a)).multiply(M.y(TURN));
  // lowest corner of the foot, so a tilted foot rests on its edge instead of sinking into the board
  const foot = (y, a, nose) => y + Math.min(...[[-2, -2], [-2, 2], [2, -2], [2, 2]].map(([x, z]) =>
    new THREE.Vector3(x, -LEG, z).applyMatrix4(world(a, nose)).y));
  const hips = { left: hip(HIP), right: hip(-HIP) }; // with body turned, the right hip is toward the nose
  const floor = Math.max(foot(hips.right, SPLAY, true), foot(hips.left, SPLAY, false));
  const angle = (y, nose) => {
    let lo = SPLAY, hi = 80;
    for (let i = 0; i < 30; i++) { const mid = (lo + hi) / 2; foot(y, mid, nose) > floor ? hi = mid : lo = mid; }
    return lo;
  };
  const inv = body.clone().invert();
  const local = (y, nose) => euler(inv.clone().multiply(world(angle(y, nose), nose)));
  return { right_leg: local(hips.right, true), left_leg: local(hips.left, false), floor };
};

// o: pitch, roll (degrees), bob (y), look (head turn past the nose, + toward the toes), front and back arm
// raise (z, away from the body along the board) and swing (x, toward the toes).
const stance = o => {
  const body = M.x(-o.pitch).multiply(M.z(-o.roll)).multiply(M.y(TURN));
  const l = legs(body, o.roll);
  return {
    body: { rotation: euler(body), position: [0, o.bob - l.floor, 0] },
    head: { rotation: [o.roll * 0.5, 80 - (o.look || 0), -o.pitch * 0.7] },
    left_leg: { rotation: l.left_leg }, right_leg: { rotation: l.right_leg },
    right_arm: { rotation: [o.swing || 0, 0, -o.front] },
    left_arm: { rotation: [o.swing || 0, 0, o.back] }
  };
};

// Same flight in each direction: lean, look and arms per direction, bob and roll from the prop.
const DIRS = {
  hover: { pitch: 0, roll: 0, front: 40, back: 40 },
  forward: { pitch: 16, roll: 0, front: 20, back: 65, look: 0 },
  backward: { pitch: -12, roll: 0, front: 70, back: 25, swing: 10 },
  left: { pitch: 4, roll: -16, front: 45, back: 45, swing: 35, look: -25 },
  right: { pitch: 4, roll: 16, front: 45, back: 45, swing: -25, look: 25 }
};
// prop: { length, bob, roll, pitch } as functions of the phase, matching the prop animation.
const flight = (prefix, prop) => Object.fromEntries(Object.entries(DIRS).map(([dir, d]) => {
  const pr = dir === 'hover' ? prop.hover : prop.move;
  return [prefix + '_' + dir, P.loop(pr.length, p => stance({ ...d,
    pitch: d.pitch + (pr.pitch ? pr.pitch(p) : 0), roll: d.roll + pr.roll(p),
    bob: pr.bob(p), front: d.front + 4 * sin(p), back: d.back - 4 * sin(p) }))];
}));

const animations = {
  // Broom: hover pairs with broom hover (2 s: bob 0.6, pitch 1.5, roll 3), the directions with broom fly
  // (0.5 s: bob 0.15, no roll).
  ...flight('broom', {
    hover: { length: 2, bob: p => 0.6 * sin(p), pitch: p => 1.5 * cos(p), roll: p => 3 * sin(p / 2) },
    move: { length: 0.5, bob: p => 0.15 * sin(p), roll: () => 0 } }),
  // Greatsword: every direction pairs with the sword ride (2 s: bob 0.4, roll 3 on the blade, opposite sign
  // because the blade's y runs tail to nose).
  ...flight('sword', {
    hover: { length: 2, bob: p => 0.4 * sin(p), roll: p => -3 * sin(p / 2) },
    move: { length: 2, bob: p => 0.4 * sin(p), roll: p => -3 * sin(p / 2) } })
};

module.exports = { name: 'player_ride', group: 'player', kind: 'player', spec: P.spec, animations, textures: P.textures };
