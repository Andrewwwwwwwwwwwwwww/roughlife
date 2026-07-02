// Generates Rough Life's JSON assets and all original pixel-art textures.
// Run from the repo root: node tools/gen_assets.js
"use strict";
const fs = require("fs");
const path = require("path");
const zlib = require("zlib");

const RES = path.join(__dirname, "..", "src", "main", "resources");

// ---------- minimal PNG writer ----------
const CRC_TABLE = (() => {
  const t = new Int32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c;
  }
  return t;
})();
function crc32(buf) {
  let c = 0xffffffff;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}
function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type, "ascii"), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(body));
  return Buffer.concat([len, body, crc]);
}
function writePng(file, width, height, rgba) {
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8; // bit depth
  ihdr[9] = 6; // RGBA
  const raw = Buffer.alloc((width * 4 + 1) * height);
  for (let y = 0; y < height; y++) {
    raw[y * (width * 4 + 1)] = 0; // no filter
    rgba.copy(raw, y * (width * 4 + 1) + 1, y * width * 4, (y + 1) * width * 4);
  }
  const png = Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk("IHDR", ihdr),
    chunk("IDAT", zlib.deflateSync(raw, { level: 9 })),
    chunk("IEND", Buffer.alloc(0)),
  ]);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, png);
  console.log("png ", path.relative(RES, file));
}
function hex(c) {
  // #RRGGBB or #RRGGBBAA -> [r,g,b,a]
  const v = c.replace("#", "");
  return [
    parseInt(v.slice(0, 2), 16),
    parseInt(v.slice(2, 4), 16),
    parseInt(v.slice(4, 6), 16),
    v.length === 8 ? parseInt(v.slice(6, 8), 16) : 255,
  ];
}
// grid: array of strings, palette: char -> #hex ('.' transparent)
function art(grid, palette) {
  const h = grid.length;
  const w = grid[0].length;
  const buf = Buffer.alloc(w * h * 4);
  for (let y = 0; y < h; y++) {
    if (grid[y].length !== w) throw new Error("ragged row " + y);
    for (let x = 0; x < w; x++) {
      const ch = grid[y][x];
      if (ch === ".") continue;
      const col = palette[ch];
      if (!col) throw new Error("no palette entry for '" + ch + "'");
      const [r, g, b, a] = hex(col);
      const o = (y * w + x) * 4;
      buf[o] = r; buf[o + 1] = g; buf[o + 2] = b; buf[o + 3] = a;
    }
  }
  return { w, h, buf };
}
function scale(img, f) {
  const { w, h, buf } = img;
  const out = Buffer.alloc(w * f * h * f * 4);
  for (let y = 0; y < h * f; y++)
    for (let x = 0; x < w * f; x++) {
      const src = ((Math.floor(y / f) * w) + Math.floor(x / f)) * 4;
      const dst = (y * w * f + x) * 4;
      buf.copy(out, dst, src, src + 4);
    }
  return { w: w * f, h: h * f, buf: out };
}
function combineH(imgs) {
  const h = imgs[0].h;
  const w = imgs.reduce((s, i) => s + i.w, 0);
  const out = Buffer.alloc(w * h * 4);
  let ox = 0;
  for (const img of imgs) {
    for (let y = 0; y < h; y++)
      img.buf.copy(out, (y * w + ox) * 4, y * img.w * 4, (y + 1) * img.w * 4);
    ox += img.w;
  }
  return { w, h, buf: out };
}
function json(file, obj) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, JSON.stringify(obj, null, 2) + "\n");
  console.log("json", path.relative(RES, file));
}

// ---------- item JSONs ----------
const ITEMS = [
  "flint_shard", "plant_fiber", "flint_knife", "flint_hatchet",
  "bandage", "splint", "canteen", "charcoal_filter", "purified_water_bottle",
];
const HANDHELD = new Set(["flint_knife", "flint_hatchet"]);
for (const item of ITEMS) {
  json(path.join(RES, "assets/roughlife/items", item + ".json"), {
    model: { type: "minecraft:model", model: "roughlife:item/" + item },
  });
  json(path.join(RES, "assets/roughlife/models/item", item + ".json"), {
    parent: HANDHELD.has(item) ? "minecraft:item/handheld" : "minecraft:item/generated",
    textures: { layer0: "roughlife:item/" + item },
  });
}

