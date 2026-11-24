"""Builds the Ammunition Assembler model (untextured) and saves it as a .blend.

    blender -b --factory-startup --python art/assembler/model.py -- <out.blend> <preview dir>

Then run texture.py on the .blend. Polygon budget is in line with IE / IP / IT machines (~400 faces).
Rules: 8-sided cylinders, chamfers only on the roof and base outline, grilles / bezels / stacked projectiles
left to textures, and faces hidden against other boxes removed.
OBJ frame: x -1..2 along the belt, y -1..2 up (belt top 0.125), z 0 back .. 2 front.
"""
import bpy, bmesh, math, os, sys
from mathutils import Vector, Matrix

blend_out, out_dir = sys.argv[sys.argv.index("--") + 1:][:2]
SIDES = 8

PALETTE = {
    "body": (0.30, 0.31, 0.34), "panel": (0.40, 0.41, 0.45), "trim": (0.22, 0.23, 0.25),
    "mech": (0.74, 0.76, 0.79), "accent": (0.34, 0.19, 0.13), "hydro": (0.21, 0.35, 0.56), "dark": (0.07, 0.07, 0.08),
    "copper": (0.75, 0.44, 0.22), "brass": (0.80, 0.64, 0.30), "belt": (0.45, 0.27, 0.18),
    "red": (0.80, 0.12, 0.08), "glass": (0.62, 0.80, 0.86), "rubber": (0.16, 0.17, 0.19),
    "white": (0.92, 0.92, 0.90), "lamp": (0.55, 0.10, 0.08), "screen": (0.06, 0.13, 0.12), "grille": (0.50, 0.52, 0.55),
}
GROUP = [None]
BOXES = []   # (aabb, object) for hidden-face culling


def bl(x, y, z):
    return Vector((x, -z, y))


def mat(color):
    name = "flat_" + color
    if name not in bpy.data.materials:
        m = bpy.data.materials.new(name)
        m.diffuse_color = (*PALETTE[color], 1)
    return bpy.data.materials[name]


def group(name):
    col = bpy.data.collections.new(name)
    bpy.context.scene.collection.children.link(col)
    GROUP[0] = col


def _object(name, color, build):
    mesh = bpy.data.meshes.new(name)
    ob = bpy.data.objects.new(name, mesh)
    GROUP[0].objects.link(ob)
    ob.data.materials.append(mat(color))
    bm = bmesh.new()
    build(bm)
    bm.normal_update()
    bm.to_mesh(mesh)
    bm.free()
    return ob


def box(x0, x1, y0, y1, z0, z1, color, name="part", occluder=True):
    def build(bm):
        v = {}
        for xi, x in enumerate((x0, x1)):
            for yi, y in enumerate((y0, y1)):
                for zi, z in enumerate((z0, z1)):
                    v[xi, yi, zi] = bm.verts.new(bl(x, y, z))
        for idx in ([(0, 1, 1), (1, 1, 1), (1, 1, 0), (0, 1, 0)], [(0, 0, 0), (1, 0, 0), (1, 0, 1), (0, 0, 1)],
                    [(0, 0, 1), (1, 0, 1), (1, 1, 1), (0, 1, 1)], [(1, 0, 0), (0, 0, 0), (0, 1, 0), (1, 1, 0)],
                    [(1, 0, 1), (1, 0, 0), (1, 1, 0), (1, 1, 1)], [(0, 0, 0), (0, 0, 1), (0, 1, 1), (0, 1, 0)]):
            bm.faces.new([v[i] for i in idx])
    ob = _object(name, color, build)
    if occluder:
        BOXES.append(((x0, x1, y0, y1, z0, z1), ob))
    return ob


def cyl(cx, cz, r0, y0, y1, color, r1=None, name="cyl", sides=SIDES):
    r1 = r0 if r1 is None else r1
    def build(bm):
        bmesh.ops.create_cone(bm, cap_ends=True, cap_tris=False, segments=sides, radius1=r0, radius2=r1,
                              depth=y1 - y0, matrix=Matrix.Translation(bl(cx, (y0 + y1) / 2, cz)) @ Matrix.Rotation(math.pi / sides, 4, 'Z'))
    return _object(name, color, build)


