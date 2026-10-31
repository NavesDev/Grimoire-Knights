// Wind magic 1 (crescent blade): a thin white crescent slash of wind lying flat, flying toward north (-z),
// like a "(" turned forward: a little thicker in the middle, both tips pointed. Three thin layers of
// see-through cubes, brightest on the edge.
const W = load('./wind_painter.js');
const F = load('./fireball.js');

const R = 10, Y = 4, CZ = 6, SPAN = 75, LAYERS = 3;
const arc = (r, cz, a0, a1, n, y = Y) => Array.from({ length: n + 1 }, (_, i) => {
  const a = (a0 + (a1 - a0) * i / n) * Math.PI / 180; return [r * Math.sin(a), y, cz - r * Math.cos(a)];
});
// Each layer sits further back and spans less, so the stack forms a crescent with pointed tips.
const layers = Array.from({ length: LAYERS }, (_, i) => {
  const f = i / (LAYERS - 1), span = SPAN * (1 - 0.4 * f), n = 10;
  const pts = arc(R - i * 0.15, CZ + i * 0.35, -span, span, n, Y + (i % 2 ? 0.08 : -0.08));
  const widths = pts.slice(1).map((_, k) => 0.45 * Math.pow(1 - Math.abs((k + 0.5) / n - 0.5) * 2, 0.6) + 0.15);
  return W.stroke(`layer_${i + 1}`, pts, widths, i === 0 ? 'edge' : 'gust', 0.35);
}).flat();
const spec = [{ name: 'root', origin: [0, 0, 0], children: [{ name: 'blade', origin: [0, Y, 0], cubes: layers }] }];

const wave = F.wave;
// spin (0.5 s loop): the crescent rocks on its flight axis and breathes.
const animations = { spin: { loop: 'loop', length: 0.5, bones: {
  blade: { rotation: [0, 0, wave(720, 6)] },
  root: { scale: [`1 + ${wave(1440, 0.04)}`, 1, `1 + ${wave(1440, 0.08, 90)}`] }
} } };

module.exports = { name: 'wind_blade', group: 'ability', kind: 'entity', spec, animations, textures: { wind_blade: W.painter } };
