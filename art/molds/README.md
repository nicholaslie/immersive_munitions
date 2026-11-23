# Casing molds (64x64)

`categories/` holds the 8 TACZ weapon-category molds (pistol, smg, rifle, dmr, sniper, mg, shotgun, rpg), drawn in the style of the 16x16 mold_* icons: grey bevelled block, see-through casing-shaped recess (mouth up-left, head down-right). Shotgun is just the brass base; rpg is the short grenade cup; mg is a rifle case wrapped in a belt link.

Per-caliber molds (`mold_casing_<id>.png` in the mod) are the category mold plus the caliber label. `art/labels/Labels.java` generates them straight into the mod textures; they aren't stored here.

Category used per caliber (majority of the guns that use it; ties go to the first listed):
pistol: 9mm 22wmr 357mag 45acp 500mag 50ae 762x25 | smg: 46x30 57x28 | rifle: 300blk 308 545x39 556x45 58x42 68x51fury 6x43 762x39 | sniper: 30_06 338 45_70 50bmg 762x54 792x57 | shotgun: 12g | rpg: 40mm, rpg_rocket
To reassign one, change its category in the `CALS` table in `art/labels/Labels.java` and re-run it. The category also sets the label's color line.
