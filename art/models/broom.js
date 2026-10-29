// Flying broom. Handle along z, front toward north (-z), bristles at the back.
const K = load('./lib.js');

const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'broom', origin: [0, 0, 0], cubes: [
  { name: 'handle', from: [-0.75, 0, -15], to: [0.75, 1.5, 9], mat: 'wood' },
  { name: 'handle_cap', from: [-1, -0.25, -16], to: [1, 1.75, -15], mat: 'woodDark' },
  { name: 'grip_wrap', from: [-1, -0.25, -4], to: [1, 1.75, -1], mat: 'wrap' },
  { name: 'binding', from: [-1.75, -1, 8], to: [1.75, 2.5, 10.5], mat: 'rope' }
], children: [{ name: 'bristles', origin: [0, 0.75, 10.5], cubes: [
  { name: 'bristles_inner', from: [-2.5, -1.75, 10.5], to: [2.5, 3.25, 15], mat: 'straw' },
  { name: 'bristles_outer', from: [-3.25, -2.5, 13], to: [3.25, 4, 18.5], mat: 'straw' }] }] }] }];

const STRAW = ['#d9b64e', '#c9a23e', '#e8cc6a', '#b58f30'];

const painter = (ctx, r, m) => {
  const { u, v, w, h } = r, rnd = K.rng(23 + u + v * 9), px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  const flat = r.face === 'up' || r.face === 'down';
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    let c;
    if (m === 'wood') c = K.shade('#7a5232', 1 + ((flat ? i : j) % 5 === 0 ? -0.15 : 0) + (rnd() - 0.5) * 0.1);
    else if (m === 'woodDark') c = K.shade('#4e3420', 1 + (rnd() - 0.5) * 0.1);
    else if (m === 'wrap') c = ((i + j) % 3 === 0) ? '#3a2416' : '#5e3a22';
    else if (m === 'rope') c = ((i + j) % 2 === 0) ? '#b8954e' : '#8f6f34';
    else if (m === 'straw') {
      const stripe = (r.face === 'north' || r.face === 'south') ? (i * 3 + j) % 4 : flat ? i % 3 : j % 3;
      if (r.face === 'south' && rnd() < 0.15) continue;
      c = K.shade(STRAW[stripe % 4], 1 + (rnd() - 0.5) * 0.12);
    } else c = '#ff00ff';
    px(i, j, c);
  }
};

module.exports = { name: 'broom', group: 'ability', kind: 'entity', spec, textures: { broom: painter } };