def disc(cx, cy, r, z0, z1, color, name="disc", sides=SIDES):
    def build(bm):
        m = Matrix.Translation(bl(cx, cy, (z0 + z1) / 2)) @ Matrix.Rotation(math.radians(90), 4, 'X') @ Matrix.Rotation(math.pi / sides, 4, 'Z')
        bmesh.ops.create_cone(bm, cap_ends=True, cap_tris=False, segments=sides, radius1=r, radius2=r, depth=z1 - z0, matrix=m)
    return _object(name, color, build)


def strip(x, y0, y1, z0, z1, color, name="strip"):
    """Double-sided flat quad in the y/z plane (for flaps and gates): 2 faces."""
    def build(bm):
        for flip in (False, True):
            quad = [bm.verts.new(bl(x, y0, z0)), bm.verts.new(bl(x, y0, z1)),
                    bm.verts.new(bl(x, y1, z1)), bm.verts.new(bl(x, y1, z0))]
            bm.faces.new(quad[::-1] if flip else quad)
    return _object(name, color, build)


# ---------- body ----------
def body():
    group("body")
    box(-0.99, 1.99, -1, -0.16, 0.01, 1.99, "body", "plinth")   # top stops under the wood band
    box(-1, 2, -0.16, 0.0, 0.0, 2.0, "trim", "base_trim")         # treated-wood band around the top of the base
    box(-1, 2, 0, 0.125, 1.06, 1.94, "belt", "belt")
    box(-1, 2, 0, 0.19, 1.0, 1.06, "trim", "belt_rail_back")
    box(-1, 2, 0, 0.19, 1.94, 2.0, "trim", "belt_rail_front")
    box(0.25, 0.75, -0.75, -0.25, 1.99, 2.0, "red", "rs_lamp")
    box(-0.99, 1.99, 1.30, 1.94, 0.01, 1.975, "body", "roof")
    box(-1, 2, 1.94, 1.997, 0, 2.0, "trim", "roof_cap")
    box(-1, 2, 1.22, 1.38, 0, 2.0, "accent", "upper_trim")        # wood band all the way round, under the screens
    box(-0.92, 1.92, 1.21, 1.22, 0.92, 1.94, "trim", "ceiling")    # metal tunnel ceiling the station mounts hang from
    for x in (0.0, 1.0):
        box(x - 0.03, x + 0.03, 1.05, 1.30, 0.93, 1.92, "trim", "station_rib")
    for cx in (-0.5, 0.5):   # roof item ports: a 1px raised lip, 1px wider than the port, with the port on top
        box(cx - 0.3125, cx + 0.3125, 1.997, 2.0595, 0.1875, 0.8125, "mech", "port_lip")
        box(cx - 0.25, cx + 0.25, 2.0595, 2.0625, 0.25, 0.75, "dark", "intake", occluder=False)
    for x0 in (-1.0, 1.92):
        box(x0, x0 + 0.08, 0.19, 1.22, 0.92, 1.0, "body", "portal_post")
        box(x0, x0 + 0.08, 0.19, 1.22, 1.94, 2.0, "body", "portal_post")
        box(x0, x0 + 0.08, 0.56, 0.62, 1.0, 1.94, "trim", "gate_sill")
        strip(x0 + 0.04, 0.62, 1.30, 1.0, 1.94, "grille", "gate_grille")      # grille is a cutout texture


def flaps():
    group("flaps")
    for x in (-0.96, 1.96):
        for i in range(4):
            z0 = 1.07 + i * 0.218
            strip(x, 0.24, 0.56, z0, z0 + 0.208, "rubber", "flap")


