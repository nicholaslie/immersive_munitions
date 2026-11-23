"""Real-world dimensions (mm) for each TACZ ammo id with a casing, scaled for a 64px sprite.
Lc=case length, OAL=loaded length, groove=(dia,width) or None (rimmed), ss/se=shoulder start/end from base,
end_d=body dia at shoulder start, neck_d=case mouth outer dia, bd=bullet dia, bl=bullet length, kind=nose shape."""
import math
from engine import Obj

def S(Lc,OAL,rim_d,rim_h,groove,base_d,end_d,ss,se,neck_d,bd,bl,kind,tip=None):
    return dict(Lc=Lc,OAL=OAL,rim_d=rim_d,rim_h=rim_h,groove=groove,base_d=base_d,end_d=end_d,ss=ss,se=se,neck_d=neck_d,bd=bd,bl=bl,kind=kind,tip=tip)
CAL = {
 "22wmr":   S(26.8,34.4,7.6,1.0,None,6.9,6.7,26.8,26.8,6.6,5.7,14,"round"),
 "9mm":     S(19.15,29.7,9.96,1.2,(9.0,1.0),9.9,9.65,19.15,19.15,9.65,9.02,15,"round"),
 "45acp":   S(22.8,32.4,12.2,1.3,(11.0,1.1),12.1,11.9,22.8,22.8,11.9,11.5,16,"round"),
 "357mag":  S(33.0,40.4,11.2,1.6,None,9.6,9.4,33.0,33.0,9.4,9.1,17,"flat"),
 "50ae":    S(32.6,40.9,14.3,1.6,(13.0,1.3),13.9,13.5,32.6,32.6,13.5,12.7,18,"round"),
 "500mag":  S(41.3,54.0,14.6,1.8,None,13.3,13.1,41.3,41.3,13.1,12.7,24,"flat"),
 "45_70":   S(53.3,64.8,15.4,1.8,None,12.2,11.8,53.3,53.3,11.8,11.6,28,"flat"),
 "762x25":  S(24.9,34.9,9.9,1.0,(9.0,1.0),9.9,9.7,17.5,19.3,8.7,7.85,14,"spitzer"),
 "57x28":   S(28.9,40.5,7.95,1.0,(7.1,0.9),7.9,7.4,21.5,24.0,6.4,5.7,17,"spitzer"),
 "46x30":   S(30.0,38.5,8.0,1.0,(7.2,0.9),7.9,7.2,21.0,24.5,5.2,4.65,14,"spitzer"),
 "300blk":  S(34.7,57.4,9.6,1.2,(8.6,1.0),9.5,9.2,26.0,28.5,8.6,7.8,27,"spitzer"),
 "545x39":  S(39.5,57.0,11.3,1.4,(10.2,1.2),11.3,10.0,29.0,32.5,6.5,5.6,23,"spitzer"),
 "556x45":  S(44.7,57.4,9.6,1.3,(8.6,1.0),9.5,9.0,33.0,36.5,6.4,5.7,20,"spitzer"),
 "58x42":   S(42.0,58.0,10.9,1.3,(9.9,1.1),10.7,9.5,30.0,34.0,6.6,5.8,21,"spitzer"),
 "6x43":    S(43.0,61.0,11.3,1.4,(10.2,1.2),11.2,10.3,31.0,35.0,7.0,6.2,26,"spitzer"),
 "762x39":  S(38.7,55.9,11.35,1.5,(10.2,1.2),11.35,10.0,27.0,30.0,8.7,7.9,26,"spitzer"),
 "308":     S(51.2,69.9,12.0,1.3,(10.8,1.2),11.9,11.5,36.0,43.5,8.7,7.82,31,"spitzer"),
 "68x51fury":S(51.2,72.0,13.3,1.5,(12.0,1.2),13.0,11.9,34.0,41.0,9.0,7.0,30,"spitzer"),
 "792x57":  S(56.9,80.7,11.9,1.3,(10.7,1.2),11.9,11.2,41.0,48.0,9.0,8.2,33,"spitzer"),
 "30_06":   S(63.3,84.8,12.0,1.3,(10.8,1.2),11.9,11.2,47.0,53.5,8.7,7.82,31,"spitzer"),
 "762x54":  S(53.7,77.0,14.5,1.6,None,12.4,11.8,39.0,45.0,8.7,7.92,31,"spitzer"),
 "338":     S(69.2,93.5,14.9,1.6,(13.5,1.4),14.9,14.6,49.0,61.0,9.3,8.6,36,"spitzer"),
 "50bmg":   S(99.1,138.7,20.4,2.0,(18.5,1.6),20.4,18.0,71.0,86.0,14.6,12.9,55,"spitzer",tip=0.16),
}
def hpx(oal): return 16+46*(oal/138.7)**0.65

