from PIL import Image
import numpy as np
SLOT='/path/to/tacz/tacz_default_gun/assets/tacz/textures/ammo/slot/'
def get_label(cid, scale=1):
    a=np.array(Image.open(SLOT+cid+'.png').convert('RGBA'))
    lab=np.zeros((8,32,4),np.uint8)
    for r in range(16,24):
        for x in range(32):
            if x<48-r and a[r,x,3]>0: lab[r-16,x]=a[r,x]
    im=Image.fromarray(lab,'RGBA')
    if scale!=1: im=im.resize((32*scale,8*scale),Image.NEAREST)
    return im
def stamp(sprite, cid, scale=1):
    out=sprite.convert('RGBA').copy()
    lab=get_label(cid,scale)
    out.paste(lab,(0,0),lab)   # alpha is 0/255 so paste == replace where label opaque
    return out
