"""Tiny ray-marched surface-of-revolution renderer -> posterized pixel art (64x64).
Objects are described by an outer radial profile (y,r in px, axis = +y), an optional open cavity, and material zones."""
import math
import numpy as np
from PIL import Image

def hexc(s): return tuple(int(s[i:i+2],16) for i in (1,3,5))
RAMPS = {k:[hexc(c) for c in v] for k,v in {
 'brass' :['#3b2a1d','#5c432f','#8c5f3a','#af794f','#d7aa5e','#eec168','#fbdc7d','#fff9bf'],
 'copper':['#34190f','#5a2e1c','#86432a','#ad5a37','#cf764b','#e8976a','#f8bb94','#ffe0c8'],
 'red'   :['#2a0707','#520d0d','#7f1414','#a81c1c','#cf2a27','#ea4a3c','#fb7a62','#ffb8a3'],
 'grey'  :['#26272c','#3d3f46','#585b63','#767a84','#989ca7','#b9bdc7','#d8dbe3','#f4f6fb'],
 'olive' :['#20261a','#363f27','#4d5a38','#677649','#84965f','#a3b57c','#c3d49e','#e3efc6'],
 'steel' :['#0c0c0e','#1a1a1f','#26262d','#34343d','#45454f','#5a5a66','#76768a','#a0a0b4'],
}.items()}
MATS = list(RAMPS)

def norm(v): v=np.array(v,float); return v/np.linalg.norm(v)
LIGHT_W = norm([-0.50,0.62,0.62]); FILL_W = norm([0.9,-0.35,0.35])   # light lives in the upright 'world' frame

def Rx(a): c,s=math.cos(a),math.sin(a); return np.array([[1,0,0],[0,c,-s],[0,s,c]])
def Ry(a): c,s=math.cos(a),math.sin(a); return np.array([[c,0,s],[0,1,0],[-s,0,c]])
def Rz(a): c,s=math.cos(a),math.sin(a); return np.array([[c,-s,0],[s,c,0],[0,0,1]])
class View:
    def __init__(self,R,cam): self.R=R; self.cam=cam
_cam_side = Rx(math.radians(14)) @ Ry(math.radians(-30))
VIEW_UP   = View(Rx(math.radians(19)),Rx(math.radians(19)))
VIEW_CASE = View(Rx(math.radians(29)),Rx(math.radians(29)))      # steeper look-down so the open mouth reads
VIEW_CRIMP= View(Rx(math.radians(33)),Rx(math.radians(33)))
VIEW_SIDE = View(_cam_side @ Rz(math.radians(-90)),_cam_side)    # mouth to the right, turned toward camera

class Obj:
    def __init__(self, pts, zones, wall=0.0, floor=0.0, crimp=False, scale=1.0):
        pts=sorted(pts); ys=np.array([p[0] for p in pts],float); rs=np.array([p[1] for p in pts],float)
        ys=np.maximum.accumulate(ys+np.arange(len(ys))*1e-4)
        self.ys,self.rs=ys*scale,rs*scale; self.H=self.ys[-1]; self.zones=[(a*scale,b*scale,m) for a,b,m in zones]
        self.wall=wall*scale; self.floor=floor*scale; self.crimp=crimp
    def r_out(self,Yo): return np.interp(Yo,self.ys,self.rs)
    def sdf(self,X,Y,Z):
        Yo=Y+self.H/2; rho=np.hypot(X,Z); r=self.r_out(Yo)
        d=np.maximum(np.maximum(rho-r,-Yo),Yo-self.H)
        if self.wall>0:
            rin=np.maximum(r-self.wall,0.0)
            d=np.maximum(d,-np.maximum(rho-rin,self.floor-Yo))
        return d
    def mat_at(self,Yo):
        m=np.zeros(Yo.shape,int)
        for a,b,name in self.zones: m[(Yo>=a)&(Yo<=b+1e-6)]=MATS.index(name)
        return m

