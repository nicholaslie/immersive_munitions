"""Texture the Ammunition Assembler model and export it into the mod.

    blender -b --factory-startup --python art/assembler/texture.py -- <assembler.blend> <mod resources dir> <IE block textures dir> <preview dir> [export]

<IE block textures dir> is IE's assets/immersiveengineering/textures/block (reference/ has a copy).
With "export" it writes ammo_assembler.obj/.mtl and ammo_assembler_split.json into the mod; previews are always rendered.

OBJ frame = IE bottler frame: x -1..2, y -1..2, z 0..2; cell (floor x, floor y, floor z) is the block offset
from the master block (blueprint position 1,1,0).

Texture modes:
  tile    IE texture, one copy per block
  tile_r  tile, turned 90 degrees (vertical stripes run horizontally)
  belt    IE conveyor, running along the belt
  whole   texture (or one sheet tile) stretched over the part; whole_r turns it 90 degrees
  rect    one tile of an IE sheet stretched over the part (ports)
  panel9  our long sheet-metal panels: one panel per wall, seams and corner rivets only at its edges
  rail    our conveyor rail strip: one rivet at each end
"""
import bpy, bmesh, json, math, os, sys
from mathutils import Vector

args = sys.argv[sys.argv.index("--") + 1:]
src, res_dir, ie_tex, out_dir = args[:4]
EXPORT = len(args) > 4 and args[4] == "export"
SHEET_RL = "immersive_munitions:block/multiblocks/ammo_assembler"
SHEET_FILE = os.path.join(res_dir, "assets/immersive_munitions/textures/block/multiblocks/ammo_assembler.png")


# ---------- what each part is made of ----------
def ie(path, mode="tile"):
    return ("ie", path, mode)


def sheet(col, row):
    return ("sheet", (col, row), "whole")


def rect(path, x0, y0, w, h, size=128):
    """One tile of an IE sheet stretched over the whole part (ports and the like)."""
    return ("ie", path, ("rect", x0, y0, w, h, size))


RS_PORT = rect("multiblocks/multiblock_components", 96, 48, 16, 16)     # IE redstone control port
POWER_PORT = rect("multiblocks/multiblock_components", 0, 64, 16, 16)    # IE power input port (on top, for a connector)
ITEM_PORT = rect("multiblocks/multiblock_components", 48, 32, 16, 16)   # IE item port (orange light)
PANEL_LIGHT = ("sheet", (0, 0), ("panel9", 0, 64))     # long sheet panels, redstone-port greys
PANEL_DARK = ("sheet", (0, 0), ("panel9", 64, 64))     # long sheet panels, item-port greys
STEEL_H = ie("metal/sheetmetal_steel", "tile_r")            # IE steel sheet, stripes running horizontally
STEEL_LIGHT = ("sheet", (0, 2), "tile_r")              # the same steel brightened to the old light-panel grey
RAIL = ("sheet", (0, 0), ("rail", 64, 0))              # conveyor side rails
WOOD = ie("wooden_decoration/treated_wood_horizontal")

# by the model's flat colour
BY_COLOR = {
    "body": STEEL_LIGHT, "panel": STEEL_LIGHT, "trim": STEEL_LIGHT,
    "mech": ie("metal/sheetmetal_iron"), "accent": ie("metal/sheetmetal_brown"), "hydro": ie("metal/sheetmetal_blue"),
    "dark": ie("metal_decoration/radiator", "whole"), "red": ie("metal_decoration/redstone_engineering", "whole"),
    "belt": ie("conveyor/conveyor", "belt"), "grille": ie("metal_decoration/steel_scaffolding_top_grate_top"),
    "screen": sheet(0, 0), "white": sheet(1, 0), "glass": sheet(2, 0), "brass": sheet(0, 1), "copper": sheet(1, 1),
    "rubber": sheet(2, 1), "lamp": sheet(0, 3),
}
# by part name, overriding the colour
BY_NAME = {
    "plinth": STEEL_LIGHT, "roof": STEEL_H, "roof_cap": STEEL_H,
    "base_trim": WOOD, "upper_trim": WOOD,
    "belt_rail_back": STEEL_LIGHT, "belt_rail_front": STEEL_LIGHT,
    "lamp_plate": ie("metal/sheetmetal_steel", "whole_r"),   # turned to match the roof under it
    "port_lip": ie("metal/sheetmetal_steel"),   # raised lip under the roof ports, same steel as the roof
    "rs_lamp": RS_PORT, "power_port": POWER_PORT, "power_lip": ie("metal/sheetmetal_steel"), "intake": ITEM_PORT,
    "sight_glass": sheet(3, 1), "feed_tube": sheet(2, 0),
}

