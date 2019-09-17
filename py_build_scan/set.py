from typing import Dict, List
import sys
import os
import struct
import io
from pathlib import Path
import logging
from pathlib import Path,PurePosixPath
from PIL import Image,ImageDraw,ImageFont
import cfg

log:logging.Logger

log = logging.getLogger("pimage")


def dbformat(p:Path):
    s:str = str(p)
    s = s.replace("\\\\","/")
    s = s.replace("\\","/")
    return s

def getcloseness(ci):
    return ci.close

def compare(simg, ximg):
    px = 0
    td = 0
    while px < cfg.tns:
        d = simg[px] - ximg[px]
        td += abs(d)
        px += 1

    return td
 
def printtn(iby):
    bp = 0
    for row in range(1,16):
        print("row=", row)
        for c in range(1,16):
            r = bp + 0
            g = bp + 1
            b = bp + 2
            bp = bp + 3
            print(" r=",iby[r]," g=",iby[g]," b=", iby[b])

def search(img):
    results:List[compareditem] = []
    for k,i in cfg.imap.items() :
        cmp = compare(img.tmb, i.tmb)
        ci = cfg.compareditem(i,cmp)
        results.append(ci)
    results.sort(key=getcloseness) 
    return results

def findfile(crc:int):
    for f in cfg.flist:
        if ( f.ihash == crc ):
            return f
    return 0

def finddir(crc:int):
    for d in cfg.dlist:
        if ( d.dhash == crc ):
            return d
    return 0

def printpath(f:cfg.filent):
    d = finddir(f.dhash)
    if ( d ):
        return d.dpath + "/" + f.fname
    return "??/"+f.fname


def save(path:Path):
    dpath =  path / "dirs.txt"
    dirfile = dpath.open(mode="w+")
    dirfile.write(dbformat(cfg.dtop.absolute())+"\n")
    for  d in cfg.dlist :
        dirfile.write(str(d.dhash)+","+d.dpath+"\n")
    dirfile.close()

    fpath =  path / "files.txt"
    ffile = fpath.open(mode="w+")
    for  f in cfg.flist :
        ffile.write(str(f.dhash)+","+str(f.ihash)+","+f.fname+"\n")
    ffile.close()

    ipath = path / "images.bin"
    imgfile = ipath.open(mode="wb+")
    for  i in cfg.imap.values() :
        log.info("  Img: %s" , i)
        imgfile.write(i.ihash.to_bytes(8,'little'))
        imgfile.write(i.tmb)
    imgfile.close()


def load(path:Path):
   
    dpath =  path / "dirs.txt"
    dirfile = dpath.open() 
    line = dirfile.readline()
    line = line.rstrip();
    cfg.dtop = Path(line)
    for line in dirfile:
        line = line.rstrip()
        parts = line.split(",")
        dentry = cfg.dirent(int(parts[0]), parts[1])
        cfg.dlist.append(dentry)

    fpath:Path = path / "files.txt"
    filfile = fpath.open()
    for line in filfile:
        line = line.rstrip()
        parts = line.split(",")
        fentry = cfg.filent(int(parts[0]), int(parts[1]), parts[2])
        cfg.flist.append(fentry)

    ipath:Path = path / "images.bin"
    imgfile = open(ipath,"rb")
    ic = 0
    while True:
        bcrc = imgfile.read(8)
        if ( bcrc ) :
            l = len(bcrc)
            lf = struct.calcsize('q')
            r = struct.unpack('q', bcrc)
            crc = r[0]
            tmb = bytearray(imgfile.read(cfg.tnm))
            img = cfg.imgent(crc,tmb)
            #print(img)
            cfg.imap[crc] = img
        else:
            break

def printstate():

    log.warn("Top: %s, #Dirs: %s" , cfg.dtop, len(cfg.dlist))
    for  d in cfg.dlist :
        log.info("  Dir: %s" , d)
    for  f in cfg.flist :
        log.info("  File: %s" , f)
    for  i in cfg.imap.values() :
        log.info("  Img: %s" , i)

def clear():
    cfg.dlist.clear()
    cfg.flist.clear()
    cfg.imap.clear()
    
