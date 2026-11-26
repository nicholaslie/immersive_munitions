import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.DoubleFunction;
import javax.imageio.ImageIO;

/*
 * Draws the Immersive Armorer gun pack's ammo that isn't a cartridge (no label), to match the other ammo art: upright,
 * lit from the upper left, the ramps of sprites/source/engine.py plus glass and liquid ones. Each is turned like a
 * lathe part, as in Rocket.java.
 *   - cut_steel_rod: a short steel bar with cut ends (railgun)
 *   - burst_capacitor, stream_capacitor, high_power_capacitor: glass cells with copper end caps and a copper coil
 *     around a glowing core; burst short and red, stream long and slim and blue, high power wide and yellow
 *   - chemical: a corked glass bottle of dark fuel (chemical thrower)
 *
 *   java art/labels/ArmorerAmmo.java        (from the repo root, writes art/sprites/rounds/round_<id>.png)
 */
public class ArmorerAmmo {
    static final int[] GREY   = ramp("#26272c", "#3d3f46", "#585b63", "#767a84", "#989ca7", "#b9bdc7", "#d8dbe3", "#f4f6fb");
    static final int[] COPPER = ramp("#34190f", "#5a2e1c", "#86432a", "#ad5a37", "#cf764b", "#e8976a", "#f8bb94", "#ffe0c8");
    static final int[] RED    = ramp("#2a0707", "#520d0d", "#7f1414", "#a81c1c", "#cf2a27", "#ea4a3c", "#fb7a62", "#ffb8a3");
    static final int[] BRASS  = ramp("#3b2a1d", "#5c432f", "#8c5f3a", "#af794f", "#d7aa5e", "#eec168", "#fbdc7d", "#fff9bf");
    static final int[] GLASS  = ramp("#1d2a33", "#2f4452", "#466273", "#5f8295", "#7ca3b6", "#9cc3d4", "#c2e2ef", "#ecfbff");
    static final int[] BLUE   = ramp("#0b1a33", "#13305c", "#1c4a8a", "#2766b5", "#3a86d6", "#5ea8ee", "#8ecbf8", "#c9e9ff");
    static final int[] FUEL   = ramp("#1a0f06", "#2e1a0a", "#4a2a10", "#6b3d14", "#8f5419", "#b36e22", "#d68f36", "#f2b65c");
    static final int[] CORK   = ramp("#2b1a0e", "#45291a", "#644029", "#82573a", "#a0714c", "#bb8c62", "#d4a77e", "#ead0ad");

    static final double CX = 32.0;
    static final double[] LIGHT = normalize(-0.50, 0.62, 0.62);

    /** A turned part: radius and ramp by image row, plus an optional inner layer drawn behind glass. */
    record Part(DoubleFunction<Double> radius, DoubleFunction<int[]> ramp, DoubleFunction<Double> innerRadius,
                DoubleFunction<int[]> innerRamp) {}

    public static void main(String[] args) throws Exception {
        write("cut_steel_rod", rod());
        write("burst_capacitor", cell(18, 46, 6.5, RED, 3));
        write("stream_capacitor", cell(12, 52, 5.0, BLUE, 4));
        write("high_power_capacitor", cell(14, 50, 8.5, BRASS, 3));
        write("chemical", bottle());
    }

    static Part rod() {
        return new Part(y -> y >= 14 && y < 50 ? (y < 15 || y >= 49 ? 3.4 : 4.0) : 0, y -> GREY, null, null);
    }

    /** A glass cell between copper end caps, with a copper coil (every {@code pitch} rows) around a glowing core. */
    static Part cell(int top, int bottom, double r, int[] core, int pitch) {
        int cap = 4;
        return new Part(
                y -> y < top || y >= bottom ? 0 : (y < top + 1 || y >= bottom - 1) ? r - 1.2 : (y < top + cap || y >= bottom - cap) ? r : r - 0.6,
                y -> (y < top + cap || y >= bottom - cap) ? COPPER : GLASS,
                y -> (y >= top + cap && y < bottom - cap) ? r * 0.62 : 0,
                y -> ((int) y - top) % pitch == 0 ? COPPER : core);
    }

