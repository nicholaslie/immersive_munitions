"""8 category casing molds (64x64) in the style of the 16x16 mold_* icons: grey bevelled block with a see-through casing-shaped recess."""
import numpy as np
from PIL import Image
OUT=0; X0,Y0,X1,Y1=4,4,59,59     # block extents (inclusive), same 1-px outline / bevel / shadow logic as the 16px molds
BEV=2
C_OL_TL,C_OL_BR,C_BODY,C_HI,C_LO,C_RIM_TL,C_RIM_BR=70,54,93,107,79,54,66

def base_block():
    a=np.zeros((64,64,4),np.uint8)
    for y in range(Y0,Y1+1):
        for x in range(X0,X1+1):
            dl,dt,dr,db=x-X0,y-Y0,X1-x,Y1-y
            # rounded corners (2-px stair cut, like the originals)
            if (dl+dt)<2 or (dr+dt)<2 or (dl+db)<2 or (dr+db)<2: continue
            if dt==0 or dl==0: v=C_OL_TL
            elif db==0 or dr==0: v=C_OL_BR
            else:
                d=min(dl,dt,dr,db)-1
                if d>=BEV: v=C_BODY
                else:
                    tl=min(dl,dt); br=min(dr,db)
                    v=C_HI if tl<br else (C_LO if br<tl else C_BODY)
            a[y,x]=(v,v,v,255)
    return a

def casing_mask(pts, ss=4):
    """pts: [(u,r)] along the case from head (u=0) to mouth; axis runs bottom-right -> top-left. returns bool 64x64 centred in the recess zone."""
    L=pts[-1][0]; us=np.array([p[0] for p in pts],float); rs=np.array([p[1] for p in pts],float)
    N=64*ss; c=(np.arange(N)+0.5)/ss
    X,Y=np.meshgrid(c,c)
    hx,hy=48.0,48.0                              # head centre; shifted afterwards
    ax,ay=-2**-.5,-2**-.5; px,py=2**-.5,-2**-.5
    u=(X-hx)*ax+(Y-hy)*ay; v=(X-hx)*px+(Y-hy)*py
    ins=(u>=0)&(u<=L)&(np.abs(v)<=np.interp(u,us,rs))
    m=ins.reshape(64,ss,64,ss).mean((1,3))>=0.5
    ys,xs=np.nonzero(m); cx=(xs.min()+xs.max())/2; cy=(ys.min()+ys.max())/2
    dx=int(round(31.5-cx)); dy=int(round(33.0-cy))
    return np.roll(np.roll(m,dy,0),dx,1)

def prof(rim,rim_t,body,ub,sh_to=None,neck=None,uend=None,groove=True):
    p=[(0,rim),(rim_t,rim)]
    if groove: p+=[(rim_t+.01,body-1.3),(rim_t+1.6,body-1.3),(rim_t+1.61,body)]   # extractor groove
    else: p+=[(rim_t+.01,body)]
    if sh_to is None: p+=[(uend,body)]
    else: p+=[(ub,body),(sh_to,neck),(uend,neck)]
    return p
CATS={
 'pistol':  prof(8.4,2.4,6.8,0,uend=30),
 'smg':     prof(6.9,2.2,5.8,22,sh_to=27,neck=3.3,uend=37),
 'rifle':   prof(8.9,2.4,7.6,26,sh_to=32,neck=4.4,uend=43),
 'dmr':     prof(9.3,2.5,8.0,27,sh_to=34,neck=4.7,uend=45),
 'sniper':  prof(9.5,2.5,8.4,29,sh_to=36,neck=5.0,uend=47),
 'rpg':     prof(13.2,2.8,11.8,0,uend=22,groove=False),
 'shotgun': [(0,10.8),(2.8,10.8),(2.81,9.0),(18,9.0),(18.01,7.9),(23,7.9)],
}
# mg: rifle-size case wrapped by a belt link (flared band)
CATS['mg']=[(0,8.9),(2.4,8.9),(2.41,6.3),(4.0,6.3),(4.01,7.6),(10,7.6),(10.01,11.0),(19,11.0),(19.01,7.6),(26,7.6),(32,4.4),(43,4.4)]

def outline_recess(a,m):
    out=a.copy()
    out[m]=(0,0,0,0)
    # dark rim around the recess: darker on its top-left walls, a notch lighter on bottom-right (as in the originals)
    H,W=m.shape
    pad=np.pad(m,1)
    right=pad[1:-1,2:]; down=pad[2:,1:-1]; left=pad[1:-1,:-2]; up=pad[:-2,1:-1]
    solid=(a[...,3]>0)&~m
    tl=solid&(right|down); br=solid&(left|up)&~tl
    out[tl]=(C_RIM_TL,)*3+(255,); out[br]=(C_RIM_BR,)*3+(255,)
    return out

def build(cat):
    m=casing_mask(CATS[cat])
    a=base_block()
    assert not (m & ~(a[...,3]>0)).any()
    assert m[:10].sum()==0 and m[:,:8].sum()==0 and m[:,56:].sum()==0 and m[56:].sum()==0,(cat,'recess too close to edge/label')
    return Image.fromarray(outline_recess(a,m),'RGBA')
CATEGORIES=['pistol','smg','rifle','dmr','sniper','mg','shotgun','rpg']
if __name__=='__main__':
    import os; os.makedirs('molds_out/categories',exist_ok=True)
    S=5; sh=Image.new('RGBA',(4*64*S,2*64*S),(70,70,70,255))
    for i,c in enumerate(CATEGORIES):
        im=build(c); im.save(f'molds_out/categories/mold_{c}.png')
        big=im.resize((64*S,64*S),Image.NEAREST); sh.paste(big,((i%4)*64*S,(i//4)*64*S),big)
    sh.save('molds_preview.png')
