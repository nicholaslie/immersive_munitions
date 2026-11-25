#!/usr/bin/env python3
"""Zinc/brass models, lang entries, loot tables, recipes, tags and worldgen.

    python tools/gen_resources.py

The zinc/brass textures come from art/materials/Materials.java, and the zinc ores' block models are the two-layer
overlay models (models/block/ore_overlay.json, vanilla stone or deepslate under our zinc overlay), so they are left
alone here. Ammo items (casings, projectiles, molds) are not handled here.
"""
import json, os, sys

MOD = "immersive_munitions"
RES = "src/main/resources"
A = f"{RES}/assets/{MOD}"
D = f"{RES}/data/{MOD}"
def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        if isinstance(obj, str): f.write(obj)
        else: json.dump(obj, f, indent=2); f.write("\n")

# ---------- registry content ----------
ITEMS = {"raw_zinc":"Raw Zinc","zinc_ingot":"Zinc Ingot","zinc_nugget":"Zinc Nugget","zinc_dust":"Zinc Dust",
         "brass_ingot":"Brass Ingot","brass_nugget":"Brass Nugget","brass_dust":"Brass Dust",
         "brass_plate":"Brass Plate","brass_gear":"Brass Gear","brass_rod":"Brass Rod","brass_wire":"Brass Wire"}
BLOCKS = {"zinc_ore":"Zinc Ore","deepslate_zinc_ore":"Deepslate Zinc Ore","raw_zinc_block":"Block of Raw Zinc",
          "zinc_block":"Block of Zinc","brass_block":"Block of Brass"}
lang = {f"item.{MOD}.{k}":v for k,v in ITEMS.items()}
lang.update({f"block.{MOD}.{k}":v for k,v in BLOCKS.items()})
lang[f"itemGroup.{MOD}"] = "Immersive Munitions"
lang["immersive_munitions.config.zinc_worldgen"] = "Zinc worldgen"
# Merge so the ammo item names already in the file survive
lang_path = f"{A}/lang/en_us.json"
if os.path.exists(lang_path):
    with open(lang_path, encoding="utf-8") as f:
        lang = {**json.load(f), **lang}
w(lang_path, lang)

for k in ITEMS:
    w(f"{A}/models/item/{k}.json", {"parent":"minecraft:item/generated","textures":{"layer0":f"{MOD}:item/{k}"}})
for k in BLOCKS:
    w(f"{A}/blockstates/{k}.json", {"variants":{"":{"model":f"{MOD}:block/{k}"}}})
    if not k.endswith("zinc_ore"):   # the ores use the two-layer overlay models (ore_overlay.json)
        w(f"{A}/models/block/{k}.json", {"parent":"minecraft:block/cube_all","textures":{"all":f"{MOD}:block/{k}"}})
    w(f"{A}/models/item/{k}.json", {"parent":f"{MOD}:block/{k}"})
w(f"{RES}/pack.mcmeta", {"pack":{"description":"Immersive Munitions resources","pack_format":15}})

# ---------- loot tables ----------
def self_drop(b):
    return {"type":"minecraft:block","pools":[{"rolls":1.0,"bonus_rolls":0.0,
      "conditions":[{"condition":"minecraft:survives_explosion"}],
      "entries":[{"type":"minecraft:item","name":f"{MOD}:{b}"}]}],"random_sequence":f"{MOD}:blocks/{b}"}
def ore_drop(b, raw):
    silk = {"condition":"minecraft:match_tool","predicate":{"enchantments":[{"enchantment":"minecraft:silk_touch","levels":{"min":1}}]}}
    return {"type":"minecraft:block","pools":[{"rolls":1.0,"bonus_rolls":0.0,"entries":[{"type":"minecraft:alternatives","children":[
      {"type":"minecraft:item","conditions":[silk],"name":f"{MOD}:{b}"},
      {"type":"minecraft:item","functions":[{"function":"minecraft:apply_bonus","enchantment":"minecraft:fortune","formula":"minecraft:ore_drops"},
                                            {"function":"minecraft:explosion_decay"}],"name":f"{MOD}:{raw}"}]}]}],
      "random_sequence":f"{MOD}:blocks/{b}"}
for b in ["raw_zinc_block","zinc_block","brass_block"]: w(f"{D}/loot_tables/blocks/{b}.json", self_drop(b))
for b in ["zinc_ore","deepslate_zinc_ore"]: w(f"{D}/loot_tables/blocks/{b}.json", ore_drop(b,"raw_zinc"))

