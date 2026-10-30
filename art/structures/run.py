"""Generates the mod's structure templates (.nbt). The script is the source; see docs/structures.md.

Run from the repo root: python3 -I art/structures/run.py
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import nbt  # noqa: E402

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'src', 'main', 'resources', 'data',
                   'grimoireknights', 'structure')
DATA_VERSION = 3955  # Minecraft 1.21.1


class Template:
    def __init__(self, sx, sy, sz, fill='minecraft:air'):
        self.size = (sx, sy, sz)
        self.blocks = {}
        self.block_nbt = {}
        self.entities = []
        if fill:
            for x in range(sx):
                for y in range(sy):
                    for z in range(sz):
                        self.blocks[(x, y, z)] = fill

    def set(self, x, y, z, state, block_nbt=None):
        if not (0 <= x < self.size[0] and 0 <= y < self.size[1] and 0 <= z < self.size[2]):
            raise ValueError(f'{(x, y, z)} outside {self.size}')
        self.blocks[(x, y, z)] = state
        if block_nbt is not None:
            self.block_nbt[(x, y, z)] = block_nbt
        else:
            self.block_nbt.pop((x, y, z), None)

    def jigsaw(self, x, y, z, orientation, name, target, pool, final_state, joint='aligned'):
        self.set(x, y, z, f'minecraft:jigsaw[orientation={orientation}]', {
            'id': 'minecraft:jigsaw', 'name': name, 'target': target, 'pool': pool,
            'final_state': final_state, 'joint': joint,
            'placement_priority': 0, 'selection_priority': 0,
        })

    def entity(self, entity_id, x, y, z, extra=None):
        tag = {'id': entity_id, 'PersistenceRequired': nbt.byte(1)}
        tag.update(extra or {})
        self.entities.append({
            'pos': [x + 0.5, float(y), z + 0.5],
            'blockPos': [x, y, z],
            'nbt': tag,
        })

    def paste(self, other, ox, oy, oz):
        for (x, y, z), state in other.blocks.items():
            if state == 'minecraft:structure_void':
                continue
            self.set(x + ox, y + oy, z + oz, state, other.block_nbt.get((x, y, z)))
        for e in other.entities:
            px, py, pz = e['pos']
            bx, by, bz = e['blockPos']
            self.entities.append({'pos': [px + ox, py + oy, pz + oz], 'blockPos': [bx + ox, by + oy, bz + oz],
                                  'nbt': e['nbt']})

    def save(self, name):
        palette, index, blocks = [], {}, []
        for pos in sorted(self.blocks, key=lambda p: (p[1], p[2], p[0])):
            state = self.blocks[pos]
            if state not in index:
                index[state] = len(palette)
                palette.append(parse_state(state))
            entry = {'pos': list(pos), 'state': index[state]}
            if pos in self.block_nbt:
                entry['nbt'] = self.block_nbt[pos]
            blocks.append(entry)
        path = os.path.join(OUT, name + '.nbt')
        os.makedirs(os.path.dirname(path), exist_ok=True)
        nbt.write(path, {
            'DataVersion': DATA_VERSION,
            'size': list(self.size),
            'palette': palette,
            'blocks': blocks,
            'entities': self.entities,
        })
        print('wrote', os.path.relpath(path))


def parse_state(state):
    if '[' not in state:
        return {'Name': state}
    name, props = state[:-1].split('[')
    return {'Name': name, 'Properties': dict(p.split('=') for p in props.split(','))}


# --- Grimoire Tower -------------------------------------------------------------------------------------------
SOURCE = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'source')
SHAFT_TOP = 44         # last layer of the stone shaft; the observation room floor sits on top
SHELF_TOP = 40         # tall shelves lining the shaft
SHELF_BANDS = 8        # every 8 layers a band of planks with a light
GRIMOIRES = range(6, 39, 3)


def load(name):
    """Template from a downloaded build (art/structures/source), states as strings."""
    root = nbt.read(os.path.join(SOURCE, name + '.nbt'))
    t = Template(*root['size'], fill=None)
    palette = [p['Name'] + ('[' + ','.join(f'{k}={v}' for k, v in p['Properties'].items()) + ']'
                            if p.get('Properties') else '') for p in root['palette']]
    for b in root['blocks']:
        t.set(*b['pos'], palette[b['state']], b.get('nbt'))
    return t


def is_air(state):
    return state is None or state == 'minecraft:air'


def shaft_inside(t):
    """Floor cells inside the shaft's wall ring: flood from the middle on a layer, through air, stairs and the stair column."""
    sx, _, sz = t.size
    c = sx // 2
    inside, todo = set(), [(c, c)]
    while todo:
        x, z = todo.pop()
        state = t.blocks.get((x, 2, z))
        column = state and 'log' in state and math.hypot(x - c, z - c) < 4
        if (x, z) in inside or not (is_air(state) or column or 'slab' in state):
            continue
        inside.add((x, z))
        todo += [(x + 1, z), (x - 1, z), (x, z + 1), (x, z - 1)]
    return inside


