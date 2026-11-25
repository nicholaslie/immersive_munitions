import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/*
 * Draws the zinc and brass textures, recoloured from Immersive Engineering's metal art (and Immersive Geology's gear)
 * with this mod's own palettes, and writes them into the mod.
 *
 *   java art/materials/Materials.java        (from the repo root; needs IE's and IG's source in reference/)
 *
 * The zinc ores are an overlay (block/zinc_ore_overlay.png): IE's nickel ore patches, recoloured to zinc. Their block
 * models draw Minecraft's own stone / deepslate first and the overlay on top, like vanilla's grass block sides, so no
 * vanilla texture is shipped and resource packs that change stone change the ores with it.
 */
public class Materials {
    static final String IE = "reference/ImmersiveEngineering-1.20.1/ImmersiveEngineering-1.20.1/src/main/resources/"
            + "assets/immersiveengineering/textures/";
    static final String IG = "reference/Immersive-Geology-forge-1.20.1/Immersive-Geology-forge-1.20.1/src/main/resources/"
            + "assets/immersivegeology/textures/item/";
    static final String OUT = "src/main/resources/assets/immersive_munitions/textures/";

    // our palettes, dark to light
    static final int[] ZINC = {0x2f343a, 0x454c54, 0x5e666f, 0x79828b, 0x959ea6, 0xb1b9bf, 0xccd3d8, 0xe6ebee};
    static final int[] BRASS = {0x4a300c, 0x6a4814, 0x8c6420, 0xae822e, 0xc9a040, 0xdcba5c, 0xecd283, 0xf8e9b6};
    // zinc ore: bluer, so the ore reads against grey stone
    static final int[] ZINC_ORE = {0x4d6178, 0x5a7390, 0x6886a4, 0x7997b5, 0x8daac6, 0xa6c0d8};

    public static void main(String[] args) throws Exception {
        write("block/zinc_ore_overlay", oreOverlay(read(IE + "block/metal/ore_nickel.png")));
        write("block/raw_zinc_block", remap(read(IE + "block/metal/raw_lead.png"), ZINC));
        write("block/zinc_block", remap(read(IE + "block/metal/storage_aluminum.png"), ZINC));
        write("block/brass_block", remap(read(IE + "block/metal/storage_copper.png"), BRASS));
        write("item/raw_zinc", remap(read(IE + "item/metal_raw_lead.png"), ZINC));
        write("item/zinc_ingot", remap(read(IE + "item/metal_ingot_aluminum.png"), ZINC));
        write("item/zinc_nugget", remap(read(IE + "item/metal_nugget_aluminum.png"), ZINC));
        write("item/zinc_dust", remap(read(IE + "item/metal_dust_iron.png"), ZINC));
        write("item/brass_ingot", remap(read(IE + "item/metal_ingot_copper.png"), BRASS));
        write("item/brass_nugget", remap(read(IE + "item/metal_nugget_copper.png"), BRASS));
        write("item/brass_dust", remap(read(IE + "item/metal_dust_gold.png"), BRASS));
        write("item/brass_plate", remap(read(IE + "item/metal_plate_copper.png"), BRASS));
        write("item/brass_rod", remap(read(IE + "item/material_stick_iron.png"), BRASS));
        write("item/brass_wire", remap(read(IE + "item/material_wire_copper.png"), BRASS));
        write("item/brass_gear", remap(read(IG + "greyscale/metal/gear.png"), BRASS));
    }

    // IE's nickel ore: its patches (green-leaning; the stone is pure grey) as an overlay, each patch tone mapped onto
    // the zinc ore tones by brightness; the stone left clear
    static BufferedImage oreOverlay(BufferedImage nickel) {
        TreeSet<Integer> tones = new TreeSet<>(Comparator.comparingDouble(Materials::lum));
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) if (isPatch(nickel.getRGB(x, y))) tones.add(nickel.getRGB(x, y) & 0xffffff);
        double lo = lum(tones.first()), hi = lum(tones.last());
        Map<Integer, Integer> map = new HashMap<>();
        for (int t : tones) map.put(t, ZINC_ORE[(int) Math.round((lum(t) - lo) / (hi - lo) * (ZINC_ORE.length - 1))]);
        BufferedImage o = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            int c = nickel.getRGB(x, y);
            o.setRGB(x, y, isPatch(c) ? 0xff000000 | map.get(c & 0xffffff) : 0);
        }
        return o;
    }

    static boolean isPatch(int c) {
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        return g > r && g > b;
    }

    // the opaque pixels' brightness, stretched over their own range, mapped onto the ramp; alpha kept
    static BufferedImage remap(BufferedImage src, int[] ramp) {
        BufferedImage o = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        List<int[]> px = new ArrayList<>();
        double min = 1e9, max = -1e9;
        for (int y = 0; y < src.getHeight(); y++) for (int x = 0; x < src.getWidth(); x++) {
            int c = src.getRGB(x, y);
            o.setRGB(x, y, c);
            if ((c >>> 24) == 0) continue;
            px.add(new int[]{x, y});
            min = Math.min(min, lum(c));
            max = Math.max(max, lum(c));
        }
        for (int[] p : px) {
            int c = src.getRGB(p[0], p[1]);
            double t = max > min ? (lum(c) - min) / (max - min) : 0.5;
            o.setRGB(p[0], p[1], (c & 0xff000000) | ramp[(int) Math.round(t * (ramp.length - 1))]);
        }
        return o;
    }

    static double lum(int c) { return 0.299 * ((c >> 16) & 255) + 0.587 * ((c >> 8) & 255) + 0.114 * (c & 255); }

    static BufferedImage read(String path) throws Exception { return ImageIO.read(new File(path)); }

    static void write(String name, BufferedImage img) throws Exception {
        File f = new File(OUT + name + ".png");
        f.getParentFile().mkdirs();
        ImageIO.write(img, "png", f);
        System.out.println("wrote " + f);
    }
}