# station heads: machined look (turned steel, chrome, gunmetal housings, red handwheel)
TURNED, CHROME, HOUSING, KNURL = sheet(1, 2), sheet(2, 2), sheet(4, 2), sheet(2, 3)
# Moving parts, exported as ammo_assembler_<group>.obj and drawn by AmmoAssemblerRenderer instead of the static model.
# Their pivots and strokes live in the renderer; keep the names in sync.
DYNAMIC = {
    "powder": ("drop_tube", "expander_tip"),
    "wheel": ("metering_wheel",),
    "jaws": ("jaw_left", "jaw_right"),
    "press": ("crosshead", "ram", "seating_die", "lock_ring"),
    # not moving, but switched: the renderer moves it onto the lit lamp tile (one tile right) while the machine has energy
    "lamp": ("power_lamp",),
}
BY_NAME.update({
    "metering_valve": HOUSING, "escapement": HOUSING, "metering_wheel": sheet(3, 2),
    "pawl_lever": TURNED, "funnel_cone": TURNED, "drop_tube": TURNED, "seating_die": TURNED,
    "jaw_left": KNURL, "jaw_right": KNURL, "ram": CHROME, "guide_rod": CHROME,
})


def spec_for(ob):
    name = ob.name.split(".")[0]
    if name in BY_NAME:
        return BY_NAME[name]
    color = ob.data.materials[0].name.replace("flat_", "")
    return BY_COLOR[color]


def to_obj(v):      # Blender -> OBJ frame
    return Vector((v.x, v.z, -v.y))


def frac(a, ref):
    return a - math.floor(ref)


bpy.ops.wm.open_mainfile(filepath=src)

# bake modifiers (chamfers) into the meshes
dg = bpy.context.evaluated_depsgraph_get()
for ob in [o for o in bpy.data.objects if o.type == 'MESH' and o.modifiers]:
    mesh = bpy.data.meshes.new_from_object(ob.evaluated_get(dg))
    ob.modifiers.clear()
    ob.data = mesh

materials = {}


def material_for(spec):
    kind, key, mode = spec
    name = ("ie__" + key.replace("/", "__")) if kind == "ie" else "sheet"
    if name in materials:
        return materials[name]
    m = bpy.data.materials.new(name)
    m.use_nodes = True
    nt = m.node_tree
    bsdf = nt.nodes.get("Principled BSDF")
    tex = nt.nodes.new("ShaderNodeTexImage")
    tex.interpolation = 'Closest'
    path = SHEET_FILE if kind == "sheet" else os.path.join(ie_tex, key + ".png")
    img = bpy.data.images.load(path)
    if kind == "ie" and img.size[1] > img.size[0]:      # animated strip (conveyor): preview its first frame only
        w = img.size[0]
        frame = bpy.data.images.new(name + "_frame", w, w, alpha=True)
        frame.pixels = list(img.pixels[(img.size[1] - w) * w * 4:])
        img = frame
    tex.image = img
    nt.links.new(tex.outputs["Color"], bsdf.inputs["Base Color"])
    nt.links.new(tex.outputs["Alpha"], bsdf.inputs["Alpha"])
    materials[name] = (m, spec)
    return materials[name]


