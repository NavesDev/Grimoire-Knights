// Shared helpers for model scripts. Runs inside Blockbench (see docs/modelos.md).
const FACES = ['north', 'east', 'south', 'west', 'up', 'down'];

const faceSize = (c, f) => {
  const s = [c.to[0] - c.from[0], c.to[1] - c.from[1], c.to[2] - c.from[2]];
  if (f === 'north' || f === 'south') return [s[0], s[1]];
  if (f === 'east' || f === 'west') return [s[2], s[1]];
  return [s[0], s[2]];
};

// Deterministic LCG so every run paints the same pixels.
function rng(seed) {
  let s = seed >>> 0 || 1;
  return () => (s = (s * 1664525 + 1013904223) >>> 0) / 4294967296;
}

function shade(hex, k) {
  const n = parseInt(hex.slice(1), 16);
  const f = v => Math.max(0, Math.min(255, Math.round(v * k)));
  return `rgb(${f(n >> 16 & 255)},${f(n >> 8 & 255)},${f(n & 255)})`;
}

function px(ctx, x, y, col) {
  ctx.fillStyle = col;
  ctx.fillRect(x, y, 1, 1);
}

function noiseFill(ctx, r, base, amt, seed) {
  const rnd = rng(seed + r.u * 131 + r.v * 17);
  for (let i = 0; i < r.w; i++)
    for (let j = 0; j < r.h; j++) px(ctx, r.u + i, r.v + j, shade(base, 1 + (rnd() - 0.5) * amt));
}

// Builds the bone tree from a spec and packs every face into its own UV rect.
// spec: [{name, origin, rotation, cubes: [{name, from, to, origin, rotation, inflate, mat | paint: {face|all: mat}, hide}], children}]
function build(spec, density) {
  Undo.initEdit({ outliner: true, elements: [], textures: [] });
  const cubes = [];
  const mk = (g, parent) => {
    const grp = new Group({ name: g.name, origin: g.origin || [0, 0, 0], rotation: g.rotation || [0, 0, 0] });
    grp.addTo(parent || undefined).init();
    (g.cubes || []).forEach(c => {
      const cube = new Cube({
        name: c.name, from: c.from, to: c.to, origin: c.origin || g.origin || [0, 0, 0],
        rotation: c.rotation || [0, 0, 0], inflate: c.inflate || 0, box_uv: false
      }).addTo(grp).init();
      cubes.push({ cube, spec: c });
    });
    (g.children || []).forEach(ch => mk(ch, grp));
  };
  spec.forEach(g => mk(g));
  const rects = [];
  cubes.forEach(({ cube, spec }) => FACES.forEach(f => {
    const [w, h] = faceSize(spec, f);
    if (w <= 0.001 || h <= 0.001 || (spec.hide || []).includes(f)) {
      cube.faces[f].texture = null;
      return;
    }
    rects.push({ cube, spec, face: f, w: Math.max(1, Math.round(w * density)), h: Math.max(1, Math.round(h * density)) });
  }));
  rects.sort((a, b) => b.h - a.h || b.w - a.w);
  let size = 16, ok = false;
  while (!ok) {
    size *= 2;
    let x = 0, y = 0, rowH = 0;
    ok = true;
    for (const r of rects) {
      if (x + r.w > size) { x = 0; y += rowH; rowH = 0; }
      if (y + r.h > size) { ok = false; break; }
      r.u = x; r.v = y; x += r.w; rowH = Math.max(rowH, r.h);
    }
  }
  Project.texture_width = size;
  Project.texture_height = size;
  rects.forEach(r => { r.cube.faces[r.face].uv = [r.u, r.v, r.u + r.w, r.v + r.h]; });
  Undo.finishEdit('Build model');
  Canvas.updateAll();
  return { rects, size };
}

// painter(ctx, rect, material): rect has u, v, w, h, face and spec (the cube spec).
function render(layout, painter) {
  const cv = document.createElement('canvas');
  cv.width = cv.height = layout.size;
  const ctx = cv.getContext('2d');
  layout.rects.forEach(r => {
    const m = (r.spec.paint && (r.spec.paint[r.face] || r.spec.paint.all)) || r.spec.mat || 'default';
    painter(ctx, r, m);
  });
  return cv;
}

function applyTexture(cv, name) {
  Undo.initEdit({ textures: [], elements: Cube.all });
  Texture.all.slice().forEach(t => t.remove(true));
  const tex = new Texture({ name }).fromDataURL(cv.toDataURL('image/png')).add(false);
  Cube.all.forEach(c => FACES.forEach(f => { if (c.faces[f].texture !== null) c.faces[f].texture = tex.uuid; }));
  Undo.finishEdit('Apply texture');
  Canvas.updateAll();
}

function writePng(cv, file, fs, path) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, Buffer.from(cv.toDataURL('image/png').split(',')[1], 'base64'));
}

// Mirrors a cube spec across x (left/right parts).
const mirrorCube = (c, from = 'left', to = 'right') => ({
  ...c,
  name: c.name.replace(from, to),
  from: [-c.to[0], c.from[1], c.from[2]],
  to: [-c.from[0], c.to[1], c.to[2]],
  origin: c.origin ? [-c.origin[0], c.origin[1], c.origin[2]] : undefined,
  rotation: c.rotation ? [c.rotation[0], -c.rotation[1], -c.rotation[2]] : undefined
});

const mirrorGroup = (g, from = 'left', to = 'right') => ({
  name: g.name.replace(from, to),
  origin: [-g.origin[0], g.origin[1], g.origin[2]],
  rotation: g.rotation ? [g.rotation[0], -g.rotation[1], -g.rotation[2]] : [0, 0, 0],
  cubes: (g.cubes || []).map(c => mirrorCube(c, from, to)),
  children: (g.children || []).map(ch => mirrorGroup(ch, from, to))
});

module.exports = { FACES, rng, shade, px, noiseFill, build, render, applyTexture, writePng, mirrorCube, mirrorGroup };
