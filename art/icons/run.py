"""Generates the mod's 16x16 GUI icons (element and ability). The script is the source.

Run from the repo root: python3 -I art/icons/run.py
"""
import os
from PIL import Image

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'src', 'main', 'resources', 'assets',
                   'grimoireknights', 'textures', 'gui')

# One char per pixel; '.' is transparent.
FIRE = {
    'palette': {'k': '#3a1204', 'r': '#c8320f', 'o': '#f0701e', 'y': '#ffb43c', 'w': '#fff2b0'},
    'rows': [
        '.......k........',
        '......kok.......',
        '......kook......',
        '.....korok..k...',
        '....koroook.kk..',
        '...korooyok.kok.',
        '..korooyyookkrok',
        '..kroooyyoookrok',
        '.krooyyyyyoorrok',
        '.kroyywwyyyoorok',
        '.kroyywwwyyyorok',
        '.krooywwwwyyorok',
        '..kroyywwwyyork.',
        '..krooyyyyyoork.',
        '...kkrrooorrkk..',
        '.....kkkkkkk....',
    ],
}

# Same flame on a darker ground for the ability slot (fireball).
FIREBALL = {
    'palette': {'k': '#3a1204', 'r': '#c8320f', 'o': '#f0701e', 'y': '#ffb43c', 'w': '#fff2b0'},
    'rows': [
        '................',
        '.....kkkkkk.....',
        '...kkrrrrrrkk...',
        '..krroooooorrk..',
        '..kroooyyyoork..',
        '.kroooyyyyyoork.',
        '.krooyywwyyyork.',
        '.krooywwwwyyork.',
        '.krooywwwwyyork.',
        '.krooyywwyyyork.',
        '.kroooyyyyyoork.',
        '..kroooyyyoork..',
        '..krroooooorrk..',
        '...kkrrrrrrkk...',
        '.....kkkkkk.....',
        '................',
    ],
}

FIRE_PAL = {'k': '#3a1204', 'r': '#c8320f', 'o': '#f0701e', 'y': '#ffb43c', 'w': '#fff2b0'}
WIND_PAL = {'k': '#3c4a5a', 'g': '#8fa6bd', 'l': '#cfdeec', 'w': '#ffffff'}

# Fire, magic 2: an open hand of fire, fingers up.
FLAME_ARM = {'palette': FIRE_PAL, 'rows': [
    '.....k.k.k......',
    '....kokokok.....',
    '....kokokok.....',
    '....kokokok.k...',
    '....kokokokkok..',
    '....koyoyokok...',
    '.k..koyyyyookk..',
    'kok.koyyyyyyok..',
    '.kokkoywwwyyok..',
    '..koooywwwyyok..',
    '..kooyywwyyook..',
    '...kooyyyyook...',
    '....kroooork....',
    '.....krrrrk.....',
    '.....krrrrk.....',
    '......kkkk......',
]}

# Fire, magic 3: embers bursting out of a hot core.
EMBER_BURST = {'palette': FIRE_PAL, 'rows': [
    '.......y........',
    '..o....o....o...',
    '...o...o...o....',
    '....r..r..r.....',
    '.y...kkkkk...y..',
    '..ork.ooo.kro...',
    '....kooyook.....',
    'yoorkoywyokrrooy',
    '....kooyook.....',
    '..ork.ooo.kro...',
    '.y...kkkkk...y..',
    '....r..r..r.....',
    '...o...o...o....',
    '..o....o....o...',
    '.......y........',
    '................',
]}

# Wind element: three white gusts curling.
WIND = {'palette': WIND_PAL, 'rows': [
    '................',
    '..........lll...',
    '.........l...l..',
    '.lllllllll....l.',
    'wwwwwwwwww...l..',
    '..........lll...',
    '...gllllllll....',
    '..wwwwwwwwwwwl..',
    '.............l..',
    '....lllllll.l...',
    '...wwwwwwwwl....',
    '........l.......',
    '.......l.l......',
    '........l.......',
    '................',
    '................',
]}

# Wind, magic 1: a crescent slash like "(".
CRESCENT_BLADE = {'palette': WIND_PAL, 'rows': [
    '.........ll.....',
    '.......lwl......',
    '......lwl.......',
    '.....lwwl.......',
    '....lwwl........',
    '....lwwl........',
    '...lwwwl........',
    '...lwwwl........',
    '...lwwwl........',
    '...lwwwl........',
    '....lwwl........',
    '....lwwl........',
    '.....lwwl.......',
    '......lwl.......',
    '.......lwl......',
    '.........ll.....',
]}

# Wind, magic 2: a tornado lying on its side.
TORNADO_FANG = {'palette': WIND_PAL, 'rows': [
    '................',
    '...........ll...',
    '.......llllwwl..',
    '....lllwwwwwwwl.',
    '..llwwwgggwwwwl.',
    '.lwwgg...gggwl..',
    'lwwg..lll..gwl..',
    'lwg..lwwwl..gwl.',
    'lwg..lwwwl..gwl.',
    'lwwg..lll..gwl..',
    '.lwwgg...gggwl..',
    '..llwwwgggwwwwl.',
    '....lllwwwwwwwl.',
    '.......llllwwl..',
    '...........ll...',
    '................',
]}

# Wind, magic 3: a white bow with an arrow nocked.
GALE_BOW = {'palette': WIND_PAL, 'rows': [
    '.....ll.........',
    '....lwwl........',
    '...lwl.l........',
    '...wl...l.......',
    '..lw.....l......',
    '..wl......l.....',
    '..wl.......l....',
    'wwwwwwwwwwwwwwl.',
    '..wl.......l....',
    '..wl......l.....',
    '..lw.....l......',
    '...wl...l.......',
    '...lwl.l........',
    '....lwwl........',
    '.....ll.........',
    '................',
]}

# Body dash: a runner's streaks.
DASH = {'palette': WIND_PAL, 'rows': [
    '................',
    '..........kk....',
    '.........kwwk...',
    '.........kwwk...',
    '..........kk....',
    '.llll....kwk....',
    '........kwwwk...',
    'llllll.kwkwwk...',
    '.......k.kwwk...',
    '..llll...kwk....',
    '.........kwk....',
    '........kw.wk...',
    '.......kw...wk..',
    '......kw.....k..',
    '................',
    '................',
]}

ICONS = {'element/fire': FIRE, 'element/wind': WIND, 'ability/fireball': FIREBALL, 'ability/flame_arm': FLAME_ARM,
         'ability/ember_burst': EMBER_BURST, 'ability/crescent_blade': CRESCENT_BLADE, 'ability/tornado_fang': TORNADO_FANG,
         'ability/gale_bow': GALE_BOW, 'ability/dash': DASH}


def render(icon):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(icon['rows']):
        assert len(row) == 16, (y, row)
        for x, ch in enumerate(row):
            if ch != '.':
                img.putpixel((x, y), Image.new('RGB', (1, 1), icon['palette'][ch]).getpixel((0, 0)) + (255,))
    return img


if __name__ == '__main__':
    for name, icon in ICONS.items():
        path = os.path.join(OUT, name + '.png')
        os.makedirs(os.path.dirname(path), exist_ok=True)
        render(icon).save(path)
        print('wrote', os.path.relpath(path))
