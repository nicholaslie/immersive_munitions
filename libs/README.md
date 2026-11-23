# libs/

Third-party mod jars that the dev environment compiles and runs against. Git ignores the jars themselves, so after cloning you need to put these exact files here:

| File | Where to get it |
|---|---|
| `ImmersiveEngineering-1.20.1-10.2.0-183.jar` | CurseForge / Modrinth: Immersive Engineering, 1.20.1, 10.2.0-183 |
| `tacz-forge-1.20.1-1.2.0-hotfix.jar` | CurseForge / Modrinth: Timeless and Classics Zero, 1.20.1, 1.2.0-hotfix |

The versions are set in `gradle.properties` (`ie_version`, `tacz_version`). When you update a jar, rename it to the `<artifact>-<version>.jar` pattern and change the matching property.

These jars only load in the dev game (`runClient` / `runServer`). Nothing here goes into the built mod jar.
