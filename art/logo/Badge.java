import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Draws the mod logo: Immersive Farming's badge frame (MIT, see NOTICE.md) with our 5.56x45mm round lying corner
 * to corner in the panel.
 *
 *     java art/logo/Badge.java        (from the repo root)
 *
 * The frame is frame_immersive_farming.png, Immersive Farming's 32x32 icon with its hoe removed. It's doubled to
 * 64x64 so the round can be drawn at twice its pixel density: each logo pixel maps back to one pixel of
 * art/sprites/rounds/round_556x45.png through the 45-degree coordinates a = x - y (along the round) and b = x + y
 * (across it), so the edges are clean diagonal steps and the colours are the sprite's own.
 *
 * Writes src/main/resources/icon.png (64x64, the in-game logo) and art/logo.png (512x512, for CurseForge).
 */
public class Badge {
    static final int N = 64;
    static final double SCALE = 0.7;   // round size, in logo pixels per sprite pixel
    static final int PANEL_SHADOW = 0x99938a;
    static final int OUTLINE_LIT = 0x5a2e1c, OUTLINE_SHADE = 0x34190f;   // the sprite's outline colours
    static final int[] COPPER = {0xe8976a, 0xf8bb94, 0xffe0c8, 0xcf764b};

    public static void main(String[] args) throws Exception {
        BufferedImage frame = ImageIO.read(new File("art/logo/frame_immersive_farming.png"));
        BufferedImage sprite = trim(ImageIO.read(new File("art/sprites/rounds/round_556x45.png")));
        int sw = sprite.getWidth(), sh = sprite.getHeight();

        BufferedImage logo = new BufferedImage(N, N, BufferedImage.TYPE_INT_ARGB);
        boolean[][] panel = new boolean[N][N];
        for (int y = 0; y < N; y++) for (int x = 0; x < N; x++) {
            int c = frame.getRGB(x / 2, y / 2);
            logo.setRGB(x, y, c);
            int r = c >> 16 & 255, g = c >> 8 & 255, b = c & 255, luma = (r * 3 + g * 6 + b) / 10;
            panel[x][y] = x >= 14 && x <= 49 && y >= 14 && y <= 49 && luma > 140 && !(r > 190 && b < 130);   // beige, not trim
        }

        // the round, centred, tip to the upper right
        double spanA = sh * SCALE * Math.sqrt(2), spanB = sw * SCALE * Math.sqrt(2);
        boolean[][] on = new boolean[N][N];
        int[][] col = new int[N][N];
        for (int y = 0; y < N; y++) for (int x = 0; x < N; x++) {
            double a = x - y, b = x + y - (N - 1);
            int row = (int) Math.floor((spanA / 2 - a) / spanA * sh), column = (int) Math.floor((b + spanB / 2) / spanB * sw);
            if (row < 0 || row >= sh || column < 0 || column >= sw) continue;
            int c = sprite.getRGB(column, row);
            if ((c >>> 24) < 128) continue;
            on[x][y] = true;
            col[x][y] = c;
        }
        // the diagonal steps cut past the sprite's outline at the tip: close it with the outline colour of that side
        for (int y = 1; y < N - 1; y++) for (int x = 1; x < N - 1; x++) {
            if (!on[x][y] || !isCopper(col[x][y] & 0xffffff)) continue;
            if (on[x + 1][y] && on[x - 1][y] && on[x][y + 1] && on[x][y - 1]) continue;
            col[x][y] = 0xff000000 | (!on[x][y - 1] || !on[x - 1][y] ? OUTLINE_LIT : OUTLINE_SHADE);
        }
        for (int y = 0; y < N; y++) for (int x = 0; x < N; x++) {
            if (!on[x][y]) continue;
            for (int dy = -1; dy <= 2; dy++) for (int dx = -1; dx <= 2; dx++)
                if (!panel[x + dx][y + dy]) throw new IllegalStateException("the round touches the frame; lower SCALE");
        }
        for (int y = 1; y < N; y++) for (int x = 1; x < N; x++)
            if (!on[x][y] && on[x - 1][y - 1] && panel[x][y]) logo.setRGB(x, y, 0xff000000 | PANEL_SHADOW);
        for (int y = 0; y < N; y++) for (int x = 0; x < N; x++) if (on[x][y]) logo.setRGB(x, y, col[x][y]);

        ImageIO.write(logo, "png", new File("src/main/resources/icon.png"));
        int s = 8;
        BufferedImage big = new BufferedImage(N * s, N * s, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < N * s; y++) for (int x = 0; x < N * s; x++) big.setRGB(x, y, logo.getRGB(x / s, y / s));
        ImageIO.write(big, "png", new File("art/logo.png"));
        System.out.println("wrote src/main/resources/icon.png (64x64) and art/logo.png (512x512)");
    }

    static boolean isCopper(int c) { for (int k : COPPER) if (k == c) return true; return false; }

    static BufferedImage trim(BufferedImage s) {
        int x0 = s.getWidth(), y0 = s.getHeight(), x1 = -1, y1 = -1;
        for (int y = 0; y < s.getHeight(); y++) for (int x = 0; x < s.getWidth(); x++)
            if ((s.getRGB(x, y) >>> 24) > 0) { x0 = Math.min(x0, x); x1 = Math.max(x1, x); y0 = Math.min(y0, y); y1 = Math.max(y1, y); }
        return s.getSubimage(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
    }
}
