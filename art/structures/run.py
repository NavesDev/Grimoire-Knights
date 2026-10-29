"""Generates the mod's structure templates (.nbt). The script is the source; see docs/estruturas.md.

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
TOWER_R = 5            # wall radius; footprint 11x11
TOWER_WALL_TOP = 21    # last wall layer
SHELF_TOP = 17         # tall shelves
DOOR = (5, 10)         # door column (x, z), on the south wall


def dist(x, z, c=TOWER_R):
    return math.hypot(x - c, z - c)


def grimoire_tower():
    size = 2 * TOWER_R + 1
    t = Template(size, TOWER_WALL_TOP + 8, size)
    c = TOWER_R
    for x in range(size):
        for z in range(size):
            d = dist(x, z)
            if d > TOWER_R + 0.5:
                # outside the round wall: leave the terrain as it is
                for y in range(t.size[1]):
                    t.set(x, y, z, 'minecraft:structure_void')
                continue
            t.set(x, 0, z, 'minecraft:polished_andesite' if (x + z) % 2 else 'minecraft:stone_bricks')
            if d > TOWER_R - 0.5:
                for y in range(1, TOWER_WALL_TOP + 1):
                    window = y % 6 == 3 and (x == c or z == c) and (x, z) != DOOR
                    t.set(x, y, z, 'minecraft:glass_pane' if window else
                          ('minecraft:mossy_stone_bricks' if (x * 7 + y * 3 + z) % 11 == 0 else 'minecraft:stone_bricks'))
            elif d > TOWER_R - 1.5:
                # tall shelves lining the wall, with a gap at the door
                for y in range(1, SHELF_TOP + 1):
                    if abs(x - DOOR[0]) <= 1 and z > c and y <= 3:
                        continue
                    t.set(x, y, z, 'minecraft:bookshelf')
            # upper floor slab ring under the roof, open in the middle so the shelves show from below
            if d <= TOWER_R - 0.5 and d > 2.5:
                t.set(x, SHELF_TOP + 1, z, 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
    # door
    dx, dz = DOOR
    t.set(dx, 1, dz, 'minecraft:oak_door[facing=north,half=lower,hinge=left,open=false,powered=false]')
    t.set(dx, 2, dz, 'minecraft:oak_door[facing=north,half=upper,hinge=left,open=false,powered=false]')
    # roof: stepped cone
    for layer in range(7):
        r = TOWER_R + 0.5 - layer * 0.85
        y = TOWER_WALL_TOP + 1 + layer
        for x in range(size):
            for z in range(size):
                d = dist(x, z)
                if r - 1.2 < d <= r:
                    t.set(x, y, z, 'minecraft:deepslate_tiles')
    t.set(c, TOWER_WALL_TOP + 7, c, 'minecraft:deepslate_tile_wall[east=none,north=none,south=none,up=true,waterlogged=false,west=none]')
    # Grimoire Altar in the middle, priest beside it
    t.set(c, 1, c, 'grimoireknights:grimoire_altar[facing=south]')
    t.entity('grimoireknights:priest', c + 2, 1, c + 1)
    # floating grimoires between the shelves
    spots = [(2, 5, 3), (8, 7, 4), (3, 9, 7), (7, 11, 2), (2, 13, 5), (8, 14, 7), (4, 16, 2), (6, 6, 8)]
    for x, y, z in spots:
        t.set(x, y, z, 'grimoireknights:floating_grimoire')
    # light
    for x, z in [(2, 2), (8, 2), (2, 8), (8, 8)]:
        t.set(x, 1, z, 'minecraft:lantern[hanging=false,waterlogged=false]')
    for x, z in [(c, 3), (c, 7), (3, c), (7, c)]:
        t.set(x, TOWER_WALL_TOP, z, 'minecraft:glowstone')
    return t


# --- Church -----------------------------------------------------------------------------------------------------
def church():
    sx, sy, sz = 9, 10, 14
    t = Template(sx, sy, sz)
    # porch row z=0: path and the entrance jigsaw (faces north, back to the plaza)
    for x in range(sx):
        t.set(x, 0, 0, 'minecraft:dirt_path')
    t.jigsaw(4, 1, 0, 'north_up', 'grimoireknights:church', 'minecraft:empty', 'minecraft:empty', 'minecraft:air')
    for x in range(sx):
        for z in range(1, sz):
            t.set(x, 0, z, 'minecraft:cobblestone' if x in (0, sx - 1) or z in (1, sz - 1) else 'minecraft:spruce_planks')
            wall = x in (0, sx - 1) or z in (1, sz - 1)
            for y in range(1, 6):
                if wall:
                    window = y in (2, 3) and (z in (4, 7, 10) and x in (0, sx - 1))
                    t.set(x, y, z, 'minecraft:white_stained_glass_pane' if window else
                          ('minecraft:cobblestone' if y == 1 else 'minecraft:white_terracotta'))
    # roof
    for layer in range(5):
        y = 6 + layer
        for z in range(1, sz):
            for x in (layer, sx - 1 - layer):
                if y < sy:
                    facing = 'east' if x == layer else 'west'
                    t.set(x, y, z, f'minecraft:spruce_stairs[facing={facing},half=bottom,shape=straight,waterlogged=false]')
            if layer == 4 and y < sy:
                t.set(4, y, z, 'minecraft:spruce_slab[type=bottom,waterlogged=false]')
        for x in range(layer + 1, sx - 1 - layer):
            if y < sy:
                for z in (1, sz - 1):
                    t.set(x, y, z, 'minecraft:white_terracotta')
    # door
    t.set(4, 1, 1, 'minecraft:spruce_door[facing=south,half=lower,hinge=left,open=false,powered=false]')
    t.set(4, 2, 1, 'minecraft:spruce_door[facing=south,half=upper,hinge=left,open=false,powered=false]')
    # pews
    for z in (4, 6, 8):
        for x in (2, 3, 5, 6):
            t.set(x, 1, z, 'minecraft:spruce_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
    # altar end: carpet, candles, bell
    for x in range(2, 7):
        t.set(x, 1, 11, 'minecraft:red_carpet')
    t.set(3, 1, 12, 'minecraft:chiseled_stone_bricks')
    t.set(5, 1, 12, 'minecraft:chiseled_stone_bricks')
    t.set(3, 2, 12, 'minecraft:candle[candles=3,lit=true,waterlogged=false]')
    t.set(5, 2, 12, 'minecraft:candle[candles=3,lit=true,waterlogged=false]')
    t.set(4, 1, 12, 'minecraft:lectern[facing=north,has_book=false,powered=false]')
    # optional respawn: a bed by the side wall
    t.set(1, 1, 3, 'minecraft:white_bed[facing=north,occupied=false,part=foot]')
    t.set(1, 1, 2, 'minecraft:white_bed[facing=north,occupied=false,part=head]')
    for x, z in [(1, 6), (7, 6), (1, 10), (7, 10)]:
        t.set(x, 4, z, 'minecraft:lantern[hanging=false,waterlogged=false]')
    t.entity('grimoireknights:priest', 4, 1, 10)
    return t


# --- Starter village center: plaza with the tower -------------------------------------------------------------
def village_center():
    size = 23
    tower = grimoire_tower()
    t = Template(size, tower.size[1], size)
    for x in range(size):
        for z in range(size):
            edge = min(x, z, size - 1 - x, size - 1 - z)
            t.set(x, 0, z, 'minecraft:dirt_path' if edge < 2 or (abs(x - 11) <= 1 and z > 15)
                  else ('minecraft:cobblestone' if (x + z) % 3 == 0 else 'minecraft:stone_bricks'))
    off = (size - tower.size[0]) // 2
    t.paste(tower, off, 0, off)
    # streets leave the plaza west, north and east (vanilla plains streets); the church faces the tower door
    t.jigsaw(0, 1, 11, 'west_up', 'minecraft:street', 'minecraft:street', 'minecraft:village/plains/streets',
             'minecraft:structure_void')
    t.jigsaw(11, 1, 0, 'north_up', 'minecraft:street', 'minecraft:street', 'minecraft:village/plains/streets',
             'minecraft:structure_void')
    t.jigsaw(size - 1, 1, 11, 'east_up', 'minecraft:street', 'minecraft:street', 'minecraft:village/plains/streets',
             'minecraft:structure_void')
    t.jigsaw(11, 1, size - 1, 'south_up', 'grimoireknights:church_door', 'grimoireknights:church',
             'grimoireknights:starter_village/church', 'minecraft:air')
    for x, z in [(3, 3), (19, 3), (3, 19), (19, 19)]:
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
