import os, shutil
from PIL import Image
import labels, molds
# primary TACZ category per caliber (majority of guns that use it; ties/unused ids noted in README)
CAL={'9mm':'pistol','22wmr':'pistol','357mag':'pistol','45acp':'pistol','500mag':'pistol','50ae':'pistol','762x25':'pistol',
 '46x30':'smg','57x28':'smg',
 '300blk':'rifle','308':'rifle','545x39':'rifle','556x45':'rifle','58x42':'rifle','68x51fury':'rifle','6x43':'rifle','762x39':'rifle',
 '30_06':'sniper','338':'sniper','45_70':'sniper','50bmg':'sniper','762x54':'sniper','792x57':'sniper',
 '12g':'shotgun','40mm':'rpg'}
out='mold_pack'; shutil.rmtree(out,ignore_errors=True)
os.makedirs(out+'/categories'); os.makedirs(out+'/calibers')
for c in molds.CATEGORIES: molds.build(c).save(f'{out}/categories/mold_{c}.png')
for cid,cat in CAL.items():
    labels.stamp(Image.open(f'{out}/categories/mold_{cat}.png'),cid,1).save(f'{out}/calibers/mold_casing_{cid}.png')
print(len(CAL),'caliber molds')
