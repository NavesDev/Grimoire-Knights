// Player poses while flying on the broom or on the anti-magic greatsword, for playerAnimator. Both are ridden
// standing, like a flying skateboard: body turned sideways on the board (left foot forward), head turned to
// look where it goes, arms out along the board. Same rig as player_sword.js; each pairs with the prop
// animation of the same length (broom hover/fly, greatsword ride). Sampled, no Molang. The renderer puts the
// player on the prop; the poses only bend the body. body y turns the player; with it turned, body z tilts
// the player toward the front or back of the board.
const P = load('./player_sword.js');
const { sin } = Math;

// Skate stance: lean into the flight (body z, degrees), bob and sway from the board, arms swinging.
const stance = (lean, bob, sway, arms) => ({
  body: { rotation: [sway, -90, lean], position: [0, bob, 0] },
  head: { rotation: [0, 80, -lean * 0.6] },
  right_leg: { rotation: [0, 0, -14] }, left_leg: { rotation: [0, 0, 14] },
  right_arm: { rotation: [8 + arms, 0, -45 + arms] }, left_arm: { rotation: [8 - arms, 0, 45 + arms] }
});

const animations = {
  // Broom parked in the air: upright, bobbing and rocking with the broom.
  broom_hover: P.loop(2, p => stance(-4, 0.6 * sin(p), 3 * sin(p / 2), 5 * sin(p))),
  // Broom in flight: leaning into the wind, arms lower, quick shake.
  broom_fly: P.loop(0.5, p => stance(-18, 0.15 * sin(2 * p), 1.5 * sin(2 * p), -15 + 2 * sin(2 * p))),
  // Greatsword ride: same stance, swaying with the blade.
  sword: P.loop(2, p => stance(-8, 0.4 * sin(p), 3 * sin(p / 2), 5 * sin(p)))
};

module.exports = { name: 'player_ride', group: 'player', kind: 'player', spec: P.spec, animations, textures: P.textures };
