"""Particle textures for the Blockbench effects (docs/vfx.md). Run: python3 art/particles/textures.py

Pixel art in the mod's fire palette; flipbooks stack 16x16 frames vertically, like Blockbench expects.
Deterministic: same seed, same PNGs.
"""
import math
import random
from pathlib import Path

from PIL import Image

OUT = Path(__file__).resolve().parents[2] / 'src/main/resources/assets/grimoireknights/vfx/textures/particle'
FIRE = ['#fffbe6', '#fff19a', '#ffd23f', '#ffb12e', '#ff7a12', '#e0420c', '#a81e08']
SMOKE = ['#6a625c', '#58514c', '#4a4440', '#3a3532', '#2c2826']


def rgba(hex_color, alpha=255):
    return tuple(int(hex_color[i:i + 2], 16) for i in (1, 3, 5)) + (alpha,)


def save(img, name):
    OUT.mkdir(parents=True, exist_ok=True)
    img.save(OUT / f'{name}.png')


def flame(frames=8, size=16):
    """A flame tongue that flares up and burns down to embers over the frames."""
    rnd = random.Random(7)
    img = Image.new('RGBA', (size, size * frames))
    for f in range(frames):
        t = f / (frames - 1)
        height = size * (0.95 - 0.55 * t)
        width = size * (0.42 - 0.2 * t)
        for y in range(size):
            for x in range(size):
                up = (size - 1 - y) / height
                if up > 1:
                    continue
                half = width * (1 - up) ** 0.7 * min(1, up * 3 + 0.5) + 0.5
                dx = abs(x - (size - 1) / 2 - math.sin(up * 4 + f) * up * 1.5)
                if dx > half or rnd.random() < 0.12 + 0.4 * t * up:
                    continue
                heat = min(len(FIRE) - 1, int((dx / half) * 3 + up * 3 + t * 2.5))
                img.putpixel((x, f * size + y), rgba(FIRE[heat]))
    save(img, 'fire_burst')


def smoke(frames=8, size=16):
    """A lumpy puff that thins out over the frames."""
    rnd = random.Random(11)
    img = Image.new('RGBA', (size, size * frames))
    blobs = [(rnd.uniform(5, 11), rnd.uniform(5, 11), rnd.uniform(3, 5)) for _ in range(5)]
    for f in range(frames):
        t = f / (frames - 1)
        for y in range(size):
            for x in range(size):
                d = min(math.hypot(x - bx, y - by) / r for bx, by, r in blobs)
                if d > 1 or rnd.random() < 0.15 + 0.6 * t * d:
                    continue
                shade = min(len(SMOKE) - 1, int(d * 3 + t * 2))
                img.putpixel((x, f * size + y), rgba(SMOKE[shade], int(235 * (1 - 0.5 * t))))
    save(img, 'smoke_puff')


def spark(size=8):
    """A hot spark: white core, yellow then orange edge."""
    img = Image.new('RGBA', (size, size))
    c = (size - 1) / 2
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - c, y - c) / (size / 2)
            if d < 0.35:
                img.putpixel((x, y), rgba('#fffbe6'))
            elif d < 0.7:
                img.putpixel((x, y), rgba('#ffd23f'))
            elif d < 1:
                img.putpixel((x, y), rgba('#ff7a12', 160))
    save(img, 'spark')


def glow(size=32):
    """Soft round light for flashes (additive)."""
    img = Image.new('RGBA', (size, size))
    c = (size - 1) / 2
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - c, y - c) / (size / 2)
            if d < 1:
                img.putpixel((x, y), (255, 255, 255, int(255 * (1 - d) ** 2)))
    save(img, 'glow')


def ring(size=64):
    """Shockwave ring seen from above: a ragged band of flame, transparent inside."""
    rnd = random.Random(23)
    img = Image.new('RGBA', (size, size))
    c = (size - 1) / 2
    for y in range(size):
        for x in range(size):
            d = math.hypot(x - c, y - c) / (size / 2)
            edge = 0.86 + 0.06 * math.sin(math.atan2(y - c, x - c) * 9)
            band = abs(d - edge) / 0.12
            if band > 1 or rnd.random() < band * 0.5:
                continue
            heat = min(len(FIRE) - 1, int(band * 4 + rnd.random() * 2))
            img.putpixel((x, y), rgba(FIRE[heat], int(255 * (1 - band ** 2))))
    save(img, 'shockwave')


if __name__ == '__main__':
    flame()
    smoke()
    spark()
    glow()
    ring()