def front_screens():
    """Screens and dial are shallow recesses; bezel art, icons and counts come from the texture and the renderer."""
    group("front_screens")
    for cx in (-0.5, 0.5):
        box(cx - 0.32, cx + 0.32, 1.40, 1.86, 1.975, 1.99, "screen", "screen")
    box(1.30, 1.70, 1.44, 1.84, 1.975, 1.985, "trim", "lamp_plate")      # power indicator: lit when powered
    disc(1.5, 1.64, 0.13, 1.985, 1.994, "lamp", "power_lamp")


# ---------- stations ----------
def powder_head():
    group("powder_head")
    box(-0.72, -0.28, 1.14, 1.30, 1.28, 1.72, "trim", "powder_mount")
    box(-0.66, -0.34, 0.92, 1.14, 1.32, 1.68, "accent", "metering_valve")
    disc(-0.5, 1.03, 0.10, 1.68, 1.73, "mech", "metering_wheel")
    cyl(-0.5, 1.5, 0.065, 0.78, 0.92, "glass", name="sight_glass")
    cyl(-0.5, 1.5, 0.11, 0.62, 0.78, "mech", r1=0.04, name="funnel_cone")
    cyl(-0.5, 1.5, 0.035, 0.50, 0.70, "mech", name="drop_tube")      # top hidden in the funnel, so it can slide down
    cyl(-0.5, 1.5, 0.065, 0.43, 0.50, "brass", r1=0.045, name="expander_tip")


def projectile_head():
    group("projectile_head")
    box(0.38, 0.62, 1.16, 1.30, 1.38, 1.62, "trim", "feeder_mount")
    cyl(0.5, 1.5, 0.065, 0.76, 1.16, "glass", name="feed_tube")      # stacked projectiles: texture on the tube
    box(0.36, 0.64, 0.62, 0.76, 1.36, 1.64, "accent", "escapement")
    box(0.64, 0.73, 0.66, 0.86, 1.47, 1.53, "mech", "pawl_lever")
    box(0.40, 0.44, 0.47, 0.70, 1.45, 1.55, "mech", "jaw_left")     # tops hidden in the escapement, so they can slide down
    box(0.56, 0.60, 0.47, 0.70, 1.45, 1.55, "mech", "jaw_right")
    # the projectile in the jaws is the hopper's own item, drawn by AmmoAssemblerRenderer


def press_head():
    group("press_head")
    cyl(1.5, 1.5, 0.21, 0.96, 1.30, "hydro", name="hyd_cylinder")
    box(1.45, 1.55, 1.08, 1.22, 1.26, 1.30, "brass", "hose_fittings")
    for x in (1.26, 1.74):
        box(x - 0.02, x + 0.02, 0.50, 1.30, 1.48, 1.52, "mech", "guide_rod")
    box(1.22, 1.78, 0.72, 0.82, 1.40, 1.60, "body", "crosshead")
    cyl(1.5, 1.5, 0.06, 0.82, 1.10, "mech", name="ram")      # top hidden in the cylinder, so it can extend
    cyl(1.5, 1.5, 0.08, 0.48, 0.72, "mech", name="seating_die")
    cyl(1.5, 1.5, 0.11, 0.60, 0.66, "brass", name="lock_ring")


def back_hydraulics():
    group("back_hydraulics")
    box(-1, 1, 0, 1.22, 0.01, 0.92, "body", "back_wall")       # stops under the wood band, so their ends never share a plane
    box(1, 2, 0, 1.22, 0.40, 0.92, "body", "recessed_wall")
    box(1.0, 2.0, 0, 0.06, 0, 0.40, "trim", "unit_floor")
    cyl(1.30, 0.20, 0.17, 0.06, 0.62, "hydro", name="pump_motor")
    box(1.55, 1.92, 0.06, 0.82, 0.03, 0.38, "body", "reservoir")
    box(1.70, 1.76, 0.18, 0.72, 0.02, 0.03, "glass", "level_gauge")
    for x in (1.30, 1.45):
        box(x - 0.03, x + 0.03, 0.62, 1.30, 0.17, 0.23, "mech", "pressure_line")
    box(1.26, 1.49, 0.90, 1.02, 0.12, 0.28, "brass", "valve_block")   # a bit wider than the lines through it
    box(1.25, 1.75, -0.68, -0.18, 0.0, 0.01, "accent", "power_plate")


