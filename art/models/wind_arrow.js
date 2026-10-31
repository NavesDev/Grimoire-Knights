// Wind magic 3 arrow (white gale bow): a big white arrow of wind flying toward north (-z), spinning on its
// shaft. Thin see-through cubes: the shaft, a ">" head of four barbs swept back from the point (with a
// smaller one behind it), little wind hooks curling off the shaft and wisps at the tail.
// arrow(name, y, z) is shared with wind_bow, which shows one nocked while drawing.
const W = load('./wind_painter.js');

const LENGTH = 22, HOOKS = 6;
// ">" head: four barbs from the point at `tip`, swept back `angle` degrees, `len` long.
const chevron = (name, y, tip, len, angle, width) => [[0, angle, 0], [0, -angle, 0], [angle, 0, 0], [-angle, 0, 0]].map((rotation, k) => ({
  name: `${name}_${k + 1}`, from: [-width / 2, y - width / 2, tip], to: [width / 2, y + width / 2, tip + len], origin: [0, y, tip], rotation, mat: 'gust' }));

const arrow = (name, y, z) => {
  const tip = z - LENGTH;
  return { name, origin: [0, y, z], cubes: [
    { name: name + '_shaft', from: [-0.25, y - 0.25, tip + 0.5], to: [0.25, y + 0.25, z + 1], mat: 'gust' },
    { name: name + '_point', from: [-0.3, y - 0.3, tip - 0.6], to: [0.3, y + 0.3, tip + 1], mat: 'edge' },
    ...chevron(name + '_barb', y, tip, 5, 30, 0.6),
    ...chevron(name + '_barb_back', y, tip + 2.5, 3.5, 34, 0.45),
    ...Array.from({ length: HOOKS }, (_, k) => {
      const hz = tip + 7 + k * 2.4, up = k % 2 === 0;
      return k % 3 === 2
        ? W.curlXY(`${name}_hook_${k + 1}`, [0, y, hz], up ? 60 : 240, 40, 5, 1.7, 0.45)
        : W.curlZY(`${name}_hook_${k + 1}`, [0, y, hz], up ? 20 : 160, up ? 45 : -45, 5, 1.8, 0.45);
    }).flat(),
    ...W.curlZY(name + '_tail_up', [0, y, z], 40, 18, 4, 2, 0.4),
    ...W.curlZY(name + '_tail_down', [0, y, z], 140, -18, 4, 2, 0.4)
  ] };
};

const Y = 2;
const spec = [{ name: 'root', origin: [0, 0, 0], children: [arrow('arrow', Y, 10)] }];

// fly (0.5 s loop): spins around the shaft.
const animations = { fly: { loop: 'loop', length: 0.5, bones: { arrow: { rotation: [0, 0, 'query.anim_time * 720'] } } } };

module.exports = { name: 'wind_arrow', group: 'ability', kind: 'entity', spec, animations, arrow, textures: { wind_arrow: W.painter } };
