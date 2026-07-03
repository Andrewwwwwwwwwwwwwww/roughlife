# Changelog

## 1.5.0 — 2026-07-02
The survival-loop overhaul (fourth playtest feedback).

- WATER ECONOMY REWORK (early game was starved for water):
  - Vanilla water bottles now restore thirst (+4, 35% Grimy Gut risk) — you
    can drink from day one with just a glass bottle.
  - NEW Dirty Water item: common chest loot everywhere (~37% of chests);
    drinkable in a pinch (+3, 60% sickness) or COOKED into Purified Water in
    a furnace / campfire / smoker. Purified Water is also rare chest loot.
- TEMPERATURE FIXES (was too cold, too often):
  - Shelter matters: under any roof (no sky above) night and rain chill are
    cancelled and you get a +5 warmth bonus — houses are warm now.
  - Warmer baseline in cold biomes (12 + biome*10 mapping).
  - Body temperature reacts ~2x faster, so campfires/torches are felt in
    seconds (the "delay" reported).
  - Lanterns now radiate heat (5), torches/soul lights 4, magma 6.
- RESPAWN SCATTER: without a bed/anchor, every death respawns you at a fresh
  random wilderness spot (never the same place twice). Config
  `scatterRespawns`: no-bed (default) / always / off, `scatterRadius`.
- SLEEP IS EARNED: any hostile within 40 blocks keeps you awake (vanilla is
  8 — village beds no longer trivialize nights), and you can't sleep
  parched/starving. Config `sleepHostileRadius`, `sleepNeedsComfort`.
- DAYTIME DANGER: pillager hunting parties (2-3, sometimes vindicator-led)
  roam near surface players; drowned rise under anyone crossing open water.
  Config `dangerDaytime`, `dangerDaytimeIntervalTicks`.
- `/roughlife guide` updated for all of the above.

## 1.4.0 — 2026-07-02
Blood Moons.

- NEW: some nights (8% by default) are Blood Moons — announced with a dark-red
  title and a distant wither cry. Dangerous World spawns twice as fast with a
  50% higher hostile cap, and blood-moon mobs get Speed + Strength.
- Whether a night is a blood moon derives deterministically from the world
  seed + day number: no saved state, consistent across restarts.
- Config: `bloodMoonEnabled`, `bloodMoonChance`.
- (Fabric 26.2 has no Enhanced Celestials port — this is the original stand-in.)

## 1.3.0 — 2026-07-02
Danger + discoverability (third playtest feedback).

- NEW: "Dangerous World" — at night and underground, extra hostiles
  (zombies/skeletons/spiders/creepers, weighted) continuously spawn on dark
  ground 20-42 blocks from players, capped by nearby-hostile count. Torch-lit
  areas are safe. Config: `dangerousWorld`, `dangerMaxNearbyHostiles`,
  `dangerIntervalTicks`.
- NEW: recipe-book unlock advancements for all 9 recipes — crafting recipes
  now appear in the vanilla recipe book once you pick up a relevant
  ingredient (and REI in the pack can browse them all).
- Guide updated accordingly.

## 1.2.0 — 2026-07-02
RLCraft-accuracy pass (second playtest feedback).

- CHANGED: punching logs bare-handed now destroys the block with NO drops
  (previously slow but still dropped wood). Overlay message explains why.
  Creative mode unaffected.
- NEW: tree felling — breaking a log with any axe fells every connected log
  at or above the cut (BFS, default cap 128 logs, one durability per log;
  sneak to break a single log). Config: `treeFelling`, `treeFellingMaxLogs`.
- ART: flint shard shrunk — it's a knapped-off chip, not a boulder.

## 1.1.0 — 2026-07-02
Early-game feel + feedback fixes (first playtest feedback).

- NEW: Rocks — sneak + right-click dirt/stone/sand/gravel with an empty hand
  to scrounge a Rock off the ground. Rocks knap on stone (40% shard) and
  3 rocks craft into flint.
- NEW: punching leaves has a 35% chance to drop sticks ("sticks from trees").
- NEW: `/roughlife guide` explains the whole progression in-game.
- CHANGED: cold no longer uses a custom Hypothermia effect — it now drives the
  vanilla freezing system (frost overlay, shivering, slowdown, freeze damage),
  so leather armor blocks freezing exactly like powder snow. Hypothermia
  effect and its (off-center) icon removed.
- FIXED: heatstroke and grimy-gut effect icons re-centered on the 18x18 canvas.

## 1.0.0 — 2026-07-02
Initial release for Minecraft 26.2 (Fabric).

- Thirst system: 0–20 scale with droplet HUD bar, activity-based drain,
  slowness/weakness/damage penalties at low thirst.
- Drinks: Leather Canteen (8 sips, refillable at water, raw-water sickness
  risk), Purified Water (bottle + Charcoal Filter), Grimy Gut debuff.
- Body temperature: biome/weather/time/heat-source model with thermometer HUD,
  Hypothermia and Heatstroke effects, armor insulation.
- Injuries: Bleeding on melee hits (cured by Bandage), Fractured Bone on hard
  falls (cured by Splint).
- Flint early game: logs are ~8x slower to break without an axe; knap flint on
  stone for Flint Shards; Flint Knife and Flint Hatchet tools; Plant Fiber
  drops from grass (3 = string).
- Natural regen replaced with slow, food-gated regen (configurable
  vanilla/slow/off).
- `config/roughlife.json` with per-system toggles; `/roughlife
  status|thirst|reload` commands.
- All-original code and pixel art (textures generated by tools/gen_assets.js).
- NOT runtime-tested yet (build-verified only; mixin targets verified against
  the 26.2 deobf jar with javap).
