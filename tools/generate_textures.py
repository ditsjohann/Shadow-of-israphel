#!/usr/bin/env python3
"""Generates every texture used by the mod as original pixel art.

Run from the repository root:  python3 tools/generate_textures.py
Requires Pillow.  Output is deterministic (seeded), so re-running it only
changes files if this script changes.
"""
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/shadowofisraphel"
TEX = ASSETS / "textures"

CLEAR = (0, 0, 0, 0)


def rgba(hex_colour, alpha=255):
    hex_colour = hex_colour.lstrip("#")
    return tuple(int(hex_colour[i:i + 2], 16) for i in (0, 2, 4)) + (alpha,)


def vary(colour, rng, amount=10):
    r, g, b, a = colour
    d = rng.randint(-amount, amount)
    return (max(0, min(255, r + d)), max(0, min(255, g + d)), max(0, min(255, b + d)), a)


def fill(img, x, y, w, h, colour, rng=None, noise=0):
    for yy in range(y, y + h):
        for xx in range(x, x + w):
            img.putpixel((xx, yy), vary(colour, rng, noise) if rng and noise else colour)


def put(img, points, colour):
    for p in points:
        img.putpixel(p, colour)


# --------------------------------------------------------------------------
# Skin helpers.  Faces of a box at texture offset (u, v) with size (w, h, d)
# follow the vanilla cuboid UV layout.
# --------------------------------------------------------------------------

def faces(u, v, w, h, d):
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }


HEAD = (0, 0, 8, 8, 8)
HAT = (32, 0, 8, 8, 8)
BODY = (16, 16, 8, 12, 4)
JACKET = (16, 32, 8, 12, 4)
RIGHT_ARM = (40, 16, 4, 12, 4)
LEFT_ARM = (32, 48, 4, 12, 4)
RIGHT_LEG = (0, 16, 4, 12, 4)
LEFT_LEG = (16, 48, 4, 12, 4)


def paint_box(img, box, colour, rng, noise=8):
    for x, y, w, h in faces(*box).values():
        fill(img, x, y, w, h, colour, rng, noise)


