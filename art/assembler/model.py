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
CHAMFER = 0.03   # width of the bevel round the roof and base outline

PALETTE = {
    "body": (0.30, 0.31, 0.34), "panel": (0.40, 0.41, 0.45), "trim": (0.22, 0.23, 0.25),
    "mech": (0.74, 0.76, 0.79), "accent": (0.34, 0.19, 0.13), "hydro": (0.21, 0.35, 0.56), "dark": (0.07, 0.07, 0.08),
    "copper": (0.75, 0.44, 0.22), "brass": (0.80, 0.64, 0.30), "belt": (0.45, 0.27, 0.18),
    "red": (0.80, 0.12, 0.08), "glass": (0.62, 0.80, 0.86),
    "white": (0.92, 0.92, 0.90), "screen": (0.06, 0.13, 0.12), "grille": (0.50, 0.52, 0.55),
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


def slant(x0, x1, y0, y1, z0, z1, dx, dz, color, name="part"):
    """A box whose bottom face is shifted by (dx, dz): a leaning post."""
    def build(bm):
        v = {}
        for xi, x in enumerate((x0, x1)):
            for yi, y in enumerate((y0, y1)):
                for zi, z in enumerate((z0, z1)):
                    s = 0 if yi else 1
                    v[xi, yi, zi] = bm.verts.new(bl(x + dx * s, y, z + dz * s))
        for idx in ([(0, 1, 1), (1, 1, 1), (1, 1, 0), (0, 1, 0)], [(0, 0, 0), (1, 0, 0), (1, 0, 1), (0, 0, 1)],
                    [(0, 0, 1), (1, 0, 1), (1, 1, 1), (0, 1, 1)], [(1, 0, 0), (0, 0, 0), (0, 1, 0), (1, 1, 0)],
                    [(1, 0, 1), (1, 0, 0), (1, 1, 0), (1, 1, 1)], [(0, 0, 0), (0, 0, 1), (0, 1, 1), (0, 1, 0)]):
            bm.faces.new([v[i] for i in idx])
    return _object(name, color, build)


def ring(cx, cz, r_out, r_in, y0, y1, color, name="ring", sides=SIDES):
    """An upright tube with a real bore: outer and inner walls plus the top and bottom rims (radii to the corners,
    oriented like cyl's so the flats face the axes)."""
    def build(bm):
        angles = [math.pi / sides + k * 2 * math.pi / sides for k in range(sides)]
        ring_verts = {}
        for key, r, y in (("ob", r_out, y0), ("ot", r_out, y1), ("ib", r_in, y0), ("it", r_in, y1)):
            ring_verts[key] = [bm.verts.new(bl(cx + r * math.cos(a), y, cz + r * math.sin(a))) for a in angles]
        v = ring_verts
        for k in range(sides):
            n = (k + 1) % sides
            bm.faces.new([v["ob"][k], v["ob"][n], v["ot"][n], v["ot"][k]])     # outer wall
            bm.faces.new([v["ib"][n], v["ib"][k], v["it"][k], v["it"][n]])     # bore wall
            bm.faces.new([v["ot"][k], v["ot"][n], v["it"][n], v["it"][k]])     # top rim
            bm.faces.new([v["ob"][n], v["ob"][k], v["ib"][k], v["ib"][n]])     # bottom rim
        bmesh.ops.recalc_face_normals(bm, faces=bm.faces)
    return _object(name, color, build)


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
    """Double-sided flat quad in the y/z plane (for the gates): 2 faces."""
    def build(bm):
        for flip in (False, True):
            quad = [bm.verts.new(bl(x, y0, z0)), bm.verts.new(bl(x, y0, z1)),
                    bm.verts.new(bl(x, y1, z1)), bm.verts.new(bl(x, y1, z0))]
            bm.faces.new(quad[::-1] if flip else quad)
    return _object(name, color, build)


# ---------- body ----------
def body():
    group("body")
    box(-1, 2, 0, 0.125, 1.06, 1.94, "belt", "belt")
    box(-1, 2, 0, 0.19, 1.0, 1.06, "trim", "belt_rail_back")
    box(-1, 2, 0, 0.19, 1.94, 2.0, "trim", "belt_rail_front")
    box(-1, 2, 1.38, 1.94, 0, 2.0, "body", "roof")      # flush with the wood band under it and the cap over it
    box(-1, 2, 1.94, 1.997, 0, 2.0, "trim", "roof_cap")
    box(-1, 2, 1.22, 1.38, 0, 2.0, "accent", "upper_trim")        # wood band all the way round, under the screens
    box(-0.92, 1.92, 1.21, 1.22, 0.92, 1.94, "trim", "ceiling")    # metal tunnel ceiling the station mounts hang from
    for x in (0.0, 1.0):
        box(x - 0.03, x + 0.03, 1.05, 1.30, 0.93, 1.92, "trim", "station_rib")
    for cx in (-0.5, 0.5):   # roof item ports over the screens: IE's workbench input tile, flush with the roof
        box(cx - 0.25, cx + 0.25, 1.997, 2.0, 1.25, 1.75, "dark", "intake", occluder=False)
        box(cx - 0.125, cx + 0.125, 1.997, 2.0, 1.0625, 1.25, "dark", "intake_light", occluder=False)   # its blue light
    for x0 in (-1.0, 1.92):
        box(x0, x0 + 0.08, 0.19, 1.22, 1.94, 2.0, "body", "portal_post")
        box(x0, x0 + 0.08, 1.06, 1.22, 0.92, 1.94, "body", "gate_header")   # short housing header over the opening


def base():
    """The bottom block layer, built like IE's Automated Workbench base: dark metal with vents on the bottom half, a
    row of wood windows set 1px back between dark posts, a wood band on top, and IE's redstone panel at the front
    middle (the machine's redstone control block)."""
    group("base")
    R, TOP, MID = 1 / 16, -0.1875, -0.5      # window recess; band and metal heights
    box(-1, 2, -1, MID, 0, 2, "body", "base_metal")
    box(-1, 2, TOP, 0, 0, 2, "trim", "base_band")
    box(0.125, 0.875, -1, -0.125, 2 - R, 2.01, "red", "base_panel")
    # front and back: posts at the corners and between the three bays, windows in the bays (the front middle bay
    # holds the panel)
    posts_x = [(-1, -0.8125), (-0.0625, 0.125), (0.875, 1.0625), (1.8125, 2)]
    windows_x = [(-0.8125, -0.0625), (0.125, 0.875), (1.0625, 1.8125)]
    for z_out, z_in in ((2 - R, 2), (0, R)):
        for x0, x1 in posts_x:
            box(x0, x1, MID, TOP, z_out, z_in, "trim", "base_post")
        for i, (x0, x1) in enumerate(windows_x):
            if z_in == 2 and i == 1:
                continue
            zw = (z_out - R, z_out) if z_in == 2 else (z_in, z_in + R)
            box(x0, x1, MID, TOP, zw[0], zw[1], "accent", "base_window")
    # the two ends (two blocks deep): posts at the corners and between the blocks, two windows
    posts_z = [(0, 0.1875), (0.9375, 1.0625), (1.8125, 2)]
    windows_z = [(0.1875, 0.9375), (1.0625, 1.8125)]
    for x_out, x_in in ((-1, -1 + R), (2 - R, 2)):
        for z0, z1 in posts_z:
            box(x_out, x_in, MID, TOP, z0, z1, "trim", "base_post")
        for z0, z1 in windows_z:
            xw = (x_in, x_in + R) if x_out == -1 else (x_out - R, x_out)
            box(xw[0], xw[1], MID, TOP, z0, z1, "accent", "base_window")


def front_screens():
    """Screens and dial are shallow recesses; bezel art, icons and counts come from the texture and the renderer."""
    group("front_screens")
    for cx in (-0.5, 0.5):
        box(cx - 0.375, cx + 0.375, 1.4375, 1.875, 2.0, 2.015, "screen", "screen")    # 12x7px, at true scale


# ---------- stations ----------
def powder_head():
    group("powder_head")
    box(-0.72, -0.28, 1.14, 1.30, 1.28, 1.72, "trim", "powder_mount")
    box(-0.66, -0.34, 0.92, 1.14, 1.32, 1.68, "accent", "metering_valve")
    disc(-0.5, 1.03, 0.10, 1.68, 1.73, "mech", "metering_wheel")
    cyl(-0.5, 1.5, 0.065, 0.78, 0.92, "glass", name="sight_glass")
    # dark holes where the glass meets the valve above and the collar below (the lower one shows only when it's empty)
    cyl(-0.5, 1.5, 0.064, 0.9185, 0.9195, "screen", name="sight_hole_top")
    cyl(-0.5, 1.5, 0.064, 0.7805, 0.7815, "screen", name="sight_hole_bottom")
    # like IE's bottling machine: a dark collar holding a smooth square taper, ending in a small brass tip. The taper
    # and tip slide down when pouring; the taper's top stays hidden in the collar
    box(-0.625, -0.375, 0.70, 0.78, 1.375, 1.625, "accent", "nozzle_collar")
    cyl(-0.5, 1.5, 0.0625 * math.sqrt(2), 0.50, 0.76, "mech", r1=0.125 * math.sqrt(2), name="drop_tube", sides=4)
    box(-0.5625, -0.4375, 0.46, 0.50, 1.4375, 1.5625, "brass", "nozzle_tip")   # where the powder comes out
    box(-0.53125, -0.46875, 0.4595, 0.46, 1.46875, 1.53125, "screen", "nozzle_hole", occluder=False)   # dark 1px hole under the tip


def projectile_head():
    group("projectile_head")
    # glass feed tube, just wide enough inside for the widest projectile (the 12 gauge slug, about 0.15 across), from the ceiling down to the escapement; AmmoAssemblerRenderer draws the stack of
    # projectiles inside it
    cyl(0.5, 1.5, 0.092, 0.76, 1.22, "glass", name="feed_tube")
    cyl(0.5, 1.5, 0.091, 1.2085, 1.2095, "screen", name="tube_hole")     # dark circle on the ceiling: the tube comes out of it
    cyl(0.5, 1.5, 0.091, 0.7615, 0.7625, "screen", name="tube_hole_bottom")   # and where it enters the escapement
    # like a real bullet feed die: an octagonal lock ring where the tube enters, a round die body (both bored through
    # with an octagonal hole, a straight shot from the tube), and four short spring fingers, nearly straight, each with
    # a lip at the tip that catches under the bullet. The fingers' tops are hidden in the die body, so they can slide
    # down. Sized for the widest projectile (the 12 gauge slug, about 0.15 across): the bore clears it, and the fingers sit
    # far enough apart side to side that their lips clear it too. The rounds are flat sprites facing the front, so
    # front to back the fingers can sit closer
    ring(0.5, 1.5, 0.185, 0.09, 0.70, 0.76, "accent", name="escapement")     # lock ring
    ring(0.5, 1.5, 0.168, 0.09, 0.625, 0.70, "accent", name="escapement")    # die body, just big enough for the fingers
    half, inset = 0.0234375, 0.004    # 3/4-pixel fingers
    lean, lip = 0.006, 0.012          # the fingers' lean in, and how far their lips step in
    off_x, off_z = 0.0745 + half + lean + lip, 0.035   # open, the lips just touch the slug's sides
    for cx in (0.5 - off_x, 0.5 + off_x):
        for cz in (1.5 - off_z, 1.5 + off_z):
            sx, sz = (1 if cx < 0.5 else -1), (1 if cz < 1.5 else -1)
            # left and right pairs are separate parts: the renderer slides them in to grip each round by its sides
            side = "finger_l" if cx < 0.5 else "finger_r"
            box(cx - half + inset, cx + half - inset, 0.625, 0.70, cz - half + inset, cz + half - inset, "mech", side,
                occluder=False)
            slant(cx - half, cx + half, 0.49, 0.625, cz - half, cz + half, lean * sx, lean * sz, "mech", side)
            tx, tz = cx + (lean + lip) * sx, cz + (lean + lip) * sz
            box(tx - half, tx + half, 0.47, 0.49, tz - half, tz + half, "mech", side, occluder=False)
    # the projectile in the jaws is the hopper's own item, drawn by AmmoAssemblerRenderer


def press_head():
    group("press_head")
    # plain and blocky like IE's machines: a square hydraulic cylinder, 1px guide rods, a crosshead bar, a square
    # ram and die
    box(1.3125, 1.6875, 0.9375, 1.30, 1.3125, 1.6875, "hydro", "hyd_cylinder")
    for x in (1.28125, 1.71875):
        box(x - 0.03125, x + 0.03125, 0.50, 1.30, 1.46875, 1.53125, "mech", "guide_rod")
    box(1.1875, 1.8125, 0.6875, 0.8125, 1.375, 1.625, "body", "crosshead")
    box(1.4375, 1.5625, 0.8125, 1.25, 1.4375, 1.5625, "mech", "ram")      # top hidden in the cylinder, so it can extend (up to 0.3)
    # a real seating die: an octagonal lock ring under the crosshead and a round die body, with the seating stem's
    # dark cupped mouth underneath (it matches the feed die)
    cyl(1.5, 1.5, 0.165, 0.64, 0.6875, "accent", name="die_lock_ring")
    cyl(1.5, 1.5, 0.125, 0.48, 0.64, "mech", name="seating_die")
    cyl(1.5, 1.5, 0.06, 0.4785, 0.4795, "screen", name="die_mouth")


def back_wall():
    group("back_wall")
    box(-1, 2, 0, 1.22, 0.01, 0.92, "body", "back_wall")       # stops under the wood band, so their ends never share a plane
    # power input on the roof over the press bay, back row: IE's Automated Workbench port tile, half a block wide and
    # flush with the roof like the workbench's, for a wire connector
    box(1.25, 1.75, 1.997, 2.0, 0.25, 0.75, "accent", "power_port", occluder=False)


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


CHAMFERED = ("roof_cap",)


def chamfer_outline():
    """Chamfer only the machine's outer outline: the roof cap's top edges and the plinth's bottom edges. The cap and
    plinth are flush with the walls they sit on, so every other edge stays square, or the chamfer shows as a gap."""
    for name in CHAMFERED:
        for ob in bpy.data.objects:
            if ob.name.split(".")[0] == name:
                bm = bmesh.new()
                bm.from_mesh(ob.data)
                weight = bm.edges.layers.float.get("bevel_weight_edge") or bm.edges.layers.float.new("bevel_weight_edge")
                zs = [v.co.z for v in bm.verts]          # Blender z is up
                outer = max(zs) if name == "roof_cap" else min(zs)   # the roof's top, the base's bottom
                for e in bm.edges:
                    e[weight] = 1.0 if all(abs(v.co.z - outer) < 1e-4 for v in e.verts) else 0.0
                bm.to_mesh(ob.data)
                bm.free()
                mod = ob.modifiers.new("chamfer", 'BEVEL')
                mod.width, mod.segments, mod.limit_method = CHAMFER, 1, 'WEIGHT'


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
body(); base(); front_screens(); powder_head(); projectile_head(); press_head(); back_wall()
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
