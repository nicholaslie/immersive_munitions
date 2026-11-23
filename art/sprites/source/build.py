import os, sys
from engine import make, VIEW_UP, VIEW_SIDE, VIEW_CASE, VIEW_CRIMP
from calibers import *
import pellets   # labels are added afterwards by art/labels/Labels.java
def jobs():
    for n in CAL:
        yield n,'casings',f'casing_{n}',build_casing(n),VIEW_CASE
        yield n,'casings_sideways',f'casing_{n}_side',build_casing(n),VIEW_SIDE
        yield n,'rounds',f'round_{n}',build_round(n),VIEW_UP
        yield n,'bullets',f'bullet_{n}',build_bullet(n),VIEW_UP
    yield '12g','casings','casing_12g',build_shell12(False),VIEW_CASE
    yield '12g','casings_sideways','casing_12g_side',build_shell12(False),VIEW_SIDE
    yield '12g','rounds','round_12g',build_shell12(True),VIEW_CRIMP
    yield '12g','bullets','bullet_12g',None,None   # buckshot pile, drawn by pellets.py
    yield '12g','casings','casing_12g_brass',build_shell12(False,'brass'),VIEW_CASE
    yield '12g','casings_sideways','casing_12g_brass_side',build_shell12(False,'brass'),VIEW_SIDE
    yield '40mm','casings','casing_40mm',build_grenade('case'),VIEW_CASE
    yield '40mm','casings_sideways','casing_40mm_side',build_grenade('case'),VIEW_SIDE
    yield '40mm','rounds','round_40mm',build_grenade('round'),VIEW_UP
    yield '40mm','bullets','bullet_40mm',build_grenade('proj'),VIEW_UP
if __name__=="__main__":
    only=set(sys.argv[1:]); out='out64'
    for n,folder,fname,b,view in jobs():
        if only and n not in only: continue
        os.makedirs(f'{out}/{folder}',exist_ok=True)
        im=pellets.build() if b is None else make(b,view,1.5 if view is VIEW_SIDE else 1.0)
        im.save(f'{out}/{folder}/{fname}.png'); print(fname,end=' ',flush=True)
    print()
