"""Armor and outfit textures: 64x32 model layers, 16x16 icons and the armor toggle sprites. Run: python3 art/armor/textures.py

Deterministic pixel art painted on the vanilla humanoid armor UV layout (layer 1: helmet, chestplate, boots;
layer 2: leggings). Outfits (cosmetic accessories) reuse the same layout, drawn by their Curios renderer.
"""
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2] / 'src/main/resources/assets/grimoireknights/textures'
OUTLINE = (24, 18, 30, 255)

# Cuboid UV: (u, v, w, h, d) -> faces (x0, y0, x1, y1)
def faces(u, v, w, h, d):
    return {
        'top': (u + d, v, u + d + w, v + d), 'bottom': (u + d + w, v, u + d + 2 * w, v + d),
        'right': (u, v + d, u + d, v + d + h), 'front': (u + d, v + d, u + d + w, v + d + h),
        'left': (u + d + w, v + d, u + 2 * d + w, v + d + h), 'back': (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
    }

HEAD, BODY, ARM, LEG = faces(0, 0, 8, 8, 8), faces(16, 16, 8, 12, 4), faces(40, 16, 4, 12, 4), faces(0, 16, 4, 12, 4)


def rgba(hex_color, alpha=255):
    return tuple(int(hex_color[i:i + 2], 16) for i in (1, 3, 5)) + (alpha,)


def shade(color, k):
    return tuple(max(0, min(255, int(c * k))) for c in color[:3]) + (color[3],)


class Palette:
    def __init__(self, base, trim, accent, seed):
        self.base, self.trim, self.accent = rgba(base), rgba(trim), rgba(accent)
        self.rnd = random.Random(seed)

    def metal(self, y, y0, y1):
        """Light from the top, a little noise like vanilla metal."""
        t = (y - y0) / max(1, y1 - y0 - 1)
        return shade(self.base, 1.15 - 0.35 * t + self.rnd.uniform(-0.04, 0.04))


def plate(img, box, pal, rows=None, border=True):
    x0, y0, x1, y1 = box
    ys = range(y0, y1) if rows is None else range(max(y0, y1 - rows), y1)
    for y in ys:
        for x in range(x0, x1):
            edge = border and (x in (x0, x1 - 1) or y in (ys.start, y1 - 1))
            img.putpixel((x, y), shade(pal.metal(y, y0, y1), 0.7) if edge else pal.metal(y, y0, y1))


def line(img, box, y, color):
    x0, _, x1, _ = box
    for x in range(x0, x1):
        img.putpixel((x, y), color)


def armor(name, pal, gem):
    one = Image.new('RGBA', (64, 32))
    for face, box in HEAD.items():  # closed helm with a dark visor slit and a trim band
        plate(one, box, pal)
        line(one, box, box[1] + 1, pal.trim)
    fx0, fy0, fx1, _ = HEAD['front']
    for x in range(fx0 + 1, fx1 - 1):
        one.putpixel((x, fy0 + 3), OUTLINE)
        one.putpixel((x, fy0 + 4), shade(pal.base, 0.35) if x in (fx0 + 3, fx1 - 4) else OUTLINE)
    one.putpixel(((fx0 + fx1) // 2, fy0 + 1), gem)
    for box in BODY.values():  # breastplate: ridge, collar trim, belt
        plate(one, box, pal)
        line(one, box, box[1], pal.trim)
    bx0, by0, bx1, by1 = BODY['front']
    for y in range(by0 + 1, by1 - 2):
        one.putpixel(((bx0 + bx1) // 2 - 1, y), shade(pal.base, 1.25))
    line(one, BODY['front'], by1 - 2, pal.trim)
    one.putpixel(((bx0 + bx1) // 2 - 1, by0 + 4), gem)
    one.putpixel(((bx0 + bx1) // 2, by0 + 4), gem)
    for face, box in ARM.items():  # pauldron on top, bracer at the wrist
        plate(one, box, pal)
        if face not in ('top', 'bottom'):
            line(one, box, box[1] + 3, pal.trim)
            line(one, box, box[3] - 3, pal.trim)
    for face, box in LEG.items():  # boots: lower part of the legs
        if face == 'top':
            continue
        plate(one, box, pal, rows=None if face == 'bottom' else 5)
        if face != 'bottom':
            line(one, box, box[3] - 5, pal.trim)

    two = Image.new('RGBA', (64, 32))
    for face, box in BODY.items():  # leggings: belt only on the body
        if face in ('top', 'bottom'):
            continue
        plate(two, box, pal, rows=3)
        line(two, box, box[3] - 3, pal.trim)
    for face, box in LEG.items():
        if face == 'top':
            continue
        plate(two, box, pal)
        if face != 'bottom':
            line(two, box, box[1] + 5, pal.trim)  # knee guard
    lx0, ly0, lx1, _ = LEG['front']
    two.putpixel((lx0 + 1, ly0 + 6), gem)

    out = ROOT / 'models/armor'
    out.mkdir(parents=True, exist_ok=True)
    one.save(out / f'{name}_layer_1.png')
    two.save(out / f'{name}_layer_2.png')


# 16x16 icon silhouettes ('#' metal, 't' trim, 'g' gem)
ICONS = {
    'helmet': ['', '', '', '    tttttttt', '   #########', '   ###g#####', '   ##     ##', '   ## ### ##', '   ##     ##', '   ###   ###'],
    'chestplate': ['', '', '  ttt    ttt', '  ###t  t###', '  ##########', '   ########', '   ###gg###', '   ########', '   ########', '   ########', '   tttttttt', '   ########', '   ########'],
    'leggings': ['', '', '   tttttttttt', '   ##########', '   ####  ####', '   ###    ###', '   t##    ##t', '   ###    ###', '   ###    ###', '   ###    ###', '   ###    ###', '   ###    ###', '   ###    ###'],
    'boots': ['', '', '', '', '', '', '   ###    ###', '   ###    ###', '   ttt    ttt', '   ###    ###', '  ####   ####', '  ####   ####'],
}


def outline(img):
    src = img.copy()
    for y in range(16):
        for x in range(16):
            if src.getpixel((x, y))[3]:
                continue
            if any(0 <= x + dx < 16 and 0 <= y + dy < 16 and src.getpixel((x + dx, y + dy))[3] == 255
                   for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                img.putpixel((x, y), OUTLINE)


def icon(rows, pal, gem, name):
    img = Image.new('RGBA', (16, 16))
    top = next(i for i, r in enumerate(rows) if r.strip())
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == '#':
                img.putpixel((x, y), pal.metal(y, top, len(rows)))
            elif ch == 't':
                img.putpixel((x, y), pal.trim)
            elif ch == 'g':
                img.putpixel((x, y), gem)
    outline(img)
    out = ROOT / 'item'
    out.mkdir(parents=True, exist_ok=True)
    img.save(out / f'{name}.png')


def ingot():
    pal = Palette('#8c9bb0', '#5fb8ff', '#5fb8ff', 3)
    rows = ['', '', '', '', '', '', '      ######', '     #tttttt#', '    ##########', '   ############', '   ############', '    ##########']
    icon(rows, pal, pal.accent, 'mana_steel_ingot')


def cloth(img, box, pal, rows=None, trim_edges=True):
    """Cloth: flat with soft folds, gold trim on the border."""
    x0, y0, x1, y1 = box
    ys = range(y0, y1) if rows is None else range(max(y0, y1 - rows), y1)
    for y in ys:
        for x in range(x0, x1):
            edge = trim_edges and y == y1 - 1  # hem
            fold = 0.9 if (x - x0) % 3 == 1 else 1.0
            img.putpixel((x, y), pal.trim if edge else shade(pal.metal(y, y0, y1), fold))


def outfit(name, robe, trousers, leather):
    one = Image.new('RGBA', (64, 32))
    for face, box in HEAD.items():  # hood: open in front, face shows through
        if face == 'bottom':
            continue
        x0, y0, x1, y1 = box
        if face == 'front':
            for y in range(y0, y1):
                for x in range(x0, x1):
                    if y < y0 + 2 or x in (x0, x1 - 1):
                        one.putpixel((x, y), robe.trim if y == y0 + 1 or x in (x0, x1 - 1) else robe.metal(y, y0, y1))
        else:
            cloth(one, box, robe, trim_edges=False)
    for face, box in BODY.items():  # robe: dark coat, gold clasp line down the front
        cloth(one, box, robe, trim_edges=face not in ('top', 'bottom'))
    bx0, by0, bx1, by1 = BODY['front']
    for y in range(by0, by1):
        one.putpixel(((bx0 + bx1) // 2 - 1, y), robe.trim)
    one.putpixel(((bx0 + bx1) // 2 - 1, by0 + 1), robe.accent)
    for face, box in ARM.items():
        cloth(one, box, robe, trim_edges=face not in ('top', 'bottom'))
    for face, box in LEG.items():  # boots
        if face != 'top':
            cloth(one, box, leather, rows=None if face == 'bottom' else 4, trim_edges=False)

    two = Image.new('RGBA', (64, 32))
    for face, box in BODY.items():  # belt
        if face not in ('top', 'bottom'):
            cloth(two, box, leather, rows=2, trim_edges=False)
    for face, box in LEG.items():
        if face != 'top':
            cloth(two, box, trousers, trim_edges=False)

    out = ROOT / 'models/outfit'
    out.mkdir(parents=True, exist_ok=True)
    one.save(out / f'{name}_layer_1.png')
    two.save(out / f'{name}_layer_2.png')


OUTFIT_ICONS = {
    'hood': ['', '', '', '     ######', '    ########', '   ###tttt###', '   ##t    t##', '   ##t    t##', '   ##t    t##', '   ###    ###', '  ####    ####'],
    'robe': ['', '', '  ###t  t###', '  ####tt####', '  ####tt####', '   ###tt###', '   ###tt###', '   ###tt###', '   ###tt###', '   ###tt###', '  ####tt####', '  tttttttttt'],
    'trousers': ['', '', '   bbbbbbbbbb', '   ##########', '   ####  ####', '   ###    ###', '   ###    ###', '   ###    ###', '   ###    ###', '   ###    ###', '   ###    ###', '   ###    ###'],
    'boots': ['', '', '', '', '', '', '   ###    ###', '   ###    ###', '   ###    ###', '   ###    ###', '  ####   ####', '  ####   ####'],
}


def outfit_icons(name, pals):
    for piece, rows in OUTFIT_ICONS.items():
        pal = pals[piece]
        img = Image.new('RGBA', (16, 16))
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch == '#':
                    img.putpixel((x, y), pal.metal(y, 2, len(rows)))
                elif ch == 't':
                    img.putpixel((x, y), pal.trim)
                elif ch == 'b':
                    img.putpixel((x, y), pals['boots'].base)
        outline(img)
        img.save(ROOT / 'item' / f'{name}_{piece}.png')


def eye_sprites():
    """10x10 armor toggle: open eye (armor shown) and crossed eye (hidden)."""
    eye = ['', '', '   ####', '  #....#', ' #..oo..#', ' #..oo..#', '  #....#', '   ####']
    colors = {'#': rgba('#e8e0c8'), '.': rgba('#2a2438'), 'o': rgba('#5fb8ff')}
    out = ROOT / 'gui/sprites/cosmetic'
    out.mkdir(parents=True, exist_ok=True)
    for name, slash in (('armor_shown', False), ('armor_hidden', True)):
        img = Image.new('RGBA', (10, 10), rgba('#1a1622', 200))
        for y, row in enumerate(eye):
            for x, ch in enumerate(row):
                if ch in colors:
                    img.putpixel((x, y), colors[ch] if not slash or ch != 'o' else rgba('#6a6478'))
        if slash:
            for i in range(1, 9):
                img.putpixel((i, 9 - i), rgba('#e05050'))
        img.save(out / f'{name}.png')


if __name__ == '__main__':
    sets = {
        'knight': (Palette('#8c9bb0', '#c8d2de', '#5fb8ff', 11), rgba('#5fb8ff')),
        'elite_knight': (Palette('#4c5466', '#d9b24a', '#3fe0d0', 13), rgba('#3fe0d0')),
    }
    for name, (pal, gem) in sets.items():
        armor(name, pal, gem)
        for piece, rows in ICONS.items():
            icon(rows, pal, gem, f'{name}_{piece}')
    ingot()
    robe = Palette('#26222e', '#d9b24a', '#c0392b', 17)
    trousers = Palette('#3a3a48', '#3a3a48', '#3a3a48', 19)
    leather = Palette('#5a3c26', '#5a3c26', '#5a3c26', 23)
    outfit('magic_knight', robe, trousers, leather)
    outfit_icons('magic_knight', {'hood': robe, 'robe': robe, 'trousers': trousers, 'boots': leather})
    eye_sprites()