cells = set()
for ob in [o for o in bpy.data.objects if o.type == 'MESH']:
    spec = spec_for(ob)
    mat, _ = material_for(spec)
    ob.data.materials.clear()
    ob.data.materials.append(mat)
    kind, key, mode = spec

    bm = bmesh.new()
    bm.from_mesh(ob.data)
    # cut tiling textures at block boundaries: a Minecraft face can't repeat its texture
    if mode in ("tile", "tile_r", "belt") or (isinstance(mode, tuple) and mode[0] != "rect"):
        pts = [to_obj(ob.matrix_world @ v.co) for v in bm.verts]
        for axis, bl_axis, sign in ((0, Vector((1, 0, 0)), 1), (1, Vector((0, 0, 1)), 1), (2, Vector((0, -1, 0)), 1)):
            lo, hi = min(p[axis] for p in pts), max(p[axis] for p in pts)
            for k in range(math.floor(lo) + 1, math.ceil(hi)):
                if lo + 1e-4 < k < hi - 1e-4:
                    co = Vector((0, 0, 0))
                    if axis == 0: co.x = k
                    elif axis == 1: co.z = k
                    else: co.y = -k
                    geom = bm.verts[:] + bm.edges[:] + bm.faces[:]
                    bmesh.ops.bisect_plane(bm, geom=geom, plane_co=co, plane_no=bl_axis)
                    bmesh.ops.split_edges(bm, edges=[e for e in bm.edges if e.select])
    bmesh.ops.triangulate(bm, faces=[f for f in bm.faces if len(f.verts) > 4])

    uv = bm.loops.layers.uv.verify()
    # extent of the whole part as seen along each axis, so split or triangulated faces share one mapping
    allp = [to_obj(ob.matrix_world @ v.co) for v in bm.verts]
    xs, ys, zs = [p.x for p in allp], [p.y for p in allp], [p.z for p in allp]
    part_bounds = {
        (0, True): (-max(zs), -min(zs), min(ys), max(ys)), (0, False): (min(zs), max(zs), min(ys), max(ys)),
        (1, True): (min(xs), max(xs), -max(zs), -min(zs)), (1, False): (min(xs), max(xs), min(zs), max(zs)),
        (2, True): (min(xs), max(xs), min(ys), max(ys)), (2, False): (-max(xs), -min(xs), min(ys), max(ys)),
    }
    def plane_uv(f):
        """Face normal, axis, and its corners in face-plane coords (u right, v up, seen from outside)."""
        n = to_obj(ob.matrix_world.to_3x3() @ f.normal)
        ax = max(range(3), key=lambda i: abs(n[i]))
        pts = [to_obj(ob.matrix_world @ l.vert.co) for l in f.loops]
        if ax == 0:
            uvs = [((-p.z if n.x > 0 else p.z), p.y) for p in pts]
        elif ax == 1:
            uvs = [(p.x, (-p.z if n.y > 0 else p.z)) for p in pts]
        else:
            uvs = [((p.x if n.z > 0 else -p.x), p.y) for p in pts]
        return n, ax, pts, uvs

    # panels and rails: every flat wall of a part is one piece, so find each wall's extent
    groups = {}
    def group_key(f):
        n, ax, pts, uvs = plane_uv(f)
        c = to_obj(ob.matrix_world @ f.calc_center_median())
        return (ax, n[ax] > 0, round(c[ax], 3))
    if isinstance(mode, tuple) and mode[0] in ("panel9", "rail"):
        for f in bm.faces:
            k = group_key(f)
            _, _, _, uvs = plane_uv(f)
            g = groups.setdefault(k, [1e9, -1e9, 1e9, -1e9])
            for u, v in uvs:
                g[0], g[1], g[2], g[3] = min(g[0], u), max(g[1], u), min(g[2], v), max(g[3], v)
    for f in bm.faces:
        n = to_obj(ob.matrix_world.to_3x3() @ f.normal)
        c = to_obj(ob.matrix_world @ f.calc_center_median())
        inside = c - n * 0.01          # step back from the surface so outer faces count for their own block
        cell = (min(max(math.floor(inside.x), -1), 1), min(max(math.floor(inside.y), -1), 1), min(max(math.floor(inside.z), 0), 1))
        cells.add(cell)
        ax = max(range(3), key=lambda i: abs(n[i]))
        pts = [to_obj(ob.matrix_world @ l.vert.co) for l in f.loops]
        # 2D coords in the face's plane, u to the right and v up as you look at the face
        if ax == 0:
            uvs = [((-p.z if n.x > 0 else p.z), p.y) for p in pts]
            ref = ((-c.z if n.x > 0 else c.z), c.y)
        elif ax == 1:
            uvs = [(p.x, (-p.z if n.y > 0 else p.z)) for p in pts]
            ref = (c.x, (-c.z if n.y > 0 else c.z))
        else:
            uvs = [((p.x if n.z > 0 else -p.x), p.y) for p in pts]
            ref = ((c.x if n.z > 0 else -c.x), c.y)

        if mode == "belt":                   # conveyor texture runs along the belt (x)
            uvs = [(p.z, -p.x) for p in pts]
            ref = (c.z, -c.x)
        if isinstance(mode, tuple) and mode[0] == "rail":
            # strip along the belt: end pieces at the rail's ends, thin faces show the strip's middle rows at true scale
            _, ox, oy = mode
            umin, umax, vmin, vmax = groups[group_key(f)]
            ncols = max(1, math.ceil(umax - math.floor(umin + 1e-6) - 1e-6))
            col = min(max(math.floor(ref[0]) - math.floor(umin + 1e-6), 0), ncols - 1)
            left, right = col == 0, col == ncols - 1
            sx = 3 if (left and right) else 0 if left else 2 if right else 1
            vmid = (vmin + vmax) / 2
            out = []
            for u, v in uvs:
                fu = (u - umin) if left else (1 - (umax - u)) if right else frac(u, ref[0])
                if left and right:
                    fu = (u - umin) / ((umax - umin) or 1)
                fu = min(max(fu, 0), 1)
                px = ox + (sx + fu) * 16
                py = oy + 8 - (v - vmid) * 16
                out.append((px / 128, 1 - py / 128))
        elif isinstance(mode, tuple) and mode[0] == "panel9":
            _, ox, oy = mode
            umin, umax, vmin, vmax = groups[group_key(f)]
            ncols = max(1, math.ceil(umax - math.floor(umin + 1e-6) - 1e-6))
            nrows = max(1, math.ceil(vmax - math.floor(vmin + 1e-6) - 1e-6))
            col = min(max(math.floor(ref[0]) - math.floor(umin + 1e-6), 0), ncols - 1)
            row = min(max(math.floor(ref[1]) - math.floor(vmin + 1e-6), 0), nrows - 1)
            left, right, bottom, top = col == 0, col == ncols - 1, row == 0, row == nrows - 1
            sx = 3 if (left and right) else 0 if left else 2 if right else 1
            sy = 3 if (top and bottom) else 0 if top else 2 if bottom else 1
            out = []
            for u, v in uvs:
                fu = (u - umin) if left else (1 - (umax - u)) if right else frac(u, ref[0])
                fv = (v - vmin) if bottom else (1 - (vmax - v)) if top else frac(v, ref[1])
                if left and right:                 # narrower than a block: squeeze the whole cell in
                    fu = (u - umin) / ((umax - umin) or 1)
                if top and bottom:
                    fv = (v - vmin) / ((vmax - vmin) or 1)
                fu, fv = min(max(fu, 0), 1), min(max(fv, 0), 1)
                px = ox + (sx + fu) * 16
                py = oy + sy * 16 + (1 - fv) * 16
                out.append((px / 128, 1 - py / 128))
        elif mode in ("tile", "belt"):
            out = [(frac(u, ref[0]), frac(v, ref[1])) for u, v in uvs]
        elif mode == "tile_r":
            out = [(frac(v, ref[1]), 1 - frac(u, ref[0])) for u, v in uvs]
        else:                               # whole texture (or sheet tile) stretched over the part, seen from this side
            u0, u1, v0, v1 = part_bounds[(ax, n[ax] > 0)]
            out = [((u - u0) / ((u1 - u0) or 1), (v - v0) / ((v1 - v0) or 1)) for u, v in uvs]
            if kind == "sheet":
                col, row = key
                out = [((col + u) / 8, 1 - (row + 1 - v) / 8) for u, v in out]
            elif isinstance(mode, tuple) and mode[0] == "rect":
                _, x0, y0, w, h, size = mode
                out = [((x0 + u * w) / size, 1 - (y0 + (1 - v) * h) / size) for u, v in out]
            elif mode == "whole_r":
                out = [(v, 1 - u) for u, v in out]
        if kind == "sheet" and mode in ("tile", "tile_r"):   # one sheet tile, repeated per block
            col, row = key
            out = [((col + u) / 8, 1 - (row + 1 - v) / 8) for u, v in out]
        for l, (u, v) in zip(f.loops, out):
            l[uv].uv = (u, v)
    bm.to_mesh(ob.data)
    bm.free()

