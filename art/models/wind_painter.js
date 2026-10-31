// Shared paint and shapes for the wind spells: white air, never green, rendered with a translucent render
// type (see docs/models.md). Two ways to build wind: thin cubes chained into curls and strokes, painted
// with the see-through gust material; or flat planes (zero-thickness cubes) with the gust drawn on the
// texture as smooth tapered strokes.
const K = load('./lib.js');
const WHITE = ['#ffffff', '#f4f8fb', '#e9f0f5'];
const MIST = ['#dfe8ef', '#cfdbe4', '#f4f8fb'];
const STONE = ['#7d7a74', '#6a6762', '#8e8a82', '#5a5853'];
const pick = (rnd, arr) => arr[Math.floor(rnd() * arr.length)];
const HALO = 'rgba(214, 228, 240, 0.45)';
const CORE = '#ffffff';

// Cube materials: gust (see-through white streaks: brighter along the middle, fading and broken at the
// sides), edge (solid bright), wind (streaky with holes), mist (sparse haze), stone (rock chips).
function paintCube(ctx, r, m) {
  const { u, v, w, h } = r, rnd = K.rng(211 + u * 7 + v * 13), px = (x, y, c) => K.px(ctx, u + x, v + y, c);
  const along = w >= h;
  for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
    const streak = ((along ? j : i) * 5 + Math.floor((along ? i : j) / 3)) % 4;
    if (m === 'gust') {
      const across = along ? (h > 1 ? j / (h - 1) : 0.5) : (w > 1 ? i / (w - 1) : 0.5), mid = 1 - Math.abs(across - 0.5) * 2;
      if (rnd() < 0.18 * (1 - mid) + 0.06) continue;
      px(i, j, `rgba(${streak === 1 ? '255,255,255' : '236,243,248'}, ${(0.35 + 0.55 * mid * (0.7 + 0.3 * rnd())).toFixed(2)})`);
    } else if (m === 'edge') px(i, j, pick(rnd, WHITE));
    else if (m === 'wind') { if (streak === 0 || rnd() < 0.25) continue; px(i, j, streak === 1 ? '#ffffff' : pick(rnd, MIST)); }
    else if (m === 'mist') { if (rnd() < 0.6) continue; px(i, j, pick(rnd, MIST)); }
    else if (m === 'stone') px(i, j, K.shade(pick(rnd, STONE), 1 + (rnd() - 0.5) * 0.15));
    else px(i, j, '#ff00ff');
  }
}

// Maps plane coordinates (model units) to texture pixels for one face of a flat cube, as seen from that
// face's own side. Planes: yz (east/west faces, coords [z, y]), xy (north/south, [x, y]), xz (up/down, [x, z]).
function faceMap(r) {
  const { from, to } = r.spec, f = r.face;
  const span = f === 'east' || f === 'west' ? to[2] - from[2] : to[0] - from[0], s = r.w / span;
  if (f === 'east') return (a, b) => [r.u + (to[2] - a) * s, r.v + (to[1] - b) * s, s];
  if (f === 'west') return (a, b) => [r.u + (a - from[2]) * s, r.v + (to[1] - b) * s, s];
  if (f === 'north') return (a, b) => [r.u + (to[0] - a) * s, r.v + (to[1] - b) * s, s];
  if (f === 'south') return (a, b) => [r.u + (a - from[0]) * s, r.v + (to[1] - b) * s, s];
  if (f === 'up') return (a, b) => [r.u + (a - from[0]) * s, r.v + (b - from[2]) * s, s];
  return (a, b) => [r.u + (a - from[0]) * s, r.v + (to[2] - b) * s, s];
}

// Pen for one face: draws in plane coordinates. gust draws a stroke through points [[a, b]] whose width
// goes from w0 to w1 (model units), white core over a soft halo; blob fills a closed outline.
function pen(ctx, r) {
  const map = faceMap(r);
  const segment = (pts, w0, w1, color, extra) => {
    ctx.strokeStyle = color;
    ctx.lineCap = 'round';
    for (let i = 1; i < pts.length; i++) {
      const [x0, y0, s] = map(...pts[i - 1]), [x1, y1] = map(...pts[i]), t = (i - 0.5) / (pts.length - 1);
      ctx.lineWidth = Math.max(1, ((w0 + (w1 - w0) * t) * extra) * s);
      ctx.beginPath(); ctx.moveTo(x0, y0); ctx.lineTo(x1, y1); ctx.stroke();
    }
  };
  return {
    gust: (pts, w0, w1 = 0.1) => { segment(pts, w0, w1, HALO, 2.2); segment(pts, w0, w1, CORE, 1); },
    blob: (pts, haloWidth = 0.5) => {
      const p = pts.map(q => map(...q));
      ctx.beginPath(); p.forEach(([x, y], i) => (i ? ctx.lineTo(x, y) : ctx.moveTo(x, y))); ctx.closePath();
      ctx.lineJoin = 'round'; ctx.strokeStyle = HALO; ctx.lineWidth = haloWidth * p[0][2]; ctx.stroke();
      ctx.fillStyle = CORE; ctx.fill();
    }
  };
}

