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

ICONS = {'element/fire': FIRE, 'ability/fireball': FIREBALL}


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
