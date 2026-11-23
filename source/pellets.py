"""Lead buckshot pile for 12 gauge: spheres rendered with the same lighting/ramp/outline rules as engine.py."""
import math
import numpy as np
from PIL import Image
from engine import RAMPS, LIGHT_W, FILL_W, norm, Rx

def render_pellets(pellets, W=64, tilt=19):
    """pellets: list of (x,y,z,r) in px, y up, z toward camera (screen space)."""
    cam=Rx(math.radians(tilt)); LIGHT=cam@LIGHT_W; FILL=cam@FILL_W; HALF=norm(LIGHT+[0,0,1])
    c=(np.arange(W)+0.5)-W/2
    sx,sy=np.meshgrid(c,-c)
    depth=np.full((W,W),-1e9); nrm=np.zeros((W,W,3)); pid=np.full((W,W),-1)
    for i,(px,py,pz,r) in enumerate(pellets):
        dx=sx-px; dy=sy-py; d2=r*r-dx*dx-dy*dy
        m=d2>0
        z=pz+np.sqrt(np.where(m,d2,0))
        upd=m&(z>depth)
        depth=np.where(upd,z,depth); pid=np.where(upd,i,pid)
        n=np.stack([dx,dy,z-pz],-1)/r
        nrm=np.where(upd[...,None],n,nrm)
    hit=pid>=0
    diff=np.clip(nrm@LIGHT,0,1); spec=np.clip(nrm@HALF,0,1)**40; fill=np.clip(nrm@FILL,0,1)
    t=0.10+0.60*diff+0.12*fill+0.14*spec   # lead: duller, less glossy than the jacketed bullets
    # crevice darkening where a pellet is occluded by a nearer neighbour
    ramp=np.clip(np.floor(t*8),0,7).astype(int)
    for dy_,dx_ in ((0,1),(1,0),(0,-1),(-1,0)):
        nb=np.roll(np.roll(depth,dy_,0),dx_,1); nbh=np.roll(np.roll(hit,dy_,0),dx_,1)
        occl=hit&nbh&(nb>depth+1.6)
        ramp=np.where(occl,np.maximum(ramp-3,0),ramp)
    a=hit
    up=~np.roll(a,1,0); left=~np.roll(a,1,1); down=~np.roll(a,-1,0); right=~np.roll(a,-1,1)
    up[0]=True; down[W-1]=True
    edge=a&(up|left|down|right)
    ramp=np.where(edge&(down|right),0,np.where(edge,np.minimum(ramp,1),ramp))
    cols=np.array(RAMPS['grey'])
    out=np.zeros((W,W,4),np.uint8); out[...,:3]=cols[ramp]; out[...,3]=np.where(a,255,0)
    return Image.fromarray(out,'RGBA')

# 00-buck style pile: front row, middle row, top, plus rear pellets peeking out
R=6.4
PELLETS=[
 (-12.5,-12,0,R),(0,-12.5,2,R),(12.5,-12,0,R),      # bottom row
 (-6.2,-1.5,4,R),(6.5,-1.5,4,R),                    # middle row, nested
 (-17,-2,-4,R),(18,-1,-4,R),                        # side pellets, set back
 (0,9,3,R),                                         # top, nested
 (-8,10,-4,R),(9,10.5,-4,R),                        # rear top pair
]
def build():
    im=render_pellets(PELLETS)
    bb=im.getbbox(); cx,cy=(bb[0]+bb[2])//2,(bb[1]+bb[3])//2
    out=Image.new('RGBA',(64,64),(0,0,0,0)); out.paste(im.crop((cx-32,cy-32,cx+32,cy+32)),(0,0)); return out
if __name__=='__main__':
    im=build(); im.save('pellets_test.png'); print(im.getbbox())
    Image.new('RGBA',(64*8,64*8),(70,70,70,255)).save('bg.png')
    bg=Image.open('bg.png'); big=im.resize((512,512),Image.NEAREST); bg.paste(big,(0,0),big); bg.save('pellets_big.png')
