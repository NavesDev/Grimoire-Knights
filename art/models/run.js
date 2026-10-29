// Generates models inside Blockbench: for each model, opens a GeckoLib project, builds it,
// writes the textures and geo to assets/ and the .bbmodel to art/blockbench/.
// Usage: see docs/modelos.md.
const MODELS = [
  'grimoire', 'fireball', 'order_mage', 'renegade_captain', 'greatsword', 'katana', 'reverse_blade', 'broom',
  'squad_mantle', 'grimoire_altar', 'magic_torch', 'squad_banner', 'mage_tower_core', 'griffin', 'demon_form'
];
const DENSITY = 2; // texture pixels per model unit, same for every model

const K = load('./lib.js');
const ROOT = path.resolve(__dirname, '../..');
const ASSETS = path.join(ROOT, 'src/main/resources/assets/grimoireknights');

function generate(id) {
  const def = load('./' + id + '.js');
  const old = ModelProject.all.find(p => p.name === def.name);
  if (old) old.close(true);
  newProject(Formats.geckolib_model);
  Project.name = def.name;
  Project.model_identifier = def.name;
  Project.geckolib_modid = 'grimoireknights';
  Project.geckolib_model_type = def.kind === 'block' ? 'Block' : 'Entity';

  const layout = K.build(def.spec, DENSITY);
  const texDir = path.join(ASSETS, 'textures', def.kind === 'block' ? 'block' : 'entity', def.group);
  const written = [];
  Object.entries(def.textures).forEach(([name, painter]) => {
    const file = path.join(texDir, name + '.png');
    K.writePng(K.render(layout, painter), file, fs, path);
    written.push(path.relative(ROOT, file));
  });
  const [firstName, firstPainter] = Object.entries(def.textures)[0];
  K.applyTexture(K.render(layout, def.preview || firstPainter), def.preview ? def.name + '_preview' : firstName);

  const geo = path.join(ASSETS, 'geo', def.group, def.name + '.geo.json');
  fs.mkdirSync(path.dirname(geo), { recursive: true });
  fs.writeFileSync(geo, Codecs.bedrock.compile());
  const bb = path.join(ROOT, 'art/blockbench', def.name + '.bbmodel');
  fs.mkdirSync(path.dirname(bb), { recursive: true });
  fs.writeFileSync(bb, Codecs.project.compile({ compressed: false }));
  Project.save_path = bb;
  Project.saved = true;
  written.push(path.relative(ROOT, geo), path.relative(ROOT, bb));
  return written;
}

// ids: list of model names, or omit for all.
module.exports = ids => (ids && ids.length ? ids : MODELS).map(id => ({ id, files: generate(id) }));
