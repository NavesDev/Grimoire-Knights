// Shared painter for the anti-magic swords (black blade, chipped edge).
const K = load('./lib.js');

const painter = P => (ctx, r, m) => {
  const { u, v, w, h } = r, rnd = K.rng(31 + u * 3 + v * 7), px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    if (m === 'blade') {
      const edge = i === 0 || i === w - 1;
      if (edge && rnd() < P.chip) continue;
      const blot = Math.sin(i * 1.7 + j * 0.45) + Math.cos(j * 0.9 - i * 0.6);
      const c = edge ? P.edge : (i === 1 || i === w - 2) ? P.bevel : blot > 1.2 ? P.blot : P.blade;
      px(i, j, K.shade(c, 1 + (rnd() - 0.5) * 0.14));
    } else if (m === 'edge') px(i, j, K.shade(P.edge, 1 + (rnd() - 0.5) * 0.12));
    else if (m === 'ridge') px(i, j, K.shade(P.ridge, 1 + (rnd() - 0.5) * 0.1));
    else if (m === 'metal') px(i, j, K.shade(P.metal, 1 + (rnd() - 0.5) * 0.2));
    else if (m === 'grip') px(i, j, ((j + i) % 4 < 2) ? P.grip : P.gripDark);
    else if (m === 'tsuka') px(i, j, (Math.abs((j % 4) - 1.5) + Math.abs(i - (w - 1) / 2) * 0.8 < 1.6) ? P.gripDark : P.grip);
    else px(i, j, '#ff00ff');
  }
};

const anti = { blade: '#26262c', bevel: '#33333b', blot: '#1a1719', edge: '#5b5b66', ridge: '#30303a', metal: '#3a3431', grip: '#4f3324', gripDark: '#2a1a12', chip: 0.18 };

module.exports = { painter, anti };