def render(obj, view, W=96, step=0.5, extent=62):
    R=view.R; LIGHT=view.cam@LIGHT_W; FILL=view.cam@FILL_W; HALF=norm(LIGHT+[0,0,1])
    c=(np.arange(W)+0.5)-W/2
    sx,sy=np.meshgrid(c,-c)                       # screen x right, y up
    sx=sx.ravel(); sy=sy.ravel(); M=sx.size
    ds=np.arange(extent,-extent,-step); N=ds.size
    S=np.stack([np.repeat(sx[:,None],N,1),np.repeat(sy[:,None],N,1),np.repeat(ds[None,:],M,0)],-1)   # M,N,3
    P=S@R                                           # R^T applied (row-vector form): P = R^T S
    inside=obj.sdf(P[...,0],P[...,1],P[...,2])<0
    hit=inside.any(1); first=inside.argmax(1); first=np.maximum(first,1)
    idx=np.arange(M)
    lo=ds[first-1]*np.ones(M); hi=ds[first]*np.ones(M)       # lo=outside, hi=inside
    def pt(d): 
        s=np.stack([sx,sy,d],-1); return s@R
    for _ in range(10):
        mid=(lo+hi)/2; p=pt(mid); ins=obj.sdf(p[:,0],p[:,1],p[:,2])<0
        hi=np.where(ins,mid,hi); lo=np.where(ins,lo,mid)
    dh=(lo+hi)/2; Ph=pt(dh); X,Y,Z=Ph[:,0],Ph[:,1],Ph[:,2]
    e=0.05
    g=np.stack([obj.sdf(X+e,Y,Z)-obj.sdf(X-e,Y,Z),obj.sdf(X,Y+e,Z)-obj.sdf(X,Y-e,Z),obj.sdf(X,Y,Z+e)-obj.sdf(X,Y,Z-e)],-1)
    g/=np.maximum(np.linalg.norm(g,axis=1,keepdims=True),1e-9)
    n=g@R.T                                           # to screen space
    Yo=Y+obj.H/2; rho=np.hypot(X,Z)
    diff=np.clip(n@LIGHT,0,1); spec=np.clip(n@HALF,0,1)**34; fill=np.clip(n@FILL,0,1)
    t=0.14+0.66*diff+0.13*fill+0.30*spec
    if obj.wall>0:                                   # open mouth: inner walls fall into shadow with depth
        rin=np.maximum(obj.r_out(Yo)-obj.wall,0)
        cav=(rho<=rin+0.2)&(Yo<obj.H-0.25)
        depth=np.clip((obj.H-Yo)/max(obj.H-obj.floor,1),0,1)
        t=np.where(cav,0.03+0.20*diff*(1-depth)**1.5,t)
    if obj.crimp:                                    # folded star crimp on a shotshell top
        top=(Yo>obj.H-0.35)&(n[:,1]>0.5)&(True)
        ang=np.arctan2(Z,X); sector=np.floor((ang+math.pi)/(2*math.pi/6)).astype(int)%2
        t=np.where(top,t*(0.55+0.4*np.clip(rho/np.maximum(obj.r_out(Yo),1e-3),0,1))*np.where(sector==0,1.0,0.72),t)
    mat=obj.mat_at(Yo)
    ang_all=np.arctan2(Z,X)
    t=t+np.where(mat==MATS.index('red'),0.075*np.sin(ang_all*10),0.0)      # plastic hull flutes
    ramp_i=np.clip(np.floor(t*8),0,7).astype(int)
    img=np.zeros((M,4),np.uint8)
    cols=np.array([RAMPS[m] for m in MATS])          # K,8,3
    img[:,:3]=cols[mat,ramp_i]; img[:,3]=np.where(hit,255,0)
    # inner-edge detection: darken the farther pixel at depth discontinuities (flanges, steps, lip)
    depth_map=np.where(hit,dh,-1e9).reshape(W,W); alpha=hit.reshape(W,W)
    ridx=ramp_i.reshape(W,W).copy(); matg=mat.reshape(W,W)
    for dy,dx in ((0,1),(1,0),(0,-1),(-1,0)):
        nb=np.roll(np.roll(depth_map,dy,0),dx,1)
        occl=alpha&(nb>depth_map+1.6)&(np.roll(np.roll(alpha,dy,0),dx,1))
        ridx=np.where(occl,np.maximum(ridx-3,0),ridx)
    # silhouette outline: dark on bottom/right, a notch lighter on top/left
    out=np.zeros((W,W,4),np.uint8); a=alpha
    up=~np.roll(a,1,0); left=~np.roll(a,1,1); down=~np.roll(a,-1,0); right=~np.roll(a,-1,1)
    for yy in (0,W-1): up[0]=True; down[W-1]=True
    edge=a&(up|left|down|right)
    ridx=np.where(edge&(down|right),0,np.where(edge,np.minimum(ridx,1),ridx))
    rgb=cols[matg,ridx]
    out[...,:3]=rgb; out[...,3]=np.where(a,255,0)
    # drop isolated pixels
    nbc=sum(np.roll(a,s,ax).astype(int) for ax in (0,1) for s in (1,-1))
    out[...,3]=np.where(a&(nbc==0),0,out[...,3])
    return Image.fromarray(out,'RGBA')

def fit64(img, margin=1):
    """crop to 64x64 centred on the sprite's bounding box; returns (img64, needs_shrink_factor)"""
    bb=img.getbbox(); w,h=bb[2]-bb[0],bb[3]-bb[1]
    if max(w,h)>64-2*margin: return None,(64-2*margin)/max(w,h)
    cx,cy=(bb[0]+bb[2])//2,(bb[1]+bb[3])//2
    out=Image.new('RGBA',(64,64),(0,0,0,0)); out.paste(img.crop((cx-32,cy-32,cx+32,cy+32)),(0,0)); return out,1.0

def make(build, view, s0=1.0):
    """build(scale)->Obj ; starts at s0 and auto-shrinks until the sprite fits 64x64"""
    s=s0
    for _ in range(4):
        obj=build(s); im=render(obj,view); out,f=fit64(im)
        if out is not None: return out
        s*=f*0.995
    return out
