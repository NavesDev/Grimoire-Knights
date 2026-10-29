// Griffin: eagle head, front legs and wings; lion body, back legs and tail. Faces north (-z).
const K = load('./lib.js');

const frontLeg = { name: 'left_front_leg', origin: [3.5, 15, -6], cubes: [
  { name: 'left_front_thigh', from: [1.5, 9, -8], to: [5.5, 15, -4], mat: 'feather_body' },
  { name: 'left_front_shin', from: [2, 2, -7.5], to: [5, 9, -4.5], mat: 'scale' },
  { name: 'left_front_foot', from: [1.5, 0, -10], to: [5.5, 2, -4.5], paint: { up: 'scale', all: 'talon' } }] };
const backLeg = { name: 'left_back_leg', origin: [4, 15, 8], cubes: [
  { name: 'left_back_thigh', from: [2, 7, 5], to: [6.5, 17, 11.5], mat: 'fur' },
  { name: 'left_back_shin', from: [2.5, 1.5, 8], to: [5.5, 7, 11], mat: 'fur' },
  { name: 'left_back_paw', from: [2, 0, 6.5], to: [6, 1.5, 11], mat: 'paw' }] };
// Wings are spread in the rest pose; the idle animation folds them.
const wing = { name: 'left_wing', origin: [5, 21.5, -4], rotation: [0, 0, -10], cubes: [
  { name: 'left_wing_arm', from: [5, 20.5, -5], to: [16, 22, 0], mat: 'wing_top' },
  { name: 'left_wing_cover', from: [5, 20.75, 0], to: [16, 21.75, 7], mat: 'wing' }
], children: [{ name: 'left_wing_tip', origin: [16, 21.25, -4], rotation: [0, 0, -8], cubes: [
  { name: 'left_wing_hand', from: [16, 20.5, -4.5], to: [25, 21.75, 0], mat: 'wing_top' },
  { name: 'left_wing_primaries', from: [16, 20.75, 0], to: [27, 21.5, 9], mat: 'wing' }] }] };

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'body', origin: [0, 17, 0], cubes: [
  { name: 'torso', from: [-5, 11, -8], to: [5, 22, 10], paint: { north: 'chest', up: 'back', all: 'fur' } },
  { name: 'chest_ruff', from: [-5.5, 13, -9.5], to: [5.5, 22.5, -4], mat: 'feather_body' }
], children: [
  { name: 'neck', origin: [0, 20, -8], rotation: [-15, 0, 0], cubes: [{ name: 'neck_cube', from: [-3, 19, -13], to: [3, 27, -7], mat: 'feather_head' }],
    children: [{ name: 'head', origin: [0, 27, -10], cubes: [
      { name: 'skull', from: [-3.5, 26, -17], to: [3.5, 32, -10], paint: { west: 'head_side', east: 'head_side', all: 'feather_head' } },
      { name: 'beak', from: [-1.5, 27, -21], to: [1.5, 30, -17], mat: 'beak' },
      { name: 'beak_hook', from: [-1.5, 26, -22], to: [1.5, 28.5, -21], mat: 'beak' },
      { name: 'jaw', from: [-1.25, 26, -20], to: [1.25, 27, -17], mat: 'beak' },
      { name: 'left_tuft', from: [1.5, 31, -12], to: [3.5, 34, -10], origin: [2.5, 31, -11], rotation: [-25, 0, 0], mat: 'feather_head' },
      { name: 'right_tuft', from: [-3.5, 31, -12], to: [-1.5, 34, -10], origin: [-2.5, 31, -11], rotation: [-25, 0, 0], mat: 'feather_head' }] }] },
  frontLeg, K.mirrorGroup(frontLeg), backLeg, K.mirrorGroup(backLeg), wing, K.mirrorGroup(wing),
  { name: 'tail', origin: [0, 20, 10], rotation: [35, 0, 0], cubes: [{ name: 'tail_base', from: [-1, 19, 10], to: [1, 21, 21], mat: 'fur' }],
    children: [{ name: 'tail_tuft', origin: [0, 20, 21], cubes: [{ name: 'tuft', from: [-2, 18, 20], to: [2, 22, 25], mat: 'tuft' }] }] }] }] }];

const painter = (ctx, r, m) => {
  const { u, v, w, h } = r, rnd = K.rng(83 + u * 3 + v * 11), px = (x, y, c) => K.px(ctx, u + x, v + y, c), n = (c, a = 0.1) => K.shade(c, 1 + (rnd() - 0.5) * a);
  const flat = r.face === 'up' || r.face === 'down';
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    let c;
    switch (m) {
      case 'fur': c = n(j > h * 0.7 && r.face !== 'up' ? '#d9b27a' : '#b8874a', 0.12); break;
      case 'back': c = n((j + i) % 7 === 0 ? '#8f6435' : '#a8783f', 0.1); break;
      case 'chest': c = n('#ead9b8', 0.1); break;
      case 'feather_body': c = (j % 3 === 2) ? K.shade('#d8cdb6', 0.85) : n('#e8dfcc', 0.08); break;
      case 'feather_head': c = ((j % 3 === 2) && rnd() < 0.6) ? '#d4ccbd' : n('#f2ede2', 0.06); break;
      case 'head_side': {
        c = n('#f2ede2', 0.06);
        const eyeX = r.face === 'west' ? 3 : w - 4;
        if (j >= 4 && j <= 6 && Math.abs(i - eyeX) <= 1) c = (j === 5 && i === eyeX) ? '#1a1208' : '#e8a21e';
        if (j === 4 && Math.abs(i - eyeX) <= 2) c = '#6b5a40';
        break;
      }
      case 'beak': c = n(j < 1 ? '#f5c650' : '#e0a83a', 0.08); break;
      case 'scale': c = ((i + j * 2) % 4 === 0) ? '#a8862e' : n('#d1a843', 0.1); break;
      case 'talon': c = n(j >= h - 1 ? '#2a2420' : '#d1a843', 0.1); if (j >= h - 2 && i % 3 === 0) c = '#2a2420'; break;
      case 'paw': c = n('#a07240', 0.1); break;
      case 'tuft': c = n('#5a3b22', 0.15); break;
      case 'wing_top': c = (i % 4 === 0) ? '#4e3420' : n('#6b4a2e', 0.12); break;
      case 'wing': {
        // Feather stripes with white tips and a ragged trailing edge.
        const feather = flat ? Math.floor(i / 3) % 2 : 0, tip = flat ? j >= h - 3 : false;
        c = tip ? n('#efe8da', 0.06) : n(feather ? '#7a5434' : '#634329', 0.1);
        if (flat && j >= h - 1 && i % 3 === 2) continue;
        break;
      }
      default: c = '#ff00ff';
    }
    px(i, j, c);
  }
};

module.exports = { name: 'griffin', group: 'enemy', kind: 'entity', spec, textures: { griffin: painter } };
