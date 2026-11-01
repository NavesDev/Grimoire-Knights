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

MANA_PAL = {'k': '#0c2a5a', 'd': '#185fa5', 'm': '#378add', 'l': '#85b7eb', 'w': '#e6f1fb'}

# Mana branch, max mana: a mana crystal with a plus.
MAX_MANA = {'palette': MANA_PAL, 'rows': [
    '...........kkk..',
    '.......kk..kwk..',
    '......kwlk.kwk..',
    '.....kwllmkkwkkk',
    '....kwllmmkwwwwk',
    '...kwllmmmmkkwkk',
    '..kwlllmmmmmkwk.',
    '..kmmmmmmmmmkkk.',
    '...kmmmmmmmdk...',
    '....kmmmmmdk....',
    '.....kmmmdk.....',
    '......kmdk......',
    '.......kk.......',
    '................',
    '................',
    '................',
]}

# Mana branch, regeneration: a mana drop with sparkles rising.
MANA_REGEN = {'palette': MANA_PAL, 'rows': [
    '.......k.....w..',
    '......klk...wlw.',
    '......klk....w..',
    '.....kwlmk......',
    '.....kwlmk...l..',
    '....kwllmmk.lwl.',
    '....kwlmmmk..l..',
    '...kwlmmmmmk....',
    '...klmmmmmmk....',
    '...klmmmmmdk....',
    '...kmmmmmmdk....',
    '....kmmmmddk....',
    '.....kddddk.....',
    '......kkkk......',
    '................',
    '................',
]}

# Mana branch, Mana Control: a rune circle holding a steady mana spark.
MANA_CONTROL = {'palette': MANA_PAL, 'rows': [
    '.....kkkkkk.....',
    '...kkddddddkk...',
    '..kdd..l...ddk..',
    '.kd....l.....dk.',
    '.kd...kmk....dk.',
    'kd...kmlmk....dk',
    'kdl.kmlwlmk.l.dk',
    'kdllkmwwwmkllldk',
    'kd.lkmlwlmk..ldk',
    'kd...kmlmk....dk',
    '.kd...kmk....dk.',
    '.kd....l.....dk.',
    '..kdd..l...ddk..',
    '...kkddddddkk...',
    '.....kkkkkk.....',
    '................',
]}

# Wind passive, tailwind: a feather riding two gusts.
TAILWIND = {'palette': WIND_PAL, 'rows': [
    '...........kkk..',
    '..........klwk..',
    '.........klwlk..',
    '........klwlk...',
    '.......klwlk....',
    '......klwlk.....',
    '.....klwlk......',
    '....klwlk.......',
    '....kwlk........',
    '...kgk..........',
    '..kgk...........',
    '................',
    'llllllllll......',
    '...wwwwwwwwwww..',
    '.lllllll........',
    '................',
]}

# Fire passive, phoenix: a firebird with its wings spread.
PHOENIX = {'palette': FIRE_PAL, 'rows': [
    '.......kk.......',
    '......kyyk......',
    'k.....kywk.....k',
    'kok...kyyk...kok',
    'kook..kook..kook',
    '.kook.kyyk.kook.',
    '.kyook.yy.kooyk.',
    '..kyyookyookyyk.',
    '...kyyyoyyyyyk..',
    '....kkyywwyykk..',
    '......kywwyk....',
    '......kooook....',
    '.....korkkrok...',
    '....kor.k..rok..',
    '....kr...k..rk..',
    '.....k.......k..',
]}

BODY_PAL = {'k': '#3a2a1a', 'd': '#8a6a44', 'm': '#c9a46e', 'l': '#efdcb4', 'w': '#ffffff'}

# Body branch, fall resistance: a feather drifting down over a soft landing line.
FALL_RESISTANCE = {'palette': BODY_PAL, 'rows': [
    '...........kk...',
    '..........klwk..',
    '.........klmlk..',
    '........klmmlk..',
    '.......klmmlk...',
    '......klmmdlk...',
    '.....klmmdlk....',
    '....klmmdlk.....',
    '....kmmdlk......',
    '...kmmdkk.......',
    '...kmdk.........',
    '..kdk...........',
    '.kk.............',
    '................',
    '.kkkkkkkkkkkkkk.',
    '..kllllllllllk..',
]}

# Body branch, vigor: a heart.
VIGOR = {'palette': {'k': '#3a0a0a', 'd': '#8a1a1a', 'm': '#d23434', 'l': '#f07a7a', 'w': '#ffffff'}, 'rows': [
    '................',
    '................',
    '...kkk....kkk...',
    '..kllmk..kmmmk..',
    '.klwlmmkkmmmmdk.',
    '.klllmmmmmmmmdk.',
    '.kllmmmmmmmmmdk.',
    '.klmmmmmmmmmmdk.',
    '..kmmmmmmmmmdk..',
    '...kmmmmmmmdk...',
    '....kmmmmmdk....',
    '.....kmmmdk.....',
    '......kmdk......',
    '.......kk.......',
    '................',
    '................',
]}

# Body branch, roll: a curled arrow rolling over the ground line.
ROLL = {'palette': BODY_PAL, 'rows': [
    '................',
    '.....kkkkk......',
    '...kkmmmmmkk....',
    '..kmmkkkkkmmk...',
    '.kmmk.....kmmk..',
    '.kmk.......kmk..',
    '.kmk.......kmk.k',
    '.kmk.......kmkkk',
    '.kmmk......kmlk.',
    '..kmk.....kmlk..',
    '..kmmk...kmllk..',
    '...kk.....kkk...',
    '................',
    '.kkkkkkkkkkkkkk.',
    '..kllllllllllk..',
    '................',
]}

ICONS = {'element/fire': FIRE, 'element/wind': WIND, 'ability/fireball': FIREBALL, 'ability/flame_arm': FLAME_ARM,
         'ability/ember_burst': EMBER_BURST, 'ability/crescent_blade': CRESCENT_BLADE, 'ability/tornado_fang': TORNADO_FANG,
         'ability/gale_bow': GALE_BOW, 'ability/dash': DASH, 'skill/max_mana': MAX_MANA, 'skill/mana_regen': MANA_REGEN,
         'skill/mana_control': MANA_CONTROL, 'skill/tailwind': TAILWIND, 'skill/phoenix': PHOENIX,
         'skill/fall_resistance': FALL_RESISTANCE,
         'skill/vigor': VIGOR, 'skill/roll': ROLL}


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
