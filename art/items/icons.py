"""Inventory icons (16x16 pixel art). Run: python3 art/items/icons.py

Deterministic: same seed, same PNGs. Colors follow the 3D models (broom.js) and the mana HUD.
"""
import math
import random
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parents[2] / 'src/main/resources/assets/grimoireknights/textures/item'
OUTLINE = (24, 18, 30, 255)


def rgba(hex_color, alpha=255):
    return tuple(int(hex_color[i:i + 2], 16) for i in (1, 3, 5)) + (alpha,)


def outline(img):
    """Dark 1px outline around every opaque pixel, like vanilla items."""
    src = img.copy()
    for y in range(16):
        for x in range(16):
            if src.getpixel((x, y))[3]:
                continue
            if any(0 <= x + dx < 16 and 0 <= y + dy < 16 and src.getpixel((x + dx, y + dy))[3] == 255
                   for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                img.putpixel((x, y), OUTLINE)


def save(img, name):
    OUT.mkdir(parents=True, exist_ok=True)
    img.save(OUT / f'{name}.png')


def broom():
    """The flying broom (broom.js) laid diagonally: cap top right, straw bristles bottom left."""
    img = Image.new('RGBA', (16, 16))
    wood, dark, wrap, rope = '#7a5232', '#4e3420', '#5e3a22', '#b8954e'
    straw = ['#d9b64e', '#c9a23e', '#e8cc6a', '#b58f30']
    for k in range(4, 13):  # handle, from (14, 1) down to (6, 9)
        x, y = 15 - k + 3, k - 3
        img.putpixel((x, y), rgba(dark if k < 5 else wrap if 7 <= k <= 8 else wood))
        if k >= 5:
            img.putpixel((x - 1, y), rgba('#5c3c24' if not 7 <= k <= 8 else '#3a2416'))
    for x, y in ((6, 9), (5, 9), (6, 10), (5, 10)):
        img.putpixel((x, y), rgba(rope))
    rnd = random.Random(5)
    for y in range(9, 16):  # bristles fan out from the binding toward the bottom left corner
        for x in range(0, 8):
            u = ((5.5 - x) + (y - 9.5)) / math.sqrt(2)
            v = ((x - 5.5) + (y - 9.5)) / math.sqrt(2)
            if u < 0.9 or u > 7.6 or abs(v) > 1.2 + u * 0.5 or img.getpixel((x, y))[3]:
                continue
            img.putpixel((x, y), rgba(straw[(int(v * 2) + rnd.randrange(2)) % 4]))
    outline(img)
    save(img, 'broom')


def mana_dust():
    """A small heap of glowing blue mana dust with a few sparkles above it."""
    img = Image.new('RGBA', (16, 16))
    rnd = random.Random(9)
    shades = ['#2a3f99', '#3a5fd9', '#4f7cff', '#8fb0ff', '#d6e2ff']
    for y in range(8, 15):
        half = (y - 7) * 1.1
        for x in range(16):
            dx = abs(x - 7.5)
            if dx > half or rnd.random() < 0.08:
                continue
            light = 1 - (y - 8) / 7 * 0.6 - dx / 8 * 0.4 + (rnd.random() - 0.5) * 0.5
            img.putpixel((x, y), rgba(shades[max(0, min(4, int(light * 4.5)))]))
    for x, y in ((4, 4), (11, 3), (8, 6)):
        img.putpixel((x, y), rgba('#ffffff'))
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            img.putpixel((x + dx, y + dy), rgba('#8fb0ff', 200))
    outline(img)
    save(img, 'mana_dust')


if __name__ == '__main__':
    broom()
    mana_dust()