// ---------- recipes ----------
const R = (n, o) => json(path.join(RES, "data/roughlife/recipe", n + ".json"), o);
R("flint_knife", {
  type: "minecraft:crafting_shaped", category: "equipment",
  key: { f: "roughlife:flint_shard", s: "minecraft:stick" },
  pattern: ["f", "s"],
  result: { count: 1, id: "roughlife:flint_knife" },
});
R("flint_hatchet", {
  type: "minecraft:crafting_shaped", category: "equipment",
  key: { f: "roughlife:flint_shard", p: "roughlife:plant_fiber", s: "minecraft:stick" },
  pattern: ["fp", "s "],
  result: { count: 1, id: "roughlife:flint_hatchet" },
});
R("string_from_fiber", {
  type: "minecraft:crafting_shapeless", category: "misc",
  ingredients: ["roughlife:plant_fiber", "roughlife:plant_fiber", "roughlife:plant_fiber"],
  result: { count: 1, id: "minecraft:string" },
});
R("bandage", {
  type: "minecraft:crafting_shapeless", category: "misc",
  ingredients: ["minecraft:paper", "minecraft:string", "roughlife:plant_fiber"],
  result: { count: 2, id: "roughlife:bandage" },
});
R("splint", {
  type: "minecraft:crafting_shapeless", category: "misc",
  ingredients: ["minecraft:stick", "minecraft:stick", "roughlife:plant_fiber", "roughlife:plant_fiber"],
  result: { count: 1, id: "roughlife:splint" },
});
R("canteen", {
  type: "minecraft:crafting_shaped", category: "equipment",
  key: { l: "minecraft:leather", b: "minecraft:glass_bottle", s: "minecraft:string" },
  pattern: ["s s", "lbl", " l "],
  result: { count: 1, id: "roughlife:canteen" },
});
R("charcoal_filter", {
  type: "minecraft:crafting_shapeless", category: "misc",
  ingredients: ["minecraft:charcoal", "minecraft:paper", "roughlife:plant_fiber"],
  result: { count: 2, id: "roughlife:charcoal_filter" },
});
R("purified_water_bottle", {
  type: "minecraft:crafting_shapeless", category: "misc",
  ingredients: ["minecraft:potion", "roughlife:charcoal_filter"],
  result: { count: 1, id: "roughlife:purified_water_bottle" },
});

// ---------- tags ----------
json(path.join(RES, "data/minecraft/tags/item/axes.json"), {
  replace: false, values: ["roughlife:flint_hatchet"],
});
json(path.join(RES, "data/minecraft/tags/item/swords.json"), {
  replace: false, values: ["roughlife:flint_knife"],
});
json(path.join(RES, "data/roughlife/tags/item/repairs_flint_tools.json"), {
  replace: false, values: ["roughlife:flint_shard"],
});

// ---------- textures ----------
const T = (n) => path.join(RES, "assets/roughlife/textures", n);

// flint shard: chipped dark stone triangle
writePng(T("item/flint_shard.png"), 16, 16, art([
  "................",
  "................",
  "......d.........",
  ".....ddl........",
  "....dDdll.......",
  "....dDDdl.......",
  "...dDDDddl......",
  "...dDDdDdll.....",
  "..dDDdDDDdl.....",
  "..dDdDDDDddl....",
  ".ddDDDDdDDdl....",
  ".dDDdDDDDDddl...",
  ".ddddddddddddl..",
  "..lllllllllll...",
  "................",
  "................",
], { d: "#4a4a52", D: "#2e2e34", l: "#8a8a96" }).buf);

// plant fiber: loose tan-green strands
writePng(T("item/plant_fiber.png"), 16, 16, art([
  "................",
  "....g...........",
  "...gt.....g.....",
  "...gt....gt.....",
  "..gt....gt......",
  "..gt...gtg......",
  "..gtg..gt.g.....",
  "...gtg.tg.tg....",
  "...g.tgt...t....",
  "....t.tg...t....",
  "....t..t..t.....",
  "...t...tt.t.....",
  "...t....ttt.....",
  "..t......t......",
  "................",
  "................",
], { g: "#7c9a4e", t: "#c9b57a" }).buf);