# ---------- export ----------
if EXPORT:
    model_dir = os.path.join(res_dir, "assets/immersive_munitions/models/block")

    def export_obj(name, keep):
        """Writes <name>.obj/.mtl from the mesh objects whose base name passes keep."""
        obj_path = os.path.join(model_dir, name + ".obj")
        bpy.ops.object.select_all(action='DESELECT')
        for ob in bpy.data.objects:
            ob.select_set(ob.type == 'MESH' and keep(ob.name.split(".")[0]))
        bpy.ops.wm.obj_export(filepath=obj_path, export_selected_objects=True, forward_axis='NEGATIVE_Z', up_axis='Y',
                              export_uv=True, export_normals=True, export_materials=True, apply_modifiers=True,
                              export_triangulated_mesh=False, path_mode='STRIP')
        with open(obj_path[:-4] + ".mtl", "w") as f:
            for mname, (m, (kind, key, mode)) in sorted(materials.items()):
                rl = SHEET_RL if kind == "sheet" else "immersiveengineering:block/" + key
                f.write(f"newmtl {mname}\nmap_Kd {rl}\n\n")

    # the static machine, without the parts AmmoAssemblerRenderer moves
    moving = {part for parts in DYNAMIC.values() for part in parts}
    export_obj("ammo_assembler", lambda n: n not in moving)
    for group, parts in DYNAMIC.items():
        name = "ammo_assembler_" + group
        export_obj(name, lambda n, parts=parts: n in parts)
        with open(os.path.join(model_dir, name + ".json"), "w") as f:
            json.dump({"loader": "forge:obj", "model": f"immersive_munitions:models/block/{name}.obj",
                       "flip_v": True, "automatic_culling": False,
                       "textures": {"particle": "immersiveengineering:block/metal/sheetmetal_steel"}}, f, indent=2)
            f.write("\n")

    faces = sum(len(o.data.polygons) for o in bpy.data.objects if o.type == 'MESH')
    print("FACES", faces, "CELLS", sorted(cells))
    split = {
        "parent": "minecraft:block/block",
        "loader": "immersiveengineering:basic_split",
        "dynamic": False,
        "split_parts": [list(c) for c in sorted(cells)],
        "inner_model": {
            "parent": "minecraft:block/block",
            "loader": "forge:obj",
            "model": "immersive_munitions:models/block/ammo_assembler.obj",
            "flip_v": True,
            "automatic_culling": False,
            "render_type": "minecraft:cutout",
            "textures": {"particle": "immersiveengineering:block/metal/sheetmetal_steel"},
        },
        "textures": {"particle": "immersiveengineering:block/metal/sheetmetal_steel"},
    }
    with open(os.path.join(model_dir, "ammo_assembler_split.json"), "w") as f:
        json.dump(split, f, indent=2)
        f.write("\n")
    bpy.ops.wm.save_as_mainfile(filepath=os.path.join(out_dir, "assembler_textured.blend"))
