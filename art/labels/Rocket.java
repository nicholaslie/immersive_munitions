import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/*
 * Draws the RPG rocket sprite (no label) to match the other ammo art: upright, lit from the upper left,
 * same color ramps as sprites/source/engine.py.
 *
 *   java art/labels/Rocket.java        (from the repo root, writes art/sprites/rounds/round_rpg_rocket.png)
 */
public class Rocket {
    static final int[] OLIVE = ramp("#20261a", "#363f27", "#4d5a38", "#677649", "#84965f", "#a3b57c", "#c3d49e", "#e3efc6");
    static final int[] GREY  = ramp("#26272c", "#3d3f46", "#585b63", "#767a84", "#989ca7", "#b9bdc7", "#d8dbe3", "#f4f6fb");
    static final int[] STEEL = ramp("#0c0c0e", "#1a1a1f", "#26262d", "#34343d", "#45454f", "#5a5a66", "#76768a", "#a0a0b4");
    static final int[] BRASS = ramp("#3b2a1d", "#5c432f", "#8c5f3a", "#af794f", "#d7aa5e", "#eec168", "#fbdc7d", "#fff9bf");

    static final double CENTER_X = 32.0;
    static final double[] LIGHT = normalize(-0.50, 0.62, 0.62);   // x right, y up, z toward the viewer

    // Outer radius at a given image row (y grows downward). 0 means empty.
    static double radius(double y) {
        if (y < 3) return 0;
        if (y < 5) return 1.3;                                  // fuze tip
        if (y < 19) return lerp(1.7, 7.6, (y - 5) / 14.0);      // nose cone
        if (y < 27) return 7.6;                                 // warhead
        if (y < 34) return lerp(7.6, 3.2, (y - 27) / 7.0);      // rear taper
        if (y < 36) return 3.7;                                 // coupling ring
        if (y < 55) return 3.0;                                 // motor
        if (y < 60) return lerp(2.6, 2.2, (y - 55) / 5.0);      // nozzle
        return 0;
    }

    static int[] material(double y) {
        if (y < 5) return GREY;
        if (y < 34) return OLIVE;
        if (y < 36) return BRASS;
        if (y < 55) return GREY;
        return STEEL;
    }

    public static void main(String[] args) throws Exception {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int[][][] rampAt = new int[64][64][];   // which ramp each pixel came from, for the outline pass

        drawFins(image, rampAt);
        drawBody(image, rampAt);
        for (int y = 47; y <= 57; y++) image.setRGB(32, y, opaque(GREY[2]));   // front fin, edge-on

        File file = new File("art/sprites/rounds/round_rpg_rocket.png");
        ImageIO.write(outlined(image, rampAt), "png", file);
        System.out.println("wrote " + file);
    }

    // Two side fins with a swept leading edge. The left one faces the light.
    static void drawFins(BufferedImage image, int[][][] rampAt) {
        for (int y = 46; y <= 57; y++) {
            double span = 2.0 + 5.5 * Math.min(1, (y - 46) / 12.0 * 1.6);
            for (int side = -1; side <= 1; side += 2) {
                boolean lit = side < 0;
                for (int x = 0; x < 64; x++) {
                    double dx = (x + 0.5 - CENTER_X) * side;
                    if (dx <= 2.6 || dx >= 3.0 + span) continue;
                    boolean edge = y == 57 || dx > 2.0 + span;
                    int color = edge ? (lit ? GREY[4] : GREY[2]) : (lit ? GREY[5] : GREY[3]);
                    image.setRGB(x, y, opaque(color));
                    rampAt[y][x] = GREY;
                }
            }
        }
    }

    // Shades each row as a slice of a cylinder, tilted by how fast the radius changes.
    static void drawBody(BufferedImage image, int[][][] rampAt) {
        for (int y = 0; y < 64; y++) {
            double yc = y + 0.5, r = radius(yc);
            if (r <= 0) continue;

            double slope = radius(yc + 0.5) - radius(yc - 0.5);   // widening downward means facing up
            double scale = 1 / Math.sqrt(1 + slope * slope);
            int[] ramp = material(yc);
            double seam = (y == 19 || y == 26) ? -0.22 : 0;       // panel lines on the warhead

            for (int x = 0; x < 64; x++) {
                double dx = (x + 0.5 - CENTER_X) / r;
                if (Math.abs(dx) > 1) continue;
                double[] normal = normalize(dx * scale, slope * scale, Math.sqrt(1 - dx * dx) * scale);
                image.setRGB(x, y, opaque(shade(normal, ramp, seam)));
                rampAt[y][x] = ramp;
            }
        }
    }

    // Maps lighting onto tones 1..7 of the ramp. Tone 0 is kept for outlines.
    static int shade(double[] normal, int[] ramp, double bias) {
        double d = normal[0] * LIGHT[0] + normal[1] * LIGHT[1] + normal[2] * LIGHT[2];
        double v = 0.10 + 0.78 * Math.max(0, d) + bias;
        int tone = 1 + (int) Math.round(v * 5.4);
        return ramp[Math.max(1, Math.min(7, tone))];
    }

    // Every pixel touching transparency gets the darkest tone of its own ramp.
    static BufferedImage outlined(BufferedImage image, int[][][] rampAt) {
        BufferedImage out = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int pixel = image.getRGB(x, y);
                if (isClear(pixel)) continue;
                boolean edge = x == 0 || y == 0 || x == 63 || y == 63
                        || isClear(image.getRGB(x - 1, y)) || isClear(image.getRGB(x + 1, y))
                        || isClear(image.getRGB(x, y - 1)) || isClear(image.getRGB(x, y + 1));
                int[] ramp = rampAt[y][x] != null ? rampAt[y][x] : GREY;
                out.setRGB(x, y, edge ? opaque(ramp[0]) : pixel);
            }
        }
        return out;
    }

    static int[] ramp(String... hex) {
        int[] colors = new int[hex.length];
        for (int i = 0; i < hex.length; i++) colors[i] = Integer.parseInt(hex[i].substring(1), 16);
        return colors;
    }

    static double[] normalize(double x, double y, double z) {
        double length = Math.sqrt(x * x + y * y + z * z);
        return new double[]{x / length, y / length, z / length};
    }

    static double lerp(double a, double b, double t) {
        return a + (b - a) * Math.max(0, Math.min(1, t));
    }

    static int opaque(int rgb) { return 0xff000000 | rgb; }
    static boolean isClear(int argb) { return (argb >>> 24) == 0; }
}