def nose(kind,R,y0,y1,n=22):
    """points (y,r) for the exposed bullet from y0 (cylinder end) to y1 (tip)"""
    L=y1-y0; pts=[]
    if kind=="flat":
        cyl=0.6; yc=y0+L*cyl; pts=[(y0,R),(yc,R)]
        for i in range(1,5): t=i/4; pts.append((yc+(L*(1-cyl))*t*0.92, R*(1-0.42*t)))
        pts.append((y1-0.01,R*0.58)); pts.append((y1,R*0.5)); return pts
    cyl=0.30 if kind=="spitzer" else 0.32
    yc=y0+L*cyl; pts=[(y0,R),(yc,R)]; Ln=y1-yc
    for i in range(1,n+1):
        t=i/n
        if kind=="round": r=R*math.sqrt(max(1-t**2.1,0))
        else:                                              # tangent ogive (real spitzer profile)
            rho=(R*R+Ln*Ln)/(2*R); r=math.sqrt(max(rho*rho-(Ln*t)**2,0))+R-rho
        pts.append((yc+Ln*t, max(r,R*0.1)))
    return pts

def kscale(s):
    H=hpx(s['OAL']); ky=H/s['OAL']; kx=1.5*ky**0.6; return kx,ky

def case_pts(s,kx,ky):
    rr=lambda d:d*kx/2
    pts=[]; rim_h=max(s['rim_h']*ky,2.0); R0=rr(s['rim_d'])
    pts+=[(0,R0-0.9),(0.9,R0),(rim_h,R0)]; y=rim_h
    if s['groove']:
        gd,gh=s['groove']; gh=max(gh*ky,1.6)
        pts+=[(y+0.01,rr(gd)),(y+gh,rr(gd)),(y+gh+0.01,rr(s['base_d']))]; y+=gh
    else: pts+=[(y+0.01,rr(s['base_d']))]
    Lc=s['Lc']*ky
    ss=max(s['ss']/s['Lc']*Lc,y+1); se=max(s['se']/s['Lc']*Lc,ss+0.02)
    pts+=[(ss,rr(s['end_d'])),(se,rr(s['neck_d'])),(Lc,rr(s['neck_d']))]
    return pts,Lc

def build_casing(name):
    s=CAL[name]; kx,ky=kscale(s)
    def b(scale):
        pts,Lc=case_pts(s,kx,ky)
        return Obj(pts,[(0,Lc,'brass')],wall=max(1.3,0.7*kx),floor=2.4,scale=scale)
    return b

def build_round(name):
    s=CAL[name]; kx,ky=kscale(s)
    def b(scale):
        pts,Lc=case_pts(s,kx,ky); R=s['bd']*kx/2; OAL=s['OAL']*ky
        pts=pts+[(Lc+0.02,R)]+nose(s['kind'],R,Lc+0.02,OAL)
        zones=[(0,Lc,'brass'),(Lc+0.01,OAL+1,'copper')]
        if s['tip']: zones.append((OAL*(1-s['tip']),OAL+1,'steel'))
        return Obj(pts,zones,scale=scale)
    return b