# ---------- preview ----------
scene = bpy.context.scene
scene.render.engine = 'BLENDER_WORKBENCH'
scene.display.shading.light = 'STUDIO'
scene.display.shading.color_type = 'TEXTURE'
scene.render.resolution_x, scene.render.resolution_y = 900, 720
scene.world = bpy.data.worlds.new("w")
scene.world.color = (0.55, 0.57, 0.6)
cam = bpy.data.objects.new("cam", bpy.data.cameras.new("cam"))
cam.data.type = 'ORTHO'
scene.collection.objects.link(cam)
scene.camera = cam
for name, center, scale, d in (("tex_front_left", (0.5, 0.5, 1.0), 5.0, (-0.55, -1, 0.5)),
                               ("tex_front_right", (0.5, 0.5, 1.0), 5.0, (0.6, -1, 0.5)),
                               ("tex_stations", (0.6, 0.85, 1.5), 2.7, (0.3, -1, 0.3)),
                               ("tex_ceiling", (0.5, 1.0, 1.6), 2.7, (0.2, -1, -0.45)),
                               ("tex_back", (0.5, 0.5, 1.0), 5.0, (0.55, 1, 0.45)),
                               ("tex_end_input", (0.5, 0.5, 1.0), 3.4, (-1, 0, 0.12)),
                               ("tex_end_output", (0.5, 0.5, 1.0), 3.4, (1, 0, 0.12)),
                               ("tex_roof_ports", (0.0, 2.0, 1.5), 2.2, (0.35, -1, 1.1)),
                               ("tex_end_output_zoom", (2.0, 0.65, 0.66), 1.3, (1, 0, 0.05)),
                               ("tex_power_input", (1.5, 2.0, 0.5), 2.4, (0.45, 1, 0.9))):
    ctr = Vector((center[0], -center[2], center[1]))
    cam.data.ortho_scale = scale
    cam.location = ctr + Vector(d).normalized() * 20
    cam.rotation_euler = (ctr - cam.location).to_track_quat("-Z", "Y").to_euler()
    scene.render.filepath = os.path.join(out_dir, f"{name}.png")
    bpy.ops.render.render(write_still=True)
print("done")
