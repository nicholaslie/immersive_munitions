# Immersive Munitions

An Immersive Engineering addon for making TaCZ ammunition from physical parts: casings, projectiles and gunpowder. It adds zinc and brass materials, and worldgen that steps aside when Immersive Geology is installed.

**Minecraft 1.20.1 · Forge 47.x · Java 17** · Current version: `0.0.1-alpha` (see [CHANGELOG.md](CHANGELOG.md))

## Repo layout

```
src/main/java/dev/immersive_munitions/
  ImmersiveMunitions.java   mod entry point (@Mod)
  Config.java               config spec
  client/                   creative tabs, bundled TaCZ ammo icon resource pack
  processing/               ammo assembly processor
  registry/                 items, blocks, creative tabs
  tacz/                     building TaCZ ammo stacks
  worldgen/                 zinc ore biome modifiers
src/main/resources/
  assets/immersive_munitions/   models, textures, lang, blockstates
  data/                         recipes, loot tables, tags, worldgen
  resourcepacks/immersive_munitions_ammo/   optional TaCZ icon pack shipped inside the jar
  META-INF/mods.toml            mod metadata (values filled from gradle.properties)
art/      source artwork and sprite generators (not packaged), see art/README.md
tools/    gen_resources.py: regenerates zinc/brass textures, models and data
libs/     IE + TaCZ jars for the dev environment (not committed), see libs/README.md
```

## First-time setup

1. Install a JDK. Gradle runs on any JDK from 17 to 21 and downloads Java 17 for compiling by itself.
2. Put the two dependency jars in `libs/`, as listed in [libs/README.md](libs/README.md).
3. In VS Code, install the recommended extensions when prompted (Java Extension Pack and Gradle for Java). In your VS Code user settings, set `"java.import.gradle.java.home"` to a JDK 17 or 21 folder. The Java extension ships with Java 25, which Gradle 8.8 can't run on.
4. Build once from the repo root. The first run decompiles Minecraft and takes a few minutes:
   ```bat
   gradlew.bat build
   ```
5. Generate the VS Code launch configs:
   ```bat
   gradlew.bat genVSCodeRuns
   ```
   You can then start the game with F5 (choose "runClient"), with debugging and hot-swap.

## Everyday commands

| Command | What it does |
|---|---|
| `gradlew.bat runClient` | Launches a dev Minecraft client with this mod, IE and TaCZ |
| `gradlew.bat runServer` | Launches a dev dedicated server |
| `gradlew.bat build` | Builds the release jar to `build/libs/immersive_munitions-<version>.jar` |
| `gradlew.bat --refresh-dependencies` | Fixes a broken or stale dependency cache |

Dev worlds, logs and configs go in `run/`, which git ignores.

## Versioning

The version is `mod_version` in `gradle.properties`, using `MAJOR.MINOR.PATCH-alpha` (alpha 0.01 is `0.0.1-alpha`, alpha 0.02 is `0.0.2-alpha`). Bump it and add a line to `CHANGELOG.md` for each build you hand out.

## Optional TaCZ icon pack

The jar ships a resource pack, **Immersive Munitions — TaCZ Ammunition Icons**, that replaces TaCZ's ammo inventory icons with this mod's artwork (dropped and held rounds keep TaCZ's 3D model). Players turn it on under **Options → Resource Packs**.

## Regenerating materials

From the repo root:

- `java art/materials/Materials.java` redraws the zinc and brass textures from IE's metal art, in this mod's palettes.
- `python tools/gen_resources.py` rebuilds the zinc/brass models, tags, loot tables, recipes and worldgen.
- `java tools/CasingRecipes.java` rebuilds the casing recipes (metal press, casing mold blueprints).

## Credits and license

The mod's code and original art are licensed GPL-3.0-or-later; see [LICENSE](LICENSE). Some material comes from other mods: creative-tab code adapted from TaCZ (GPLv3), and textures recolored from Immersive Engineering (Blu's License of Common Sense) and Immersive Geology (LGPL). [NOTICE.md](NOTICE.md) covers the details.
