import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Draws the 64x64 RPG rocket sprite (no label) in the same style as the other ammo sprites:
 * upright, lit from the upper left, posterized onto the shared 8-tone ramps from sprites/source/engine.py.
 *
 * Run from the repo root:  java art/labels/Rocket.java
 * Writes art/sprites/rounds/round_rpg_rocket.png
 */
public class Rocket {
    static int[] ramp(String... hex) { int[] r = new int[hex.length]; for (int i = 0; i < hex.length; i++) r[i] = Integer.parseInt(hex[i].substring(1), 16); return r; }
    static final int[] OLIVE = ramp("#20261a", "#363f27", "#4d5a38", "#677649", "#84965f", "#a3b57c", "#c3d49e", "#e3efc6");
    static final int[] GREY  = ramp("#26272c", "#3d3f46", "#585b63", "#767a84", "#989ca7", "#b9bdc7", "#d8dbe3", "#f4f6fb");
    static final int[] STEEL = ramp("#0c0c0e", "#1a1a1f", "#26262d", "#34343d", "#45454f", "#5a5a66", "#76768a", "#a0a0b4");
    static final int[] BRASS = ramp("#3b2a1d", "#5c432f", "#8c5f3a", "#af794f", "#d7aa5e", "#eec168", "#fbdc7d", "#fff9bf");

    static final double CX = 32.0;
    // light from the upper left, toward the viewer (x right, y up, z toward viewer)
    static final double[] L = norm(-0.50, 0.62, 0.62);
    static double[] norm(double x, double y, double z) { double n = Math.sqrt(x * x + y * y + z * z); return new double[]{x / n, y / n, z / n}; }

    /** Outer radius at image row y (y grows downward), 0 = nothing. */
    static double radius(double y) {
        if (y < 3) return 0;
        if (y < 5) return 1.3;                                      // fuze tip
        if (y < 19) return lerp(1.7, 7.6, (y - 5) / 14.0);          // nose cone
        if (y < 27) return 7.6;                                     // warhead body
        if (y < 34) return lerp(7.6, 3.2, (y - 27) / 7.0);          // rear taper
        if (y < 36) return 3.7;                                     // coupling ring
        if (y < 55) return 3.0;                                     // rocket motor
        if (y < 60) return lerp(2.6, 2.2, (y - 55) / 5.0);          // nozzle
        return 0;
    }
    static int[] material(double y) {
        if (y < 5) return GREY;
        if (y < 34) return OLIVE;
        if (y < 36) return BRASS;
        if (y < 55) return GREY;
        return STEEL;
    }
    static double lerp(double a, double b, double t) { return a + (b - a) * Math.max(0, Math.min(1, t)); }

    static int tone(double[] n, int[] ramp, double bias) {
        double d = n[0] * L[0] + n[1] * L[1] + n[2] * L[2];
        double v = 0.10 + 0.78 * Math.max(0, d) + bias;
        int idx = 1 + (int) Math.round(v * 5.4);                    // 1..7, 0 is reserved for outlines
        return ramp[Math.max(1, Math.min(7, idx))];
    }

    public static void main(String[] a) throws Exception {
        BufferedImage im = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int[][] mat = new int[64][64];   // ramp id per pixel for outlines: 1 olive 2 grey 3 steel 4 brass

        // tail fins (behind the motor): four fins, two seen side-on, drawn first
        for (int y = 46; y <= 58; y++) {
            double t = (y - 46) / 12.0;
            double span = 2.0 + 5.5 * Math.min(1, t * 1.6);                // swept leading edge
            for (int side = -1; side <= 1; side += 2) {
                for (int x = 0; x < 64; x++) {
                    double dx = (x + 0.5 - CX) * side;
                    if (dx > 2.6 && dx < 3.0 + span && y <= 57) {
                        int[] r = side < 0 ? GREY : GREY;
                        int c = side < 0 ? r[5] : r[3];
                        if (y == 57 || dx > 2.0 + span) c = side < 0 ? r[4] : r[2];
                        im.setRGB(x, y, 0xff000000 | c);
                        mat[y][x] = 2;
                    }
                }
            }
        }
        // body of revolution
        for (int y = 0; y < 64; y++) {
            double yc = y + 0.5, r = radius(yc);
            if (r <= 0) continue;
            double dr = (radius(yc + 0.5) - radius(yc - 0.5));               // radius change per pixel downward
            double c = 1 / Math.sqrt(1 + dr * dr), up = dr * c;              // growing radius downward -> faces up
            int[] ramp = material(yc);
            for (int x = 0; x < 64; x++) {
                double dx = (x + 0.5 - CX) / r;
                if (Math.abs(dx) > 1) continue;
                double nz = Math.sqrt(1 - dx * dx);
                double[] n = norm(dx * c, up, nz * c);
                double bias = 0;
                if (y == 19 || y == 26) bias = -0.22;                        // panel seams on the warhead
                im.setRGB(x, y, 0xff000000 | tone(n, ramp, bias));
                mat[y][x] = ramp == OLIVE ? 1 : ramp == GREY ? 2 : ramp == STEEL ? 3 : 4;
            }
        }
        // front fin seen edge-on over the motor
        for (int y = 47; y <= 57; y++) { im.setRGB(32, y, 0xff000000 | GREY[2]); }
        // outlines: darkest tone of the neighbouring material on every edge pixel
        BufferedImage out = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
            int p = im.getRGB(x, y);
            if ((p >>> 24) == 0) continue;
            boolean edge = x == 0 || y == 0 || x == 63 || y == 63
                || (im.getRGB(x - 1, y) >>> 24) == 0 || (im.getRGB(x + 1, y) >>> 24) == 0
                || (im.getRGB(x, y - 1) >>> 24) == 0 || (im.getRGB(x, y + 1) >>> 24) == 0;
            int[] ramp = switch (mat[y][x]) { case 1 -> OLIVE; case 3 -> STEEL; case 4 -> BRASS; default -> GREY; };
            out.setRGB(x, y, edge ? 0xff000000 | ramp[0] : p);
        }
        File f = new File("art/sprites/rounds/round_rpg_rocket.png");
        ImageIO.write(out, "png", f);
        System.out.println("wrote " + f);
    }
}
