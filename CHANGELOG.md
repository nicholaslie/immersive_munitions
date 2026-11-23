# Changelog

## 0.0.1-alpha (2026-11-23)

New alpha base, built from the 0.2.19 source ("remove spent shells, revert tab icon").

- Repo restructured: the Gradle project is now at the repo root and the art moved to `art/`.
- Dev environment: Immersive Engineering and TaCZ now load inside `runClient` / `runServer` (deobfuscated from `libs/`), and the mixin refmap settings are in place for TaCZ.

## 0.2.19 and earlier

Pre-alpha builds. From the 0.2.x README:

- Four left-side creative inventory categories: Brass & Zinc, Molds, Shells, and Projectiles.
- Finished-round items are no longer registered by Immersive Munitions; the assembly processor outputs TaCZ's native ammunition stacks.
- Optional bundled resource pack, **Immersive Munitions — TaCZ Ammunition Icons**, that overrides TaCZ ammo slot/UV textures.
- Dedicated icon-only items for the creative tab and side selectors so caliber labels aren't drawn on tab icons.
- Spent shells removed (0.2.19).
