// Shared painter for the anti-magic swords (worn black blade, grey patches, dull bronze hilt).
const K = load('./lib.js');

// Smooth value noise in [0, 1], deterministic per seed.
const hash = (x, y, s) => {
  let n = (x * 374761393 + y * 668265263 + s * 2147483647) >>> 0;
  n = ((n ^ (n >>> 13)) * 1274126177) >>> 0;
  return ((n ^ (n >>> 16)) >>> 0) / 4294967296;
};
const valueNoise = (x, y, cell, s) => {
  const gx = Math.floor(x / cell), gy = Math.floor(y / cell), fx = x / cell - gx, fy = y / cell - gy;
  const sm = t => t * t * (3 - 2 * t), a = sm(fx), b = sm(fy);
  const top = hash(gx, gy, s) * (1 - a) + hash(gx + 1, gy, s) * a;
  const bot = hash(gx, gy + 1, s) * (1 - a) + hash(gx + 1, gy + 1, s) * a;
  return top * (1 - b) + bot * b;
};
const patch = (x, y, s) => valueNoise(x, y, 6, s) * 0.7 + valueNoise(x, y, 2.5, s + 7) * 0.3;

const painter = P => (ctx, r, m) => {
  const { u, v, w, h } = r, rnd = K.rng(31 + u * 3 + v * 7), px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    const grain = 1 + (rnd() - 0.5) * 0.12;
    if (m === 'blade' || m === 'bladeRidge') {
      const edge = i === 0 || i === w - 1;
      const chipped = edge && rnd() < P.chip;
      const ridge = m === 'bladeRidge' && Math.abs(i - (w - 1) / 2) < 0.6;
      const worn = patch(u + i, v + j, 5) > 0.58;
      const c = ridge || chipped ? P.ridge : edge ? P.edge : worn ? P.blot : P.blade;
      const scratch = !ridge && rnd() < 0.03 ? 1.25 : 1;
      px(i, j, K.shade(c, grain * scratch));
    } else if (m === 'edge') px(i, j, K.shade(P.edge, grain));
    else if (m === 'ridge') px(i, j, K.shade(P.ridge, grain));
    else if (m === 'metal') px(i, j, K.shade(P.metal, 1 + (rnd() - 0.5) * 0.2));
    else if (m === 'bronze') {
      // Solid tarnished bronze: grime in patches, a few rubbed spots.
      const grime = patch(u + i, v + j, 11) > 0.6 ? 0.78 : 1, rub = rnd() < 0.05 ? 1.2 : 1;
      px(i, j, K.shade(P.bronze, grain * grime * rub));
    }
    else if (m === 'grip') px(i, j, ((j + i) % 4 < 2) ? P.grip : P.gripDark);
    else if (m === 'tsuka') {
      // Stacked diamonds (gripDark) between the dark wrap: widths 1, 3, 1, gap per 4 rows.
      const half = [0, 1, 0, -1][j % 4];
      px(i, j, Math.abs(i - (w - 1) / 2) <= half + 0.01 ? P.gripDark : P.grip);
    }
    else px(i, j, '#ff00ff');
  }
};

const anti = {
  blade: '#17171c', blot: '#33343d', edge: '#2a2a31', ridge: '#0e0e12', metal: '#2b2622',
  bronze: '#4f4529', grip: '#3a2619', gripDark: '#1f140d', chip: 0.12
};

module.exports = { painter, anti, patch };