// painter(shapes): cubes whose material is 'shape:<name>' get shapes[name](pen, rect) drawn on each face,
// clipped to the face; every other material is painted as a cube.
const planePainter = shapes => (ctx, r, m) => {
  if (!m.startsWith('shape:')) return paintCube(ctx, r, m);
  ctx.save();
  ctx.beginPath(); ctx.rect(r.u, r.v, r.w, r.h); ctx.clip();
  shapes[m.slice(6)](pen(ctx, r), r);
  ctx.restore();
};

// Flat cube in a plane through the center c: yz spans [z0, z1] x [y0, y1] at x = c[0], and so on.
const plane = (name, kind, a0, a1, b0, b1, at, shape, extra = {}) => ({
  name, mat: 'shape:' + shape, res: 3, ...extra,
  from: kind === 'yz' ? [at, b0, a0] : kind === 'xy' ? [a0, b0, at] : [a0, at, b0],
  to: kind === 'yz' ? [at, b1, a1] : kind === 'xy' ? [a1, b1, at] : [a1, at, b1]
});

// Points of a curl: from `at` heading `dir` degrees (0 = +b, 90 = +a), turning `turn` degrees per unit
// travelled and tightening as it goes, for `length` units. Gives the hooked ends of gusts.
function curl(at, dir, turn, length, step = 0.4) {
  const pts = [at];
  let [a, b] = at, d = dir;
  for (let s = 0; s < length; s += step) {
    a += Math.sin(d * Math.PI / 180) * step; b += Math.cos(d * Math.PI / 180) * step;
    d += turn * step * (1 + 1.5 * s / length);
    pts.push([a, b]);
  }
  return pts;
}

// Cube shapes. Rotations: +x turns the y axis toward +z, +z turns it toward -x, +y turns the x axis toward -z.
const DEG = 180 / Math.PI;
const r2 = n => Math.round(n * 100) / 100;

// Curl of cubes in the plane facing north/south (x, y): starts at `at` heading `dir` degrees (0 = up,
// 90 = +x), turning `turn` degrees on the first segment and tighter on each next one while the segments
// shrink and thin out, so it ends in a hook like a gust.
function curlXY(name, at, dir, turn, n, len, width, mat = 'gust', depth = width) {
  const cubes = [];
  let [x, y] = at, a = dir, l = len, w = width;
  for (let k = 0; k < n; k++) {
    const rad = a / DEG, nx = x + Math.sin(rad) * l, ny = y + Math.cos(rad) * l, cx = (x + nx) / 2, cy = (y + ny) / 2;
    cubes.push({ name: `${name}_${k + 1}`, from: [r2(cx - w / 2), r2(cy - l * 0.6), r2(at[2] - depth / 2)], to: [r2(cx + w / 2), r2(cy + l * 0.6), r2(at[2] + depth / 2)],
      origin: [r2(cx), r2(cy), at[2]], rotation: [0, 0, r2(-a)], mat });
    x = nx; y = ny; a += turn * Math.pow(1.25, k); l *= 0.8; w = Math.max(0.25, w * 0.78);
  }
  return cubes;
}

// Same in the side plane (z, y): dir 0 = up, 90 = +z.
function curlZY(name, at, dir, turn, n, len, width, mat = 'gust') {
  const cubes = [];
  let [, y, z] = at, a = dir, l = len, w = width;
  for (let k = 0; k < n; k++) {
    const rad = a / DEG, nz = z + Math.sin(rad) * l, ny = y + Math.cos(rad) * l, cz = (z + nz) / 2, cy = (y + ny) / 2;
    cubes.push({ name: `${name}_${k + 1}`, from: [r2(at[0] - w / 2), r2(cy - l * 0.6), r2(cz - w / 2)], to: [r2(at[0] + w / 2), r2(cy + l * 0.6), r2(cz + w / 2)],
      origin: [at[0], r2(cy), r2(cz)], rotation: [r2(a), 0, 0], mat });
    z = nz; y = ny; a += turn * Math.pow(1.25, k); l *= 0.8; w = Math.max(0.25, w * 0.78);
  }
  return cubes;
}

// Stroke of cubes through points [[x, y, z]] lying near the horizontal plane: each segment is a cube along x
// turned on y to follow the stroke. widths: one per segment.
function stroke(name, pts, widths, mat = 'gust', thick = 0.5) {
  return pts.slice(1).map((p, k) => {
    const q = pts[k], dx = p[0] - q[0], dz = p[2] - q[2], len = Math.hypot(dx, dz) * 1.2, w = widths[k];
    const c = [(p[0] + q[0]) / 2, (p[1] + q[1]) / 2, (p[2] + q[2]) / 2];
    return { name: `${name}_${k + 1}`, from: [r2(c[0] - len / 2), r2(c[1] - thick / 2), r2(c[2] - w / 2)], to: [r2(c[0] + len / 2), r2(c[1] + thick / 2), r2(c[2] + w / 2)],
      origin: c.map(r2), rotation: [0, r2(-Math.atan2(dz, dx) * DEG), 0], mat };
  });
}

module.exports = { painter: planePainter({}), planePainter, plane, curl, curlXY, curlZY, stroke };