# ---------- cleanup ----------
def cull_hidden_faces():
    """Delete faces of boxes that lie flat against (or inside) another box, e.g. bottoms resting on surfaces."""
    eps = 1e-3
    removed = 0
    for aabb, ob in BOXES:
        bm = bmesh.new()
        bm.from_mesh(ob.data)
        doomed = []
        if ob.name.split(".")[0] in CHAMFERED:
            bm.free()
            continue
        for f in bm.faces:
            centre = f.calc_center_median()
            # every corner (pulled slightly toward the centre and pushed just outside) must be inside one other box
            probes = [v.co + (centre - v.co) * 0.02 + f.normal * eps * 3 for v in f.verts]
            for other, oob in BOXES:
                if oob is ob:
                    continue
                if all(other[0] - eps <= p.x <= other[1] + eps and other[2] - eps <= p.z <= other[3] + eps
                       and other[4] - eps <= -p.y <= other[5] + eps for p in probes):
                    doomed.append(f)
                    break
        removed += len(doomed)
        bmesh.ops.delete(bm, geom=doomed, context='FACES')
        bm.to_mesh(ob.data)
        bm.free()
    return removed


CHAMFERED = ("roof_cap", "plinth")


def chamfer_outline():
    for name in CHAMFERED:
        for ob in bpy.data.objects:
            if ob.name.split(".")[0] == name:
                mod = ob.modifiers.new("chamfer", 'BEVEL')
                mod.width, mod.segments, mod.limit_method = 0.03, 1, 'ANGLE'


def count():
    dg = bpy.context.evaluated_depsgraph_get()
    per = {}
    for ob in bpy.data.objects:
        if ob.type == 'MESH':
            col = ob.users_collection[0].name
            per[col] = per.get(col, 0) + len(ob.evaluated_get(dg).data.polygons)
    return per


def shoot(scene, cam, center, scale, d, name):
    cam.data.ortho_scale = scale
    cam.location = center + d.normalized() * 20
    cam.rotation_euler = (center - cam.location).to_track_quat("-Z", "Y").to_euler()
    scene.render.filepath = os.path.join(out_dir, name)
    bpy.ops.render.render(write_still=True)


bpy.ops.wm.read_factory_settings(use_empty=True)
body(); flaps(); front_screens(); powder_head(); projectile_head(); press_head(); back_hydraulics()
culled = cull_hidden_faces()
chamfer_outline()
per = count()
print("CULLED", culled)
for k, v in per.items():
    print("COL", k, v)
print("TOTAL", sum(per.values()))
bpy.ops.wm.save_as_mainfile(filepath=blend_out)

scene = bpy.context.scene
scene.render.engine = 'BLENDER_WORKBENCH'
scene.display.shading.light = 'STUDIO'
scene.display.shading.color_type = 'MATERIAL'
scene.display.shading.show_cavity = True
scene.display.shading.show_object_outline = True
scene.render.resolution_x, scene.render.resolution_y = 800, 650
scene.world = bpy.data.worlds.new("w")
scene.world.color = (0.55, 0.57, 0.6)
cam = bpy.data.objects.new("cam", bpy.data.cameras.new("cam"))
cam.data.type = 'ORTHO'
scene.collection.objects.link(cam)
scene.camera = cam
shoot(scene, cam, bl(0.5, 0.5, 1.0), 5.0, Vector((-0.55, -1, 0.5)), "model_front_left.png")
shoot(scene, cam, bl(0.6, 0.8, 1.5), 2.6, Vector((0.35, -1, 0.35)), "model_stations_close.png")
shoot(scene, cam, bl(0.5, 0.5, 1.0), 5.0, Vector((0.55, 1, 0.45)), "model_back.png")
print("done")
