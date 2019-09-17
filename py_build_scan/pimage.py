from typing import Dict, List
import sys
import os
import io
import struct
import logging
import zlib
import set
from PIL import Image,ImageDraw,ImageFont
from pathlib import Path,PurePosixPath
import cfg



#
# imgfileinfo - class used to hold useful information relating to an image file
#
class imgfileinfo(object):
    ihash:int=0
    dhash:int=0
    tmb:bytearray
    name:str
    relpath:str

    def __init__(self):
        name=""
        relpath=""
        ihash=0
        dhash=0

    def __str__(self):
        bl = len(self.tmb)
        s = "imgfileinfo(ih=" + str(self.ihash) + ", dh=" + str(self.ihash) + ", tlen=" + str(bl) + ", na=" + self.name + ", relp=" + self.relpath + ")"
        return s

#
# standardizePath - makes all paths confirm to a standard format with forward slashes, no tailing 
#
def standardizePath(p:str):
    p = p.replace("\\\\","/")
    p = p.replace("\\","/")
    if p.endswith("/"):
        p = p[0..len(p)-1]
    return p

#
# Set up logger
#
def setlogging():
    log = logging.getLogger("pimage")
    handler = logging.StreamHandler()
    handler.formatter = logging.Formatter('%(asctime)s %(levelname)s : %(message)s')
    handler.level = logging.INFO
    fhandler = logging.FileHandler('ptest.log','w+')
    fhandler.level = logging.DEBUG
    fhandler.formatter = logging.Formatter('%(asctime)s %(levelname)s : %(message)s')
    log.setLevel(logging.DEBUG)
    log.addHandler(handler)
    log.addHandler(fhandler)

#
# psplit - Splits a path into path & filename parts making sure path starts 
# with a '/' and is relative to the top directoru
#
def psplit(p:Path):
    strp = p
    
    stop = cfg.dtop
    rpos = str(os.path.relpath(strp,stop))
    rpos = standardizePath(rpos)
    if  rpos == "." : # catch first dir and remove '.'
        rpos = ""
    if not str.startswith(rpos,"/"):
        rpos = "/" + rpos
    name = ""
    if not os.path.isdir(p) :
        name = os.path.basename(p) # split off filename into name and use whats left in path
        rpos = rpos[0:len(rpos)-len(name)-1]
    else:
        name = ""

    return [ rpos,name ]
    
#
# procsetdir - called by file walker. creates a 'dirent' and adds to the dir list
#
def procsetdir(d:str):
    rpos = psplit(d)
    dhash = gethash(rpos[0])
    de = cfg.dirent(dhash,rpos[0])
    cfg.dlist.append(de)
    log.info("DIR: %s HASH: %d", rpos, dhash)

#
# getbhash - returns crc32 of a byte array ( the file content )
def getbhash(key):
    hash = zlib.crc32(key)
    return hash

#
# finfo - takes an image file and calculates paths, hashes & thumbnail.
# returns in a imgfileinfo class
#
def finfo(f:Path):
    fi = imgfileinfo()
    rpos = psplit(f)
    fi.name = rpos[1]
    fi.relpath = rpos[0]
    fi.dhash = gethash(fi.relpath)
    ifile = f.open("rb") 
    by = ifile.read()
    fi.ihash = getbhash(by)
    image = Image.open(io.BytesIO(by))
    tn = image.resize([16,16])
    rgbtn = tn.convert('RGB')
    fi.tmb = rgbtn.tobytes()
    ifile.close()
    return fi

def makeentries(f:Path):
    log.info("PFILE: %s", f)
    rpos = psplit(f)
    ipos = finfo(f)
    dhash = gethash(rpos[0])
    fe = set.filent(dhash,ipos[0],rpos[1])
    ie = set.imgent(ipos[0],ipos[1])
    return [ rpos, ipos, dhash, fe, ie ]


def procsetfile(f:Path):
    fi = finfo(f)
    log.info("Processing file %s", fi)
    fe = cfg.filent(fi.dhash, fi.ihash, fi.name)
    ie = cfg.imgent(fi.ihash, fi.tmb)
    cfg.flist.append(fe)
    cfg.imap[ie.ihash] = ie


def file_walker(walk):
    log.info("Walk at: %s" , walk )
    procsetdir(walk)
    
    for item in walk.iterdir():
        if item.is_dir():
            file_walker(item)
        else:
            procsetfile(item)

def create(setl, files):
 
    setp = Path(setl) #standardizePath(setl)
    filesp = Path(files) #standardizePath(os.path.abspath(files))
    cfg.dtop = Path(files)#.absolutePath() # standardizePath(os.path.abspath(files))

    if not setp.is_dir() :
        log.warn("set dosnt exist, create")
        os.mkdir(setp);

    if not cfg.dtop.is_dir() :
        log.fatal("top dir %s dosn't exist, exit", cfg.dtop.str())
        usage()

    if not filesp.is_dir() :
        log.fatal("files dir %s dosn't exist, exit" , filesp  )
        usage()

    log.info("Create...\n")
    file_walker(filesp)
    log.info("Created\n")

    set.printstate()
    set.save(setp)


def gethash(pstr):
    b = pstr.encode('utf-8')
    hash = zlib.crc32(b)
    return hash
    hash:long = 5381;
    l = len(pstr)
    for cx in pstr:
        hash = ((hash << 5) + hash) + ord(cx)
    return hash
    
def usage():
    log.info("Usage: pimage.py -c <set> <top> <files> | -s <set> <file>")
    exit(0)


# Start Here

setlogging()

if ( len(sys.argv) < 3 ):
    usage()

if ( sys.argv[1] == "-s" and len(sys.argv) == 4 ):
    p = standardizePath(sys.argv[2])
    set.load(Path(sys.argv[2]))
    set.printstate()

    f = standardizePath(sys.argv[3])
    fi = finfo(Path(f))

    results = set.search(fi)
    if ( len(results) == 0 ):
        print("No images found")
        exit(0)

    for ci in results :
        print("res:" , ci.img)
        fi = set.findfile(ci.img.ihash)
        if ( fi != 0 ):
            print("Img " + str(ci.img.ihash) + ", cls=" + str(ci.close) + ", file=" + set.findfile(ci.img.ihash).fname)
        else:
            print("Img " + str(ci.img.ihash) + ", cls=" + str(ci.close) + ", file=?")

elif ( sys.argv[1] == "-c" and len(sys.argv) == 4 ):
    create(sys.argv[2], sys.argv[3] )
    log.info("Final state:")
    #set.printstate()
    #set.clear()
    #set.load(Path(sys.argv[2]))
    #log.info("Readback state:")
    #set.printstate()

else:
    usage();