// flint knife: shard blade lashed to a short handle (diagonal, handheld)
writePng(T("item/flint_knife.png"), 16, 16, art([
  "..........dl....",
  ".........dDdl...",
  "........dDDd....",
  ".......dDDdl....",
  "......dDDd......",
  ".....dDDdl......",
  "....fdDd........",
  "...ffdd.........",
  "...wff..........",
  "..www...........",
  ".wWw............",
  ".wW.............",
  "wW..............",
  "................",
  "................",
  "................",
], { d: "#4a4a52", D: "#2e2e34", l: "#8a8a96", w: "#8a6a3c", W: "#6b4f2a", f: "#c9b57a" }).buf);

// flint hatchet: flint head bound to a stick
writePng(T("item/flint_hatchet.png"), 16, 16, art([
  "......ddl.......",
  ".....dDDdl......",
  "....dDDDDdl.....",
  "....dDDDDDd.....",
  ".....ffDDDd.....",
  ".....wffddl.....",
  "....wWff........",
  "...wWw..........",
  "...wW...........",
  "..wW............",
  ".wWw............",
  ".wW.............",
  "wW..............",
  "................",
  "................",
  "................",
], { d: "#4a4a52", D: "#2e2e34", l: "#8a8a96", w: "#8a6a3c", W: "#6b4f2a", f: "#c9b57a" }).buf);

// bandage: rolled white gauze with a trailing wrap
writePng(T("item/bandage.png"), 16, 16, art([
  "................",
  "................",
  "....ccccc.......",
  "...cwwwwwc......",
  "..cwwsssswc.....",
  "..cwswwwwswc....",
  "..cwswccwswc....",
  "..cwswcwwswc....",
  "..cwswwwwswc....",
  "..cwwsssswc.....",
  "...cwwwwwcww....",
  "....ccccc..ww...",
  "............ww..",
  ".............w..",
  "................",
  "................",
], { w: "#f2ede2", s: "#d8d0bd", c: "#b8ae95" }).buf);

// splint: two sticks with fiber lashings
writePng(T("item/splint.png"), 16, 16, art([
  "................",
  "..w........w....",
  "..wW.......wW...",
  "..fff......wW...",
  "..wW......fff...",
  "..wW.......wW...",
  "..wW.......wW...",
  "..fff......wW...",
  "..wW......fff...",
  "..wW.......wW...",
  "..wW.......wW...",
  "..fff......wW...",
  "..wW......fff...",
  "..wW.......wW...",
  "................",
  "................",
], { w: "#8a6a3c", W: "#6b4f2a", f: "#c9b57a" }).buf);

// canteen: leather flask, dark cap, stitched seam and strap
writePng(T("item/canteen.png"), 16, 16, art([
  "................",
  "......kk........",
  "......kk........",
  ".....bbbb.......",
  "....bLLLLb......",
  "...bLllllLb.....",
  "..bLllssllLb....",
  "..bLlsllslLb....",
  "..bLlsllslLb....",
  "..bLllssllLb....",
  "..bLllllllLb....",
  "...bLllllLb.....",
  "....bLLLLb......",
  ".....bbbb.......",
  "................",
  "................",
], { k: "#3a3a3a", b: "#4e3319", L: "#6b4f2a", l: "#8a6a3c", s: "#c9b57a" }).buf);

// charcoal filter: paper-wrapped charcoal plug
writePng(T("item/charcoal_filter.png"), 16, 16, art([
  "................",
  "................",
  "....ppppp.......",
  "...pwwwwwp......",
  "...pwcccwp......",
  "...pwcCcwp......",
  "...pwcccwp......",
  "...pwcCCcwp.....",
  "...pwcccwp......",
  "...pwcCcwp......",
  "...pwcccwp......",
  "...pwwwwwp......",
  "....ppppp.......",
  "................",
  "................",
  "................",
], { p: "#b8ae95", w: "#f2ede2", c: "#2b2b2b", C: "#454545" }).buf);

