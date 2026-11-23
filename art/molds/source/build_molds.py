import os, shutil
import molds
# Builds the 8 unlabeled category molds. Per-caliber molds (category mold + caliber label) are made by
# art/labels/Labels.java, which also holds the caliber -> category table.
out='mold_pack'; shutil.rmtree(out,ignore_errors=True)
os.makedirs(out+'/categories')
for c in molds.CATEGORIES: molds.build(c).save(f'{out}/categories/mold_{c}.png')
print(len(molds.CATEGORIES),'category molds')
