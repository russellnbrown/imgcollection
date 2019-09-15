from typing import Dict, List
import sys
import os
import struct
import io
from pathlib import Path
import logging
from pathlib import Path,PurePosixPath
from PIL import Image,ImageDraw,ImageFont

log:logging.Logger
tns:int = 16
tnm:int = tns*tns*3
dtop:Path
#stop:str

log = logging.getLogger("fred")


class dirent(object):
	dhash:int =0
	dpath:str = ""
	def __init__(self, _dh:int, _dp:str):
		self.dhash=_dh
		self.dpath=_dp

	def __str__(self):
		return "dilent(dh="+str(self.dhash)+", dp="+str(self.dpath)+")"


class filent(object):
	dhash:int=0
	ihash:int=0
	fname:str=""
	def __init__(self, _dh:int, _ih:int, _fn:str):
		self.dhash=_dh
		self.ihash=_ih
		self.fname=_fn

	def __str__(self):
		return "filent(dh="+str(self.dhash)+", ih="+str(self.ihash)+", fn="+str(self.fname)+")"

class imgent(object):
    ihash:int=0
    tmb:bytearray
    def __init__(self,  _ihash:int, _tn:bytearray):
        self.ihash = _ihash
        self.tmb = _tn

    def __str__(self):
        bl = len(self.tmb)
        return "imgent(ih="+str(self.ihash)+", tlen=" + str(bl) +")"

class compareditem(object):
    img:imgent
    close:float=0

    def __init__(self,  _img:imgent, _close:float):
        self.img = _img
        self.close = _close

    def __str__(self):
        return "ci(im="+str(self.img.crc)+", cl="+str(self.close)+")"

dlist:List[dirent] = []
flist:List[filent] = []
imap:Dict[int,imgent] = {}
results:List[compareditem] = []

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
    while px < tns:
        d = simg[px] - ximg[px]
        td += abs(d)
        px += 1

    return td
 

def search(fileToSearch):
    f = Path(fileToSearch)
    ifile = f.open("rb") 
    by = ifile.read()
    image = Image.open(io.BytesIO(by))
    tn = image.resize([16,16])
    rgbtn = tn.convert('RGB')
    #r, g, b = rgbtn.getpixel((1,1))
    #print(r,g,b)
    iby = rgbtn.tobytes()
    bp = 0
    for row in range(1,16):
        print("row=", row)
        for c in range(1,16):
            r = bp + 0
            g = bp + 1
            b = bp + 2
            bp = bp + 3
            print(" r=",iby[r]," g=",iby[g]," b=", iby[b])
        

    ifile.close()
  

def search2(fileToFind):
    
    oimg = cv2.imread(fileToFind)
    rimg = cv2.resize(oimg,(tnm,tnm))
    img = rimg.flatten()
    for k,i in imap.items() :
        cmp = compare(img, i.tmb)
        ci = compareditem(i,cmp)
        results.append(ci)
    results.sort(key=getcloseness)

def findfile(crc:int):
    for f in flist:
        if ( f.fhash == crc ):
            return f
    return 0

def finddir(crc:int):
    for d in dlist:
        if ( d.dhash == crc ):
            return d
    return 0

def printpath(f:filent):
    d = finddir(f.dhash)
    if ( d ):
        return d.dpath + "/" + f.fname
    return "??/"+f.fname


def save(path:Path):
    dpath =  path / "dirs.txt"
    dirfile = dpath.open(mode="w+")
    dirfile.write(dbformat(dtop.absolute())+"\n")
    for  d in dlist :
        dirfile.write(str(d.dhash)+","+d.dpath+"\n")
    dirfile.close()

    fpath =  path / "files.txt"
    ffile = fpath.open(mode="w+")
    for  f in flist :
        ffile.write(str(f.dhash)+","+str(f.ihash)+","+f.fname+"\n")
    ffile.close()

    ipath = path / "images.bin"
    imgfile = ipath.open(mode="wb+")
    for  i in imap.values() :
        log.info("  Img: %s" , i)
        imgfile.write(i.ihash.to_bytes(8,'big'))
        imgfile.write(i.tmb)
    imgfile.close()


def load(path:Path):
    dpath =  path / "dirs.txt"
    dirfile = dpath.open() 
    line = dirfile.readline()
    dtop = Path(line)
    for line in dirfile:
        line = line.rstrip()
        parts = line.split(",")
        dentry = dirent(int(parts[0]), parts[1])
        dlist.append(dentry)

    fpath:Path = path / "files.txt"
    filfile = fpath.open()
    for line in filfile:
        line = line.rstrip()
        parts = line.split(",")
        fentry = filent(int(parts[0]), int(parts[1]), parts[2])
        flist.append(fentry)

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
            tmb = bytearray(imgfile.read(tnm))
            img = imgent(crc,tmb)
            #print(img)
            imap[crc] = img
        else:
            break

def printstate():
    log.warn("Top: %s, #Dirs: %s" , dtop, len(dlist))
    for  d in dlist :
        log.info("  Dir: %s" , d)
    for  f in flist :
        log.info("  File: %s" , f)
    for  i in imap.values() :
        log.info("  Img: %s" , i)
