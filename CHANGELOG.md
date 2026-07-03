# Changelog

## 1.8.5 — 2026-07-02
Razorwing rework: visible, avoidable, territorial (the dragon rule).

- Razorwings now spawn 45-70 blocks away and 16-28 blocks up — a circling
  silhouette you can see and route around, not an ambush.
- They are TERRITORIAL: each claims its spawn point as a roost (saved with
  the entity) and patrols a 24-block territory at altitude. They only attack
  players inside that airspace, and disengage the moment you leave it.
- Lethality tuned: attack 3 -> 2, Bleeding on hit 8s -> 4s, and a 3-second
  recovery climb between dive passes (a real window to shield, fight, or
  retreat). Second-of-a-pair spawn chance 40% -> 20%.

## 1.8.4 — 2026-07-02
Instant respawn scatter.

- The scatter destination hunt now starts THE MOMENT you die: the target
  chunk generates asynchronously while the death screen is up, so clicking
  Respawn places you at the new location immediately (previously the
  main thread synchronously generated far-away chunks after respawn —
  up to ~20 seconds of standing at world spawn).
- Ocean/void targets retry elsewhere (still async, up to 6 chunks), with a
  10-second safety valve that falls back to world spawn with a log warning.

## 1.8.1 — 2026-07-02
Mob AI hardening pass (full code review of all custom entities).

- FIXED (critical): Stinger Jellies could drown — as Monster subclasses they
  ran out of air underwater. They now breathe underwater, obviously.
- FIXED (visual): Wailing Skull was rotated twice (renderer body-yaw + model
  part yaw), making it over-spin when turning. Model yaw removed.
- FIXED: flyers no longer buzz endlessly against tree trunks/walls — the
  shared flight controller now climbs over obstacles on collision.
- FIXED: jelly sting cooldown could underflow over very long lifetimes.
- FIXED: the second mob of a sky pair (skulls/phantoms/razorwings) spawned
  2 blocks up without an air check and could clip into leaves; it now
  verifies clearance or shares the leader's spawn point.

## 1.8.0 — 2026-07-02
Third custom monster: the Stinger Jelly — the water itself stings.

- NEW MOB: the **Stinger Jelly** — a silent box jellyfish that drifts open
  water in swarms of 2-3, slowly looming toward swimmers. Contact stings
  (damage + Poison). Two color variants (blue/sulphur). Dries out and dies
  on land. Drops slime balls and the odd glow ink sac.
- Open-water danger spawns now roll: 20% guardian, 30% jelly swarm, 50%
  drowned.
- Textures and model layout from BetterEnd's cubozoa (Team BetterX, MIT,
  see THIRD-PARTY-NOTICES.md); entity code, AI, and animations original.

## 1.7.0 — 2026-07-02
Second custom monster: the Razorwing — the daytime sky bites back.

- NEW MOB: the **Razorwing** — a giant predatory dragonfly that patrols the
  daytime sky in ones and twos, dive-bombing travelers in strafing runs;
  its wing edges cause **Bleeding** on hit. Drops string and the occasional
  phantom membrane. Buzzes audibly (donor sounds) so you hear it coming.
- Daytime danger spawns now roll razorwings (~12%) before pillager packs.
- Model layout, texture, and buzz sounds from BetterEnd (Team BetterX, MIT,
  see THIRD-PARTY-NOTICES.md); entity code, AI, and animations original.
- Refactor: shared FloatMoveControl for all Rough Life flyers.

## 1.6.0 — 2026-07-02
First custom monster: the Wailing Skull.

- NEW MOB: the **Wailing Skull** — a flying skull that haunts the night sky,
  drifting over the wilderness and swooping at travelers (bite + peel-away
  attack pattern, vex shriek on charge). Burns up in sunlight; drops bones.
- Night-sky danger spawns are now 60% wailing skull pairs / 40% phantom pairs.
- Entity code, model, and AI are original. The texture is the flying-skull
  art from BetterNether (Team BetterX), used under its MIT license — full
  notice in THIRD-PARTY-NOTICES.md.

## 1.5.1 — 2026-07-02
Air and sea pressure (no third-party air/sea hostile mods exist on 26.2 yet).

- Night surface travel now draws phantom pairs overhead (~12% per danger
  tick, no insomnia needed).
- Open-water danger spawns are now 25% guardians ("sea monster"), 75% drowned.

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
