import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/*
 * Draws the Ammunition Assembler's own texture sheet (128x128, 16px tiles) for the parts IE has no texture for.
 * Everything else on the machine uses IE's block textures directly.
 *
 *   java art/assembler/AssemblerSheet.java [output png]       (from the repo root; needs IE's source in reference/)
 *
 * Tiles (column, row):
 *   0,0 screen                         2,0 glass
 *   0,1 brass       1,1 copper         2,1 rubber flap    3,1 glass with powder
 *   0,2 IE steel sheet metal, brightened to the light panel grey (tiles like the roof)
 *   1,2 turned steel   2,2 chrome   3,2 red handwheel   4,2 gunmetal housing
 *   2,3 knurled jaw
 *   0,3 power lamp off   1,3 power lamp on (lit while the machine has energy)
 *   4..7,0 conveyor rail strip (left end, middle, right end, both ends)
 *   0..3,4..7  panel set, light      4..7,4..7  panel set, dark
 *     A panel set is 4x4 cells: a 3x3 nine-slice (corners, edges, middle), plus a column for panels one block
 *     wide, a row for panels one block tall, and one cell for a single-block panel.
 */
public class AssemblerSheet {
    static final BufferedImage IMG = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

    public static void main(String[] args) throws Exception {
        screen(0, 0);
        glass(32, 0, false);
        ramp(0, 16, 0x5e4318, 0x8c6a2a, 0xc49a45, 0xe8c86e);   // brass
        ramp(16, 16, 0x5a2b16, 0x8a4524, 0xb8673a, 0xdb8f5c);  // copper
        flap(32, 16);
        glass(48, 16, true);
        lamp(0, 48, false);
        lamp(16, 48, true);
        // long sheet-metal panels, colours taken from IE's redstone port (light) and item port (dark)
        panelSet(0, 64, 0x7a7a7a, 0x484848, 0x8e8e8e, 0x5e5e5e, 0x9e9e9e);
        panelSet(64, 64, 0x4f4f4f, 0x2f2f2f, 0x5f5f5f, 0x3c3c3c, 0x6c6c6c);
        railSet(64, 0, 0x7a7a7a, 0x484848, 0x8e8e8e, 0x9e9e9e);
        lightSteel(0, 32, 0x7a);
        // station heads: turned and chrome parts are stretched across a whole part, so the shading wraps round it
        turned(16, 32, 0x3c3e42, 0x5c5f64, 0x7e8288, 0xa2a6ac);   // turned steel
        chrome(32, 32);
        handwheel(48, 32, 0x6e1610, 0xa82a1c, 0xd8503a);         // red paint
        housing(64, 32, 0x34363b, 0x26272b, 0x45484e, 0x6c6f75);  // dark gunmetal
        knurled(32, 48);

        File out = new File(args.length > 0 ? args[0]
                : "src/main/resources/assets/immersive_munitions/textures/block/multiblocks/ammo_assembler.png");
        out.getParentFile().mkdirs();
        ImageIO.write(IMG, "png", out);
        System.out.println("wrote " + out);
    }