def grimoire_tower():
    """Observation Tower by maik12490 (Bloxelizer), its shaft turned into the grimoire library."""
    t = load('observation_tower')
    sx, sy, sz = t.size
    c = sx // 2
    inside = shaft_inside(t)
    shaft = {(x, z) for x in range(sx) for z in range(sz) if not is_air(t.blocks.get((x, 2, z)))} | inside
    wall_next = {(x, z) for x, z in inside if any((x + dx, z + dz) not in inside for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))}
    dz = max(z for x, z in inside if x == c) + 1  # south wall, facing the church
    door = [(x, dz - 1) for x in (c - 1, c, c + 1)]  # gap in the shelves behind the door
    for y in range(sy):
        for x in range(sx):
            for z in range(sz):
                if (x, z) in inside and y <= SHAFT_TOP + 1:
                    continue
                # outside the shaft and the whole sky part keep the terrain, like the old tower
                if is_air(t.blocks.get((x, y, z))) and ((x, z) not in shaft or y > SHAFT_TOP):
                    t.set(x, y, z, 'minecraft:structure_void')
    for x, z in inside:
        t.set(x, 0, z, 'minecraft:polished_andesite' if (x + z) % 2 else 'minecraft:stone_bricks')
        for y in range(1, SHAFT_TOP + 1):
            behind = [(x + dx, y, z + dz) for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)) if (x + dx, z + dz) not in inside]
            window = any(t.blocks.get(p, '').startswith('minecraft:glass_pane') for p in behind)
            shelf = (x, z) in wall_next and y <= SHELF_TOP and not window and not ((x, z) in door and y <= 3)
            if shelf and y % SHELF_BANDS == 0:
                t.set(x, y, z, 'minecraft:glowstone' if (x == c or z == c) else 'minecraft:dark_oak_planks')
            else:
                t.set(x, y, z, 'minecraft:bookshelf' if shelf else 'minecraft:air')
        # close the stairwell into the observation room: the room is only seen from outside
        if is_air(t.blocks.get((x, SHAFT_TOP + 1, z))) or 'log' in t.blocks[(x, SHAFT_TOP + 1, z)]:
            t.set(x, SHAFT_TOP + 1, z, 'minecraft:dark_oak_planks')
    # door in the middle of the south wall
    t.set(c, 1, dz, 'minecraft:oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]')
    t.set(c, 2, dz, 'minecraft:oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]')
    # Grimoire Altar in the middle, priest beside it, grimoires circling above
    t.set(c, 1, c, 'grimoireknights:grimoire_altar[facing=south]')
    t.entity('grimoireknights:priest', c + 2, 1, c + 1)
    for y in GRIMOIRES:
        t.set(c, y, c, 'grimoireknights:floating_grimoire')
    for x, z in [(c - 3, c - 3), (c + 3, c - 3), (c - 3, c + 3), (c + 3, c + 3)]:
        t.set(x, 1, z, 'minecraft:lantern[hanging=false,waterlogged=false]')
    return t


# --- Church -----------------------------------------------------------------------------------------------------
def church():
    """Church by teylo (Bloxelizer). The pews are beds, so it is also the optional respawn point."""
    src = load('church')
    sx, sy, sz = src.size
    t = Template(sx, sy, sz)
    t.paste(src, 0, 0, 0)
    # entrance jigsaw past the porch steps, south of the door; the plaza turns the church to face the tower
    t.jigsaw(6, 1, sz - 1, 'south_up', 'grimoireknights:church', 'minecraft:empty', 'minecraft:empty', 'minecraft:air')
    t.entity('grimoireknights:priest', 6, 3, 8)
    return t


# --- Starter village center: plaza with the tower -------------------------------------------------------------
def village_center():
    tower = grimoire_tower()
    # the observation room overhangs the shaft; 7 blocks of plaza around it keep the spawn square out from under it
    off = 7
    size = tower.size[0] + 2 * off
    m = size // 2
    t = Template(size, tower.size[1], size)
    for x in range(size):
        for z in range(size):
            edge = min(x, z, size - 1 - x, size - 1 - z)
            t.set(x, 0, z, 'minecraft:dirt_path' if edge < 2 or (abs(x - m) <= 1 and z > m)
                  else ('minecraft:cobblestone' if (x + z) % 3 == 0 else 'minecraft:stone_bricks'))
    t.paste(tower, off, 0, off)
    # streets leave the plaza west, north and east (vanilla plains streets); the church faces the tower door
    t.jigsaw(0, 1, m, 'west_up', 'minecraft:street', 'minecraft:street', 'minecraft:village/plains/streets',
             'minecraft:structure_void')
    t.jigsaw(m, 1, 0, 'north_up', 'minecraft:street', 'minecraft:street', 'minecraft:village/plains/streets',
             'minecraft:structure_void')
    t.jigsaw(size - 1, 1, m, 'east_up', 'minecraft:street', 'minecraft:street', 'minecraft:village/plains/streets',
             'minecraft:structure_void')
    t.jigsaw(m, 1, size - 1, 'south_up', 'grimoireknights:church_door', 'grimoireknights:church',
             'grimoireknights:starter_village/church', 'minecraft:air')
    for x, z in [(3, 3), (size - 4, 3), (3, size - 4), (size - 4, size - 4)]:
        t.set(x, 1, z, 'minecraft:oak_fence[east=false,north=false,south=false,waterlogged=false,west=false]')
        t.set(x, 2, z, 'minecraft:lantern[hanging=false,waterlogged=false]')
    return t


# --- GameTest floor ---------------------------------------------------------------------------------------------
def empty():
    t = Template(5, 3, 5)
    for x in range(5):
        for z in range(5):
            t.set(x, 0, z, 'minecraft:stone')
    return t


if __name__ == '__main__':
    grimoire_tower().save('tower/grimoire_tower')
    church().save('starter_village/church')
    village_center().save('starter_village/center')
    empty().save('empty')