def paint_limb(img, box, colour, end_colour, end_rows, rng, noise=8, end_at_bottom=True):
    """Paints an arm or leg, with `end_rows` of a different colour (hands, shoes)."""
    paint_box(img, box, colour, rng, noise)
    f = faces(*box)
    for name in ("right", "front", "left", "back"):
        x, y, w, h = f[name]
        if end_at_bottom:
            fill(img, x, y + h - end_rows, w, end_rows, end_colour, rng, noise // 2)
    x, y, w, h = f["bottom"]
    fill(img, x, y, w, h, end_colour, rng, noise // 2)


# --------------------------------------------------------------------------
# Entities
# --------------------------------------------------------------------------

def israphel():
    rng = random.Random(1)
    img = Image.new("RGBA", (64, 64), CLEAR)
    robe = rgba("1c1424")
    robe_dark = rgba("110b17")
    trim = rgba("7a0f14")
    skin = rgba("c9c2c8")
    socket = rgba("2a1f2e")

    # Head: ghostly pale face, dark sockets.
    paint_box(img, HEAD, robe_dark, rng, 4)
    fx, fy, _, _ = faces(*HEAD)["front"]
    fill(img, fx, fy, 8, 8, skin, rng, 6)
    fill(img, fx + 1, fy + 3, 2, 2, socket)
    fill(img, fx + 5, fy + 3, 2, 2, socket)
    put(img, [(fx + 1, fy + 4), (fx + 2, fy + 4), (fx + 5, fy + 4), (fx + 6, fy + 4)], rgba("8a1218"))
    fill(img, fx + 3, fy + 5, 2, 1, rgba("a59ca3"))
    fill(img, fx + 2, fy + 6, 4, 1, socket)

    # Hood overlay: frames the face and covers the rest of the head.
    for name, (x, y, w, h) in faces(*HAT).items():
        fill(img, x, y, w, h, robe, rng, 6)
    hx, hy, _, _ = faces(*HAT)["front"]
    for yy in range(hy + 1, hy + 8):
        for xx in range(hx + 1, hx + 7):
            img.putpixel((xx, yy), CLEAR)
    put(img, [(hx + 1, hy + 1), (hx + 6, hy + 1)], robe)

    # Robe body with crimson trim and belt.
    paint_box(img, BODY, robe, rng, 6)
    bx, by, bw, bh = faces(*BODY)["front"]
    fill(img, bx + 3, by, 2, bh, trim, rng, 8)
    fill(img, bx, by + 6, bw, 1, rgba("3d2a14"))
    put(img, [(bx + 3, by + 6), (bx + 4, by + 6)], rgba("c8a03a"))
    bkx, bky, _, bkh = faces(*BODY)["back"]
    fill(img, bkx, bky + 6, 8, 1, rgba("3d2a14"))

    # Cloak layer flaring over the body.
    for name, (x, y, w, h) in faces(*JACKET).items():
        if name in ("right", "left", "back"):
            fill(img, x, y, w, h, robe_dark, rng, 5)
    jx, jy, jw, jh = faces(*JACKET)["front"]
    fill(img, jx, jy, 2, jh, robe_dark, rng, 5)
    fill(img, jx + 6, jy, 2, jh, robe_dark, rng, 5)
    fill(img, jx + 1, jy, 1, jh, trim, rng, 6)
    fill(img, jx + 6, jy, 1, jh, trim, rng, 6)

    # Sleeves with pale clawed hands; robe down to the feet.
    for arm in (RIGHT_ARM, LEFT_ARM):
        paint_limb(img, arm, robe, skin, 2, rng, 6)
        ax, ay, aw, ah = faces(*arm)["front"]
        fill(img, ax, ay + 8, aw, 1, trim)
    for leg in (RIGHT_LEG, LEFT_LEG):
        paint_limb(img, leg, robe, rgba("0a070c"), 1, rng, 6)

    save(img, TEX / "entity/israphel.png")

    # Emissive eyes, rendered full-bright on top of the skin.
    eyes = Image.new("RGBA", (64, 64), CLEAR)
    put(eyes, [(fx + 1, fy + 4), (fx + 2, fy + 4), (fx + 5, fy + 4), (fx + 6, fy + 4)], rgba("ff1a1a"))
    put(eyes, [(fx + 2, fy + 3), (fx + 5, fy + 3)], rgba("ff6a3a"))
    save(eyes, TEX / "entity/israphel_eyes.png")
    return img


def sand_monster():
    rng = random.Random(2)
    img = Image.new("RGBA", (64, 64), CLEAR)
    sand = rgba("d8c48a")
    dark = rgba("a8915a")
    hole = rgba("1a130b")

    for box in (HEAD, BODY, RIGHT_ARM, RIGHT_LEG, LEFT_ARM, LEFT_LEG):
        paint_box(img, box, sand, rng, 14)
        # Cracks and darker clumps.
        for x, y, w, h in faces(*box).values():
            for _ in range(max(1, (w * h) // 10)):
                img.putpixel((x + rng.randrange(w), y + rng.randrange(h)), vary(dark, rng, 10))

    fx, fy, _, _ = faces(*HEAD)["front"]
    fill(img, fx + 1, fy + 3, 2, 2, hole)
    fill(img, fx + 5, fy + 3, 2, 2, hole)
    put(img, [(fx + 2, fy + 3), (fx + 5, fy + 3)], rgba("5a1a10"))
    fill(img, fx + 2, fy + 6, 4, 1, hole)
    put(img, [(fx + 3, fy + 7), (fx + 4, fy + 5)], hole)

    # Lumps of sand piled on the head (overlay layer).
    hx, hy, hw, hh = faces(*HAT)["top"]
    for _ in range(26):
        img.putpixel((hx + rng.randrange(hw), hy + rng.randrange(hh)), vary(sand, rng, 18))
    for name in ("front", "right", "left", "back"):
        x, y, w, h = faces(*HAT)[name]
        for xx in range(x, x + w):
            for yy in range(y, y + rng.randint(0, 2)):
                img.putpixel((xx, yy), vary(sand, rng, 18))

    save(img, TEX / "entity/sand_monster.png")


def hat_man():
    rng = random.Random(3)
    img = Image.new("RGBA", (64, 64), CLEAR)
    skin = rgba("e0ac84")
    hair = rgba("5b3a22")
    suit = rgba("2e2f3a")
    shirt = rgba("e9e6dc")
    tie = rgba("9c1c1c")
    trousers = rgba("23232b")
    shoes = rgba("161214")

    paint_box(img, HEAD, skin, rng, 5)
    f = faces(*HEAD)
    fill(img, *f["top"], hair, rng, 8)
    fill(img, *f["back"], hair, rng, 8)
    for name in ("right", "left"):
        x, y, w, h = f[name]
        fill(img, x, y, w, 3, hair, rng, 8)
        fill(img, x + (0 if name == "left" else w - 2), y, 2, 6, hair, rng, 8)
    fx, fy, _, _ = f["front"]
    fill(img, fx, fy, 8, 2, hair, rng, 8)
    put(img, [(fx + 1, fy + 3), (fx + 2, fy + 3), (fx + 5, fy + 3), (fx + 6, fy + 3)], rgba("ffffff"))
    put(img, [(fx + 2, fy + 3), (fx + 5, fy + 3)], rgba("3b2a1a"))
    # A monocle, naturally.
    put(img, [(fx + 4, fy + 2), (fx + 7, fy + 2), (fx + 4, fy + 4), (fx + 7, fy + 4)], rgba("d4af37"))
    fill(img, fx + 2, fy + 5, 4, 1, hair)  # moustache
    put(img, [(fx + 1, fy + 5), (fx + 6, fy + 5)], hair)
    fill(img, fx + 3, fy + 6, 2, 1, rgba("a0604a"))

    paint_box(img, BODY, suit, rng, 5)
    bx, by, bw, bh = faces(*BODY)["front"]
    fill(img, bx + 2, by, 4, 6, shirt, rng, 3)
    fill(img, bx + 3, by + 1, 2, 6, tie, rng, 6)
    put(img, [(bx + 1, by + 8), (bx + 1, by + 10)], rgba("c8a03a"))

    for arm in (RIGHT_ARM, LEFT_ARM):
        paint_limb(img, arm, suit, skin, 2, rng, 5)
        ax, ay, aw, _ = faces(*arm)["front"]
        fill(img, ax, ay + 9, aw, 1, shirt)
    for leg in (RIGHT_LEG, LEFT_LEG):
        paint_limb(img, leg, trousers, shoes, 2, rng, 5)

    save(img, TEX / "entity/hat_man.png")


# --------------------------------------------------------------------------
# Items (16x16)
# --------------------------------------------------------------------------

def from_rows(rows, palette):
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), rows
    img = Image.new("RGBA", (16, 16), CLEAR)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x, y), palette[ch])
    return img


def jaffa_cake():
    rows = [
        "................",
        "................",
        "................",
        ".....oooooo.....",
        "...oocccccccoo..",
        "..occcchhccccco.",
        ".occcchhcccccccO",
        ".occcccjjjjccccO",
        ".occccjJJJjcccco",
        ".occcccjjjjccccO",
        "..occcccccccccO.",
        "..sooccccccccOs.",
        "...sssoooooOss..",
        ".....ssssssss...",
        "................",
        "................",
    ]
    palette = {
        "o": rgba("3a2214"), "O": rgba("26150c"), "c": rgba("4e2c18"),
        "h": rgba("7a4a2c"), "j": rgba("e0701c"), "J": rgba("ff9a3a"),
        "s": rgba("d7a256"),
    }
    save(from_rows(rows, palette), TEX / "item/jaffa_cake.png")


def honeydews_mead():
    rows = [
        "......kkkk......",
        "......kKKk......",
        "......gwwg......",
        ".......gg.......",
        "......gwwg......",
        ".....g.ww.g.....",
        "....g......g....",
        "...gaaaaaaaag...",
        "...gaAAaaaaag...",
        "...gaAaaaaaag...",
        "...gaaaaaaaag...",
        "...gaaaaaaddg...",
        "...gddddddddg...",
        "....gddddddg....",
        ".....gggggg.....",
        "................",
    ]
    palette = {
        "k": rgba("7a5230"), "K": rgba("9c6a3e"), "g": rgba("a9c6cf", 230),
        "w": rgba("dfeef2", 150), "a": rgba("e0a020"), "A": rgba("ffd86a"),
        "d": rgba("b4760e"),
    }
    save(from_rows(rows, palette), TEX / "item/honeydews_mead.png")


def cursed_sand():
    rng = random.Random(4)
    img = Image.new("RGBA", (16, 16), CLEAR)
    base = rgba("d2b779")
    for y in range(6, 14):
        half = int((y - 5) * 1.0) + 1
        for x in range(8 - half, 8 + half):
            if 0 <= x < 16:
                img.putpixel((x, y), vary(base, rng, 18))
    for x in range(2, 14):
        img.putpixel((x, 13), vary(rgba("a8915a"), rng, 8))
    for p in [(7, 8), (9, 10), (5, 11), (10, 12), (8, 12), (6, 9)]:
        img.putpixel(p, rgba("8e1414"))
    put(img, [(8, 6), (7, 7)], rgba("f0dca4"))
    save(img, TEX / "item/cursed_sand.png")


def shadow_shard():
    rows = [
        "................",
        "...........xx...",
        "..........xpPx..",
        ".........xpPPx..",
        "........xpPppx..",
        ".......xpPppx...",
        "......xpPpppx...",
        ".....xppPppx....",
        "....xpppPpx.....",
        "....xpppppx.....",
        "...xdpppppx.....",
        "...xddppdx......",
        "...xdddddx......",
        "....xdddx.......",
        ".....xxx........",
        "................",
    ]
    palette = {
        "x": rgba("0d0812"), "p": rgba("4a1f6e"), "P": rgba("c04ae0"),
        "d": rgba("2a1240"),
    }
    save(from_rows(rows, palette), TEX / "item/shadow_shard.png")


def cursed_idol():
    rows = [
        "................",
        "......xxxx......",
        ".....xggggx.....",
        ".....xgrgrx.....",
        ".....xggggx.....",
        "......xGGx......",
        "....xxggggxx....",
        "...xgggGGgggx...",
        "...xgxgGGgxgx...",
        "...xx.xggx.xx...",
        "......xggx......",
        ".....xgGGgx.....",
        "....xxxxxxxx....",
        "...xbbbbbbbbx...",
        "...xxxxxxxxxx...",
        "................",
    ]
    palette = {
        "x": rgba("4a3208"), "g": rgba("e7b52c"), "G": rgba("ffe27a"),
        "r": rgba("ff2020"), "b": rgba("6b5a3a"),
    }
    save(from_rows(rows, palette), TEX / "item/cursed_idol.png")


def shadow_blade():
    rows = [
        "..............xx",
        ".............xPx",
        "............xPpx",
        "...........xPpx.",
        "..........xPpx..",
        ".........xPpx...",
        "........xPpx....",
        ".......xPpx.....",
        "..xx..xPpx......",
        "..xrx.xpx.......",
        "...xrxpx........",
        "....xrrx........",
        "....xhxrx.......",
        "...xhx.xrx......",
        "..xhx...xx......",
        "..xx............",
    ]
    palette = {
        "x": rgba("0d0812"), "p": rgba("3a1856"), "P": rgba("b04ad8"),
        "r": rgba("9c141c"), "h": rgba("2b2230"),
    }
    save(from_rows(rows, palette), TEX / "item/shadow_blade.png")


def hat_texture():
    rng = random.Random(5)
    img = Image.new("RGBA", (16, 16), CLEAR)
    fill(img, 0, 0, 8, 8, rgba("1b1b20"), rng, 5)      # top / brim faces
    fill(img, 8, 0, 8, 8, rgba("24242b"), rng, 5)      # crown sides
    fill(img, 0, 8, 16, 1, rgba("15151a"), rng, 3)     # brim edge
    fill(img, 0, 12, 16, 2, rgba("8e1b1b"), rng, 8)    # red band
    save(img, TEX / "item/hat_man_hat.png")


# --------------------------------------------------------------------------
# Blocks
# --------------------------------------------------------------------------

def cursed_sandstone():
    rng = random.Random(6)
    base = rgba("b98e62")
    side = Image.new("RGBA", (16, 16), CLEAR)
    fill(side, 0, 0, 16, 16, base, rng, 8)
    fill(side, 0, 0, 16, 3, rgba("c9a072"), rng, 6)
    fill(side, 0, 3, 16, 1, rgba("8a5f3e"), rng, 4)
    fill(side, 0, 12, 16, 1, rgba("8a5f3e"), rng, 4)
    fill(side, 0, 13, 16, 3, rgba("a67a52"), rng, 6)
    # A glowing rune carved into the face.
    rune = [(7, 5), (8, 5), (6, 6), (9, 6), (7, 7), (8, 7), (7, 8), (8, 8), (6, 9), (9, 9), (7, 10), (8, 10)]
    put(side, rune, rgba("8e1414"))
    put(side, [(7, 7), (8, 8)], rgba("d0302a"))
    save(side, TEX / "block/cursed_sandstone.png")

    top = Image.new("RGBA", (16, 16), CLEAR)
    fill(top, 0, 0, 16, 16, rgba("c9a072"), rng, 7)
    for i in range(16):
        top.putpixel((i, 0), rgba("9a6f48"))
        top.putpixel((i, 15), rgba("9a6f48"))
        top.putpixel((0, i), rgba("9a6f48"))
        top.putpixel((15, i), rgba("9a6f48"))
    put(top, [(5, 5), (10, 5), (5, 10), (10, 10), (7, 7), (8, 8), (7, 8), (8, 7)], rgba("8e1414"))
    save(top, TEX / "block/cursed_sandstone_top.png")


# --------------------------------------------------------------------------
# Mod icon: Israphel's hooded face, upscaled.
# --------------------------------------------------------------------------

def icon(skin):
    face = Image.new("RGBA", (10, 10), rgba("140c1a"))
    fx, fy, _, _ = faces(*HEAD)["front"]
    face.paste(skin.crop((fx, fy, fx + 8, fy + 8)), (1, 1))
    hx, hy, _, _ = faces(*HAT)["front"]
    hood = skin.crop((hx, hy, hx + 8, hy + 8))
    face.alpha_composite(hood, (1, 1))
    eyes = Image.open(TEX / "entity/israphel_eyes.png").crop((fx, fy, fx + 8, fy + 8))
    face.alpha_composite(eyes, (1, 1))
    for x in range(10):
        face.putpixel((x, 0), rgba("1c1424"))
    big = face.resize((120, 120), Image.NEAREST)
    out = Image.new("RGBA", (128, 128), rgba("0a060d"))
    out.paste(big, (4, 4))
    save(out, ASSETS / "icon.png")


def save(img, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)
    print("wrote", path.relative_to(ROOT))


if __name__ == "__main__":
    skin = israphel()
    sand_monster()
    hat_man()
    jaffa_cake()
    honeydews_mead()
    cursed_sand()
    shadow_shard()
    cursed_idol()
    shadow_blade()
    hat_texture()
    cursed_sandstone()
    icon(skin)