// purified water: bottle with bright clean water and a sparkle
writePng(T("item/purified_water_bottle.png"), 16, 16, art([
  "................",
  "......ccc.......",
  "......gcg.......",
  "......g.g.......",
  ".....g...g......",
  "....g.....g.....",
  "...g...*...g....",
  "...g.aaaaa.g....",
  "...gaaAAaaag....",
  "...gaAAAAAag....",
  "...gaaAAAaag....",
  "...gaaaaaaag....",
  "....gaaaaag.....",
  ".....ggggg......",
  "................",
  "................",
], { c: "#b8ae95", g: "#cfe3e8", a: "#3f76e4", A: "#6fa9ff", "*": "#ffffff" }).buf);

// gui droplets: full / half / empty, 9x9 each
const dropFull = art([
  "....b....",
  "...bab...",
  "...bab...",
  "..baaab..",
  ".baaaAab.",
  ".baaAAab.",
  ".baaaaab.",
  "..baaab..",
  "...bbb...",
], { b: "#10233f", a: "#3f76e4", A: "#8fc4ff" });
const dropHalf = art([
  "....b....",
  "...bxb...",
  "...bxb...",
  "..bxxab..",
  ".bxxaAab.",
  ".bxxAAab.",
  ".bxxaaab.",
  "..bxaab..",
  "...bbb...",
], { b: "#10233f", a: "#3f76e4", A: "#8fc4ff", x: "#1d2b3d" });
const dropEmpty = art([
  "....b....",
  "...bxb...",
  "...bxb...",
  "..bxxxb..",
  ".bxxxxxb.",
  ".bxxxxxb.",
  ".bxxxxxb.",
  "..bxxxb..",
  "...bbb...",
], { b: "#10233f", x: "#1d2b3d" });
const drops = combineH([dropFull, dropHalf, dropEmpty]);
writePng(T("gui/droplets.png"), drops.w, drops.h, drops.buf);

// gui temperature: 5 thermometer states, 16x16 each
function thermo(fillRows, fluidCol, bulbCol) {
  const grid = [];
  for (let y = 0; y < 16; y++) {
    let row = "";
    for (let x = 0; x < 16; x++) {
      let ch = ".";
      // tube x 6..9, y 1..10
      if (y >= 1 && y <= 10) {
        if (x === 6 || x === 9) ch = "o";
        else if (x === 7 || x === 8) ch = y >= 11 - fillRows ? "f" : "e";
      }
      // bulb centered (7.5, 12.5) radius ~2.5
      const dx = x - 7.5, dy = y - 12.5;
      if (dx * dx + dy * dy <= 6.5) ch = "F";
      else if (dx * dx + dy * dy <= 10.5 && y >= 10) ch = "o";
      if (y === 0 && (x === 7 || x === 8)) ch = "o";
      row += ch;
    }
    grid.push(row);
  }
  return art(grid, { o: "#e8e4d8", e: "#3a3a44", f: fluidCol, F: bulbCol });
}
const temps = combineH([
  thermo(1, "#9bd8ff", "#9bd8ff"), // freezing
  thermo(3, "#59b8e8", "#59b8e8"), // cold
  thermo(5, "#59c96a", "#59c96a"), // comfortable
  thermo(7, "#e8a53a", "#e8a53a"), // hot
  thermo(10, "#e84a2e", "#e84a2e"), // burning
]);
writePng(T("gui/temperature.png"), temps.w, temps.h, temps.buf);

// mob effect icons, 18x18
function effectIcon(grid, palette) {
  return art(grid, palette);
}
const bleedIcon = effectIcon([
  "..................",
  "........r.........",
  "........r.........",
  ".......rrr........",
  ".......rRr........",
  "......rRRrr.......",
  "......rRrrr.......",
  ".....rRRrrrr......",
  ".....rRrrrrr......",
  "....rRRrrrrrr.....",
  "....rRrrrrrrr.....",
  "....rRrrrrrrr.....",
  "....rrrrrrrrr.....",
  ".....rrrrrrr......",
  "......rrrrr.......",
  ".......rrr........",
  "..................",
  "..................",
], { r: "#b8202e", R: "#ff6b6b" });
writePng(T("mob_effect/bleeding.png"), 18, 18, bleedIcon.buf);

