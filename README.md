# Better Gamba

![Build Status](https://github.com/abhinandan-git/better-gamba/actions/workflows/build.yml/badge.svg)
![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
![NeoForge 21.1.219](https://img.shields.io/badge/NeoForge-21.1.219-orange)
![Licence BUSL-1.1](https://img.shields.io/badge/Licence-BUSL--1.1-red)

> A configurable lottery machine mod for Minecraft 1.21.1 on NeoForge.  
> Place the block, insert a Celestia Coin, spin the wheel, and win rewards defined entirely by the server administrator.

---

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [How to Use](#how-to-use)
- [Configuration](#configuration)
- [Reward Format](#reward-format)
- [Hopper Automation](#hopper-automation)
- [KubeJS Crafting](#kubejs-crafting)
- [JEI Integration](#jei-integration)
- [Licence](#licence)
- [Contributing](#contributing)

---

## Features

- **Lottery Machine block** — right-click to open the GUI, insert coins, and spin the wheel
- **Five rarity tiers** — Common, Uncommon, Rare, Epic, and Omega, each with configurable weight and reward pool
- **Animated spinning wheel** — highlight cycles randomly across sections and lands on the winning tier
- **Fully TOML-driven rewards** — every item, weight, and coin cost is defined in config with no code changes required
- **Hopper automation** — insert coins via the left/right faces, extract rewards via the bottom face
- **In-game config screen** — edit all settings via Pause → Mods → Better Gamba → Config
- **Live reload** — run `/reload` after editing the config to apply changes without restarting the server
- **JEI support** — all reward tiers and their possible drops are visible in JEI

---

## Requirements

| Dependency             | Version  | Required   |
|------------------------|----------|------------|
| Minecraft Java Edition | 1.21.1   | ✅ Yes      |
| NeoForge               | 21.1.219 | ✅ Yes      |
| JEI                    | 19.x     | ❌ Optional |

---

## Installation

1. Download the latest `bettergamba-x.x.x.jar` from [Releases](https://github.com/abhinandan-git/better-gamba/releases)
2. Drop it into your `mods/` folder
3. Launch the game — `config/bettergamba-common.toml` is generated automatically on first run
4. Edit the config to define your reward pools, then run `/reload` in-game to apply

---

## How to Use

1. **Craft or obtain** the Lottery Machine block and place it in the world
2. **Insert Celestia Coins** into the coin slot in the GUI (or automate with a hopper on the side)
3. **Click Spin** — the wheel animates and lands on a random tier weighted by your config
4. **Collect your reward** from the output slot, or let a hopper beneath the block extract it automatically
5. The Spin button is disabled during an active spin and briefly during the post-spin cooldown

---

## Configuration

Open the config in-game via **Pause → Mods → Better Gamba → Config**, or edit the file directly:

```
config/bettergamba-common.toml
```

After editing the file directly, run `/reload` to apply changes without restarting.

### General Settings

| Key               | Default | Description                                    |
|-------------------|---------|------------------------------------------------|
| `spinDurationMs`  | `3000`  | Duration of the spin animation in milliseconds |
| `coinCostPerSpin` | `1`     | Number of Celestia Coins consumed per spin     |
| `logSpinEvents`   | `true`  | Log each spin result to the server console     |

### Tier Settings

Each tier has two keys — `weight` and `items`:

| Key      | Description                                                           |
|----------|-----------------------------------------------------------------------|
| `weight` | Relative probability weight. Set to `0` to disable the tier entirely. |
| `items`  | List of possible rewards for this tier. See Reward Format below.      |

Default weights:

| Tier     | Default Weight | Relative Chance |
|----------|----------------|-----------------|
| Common   | 100            | ~49%            |
| Uncommon | 60             | ~29%            |
| Rare     | 30             | ~15%            |
| Epic     | 10             | ~5%             |
| Omega    | 1              | ~0.5%           |

---

## Reward Format

Each entry in an `items` list follows one of two formats:

```toml
# Plain item — no NBT
"minecraft:diamond"
```

**Full example:**

```toml
[common]
weight = 100
items = [
    "minecraft:bread",
    "minecraft:apple",
    "minecraft:coal",
]

[omega]
weight = 1
items = [
    "minecraft:nether_star",
    "minecraft:enchanted_book{StoredEnchantments:[{id:\"minecraft:sharpness\",lvl:5}]}",
]
```

Items from other mods are fully supported as long as that mod is installed:

```toml
"somemod:special_item"
```

---

## Hopper Automation

| Face         | Behaviour                                 |
|--------------|-------------------------------------------|
| Left / Right | Inserts Celestia Coins into the coin slot |
| Bottom       | Extracts rewards from the output slot     |
| Top / Back   | No interaction                            |

A hopper directly beneath the block will automatically pull rewards out as they are delivered. If no hopper is present,
the reward drops into the world at the block's position.

---

## KubeJS Crafting

Recipes for the Lottery Machine and Celestia Coin are defined via KubeJS. Place your recipe scripts in:

```
kubejs/server_scripts/
```

Example coin recipe:

```javascript
ServerEvents.recipes(event => {
    event.shaped('bettergamba:celestia_coin', [
        ' G ',
        'GDG',
        ' G '
    ], {
        G: 'minecraft:gold_ingot',
        D: 'minecraft:diamond'
    });
});
```

---

## JEI Integration

When JEI is installed, Better Gamba registers a recipe category for each active tier. You can browse all possible
rewards for each rarity directly in JEI by looking up the Lottery Machine block.

Tiers with `weight = 0` are hidden from JEI automatically.

---

## Licence

This project is licenced under the **Business Source License 1.1 (BUSL-1.1)**.

- Source code is publicly visible for review and feedback
- Redistribution, forking, and derivative works are **not permitted** without explicit written permission
- See [LICENSE](LICENSE) for full terms

---

## Contributing

Bug reports and feedback are welcome via [GitHub Issues](https://github.com/abhinandan-git/better-gamba/issues).

Pull requests are accepted for confirmed bug fixes only — please open an issue first to discuss the fix before writing
code.

Feature requests should be submitted as issues and will be reviewed by the maintainer.