# Rough Life

An RLCraft-**inspired** hardcore survival overhaul for Fabric / Minecraft 26.2.
All code and art are original — no assets from RLCraft or its mods are used.

Survival is no longer free: you get thirsty, you get cold, you bleed, and you
can't punch trees.

## Systems

### Thirst (0–20 droplets, above the food bar)
- Drains slowly over time, much faster while sprinting, in heat, or while sick.
- Low thirst slows and weakens you; at zero you take damage (stops short of
  killing you unless `thirstDamageKills` is enabled).
- Drink from a **Leather Canteen** (right-click water to fill, right-click air
  to sip — raw water risks **Grimy Gut** sickness) or craft **Purified Water**
  (any bottle + **Charcoal Filter**) for a big, safe restore.

### Body temperature (thermometer left of the hotbar)
- Computed from biome, night/storms, rain, swimming, fire, the Nether, and
  nearby heat blocks (campfires, furnaces, lava, torches). Armor insulates.
- Freezing causes **Hypothermia** (slow, weak, then freeze damage); overheating
  causes **Heatstroke** (rapid hunger drain, then burn damage). Warm up by a
  campfire; cool off in water or shade.

### Injuries
- Melee hits can cause **Bleeding** — damage over time until you use a
  **Bandage** (paper + string + plant fiber).
- Hard falls cause a **Fractured Bone** — heavy slowness until you apply a
  **Splint** (sticks + plant fiber).

### Flint-age early game
- **Logs break extremely slowly without an axe** — no tree punching.
- Right-click stone with **flint** to knap it into **Flint Shards** (60%
  success). Shard + stick = **Flint Knife**; shard + fiber + stick =
  **Flint Hatchet**.
- Breaking grass sometimes drops **Plant Fiber**; 3 fiber = string.

### No free regen
- Vanilla food-based regeneration is replaced with a much slower version
  (1 HP / 8s, only when well-fed and not bleeding). Configurable:
  `naturalRegen` = `"slow"` (default) / `"off"` / `"vanilla"`.

## Config
`config/roughlife.json` — every system has an enable toggle, plus
`thirstDrainMultiplier`, `thirstDamageKills`, `dirtyWaterSickness`,
and `naturalRegen`. `/roughlife reload` re-reads it (ops).

## Commands
- `/roughlife status` — show your thirst and body temperature.
- `/roughlife thirst <0-20>` — set your thirst (ops).
- `/roughlife reload` — reload config (ops).

## Building
```
$env:JAVA_HOME="C:\Program Files\Microsoft\jdk-25.0.3.9-hotspot"
.\gradlew.bat build
```
Requires the mod on both server and client (HUD + items).

## License
All Rights Reserved — see LICENSE.