def build_bullet(name):
    s=CAL[name]; bl=s['bl']; hb=22+34*(bl/58)**0.6; k=hb/bl     # uniform scale: true proportions, sized to fill the sprite
    def b(scale):
        R=s['bd']*k/2*(1.3 if s['kind']=='spitzer' else 1.0); exposed=(s['OAL']-s['Lc'])*k; Lb=hb     # rifle bullets drawn chunkier for icon readability
        yc0=Lb-exposed*1.0
        pts=[(0,R*0.80),(1.4,R*0.97),(2.6,R)]
        if s['kind']=="flat": pts+=nose("flat",R,2.6,Lb)[0:]
        else: pts+=nose(s['kind'],R,max(yc0,3.0),Lb)
        zones=[(0,Lb+1,'copper')]
        if s['tip']: zones.append((Lb*(1-s['tip']),Lb+1,'steel'))
        return Obj(pts,zones,scale=scale)
    return b

# ---------- 12 gauge: red plastic hull + brass head (uniform scale), and 40mm grenade ----------
def shotgun_obj(loaded,plastic='red',scale=1.0):
    k=0.88; rim_r=20.2*k/2; head_r=19.4*k/2; hull_r=18.4*k/2; head_h=16*k; H=70*k
    pts=[(0,rim_r-0.9),(0.9,rim_r),(2.0,rim_r),(2.01,head_r),(head_h-0.6,head_r),(head_h,hull_r+0.25),(head_h+0.8,hull_r),(H-1.6,hull_r)]
    if loaded: pts+=[(H-0.7,hull_r*0.93),(H,hull_r*0.80)]
    else: pts+=[(H,hull_r)]
    zones=[(0,head_h,'brass'),(head_h+0.01,H+1,plastic)]
    return Obj(pts,zones,wall=0 if loaded else 1.5*0.9+0.2,floor=head_h*0.85,crimp=loaded,scale=scale)
def build_shell12(loaded,plastic='red'): return lambda scale:shotgun_obj(loaded,plastic,scale)

def grenade_obj(kind,scale=1.0):
    k=0.60; rim_r=41*k/2; body_r=39.5*k/2; Lc=46*k; OAL=99*k
    case=[(0,rim_r-0.9),(0.9,rim_r),(max(3*k,2.0),rim_r),(max(3*k,2.0)+0.01,body_r),(Lc-0.01,body_r),(Lc,body_r*0.97)]
    R=38.0*k/2; cap=Lc+0.02; band_a=cap+3*k; band_b=cap+9*k
    proj=[(cap,R),(band_a,R),(band_a+0.01,R*1.0),(band_b,R*1.0)]
    # round ogive nose
    y0=band_b; L=OAL-y0
    for i in range(1,18):
        t=i/17; proj.append((y0+L*t,max(R*math.sqrt(max(1-t**2.0,0)),R*0.16)))
    if kind=="case":
        return Obj(case,[(0,Lc,'brass')],wall=1.8,floor=3,scale=scale)
    if kind=="round":
        return Obj(case+proj,[(0,Lc,'brass'),(Lc+0.01,band_a,'olive'),(band_a,band_b,'copper'),(band_b,OAL+1,'olive')],scale=scale)
    # projectile alone, scaled up to fill the sprite
    kk=1.45
    pts=[(0,R*kk*0.86),(2,R*kk)]+[((y-cap)*kk+2,r*kk) for y,r in proj[1:]]
    return Obj(pts,[(0,(band_b-cap)*kk+2,'copper'),((band_a-cap)*kk+2,(band_b-cap)*kk+2,'copper'),((band_b-cap)*kk+2.01,OAL*2,'olive'),(0,(band_a-cap)*kk+2,'olive')],scale=scale)
def build_grenade(kind): return lambda scale:grenade_obj(kind,scale)

def slug_obj(scale=1.0):
    k=1.7; R=18.0*k/2*0.62; L=46
    pts=[(0,R*0.8),(1.2,R*0.96),(2.2,R)]
    for i,y in enumerate((6,11,16)): pts+=[(y,R),(y+0.5,R*0.9),(y+1.6,R*0.9),(y+2.1,R)]
    y0=21
    for i in range(1,16):
        t=i/15; pts.append((y0+(L-y0)*t,max(R*math.sqrt(max(1-t**2.0,0)),R*0.18)))
    return Obj(pts,[(0,L+1,'grey')],scale=scale)