# ---------- recipes ----------
def it(i): return {"item":f"{MOD}:{i}"}
def shaped9(res, src): return {"type":"minecraft:crafting_shaped","category":"misc","key":{"#":it(src)},"pattern":["###","###","###"],"result":{"item":f"{MOD}:{res}"}}
def unpack(res, src, n): return {"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[it(src)],"result":{"item":f"{MOD}:{res}","count":n}}
R = f"{D}/recipes"
for m in ("zinc","brass"):
    w(f"{R}/{m}_block_from_ingots.json", shaped9(f"{m}_block", f"{m}_ingot"))
    w(f"{R}/{m}_ingot_from_block.json", unpack(f"{m}_ingot", f"{m}_block", 9))
    w(f"{R}/{m}_ingot_from_nuggets.json", shaped9(f"{m}_ingot", f"{m}_nugget"))
    w(f"{R}/{m}_nugget_from_ingot.json", unpack(f"{m}_nugget", f"{m}_ingot", 9))
w(f"{R}/raw_zinc_block_from_raw_zinc.json", shaped9("raw_zinc_block","raw_zinc"))
w(f"{R}/raw_zinc_from_raw_zinc_block.json", unpack("raw_zinc","raw_zinc_block",9))
for src, t, xp in [("zinc_ore",200,0.7),("deepslate_zinc_ore",200,0.7),("raw_zinc",200,0.7),("zinc_dust",200,0.7)]:
    for kind, time in (("smelting",t),("blasting",t//2)):
        w(f"{R}/zinc_ingot_from_{kind}_{src}.json", {"type":f"minecraft:{kind}","category":"misc","group":"zinc_ingot",
          "ingredient":it(src),"result":f"{MOD}:zinc_ingot","experience":xp,"cookingtime":time})
for kind, time in (("smelting",200),("blasting",100)):
    w(f"{R}/brass_ingot_from_{kind}_brass_dust.json", {"type":f"minecraft:{kind}","category":"misc","group":"brass_ingot",
      "ingredient":it("brass_dust"),"result":f"{MOD}:brass_ingot","experience":0.5,"cookingtime":time})

# ---------- tags ----------
def tag(path, vals): w(path, {"replace":False,"values":vals})
F = f"{RES}/data/forge/tags"; MC = f"{RES}/data/minecraft/tags"
m = lambda n: f"{MOD}:{n}"
tag(f"{F}/items/ingots/zinc.json",[m("zinc_ingot")]);   tag(f"{F}/items/ingots/brass.json",[m("brass_ingot")])
tag(f"{F}/items/nuggets/zinc.json",[m("zinc_nugget")]); tag(f"{F}/items/nuggets/brass.json",[m("brass_nugget")])
tag(f"{F}/items/dusts/zinc.json",[m("zinc_dust")]);     tag(f"{F}/items/dusts/brass.json",[m("brass_dust")])
tag(f"{F}/items/raw_materials/zinc.json",[m("raw_zinc")])
for form, plural in (("plate","plates"),("gear","gears"),("rod","rods"),("wire","wires")):
    tag(f"{F}/items/{plural}/brass.json",[m(f"brass_{form}")])
    tag(f"{F}/items/{plural}.json",[f"#forge:{plural}/brass"])
tag(f"{F}/items/rods/all_metal.json",["#forge:rods/brass"])  # merges into IE's tag (replace=false)
tag(f"{F}/items/ingots.json",["#forge:ingots/zinc","#forge:ingots/brass"])
tag(f"{F}/items/nuggets.json",["#forge:nuggets/zinc","#forge:nuggets/brass"])
tag(f"{F}/items/dusts.json",["#forge:dusts/zinc","#forge:dusts/brass"])
tag(f"{F}/items/raw_materials.json",["#forge:raw_materials/zinc"])
ores = [m("zinc_ore"), m("deepslate_zinc_ore")]
tag(f"{F}/items/ores/zinc.json",ores);                  tag(f"{F}/blocks/ores/zinc.json",ores)
tag(f"{F}/items/ores.json",["#forge:ores/zinc"]);       tag(f"{F}/blocks/ores.json",["#forge:ores/zinc"])
for kind in ("items","blocks"):
    tag(f"{F}/{kind}/ores_in_ground/stone.json",[m("zinc_ore")])
    tag(f"{F}/{kind}/ores_in_ground/deepslate.json",[m("deepslate_zinc_ore")])
    tag(f"{F}/{kind}/ore_rates/singular.json",ores)
    tag(f"{F}/{kind}/storage_blocks/zinc.json",[m("zinc_block")])
    tag(f"{F}/{kind}/storage_blocks/brass.json",[m("brass_block")])
    tag(f"{F}/{kind}/storage_blocks/raw_zinc.json",[m("raw_zinc_block")])
    tag(f"{F}/{kind}/storage_blocks.json",["#forge:storage_blocks/zinc","#forge:storage_blocks/brass","#forge:storage_blocks/raw_zinc"])
allb = ores + [m("raw_zinc_block"),m("zinc_block"),m("brass_block")]
tag(f"{MC}/blocks/mineable/pickaxe.json",allb); tag(f"{MC}/blocks/needs_stone_tool.json",allb)

# ---------- worldgen ----------
w(f"{D}/worldgen/configured_feature/zinc_ore.json",{"type":"minecraft:ore","config":{"discard_chance_on_air_exposure":0.0,"size":7,"targets":[
  {"state":{"Name":m("zinc_ore")},"target":{"predicate_type":"minecraft:tag_match","tag":"minecraft:stone_ore_replaceables"}},
  {"state":{"Name":m("deepslate_zinc_ore")},"target":{"predicate_type":"minecraft:tag_match","tag":"minecraft:deepslate_ore_replaceables"}}]}})
w(f"{D}/worldgen/placed_feature/zinc_ore_placed.json",{"feature":m("zinc_ore"),"placement":[
  {"type":"minecraft:count","count":10},{"type":"minecraft:in_square"},
  {"type":"minecraft:height_range","height":{"type":"minecraft:trapezoid","min_inclusive":{"absolute":-64},"max_inclusive":{"absolute":55}}},
  {"type":"minecraft:biome"}]})
w(f"{D}/forge/biome_modifier/zinc_ore.json",{"type":f"{MOD}:add_features_unless_mod_loaded","biomes":"#minecraft:is_overworld",
  "features":m("zinc_ore_placed"),"step":"underground_ores","disabled_if_mod_loaded":"immersivegeology"})
print("done")
