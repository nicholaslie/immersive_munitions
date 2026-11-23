# Casing molds (64x64)

`categories/` holds the 8 TACZ weapon-category molds (pistol, smg, rifle, dmr, sniper, mg, shotgun, rpg), drawn in the style of the 16x16 mold_* icons: grey bevelled block, see-through casing-shaped recess (mouth up-left, head down-right). Shotgun is just the brass base; rpg is the short grenade cup; mg is a rifle case wrapped in a belt link.

`calibers/mold_casing_<id>.png` are copies of the category mold with TACZ's caliber label (32x8, copied 1:1 from TACZ's slot icons) in the top-left corner.

Category used per caliber (majority of the guns that use it; ties go to the first listed):
pistol: 9mm 22wmr 357mag 45acp 500mag 50ae 762x25 | smg: 46x30 57x28 | rifle: 300blk 308 545x39 556x45 58x42 68x51fury 6x43 762x39 | sniper: 30_06 338 45_70 50bmg 762x54 792x57 | shotgun: 12g | rpg: 40mm
Change the CAL table in source/build_molds.py and re-run to reassign any of them.