    // IE's steel sheet metal (the roof texture), brightened so its average grey matches the light panels
    static void lightSteel(int ox, int oy, int target) throws Exception {
        BufferedImage src = ImageIO.read(new File(IE_STEEL));
        int sum = 0;
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) sum += src.getRGB(x, y) & 255;
        int d = target - sum / 256;
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            int g = clamp((src.getRGB(x, y) & 255) + d);
            px(ox + x, oy + y, g << 16 | g << 8 | g);
        }
    }

    static final String IE_STEEL = "reference/ImmersiveEngineering-1.20.1/ImmersiveEngineering-1.20.1/src/main/resources/"
            + "assets/immersiveengineering/textures/block/metal/sheetmetal_steel.png";

    static void px(int x, int y, int rgb) {
        IMG.setRGB(x, y, 0xff000000 | rgb);
    }

    static void fill(int x0, int y0, int w, int h, int rgb) {
        for (int y = y0; y < y0 + h; y++) for (int x = x0; x < x0 + w; x++) px(x, y, rgb);
    }

    // dark bezel, black-and-white screen, faint scanlines, a glare corner
    static void screen(int ox, int oy) {
        fill(ox, oy, 16, 16, 0x2b2d31);
        fill(ox + 1, oy + 1, 14, 14, 0x1b1d20);
        fill(ox + 2, oy + 2, 12, 12, 0x1e1e1e);
        for (int y = oy + 3; y < oy + 14; y += 2) fill(ox + 2, y, 12, 1, 0x252525);
        px(ox + 3, oy + 3, 0x4a4a4a);
        px(ox + 4, oy + 3, 0x3a3a3a);
        px(ox + 3, oy + 4, 0x3a3a3a);
    }

    // pale glass with a highlight streak; optional dark powder filling the lower half
    static void glass(int ox, int oy, boolean powder) {
        fill(ox, oy, 16, 16, 0x9fc3cc);
        fill(ox, oy, 1, 16, 0x6f8f98);
        fill(ox + 15, oy, 1, 16, 0x6f8f98);
        fill(ox + 3, oy, 1, 16, 0xd8eef2);
        fill(ox + 4, oy, 1, 16, 0xbad8de);
        if (powder) {
            fill(ox + 1, oy + 8, 14, 8, 0x26272b);
            for (int x = ox + 1; x < ox + 15; x += 3) px(x, oy + 8, 0x3c3d42);
            px(ox + 5, oy + 11, 0x3c3d42);
            px(ox + 10, oy + 13, 0x3c3d42);
        }
    }

    // lathe-turned metal: dark edges, a highlight band left of centre, faint horizontal tool marks
    static void turned(int ox, int oy, int edge, int mid, int light, int shine) {
        int[] cols = {edge, mid, mid, light, light, shine, light, light, mid, mid, mid, mid, mid, edge, edge, edge};
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++)
            px(ox + x, oy + y, shade(cols[x], y % 4 == 2 ? -6 : 0));
    }

    // polished rod: narrow bright streak, strong falloff
    static void chrome(int ox, int oy) {
        int[] cols = {0x5a5e64, 0x8a8f96, 0xb8bdc4, 0xe6eaef, 0xffffff, 0xd2d7dd, 0xa8adb4, 0x9499a0,
                      0x8a8f96, 0x80858c, 0x767b82, 0x6c7178, 0x62676e, 0x585c62, 0x4e5258, 0x44484e};
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) px(ox + x, oy + y, cols[x]);
    }

    // handwheel seen face-on: rim, four spokes, hub
    static void handwheel(int ox, int oy, int dark, int mid, int light) {
        fill(ox, oy, 16, 16, 0x1e1f22);
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            double d = Math.hypot(x - 7.5, y - 7.5);
            if (d > 5.6 && d < 8.2) px(ox + x, oy + y, (x + y < 13) ? light : (x + y > 17) ? dark : mid);
        }
        fill(ox + 7, oy + 2, 2, 12, mid);
        fill(ox + 2, oy + 7, 12, 2, mid);
        fill(ox + 6, oy + 6, 4, 4, dark);
        fill(ox + 6, oy + 6, 2, 2, light);
    }

    // small bolted housing: bevelled edge, a bolt in each corner
    static void housing(int ox, int oy, int face, int dark, int light, int bolt) {
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++)
            px(ox + x, oy + y, shade(face, (y % 6 == 3) ? -3 : 0));
        fill(ox, oy, 16, 1, light);
        fill(ox, oy, 1, 16, light);
        fill(ox, oy + 15, 16, 1, dark);
        fill(ox + 15, oy, 1, 16, dark);
        for (int[] b : new int[][]{{2, 2}, {12, 2}, {2, 12}, {12, 12}}) {
            fill(ox + b[0], oy + b[1], 2, 2, bolt);
            px(ox + b[0], oy + b[1], light);
        }
    }

    // gripping jaw: dark steel with horizontal grooves
    static void knurled(int ox, int oy) {
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            int c = (y % 3 == 0) ? 0x1c1d20 : (y % 3 == 1) ? 0x5a5d63 : 0x3a3c41;
            px(ox + x, oy + y, (x == 0 || x == 15) ? shade(c, -10) : c);
        }
    }

    // vertical rubber strip curtain
    static void flap(int ox, int oy) {
        fill(ox, oy, 16, 16, 0x2a2c30);
        for (int x = ox + 3; x < ox + 16; x += 4) fill(x, oy, 1, 16, 0x1c1d20);
        for (int x = ox + 1; x < ox + 16; x += 4) fill(x, oy, 1, 16, 0x34363b);
    }

    // one colour of panel pieces; edges get a seam (dark gap + highlight) with a bolt per block, middles are brushed
    static void panelSet(int ox, int oy, int face, int gap, int light, int shadow, int bolt) {
        for (int cy = 0; cy < 4; cy++) for (int cx = 0; cx < 4; cx++) {
            boolean l = cx == 0 || cx == 3, r = cx == 2 || cx == 3, t = cy == 0 || cy == 3, b = cy == 2 || cy == 3;
            panelCell(ox + cx * 16, oy + cy * 16, l, r, t, b, face, gap, light, shadow, bolt);
        }
    }

    static void panelCell(int ox, int oy, boolean l, boolean r, boolean t, boolean b,
                          int face, int gap, int light, int shadow, int bolt) {
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            int c = shade(face, (y % 5 == 1) ? 2 : (y % 7 == 4) ? -2 : 0);     // faint horizontal brushing
            if (t && y == 1 || l && x == 1) c = light;                          // highlight just inside the seam
            if (b && y == 14 || r && x == 14) c = shadow;
            if (t && y == 0 || b && y == 15 || l && x == 0 || r && x == 15) c = gap;   // the seam itself
            px(ox + x, oy + y, c);
        }
        // a rivet in each corner of the panel
        if (t && l) rivet(ox + 3, oy + 3, bolt, light, gap);
        if (t && r) rivet(ox + 11, oy + 3, bolt, light, gap);
        if (b && l) rivet(ox + 3, oy + 11, bolt, light, gap);
        if (b && r) rivet(ox + 11, oy + 11, bolt, light, gap);
    }

    static void rivet(int x, int y, int bolt, int light, int gap) {
        px(x, y, bolt);
        px(x + 1, y, light);
        px(x, y + 1, light);
        px(x + 1, y + 1, gap);
    }

    // conveyor rail strip, 4 cells: left end, middle, right end, both ends. Thin faces show the middle rows,
    // so the end seam runs full height and the single rivet sits on rows 7-8.
    static void railSet(int ox, int oy, int face, int gap, int light, int bolt) {
        for (int cx = 0; cx < 4; cx++) {
            boolean l = cx == 0 || cx == 3, r = cx == 2 || cx == 3;
            for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                int c = shade(face, (y % 5 == 1) ? 2 : (y % 7 == 4) ? -2 : 0);
                if (l && x == 0 || r && x == 15) c = gap;
                px(ox + cx * 16 + x, oy + y, c);
            }
            if (l) rivet(ox + cx * 16 + 3, oy + 7, bolt, light, gap);
            if (r) rivet(ox + cx * 16 + 11, oy + 7, bolt, light, gap);
        }
    }

    // round red lens in a dark bezel; the lit version glows with a highlight
    static void lamp(int ox, int oy, boolean on) {
        fill(ox, oy, 16, 16, 0x2b2d31);
        int[][] rows = {{5, 10}, {4, 11}, {3, 12}, {3, 12}, {3, 12}, {3, 12}, {4, 11}, {5, 10}};
        for (int i = 0; i < rows.length; i++) for (int x = rows[i][0]; x <= rows[i][1]; x++) {
            int y = 4 + i;
            int c = on ? 0xe02a1c : 0x5a1410;
            if (on && (x - 7) * (x - 7) + (y - 7) * (y - 7) < 5) c = 0xff6a4d;
            if (!on && x == rows[i][0]) c = 0x3e0d0a;
            px(ox + x, oy + y, c);
        }
        px(ox + 6, oy + 6, on ? 0xffd0c0 : 0x7a2a22);
    }

    static int shade(int rgb, int d) {
        return (clamp(((rgb >> 16) & 255) + d) << 16) | (clamp(((rgb >> 8) & 255) + d) << 8) | clamp((rgb & 255) + d);
    }

    static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    // metal ramp: dark edge, body, highlight band
    static void ramp(int ox, int oy, int dark, int mid, int light, int shine) {
        fill(ox, oy, 16, 16, mid);
        fill(ox, oy, 16, 1, dark);
        fill(ox, oy + 15, 16, 1, dark);
        fill(ox, oy + 4, 16, 3, light);
        fill(ox, oy + 5, 16, 1, shine);
        for (int x = ox + 2; x < ox + 16; x += 5) px(x, oy + 11, dark);
    }
}
