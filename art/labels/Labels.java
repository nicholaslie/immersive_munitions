import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/*
 * Stamps the caliber label (32x9 black bar, text colored by category) onto the unlabeled sprites in art/
 * and writes the results into the mod and the bundled TaCZ icon pack, plus art/labels/preview.png.
 *
 *   java art/labels/Labels.java        (from the repo root)
 *
 * Only overwrites textures that already exist in the mod, so calibers we don't ship (6x43) stay out.
 * The RPG rocket has no item of its own, so it only goes into the icon pack.
 */
public class Labels {
    static final int LW = 32, LH = 9;

    // 4x7 pixel font. M and W are 5 wide, x and - are 3, the dot is 1.
    static final Map<Character, String[]> FONT = new HashMap<>();
    static void g(char c, String... rows) { FONT.put(c, rows); }
    static {
        g('0', ".##.", "#..#", "#..#", "#..#", "#..#", "#..#", ".##.");
        g('1', "..#.", ".##.", "..#.", "..#.", "..#.", "..#.", ".###");
        g('2', ".##.", "#..#", "...#", "..#.", ".#..", "#...", "####");
        g('3', "###.", "...#", "...#", ".##.", "...#", "...#", "###.");
        g('4', "#..#", "#..#", "#..#", "####", "...#", "...#", "...#");
        g('5', "####", "#...", "###.", "...#", "...#", "#..#", ".##.");
        g('6', ".##.", "#...", "#...", "###.", "#..#", "#..#", ".##.");
        g('7', "####", "...#", "..#.", "..#.", ".#..", ".#..", ".#..");
        g('8', ".##.", "#..#", "#..#", ".##.", "#..#", "#..#", ".##.");
        g('9', ".##.", "#..#", "#..#", ".###", "...#", "...#", ".##.");
        g('.', ".", ".", ".", ".", ".", ".", "#");
        g('-', "...", "...", "...", "###", "...", "...", "...");
        g('x', "...", "...", "#.#", "#.#", ".#.", "#.#", "#.#");
        g('M', "#...#", "##.##", "#.#.#", "#.#.#", "#...#", "#...#", "#...#");
        g('W', "#...#", "#...#", "#...#", "#.#.#", "#.#.#", "##.##", "#...#");
        g('R', "###.", "#..#", "#..#", "###.", "#.#.", "#..#", "#..#");
        g('B', "###.", "#..#", "#..#", "###.", "#..#", "#..#", "###.");
        g('L', "#...", "#...", "#...", "#...", "#...", "#...", "####");
        g('K', "#..#", "#..#", "#.#.", "##..", "#.#.", "#..#", "#..#");
        g('A', ".##.", "#..#", "#..#", "####", "#..#", "#..#", "#..#");
        g('C', ".###", "#...", "#...", "#...", "#...", "#...", ".###");
        g('P', "###.", "#..#", "#..#", "###.", "#...", "#...", "#...");
        g('E', "####", "#...", "#...", "###.", "#...", "#...", "####");
        g('G', ".###", "#...", "#...", "#.##", "#..#", "#..#", ".###");
        g('I', "###", ".#.", ".#.", ".#.", ".#.", ".#.", "###");
        g('N', "#..#", "##.#", "##.#", "#.##", "#.##", "#..#", "#..#");
    }
    static int textWidth(String s) {
        int w = 0;
        for (char c : s.toCharArray()) {
            String[] gl = FONT.get(c);
            if (gl == null) throw new IllegalArgumentException("No glyph for '" + c + "' in " + s + " (add it to FONT)");
            w += gl[0].length() + 1;
        }
        return w - 1;
    }

    // TaCZ ammo id, label text, category (picks the text color and the mold shape)
    record Cal(String id, String text, String cat) {}
    static final List<Cal> CALS = List.of(
        new Cal("9mm", "9MM", "pistol"), new Cal("22wmr", ".22WMR", "pistol"), new Cal("357mag", ".357", "pistol"),
        new Cal("45acp", ".45ACP", "pistol"), new Cal("500mag", ".500", "pistol"), new Cal("50ae", ".50AE", "pistol"),
        new Cal("762x25", "7.62x25", "pistol"), new Cal("46x30", "4.6x30", "smg"), new Cal("57x28", "5.7x28", "smg"),
        new Cal("300blk", "300BLK", "rifle"), new Cal("308", ".308", "rifle"), new Cal("545x39", "5.45x39", "rifle"),
        new Cal("556x45", "5.56x45", "rifle"), new Cal("58x42", "5.8x42", "rifle"), new Cal("68x51fury", "6.8x51", "rifle"),
        new Cal("6x43", "6x43", "rifle"), new Cal("762x39", "7.62x39", "rifle"), new Cal("30_06", "30-06", "sniper"),
        new Cal("338", ".338", "sniper"), new Cal("45_70", "45-70", "sniper"), new Cal("50bmg", ".50BMG", "sniper"),
        new Cal("762x54", "7.62x54", "sniper"), new Cal("792x57", "7.92x57", "sniper"), new Cal("12g", "12GA", "shotgun"),
        new Cal("40mm", "40MM", "rpg"), new Cal("rpg_rocket", "RPG-7", "rpg"));

    static final Map<String, Integer> CAT_TEXT = Map.of(
        "pistol", 0x8fd35e, "smg", 0x6cb2f0, "rifle", 0xf2a446, "sniper", 0xf06a5c,
        "shotgun", 0xc58cf0, "rpg", 0xb8c86e);
    static final int BACKGROUND = 0x000000;