    static Part bottle() {
        return new Part(y -> {
                    if (y < 12) return 0.0;
                    if (y < 19) return 3.2;                                           // cork
                    if (y < 26) return 3.6;                                           // neck
                    if (y < 32) return lerp(3.6, 10.0, (y - 26) / 6.0);               // shoulder
                    if (y < 50) return 10.0;                                          // body
                    if (y < 55) return lerp(10.0, 7.0, (y - 50) / 5.0);               // rounded base
                    return 0.0;
                },
                y -> y < 19 ? CORK : GLASS,
                y -> y >= 33 && y < 54 ? (y < 50 ? 8.8 : lerp(8.8, 6.0, (y - 50) / 4.0)) : 0,   // fuel, below the shoulder
                y -> FUEL);
    }

    static void write(String id, Part part) throws Exception {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        int[][][] rampAt = new int[64][64][];
        for (int y = 0; y < 64; y++) {
            double yc = y + 0.5, r = part.radius().apply(yc);
            if (r <= 0) continue;
            double slope = part.radius().apply(yc + 0.5) - part.radius().apply(yc - 0.5);
            double scale = 1 / Math.sqrt(1 + slope * slope);
            int[] ramp = part.ramp().apply(yc);
            double inner = part.innerRadius() == null ? 0 : part.innerRadius().apply(yc);
            for (int x = 0; x < 64; x++) {
                double dx = (x + 0.5 - CX) / r;
                if (Math.abs(dx) > 1) continue;
                double[] normal = normalize(dx * scale, slope * scale, Math.sqrt(1 - dx * dx) * scale);
                int color = shade(normal, ramp, 0);
                // behind glass: the inner layer shows through, slightly darkened, with the glass's highlight on top
                if (inner > 0 && Math.abs(x + 0.5 - CX) < inner) {
                    double di = (x + 0.5 - CX) / inner;
                    double[] n2 = normalize(di, 0, Math.sqrt(Math.max(0, 1 - di * di)));
                    color = shade(n2, part.innerRamp().apply(yc), -0.08);
                    if (dx < -0.55 && dx > -0.75) color = ramp[6];   // glass glint
                }
                image.setRGB(x, y, opaque(color));
                rampAt[y][x] = inner > 0 && Math.abs(x + 0.5 - CX) < inner ? part.innerRamp().apply(yc) : ramp;
            }
        }
        File file = new File("art/sprites/rounds/round_" + id + ".png");
        ImageIO.write(outlined(image, rampAt), "png", file);
        System.out.println("wrote " + file);
    }

    static int shade(double[] normal, int[] ramp, double bias) {
        double d = normal[0] * LIGHT[0] + normal[1] * LIGHT[1] + normal[2] * LIGHT[2];
        double v = 0.10 + 0.78 * Math.max(0, d) + bias;
        int tone = 1 + (int) Math.round(v * 5.4);
        return ramp[Math.max(1, Math.min(7, tone))];
    }

    // Every pixel touching transparency gets the darkest tone of the outer ramp at that row.
    static BufferedImage outlined(BufferedImage image, int[][][] rampAt) {
        BufferedImage out = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
            int pixel = image.getRGB(x, y);
            if (isClear(pixel)) continue;
            boolean edge = x == 0 || y == 0 || x == 63 || y == 63 || isClear(image.getRGB(x - 1, y))
                    || isClear(image.getRGB(x + 1, y)) || isClear(image.getRGB(x, y - 1)) || isClear(image.getRGB(x, y + 1));
            int[] ramp = rampAt[y][x] != null ? rampAt[y][x] : GREY;
            out.setRGB(x, y, edge ? opaque(ramp[0]) : pixel);
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
    static double lerp(double a, double b, double t) { return a + (b - a) * Math.max(0, Math.min(1, t)); }
    static int opaque(int rgb) { return 0xff000000 | rgb; }
    static boolean isClear(int argb) { return (argb >>> 24) == 0; }
}
