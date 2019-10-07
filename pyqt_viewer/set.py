from typing import Dict, List
import sys
import os
import struct
import io
from pathlib import Path
import logging
from pathlib import Path,PurePosixPath
from PIL import Image,ImageDraw,ImageFont

tns:int = 16
tnm:int = tns*tns*3

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
        return "ci(im="+str(self.img.ihash)+", cl="+str(self.close)+")"

class ImgDB(object):
    
    dtop:Path
    dlist:List[dirent] = []
    flist:List[filent] = []
    imap:Dict[int,imgent] = {}

    def getTop(self):
        return str(self.dtop)

    def getStatus(self):
        stext = "Top: %s, Dirs: %d, Files: %s, Images: %d" % ( str(self.dtop) , len(self.dlist) , len(self.flist) , len(self.imap) )
        return stext

    threshold=50.0
    
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
    
    def search(self,img):
        results:List[compareditem] = []
        for k,i in self.imap.items() :
            cmp = self.compare(img.tmb, i.tmb)
            ci = compareditem(i,cmp)
            if ( cmp < self.threshold ):
                results.append(ci)
        results.sort(key=self.getcloseness) 
        return results
    
    def findfile(self,crc:int):
        for f in self.flist:
            if ( f.ihash == crc ):
                return f
        return 0
    
    def finddir(self,crc:int):
        for d in self.dlist:
            if ( d.dhash == crc ):
                return d
        return 0
    
    def printpath(self,f):
        d = self.finddir(f.dhash)
        if ( d ):
            return d.dpath + "/" + f.fname
        return "??/"+f.fname
    
    
    def save(self,path:Path):
        dpath =  path / "dirs.txt"
        dirfile = dpath.open(mode="w+")
        dirfile.write(self.dbformat(self.dtop.absolute())+"\n")
        for  d in self.dlist :
            dirfile.write(str(d.dhash)+","+d.dpath+"\n")
        dirfile.close()
    
        fpath =  path / "files.txt"
        ffile = fpath.open(mode="w+")
        for  f in self.flist :
            ffile.write(str(f.dhash)+","+str(f.ihash)+","+f.fname+"\n")
        ffile.close()
    
        ipath = path / "images.bin"
        imgfile = ipath.open(mode="wb+")
        for  i in self.imap.values() :
            #log.debug("  Img: %s" , i)
            imgfile.write(i.ihash.to_bytes(8,'little'))
            imgfile.write(i.tmb)
        imgfile.close()
    
    
    def load(self, path):
       
        dpath =  path / 'dirs.txt'
        dirfile = dpath.open()
        line = dirfile.readline()
        line = line.rstrip()
        self.dtop = Path(line)
        for line in dirfile:
            line = line.rstrip()
            parts = line.split(",")
            dentry = dirent(int(parts[0]), parts[1])
            self.dlist.append(dentry)
    
        fpath = path / "files.txt"
        filfile = fpath.open()
        for line in filfile:
            line = line.rstrip()
            parts = line.split(",")
            fentry = filent(int(parts[0]), int(parts[1]), parts[2])
            self.flist.append(fentry)
    
        ipath = path / "images.bin"
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
                self.imap[crc] = img
            else:
                break
    
    def printstate(self):
    
        print("Top: %s, #Dirs: %s" , self.dtop, len(self.dlist))
        for  d in self.dlist :
            print("  Dir: %s" , d)
        for  f in self.flist :
            print("  File: %s" , f)
        for  i in self.imap.values() :
            print("  Img: %s" , i)
    
    def clear(self):
        self.dlist.clear()
        self.flist.clear()
        self.imap.clear()
        