const fractureIcon = effectIcon([
  "..................",
  "..ww..............",
  ".wWWw.............",
  ".wWWWw............",
  "..wWWWw...........",
  "...wWWWw..........",
  "....wWWWw.........",
  ".....wWWx.........",
  "......xxx.........",
  ".........xxx......",
  ".........xWWw.....",
  "..........wWWWw...",
  "...........wWWWw..",
  "............wWWWw.",
  ".............wWWw.",
  "..............ww..",
  "..................",
  "..................",
], { w: "#b8ae95", W: "#f2ede2", x: "#e84a2e" });
writePng(T("mob_effect/fracture.png"), 18, 18, fractureIcon.buf);

const hypoIcon = effectIcon([
  "..................",
  "........c.........",
  "....c...c...c.....",
  ".....c..c..c......",
  "......c.c.c.......",
  ".......ccc........",
  "..cccccCCCccccc...",
  ".......ccc........",
  "......c.c.c.......",
  ".....c..c..c......",
  "....c...c...c.....",
  "........c.........",
  "........c.........",
  "..................",
  "..................",
  "..................",
  "..................",
  "..................",
], { c: "#9bd8ff", C: "#ffffff" });
writePng(T("mob_effect/hypothermia.png"), 18, 18, hypoIcon.buf);

const heatIcon = effectIcon([
  "..................",
  "........y.........",
  "...y....y....y....",
  "....y...y...y.....",
  ".....yyyyyyy......",
  "....yyOOOOOyy.....",
  "..yyyOOOOOOOyyy...",
  "....yOOOOOOOy.....",
  "....yOOOOOOOy.....",
  "..yyyOOOOOOOyyy...",
  "....yyOOOOOyy.....",
  ".....yyyyyyy......",
  "....y...y...y.....",
  "...y....y....y....",
  "........y.........",
  "..................",
  "..................",
  "..................",
], { y: "#e8a53a", O: "#e84a2e" });
writePng(T("mob_effect/heatstroke.png"), 18, 18, heatIcon.buf);

const gutIcon = effectIcon([
  "..................",
  "..................",
  ".....gggggg.......",
  "....gGGGGGGg......",
  "...gGgggggGGg.....",
  "...gGgGGGggGg.....",
  "...gGgGgGGgGg.....",
  "...gGgGGgGgGg.....",
  "...gGggGGgGGg.....",
  "...gGGggggGg......",
  "....gGGGGGg.......",
  ".....gggggg.......",
  "..................",
  "..................",
  "..................",
  "..................",
  "..................",
  "..................",
], { g: "#4a6b1e", G: "#8fbe3f" });
writePng(T("mob_effect/grimy_gut.png"), 18, 18, gutIcon.buf);

// mod icon: 16x16 emblem scaled x8 -> 128x128 (droplet + hatchet on dark field)
const emblem = art([
  "kkkkkkkkkkkkkkkk",
  "kddddddddddddddk",
  "kd....a....ff.dk",
  "kd...aAa..fFf.dk",
  "kd...aAa..fff.dk",
  "kd..aaAaa.wf..dk",
  "kd.aaAAAaawW..dk",
  "kd.aaAAAaaWw..dk",
  "kd.aaaAaaWw...dk",
  "kd..aaaaaWw...dk",
  "kd...aaaWw....dk",
  "kd......Ww....dk",
  "kd.....Ww.....dk",
  "kd....Ww......dk",
  "kddddddddddddddk",
  "kkkkkkkkkkkkkkkk",
], {
  k: "#131a22", d: "#2a3a2e",
  a: "#3f76e4", A: "#8fc4ff",
  w: "#8a6a3c", W: "#6b4f2a",
  f: "#4a4a52", F: "#8a8a96",
});
const icon = scale(emblem, 8);
writePng(path.join(RES, "assets/roughlife/icon.png"), icon.w, icon.h, icon.buf);

console.log("done");
