import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/*
 * Draws the Ammunition Assembler's GUI background (textures/gui/ammo_assembler.png), remixed from IE's own GUI art so
 * it sits with IE's machines: the refinery's riveted steel panel, energy gauge and treated-wood inventory, its tanks
 * and arrows cleared away, and two of IE's item slot frames (from the fermenter) with IE's blue input marker over each.
 * The wood starts one plank row higher than IE's (y 73, not 77), so the "Inventory" label sits on wood with as much
 * above its letters (2px) as below them to the slot grid.
 *
 *   java art/gui/AssemblerGui.java [output png]       (from the repo root; needs IE's source in reference/)
 *
 * Slot and gauge positions must match client/AmmoAssemblerScreen and multiblock/AmmoAssemblerMenu.
 */
public class AssemblerGui {
    static final String IE_GUI = "reference/ImmersiveEngineering-1.20.1/ImmersiveEngineering-1.20.1/src/main/resources/"
            + "assets/immersiveengineering/textures/gui/";

    // Item slots (top-left of the 16x16 item area, as passed to the menu's slots): gunpowder, projectiles
    static final int[][] SLOTS = {{56, 34}, {92, 34}};
    // Where the wood starts (IE's starts at 77), and the energy gauge's frame (IE's is at 155,19, 11x50)
    static final int WOOD_TOP = 73, GAUGE_X = 155, GAUGE_Y = 11;

    public static void main(String[] args) throws Exception {
        BufferedImage refinery = ImageIO.read(new File(IE_GUI + "refinery.png"));
        BufferedImage fermenter = ImageIO.read(new File(IE_GUI + "fermenter.png"));
        BufferedImage out = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

        // the refinery's whole background: steel panel, wood inventory below
        for (int y = 0; y < 168; y++) for (int x = 0; x < 176; x++) out.setRGB(x, y, refinery.getRGB(x, y));

        // plain steel inside the panel's 6px riveted border (its tanks, pipes, slots and gauge cleared): the same fine
        // grey noise as the panel's own (0x57..0x65 round 0x5c)
        Random random = new Random(7);
        for (int y = 6; y < WOOD_TOP; y++) for (int x = 6; x < 170; x++) {
            int g = Math.max(0x56, Math.min(0x66, 0x5c + (int) Math.round(random.nextGaussian() * 2.2)));
            out.setRGB(x, y, 0xff000000 | g << 16 | g << 8 | g);
        }

        // one more plank row on top, staggered against the next (IE's 77..80): its plank face is IE's second row (81..83,
        // whose seam row 84 is the slot grid's black edge, so it takes the first row's seam, 80)
        for (int y = WOOD_TOP; y < 77; y++) {
            int src = y < 76 ? 81 + (y - WOOD_TOP) : 80;
            for (int x = 0; x < 176; x++) out.setRGB(x, y, refinery.getRGB(x, src));
        }

        // IE's energy gauge frame, moved up to fit the shorter panel
        copy(refinery, 155, 19, 11, 50, out, GAUGE_X, GAUGE_Y);

        // two IE item slot frames, each with the blue input marker above it
        for (int[] s : SLOTS) {
            copy(fermenter, 90, 52, 18, 18, out, s[0] - 1, s[1] - 1);
            copy(refinery, 140, 11, 2, 2, out, s[0] + 7, s[1] - 4);
        }

        File file = new File(args.length > 0 ? args[0]
                : "src/main/resources/assets/immersive_munitions/textures/gui/ammo_assembler.png");
        file.getParentFile().mkdirs();
        ImageIO.write(out, "png", file);
        System.out.println("wrote " + file);
    }

    static void copy(BufferedImage src, int sx, int sy, int w, int h, BufferedImage dst, int dx, int dy) {
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) dst.setRGB(dx + x, dy + y, src.getRGB(sx + x, sy + y));
    }
}
