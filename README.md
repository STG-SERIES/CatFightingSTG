# Cat Fighting (Paper)

Paper 1.21.11 plugin port of [Cat Fighting](https://www.curseforge.com/minecraft/mc-mods/cat-fighting) by Chesy (MIT).

Cats argue, hiss, and brawl in survival worlds. Fights are visual only — cats never take fight damage. You can also flatten cats with a shovel or a moving minecart.

Vanilla clients work as-is. Players do **not** need Fabric, Forge, or NeoForge.

> **Do not put the CurseForge Fabric/Forge/NeoForge jar in `plugins`.** Those are mods, not Paper plugins. Use the Paper jar from [Releases](../../releases).

## Download

Get the latest plugin from **[Releases](../../releases)**:

`catfighting-paper-1.21.11-1.2.1.jar`

## Requirements

| | |
| --- | --- |
| Server | [Paper](https://papermc.io/) **1.21.11** |
| Java | **21+** |
| Clients | Vanilla or any client that can join Paper |

Spigot is not supported (this plugin uses Paper's mob goal API). Folia is not supported.

## Install

1. Download `catfighting-paper-1.21.11-1.2.1.jar` from [Releases](../../releases)
2. Place it in the server `plugins` folder
3. Remove any `catfighting-fabric-*.jar` / `catfighting-neoforge-*.jar` / `catfighting-forge-*.jar` from `plugins` and `plugins/.paper-remapped`
4. Restart the server
5. Optional: edit `plugins/CatFighting/config.yml`, then run `/catfighting reload`

If Paper logs `does not contain a paper-plugin.yml or plugin.yml`, you installed the original mod jar instead of this plugin.

## Features

- Nearby cats start quarrels and fights on their own
- Sitting cats stay out of fights
- Hisses, hurt sounds, angry particles, and swipes
- Cats close in face-to-face; they do not clip into a pile or fly
- **No real cat-vs-cat damage**
- Flatten a cat by hitting it with a shovel, or by running it over with a moving minecart
- Flattened cats stay flattened across chunk unloads
- Unflatten with another shovel hit, sneak + empty-hand right-click, or `/catfighting unflatten`

Vanilla clients cannot render the original mod's pancake (Y-only scale). Flattened cats use the `SCALE` attribute and lie down, so they look small and pressed to the ground instead of paper-thin.

## Commands

| Command | Description |
| --- | --- |
| `/catfighting reload` | Reload `config.yml` |
| `/catfighting flatten` | Flatten the cat you are looking at |
| `/catfighting unflatten` | Restore the cat you are looking at |

Aliases: `/catfight`, `/cf`

| Permission | Default | Description |
| --- | --- | --- |
| `catfighting.admin` | OP | Use `/catfighting` |

Flattening with a shovel or minecart does **not** require a permission.

## Config

Generated at `plugins/CatFighting/config.yml`.

### Fights

| Key | Default | Meaning |
| --- | --- | --- |
| `enabled` | `true` | Turn cat fights on or off |
| `skip-sitting` | `true` | Sitting cats never fight |
| `tamed-cats-can-fight` | `true` | Allow tamed cats to fight |
| `same-owner-can-fight` | `true` | Cats with the same owner can fight each other |
| `kittens-can-fight` | `false` | Baby cats can fight |
| `check-interval-ticks` | `40` | How often a cat looks for a fight (20 ticks = 1 second) |
| `start-chance` | `0.08` | Chance to start a fight when another eligible cat is nearby |
| `search-range` | `8.0` | How far cats look for an opponent |
| `melee-range` | `1.45` | Distance at which they swipe |
| `duration-ticks` | `160` | How long a fight lasts |
| `cooldown-ticks` | `400` | Wait after a fight before another can start |
| `hiss-interval-ticks` | `22` | Time between hisses |
| `swipe-interval-ticks` | `10` | Time between swipes |
| `fight-speed` | `1.7` | How fast they charge in |
| `knockback` | `0.08` | How far a swipe shoves the other cat (ground only) |

### Flatten

| Key | Default | Meaning |
| --- | --- | --- |
| `enabled` | `true` | Turn flattening on or off |
| `shovel` | `true` | Hitting with a shovel flattens a cat |
| `minecart` | `true` | A moving minecart can flatten a cat |
| `minecart-min-speed` | `0.28` | Minimum minecart speed to flatten |
| `scale` | `0.22` | Flattened size (vanilla `SCALE` attribute) |
| `sneak-right-click-unflatten` | `true` | Sneak + empty-hand right-click restores a cat |
| `shovel-toggles` | `true` | Hitting a flattened cat with a shovel pops it back up |

## Build

```bash
mvn -DskipTests package
```

Java 21+ and Maven are required. The jar is written to:

`target/catfighting-paper-1.21.11-1.2.1.jar`

## Project layout

```
src/main/java/dev/catfighting/
  CatFightingPlugin.java
  fight/          cat quarrel AI
  flatten/        shovel + minecart flatten
  listeners/      spawn, damage, minecart, unflatten
  command/        /catfighting
src/main/resources/
  plugin.yml
  config.yml
```

## Credits

- Original mod: [Chesy](https://www.curseforge.com/minecraft/mc-mods/cat-fighting)
- Paper port: Kostas (STGHECKER / STG-SERIES)

## License

MIT. See [LICENSE](LICENSE).

The original Cat Fighting mod is also MIT-licensed. This plugin is an independent Paper port, not an official Chesy release.