    static int A(int rgb) { return 0xff000000 | rgb; }

    static BufferedImage label(Cal c) {
        BufferedImage im = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < LH; y++) for (int x = 0; x < LW; x++) im.setRGB(x, y, A(BACKGROUND));
        String s = c.text();
        int areaL = 1, areaR = LW - 2, w = textWidth(s);
        if (w > areaR - areaL + 1) throw new IllegalArgumentException("Label text too wide: " + s + " (" + w + "px, max " + (areaR - areaL + 1) + ")");
        int rgb = A(CAT_TEXT.get(c.cat()));
        int cx = areaL + (areaR - areaL + 1 - w) / 2;
        for (char ch : s.toCharArray()) {
            String[] gl = FONT.get(ch);
            for (int r = 0; r < gl.length; r++) for (int k = 0; k < gl[r].length(); k++)
                if (gl[r].charAt(k) == '#') im.setRGB(cx + k, 1 + r, rgb);
            cx += gl[0].length() + 1;
        }
        return im;
    }
    static BufferedImage stamp(BufferedImage sprite, Cal c) {
        BufferedImage out = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics(); g.drawImage(sprite, 0, 0, null); g.dispose();
        BufferedImage lab = label(c);
        for (int y = 0; y < LH; y++) for (int x = 0; x < LW; x++) out.setRGB(x, y, lab.getRGB(x, y));
        return out;
    }


    static final String TEX = "src/main/resources/assets/immersive_munitions/textures/item/";
    static final String SLOT = "src/main/resources/resourcepacks/immersive_munitions_ammo/assets/tacz/textures/ammo/slot/";

    static BufferedImage read(String p) throws Exception { return ImageIO.read(new File(p)); }
    static int written = 0;
    static void write(BufferedImage im, String p, boolean onlyIfExists) throws Exception {
        File f = new File(p);
        if (onlyIfExists && !f.exists()) return;
        f.getParentFile().mkdirs();
        ImageIO.write(im, "png", f);
        written++;
    }

    public static void main(String[] args) throws Exception {
        if (!new File("art/labels").isDirectory()) throw new IllegalStateException("Run this from the repo root.");
        List<BufferedImage[]> previewRows = new ArrayList<>();
        for (Cal c : CALS) {
            BufferedImage round = stamp(read("art/sprites/rounds/round_" + c.id() + ".png"), c);
            boolean rocket = c.id().equals("rpg_rocket");
            write(round, TEX + "ammo/loaded_round_reference/round_" + c.id() + ".png", !rocket);
            write(round, SLOT + c.id() + ".png", !rocket);
            BufferedImage casing = null, bullet = null, mold = null;
            if (!rocket) {
                casing = stamp(read("art/sprites/casings/casing_" + c.id() + ".png"), c);
                bullet = stamp(read("art/sprites/bullets/bullet_" + c.id() + ".png"), c);
                mold = stamp(read("art/molds/categories/mold_" + c.cat() + ".png"), c);
                write(casing, TEX + "ammo/empty_casings/casing_" + c.id() + ".png", true);
                write(bullet, TEX + "ammo/projectiles/bullet_" + c.id() + ".png", true);
                write(mold, TEX + "molds/mold_casing_" + c.id() + ".png", true);
            }
            previewRows.add(new BufferedImage[]{round, casing, bullet, mold});
        }
        System.out.println("wrote " + written + " textures");
        preview(previewRows, new File("art/labels/preview.png"));
    }

    // The grey squares only exist in the preview; the textures are transparent.
    static void preview(List<BufferedImage[]> rows, File out) throws Exception {
        int lk = 6, lcols = 5, lcell = LW * lk + 14, lrows = (CALS.size() + lcols - 1) / lcols;
        int k = 2, cell = 64 * k + 8, cols = 9, groups = (rows.size() + cols - 1) / cols;
        int W = Math.max(16 + cols * cell, 16 + lcols * lcell);
        int labelsH = 50 + lrows * (LH * lk + 12);
        int H = labelsH + 40 + groups * (4 * cell + 24);
        BufferedImage s = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = s.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setColor(new Color(0x34343a)); g.fillRect(0, 0, W, H);
        g.setColor(Color.WHITE); g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("Caliber labels, " + LW + "x" + LH + " (6x)", 16, 32);
        for (int i = 0; i < CALS.size(); i++)
            g.drawImage(label(CALS.get(i)), 16 + (i % lcols) * lcell, 48 + (i / lcols) * (LH * lk + 12), LW * lk, LH * lk, null);
        g.drawString("On the textures: round / casing / bullet / mold (2x, grey backdrop added for viewing)", 16, labelsH + 24);
        for (int i = 0; i < rows.size(); i++) {
            int gx = 16 + (i % cols) * cell, gy = labelsH + 40 + (i / cols) * (4 * cell + 24);
            for (int r = 0; r < 4; r++) {
                BufferedImage im = rows.get(i)[r];
                if (im == null) continue;
                g.setColor(new Color(0x8b8b8b)); g.fillRect(gx, gy + r * cell, 64 * k, 64 * k);
                g.drawImage(im, gx, gy + r * cell, 64 * k, 64 * k, null);
            }
        }
        g.dispose();
        ImageIO.write(s, "png", out);
        System.out.println("preview: " + out);
    }
}
