// Animations shared by the anti-magic swords. Swings and specials are player poses (playerAnimator);
// the sword only comes out of the grimoire and goes back in.
// root pivots at the pommel base, which the renderer puts on the grimoire's sword_anchor during summon
// and sheath: scaling y from there makes the sword grow out of the open pages, pommel side down. The inner
// sword bone turns 90° on y so the flats face the player's sides and the edge faces forward; it sits on
// sword, not root, so a tilt on root later still swings the blade forward instead of sideways.
// turn: -90 for the single-edged katana, so its edge (not its back) faces forward.
const animations = (turn = 90) => ({
  // Starts 0.35 s into the grimoire's draw: grows out of the pages, then rises to be grabbed.
  summon: { loop: 'hold', length: 0.7, bones: { root: {
    scale: [[0, [0.6, 0.01, 0.6]], [0.3, [1, 1.1, 1], 'catmullrom'], [0.4, [1, 1, 1], 'catmullrom']],
    position: [[0.4, [0, 0, 0]], [0.7, [0, 3, 0], 'catmullrom']]
  }, sword: { rotation: [0, turn, 0] } } },
  // Sinks back into the pages.
  sheath: { loop: 'once', length: 0.35, bones: { root: {
    position: [[0, [0, 3, 0]], [0.15, [0, 0, 0], 'catmullrom']],
    scale: [[0.1, [1, 1, 1]], [0.35, [0.6, 0.01, 0.6], 'catmullrom']]
  }, sword: { rotation: [0, turn, 0] } } }
});

// Flight on the sword (katana): bobs and sways under the player; the renderer lays it flat.
const ride = { loop: 'loop', length: 2, bones: { sword: {
  position: [0, 'math.sin(query.anim_time * 180) * 0.4', 0],
  rotation: ['math.sin(query.anim_time * 180 + 90) * 2', 0, 'math.sin(query.anim_time * 90) * 3']
} } };

module.exports = { animations, ride };
