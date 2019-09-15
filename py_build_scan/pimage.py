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

log = logging.getLogger("fred")

def standardizePath(p:str):
    p = p.replace("\\\\","/")
    p = p.replace("\\","/")
    if p.endswith("/"):
        p = p[0..len(p)-1]
    return p

def setlogging():
    handler = logging.StreamHandler()
    handler.formatter = logging.Formatter('%(asctime)s %(levelname)s : %(message)s')
    handler.level = logging.INFO
    fhandler = logging.FileHandler('ptest.log','w+')
    fhandler.level = logging.DEBUG
    fhandler.formatter = logging.Formatter('%(asctime)s %(levelname)s : %(message)s')
    log.setLevel(logging.DEBUG)
    log.addHandler(handler)
    log.addHandler(fhandler)

def psplit(p:Path):
    strp = p
    stop = set.dtop
    rpos = str(os.path.relpath(strp,stop))
    rpos = standardizePath(rpos)
    if  rpos == "." : # catch first dir and remove '.'
        rpos = ""
    if not str.startswith(rpos,"/"):
        rpos = "/" + rpos
    name = ""
    if not os.path.isdir(p) :
        name = os.path.basename(p)
        rpos = rpos[0:len(rpos)-len(name)-1]
    else:
        name = ""

    return [ rpos,name ]
    
def procsetdir(d:str):
    rpos = psplit(d)
    dhash = gethash(rpos[0])
    de = set.dirent(dhash,rpos[0])
    set.dlist.append(de)
    log.info("DIR: %s HASH: %d", rpos, dhash)

def getbhash(key):
    hash = zlib.crc32(key)
    return hash

def finfo(f:Path):
    ifile = f.open("rb") 
    by = ifile.read()
    crc:int = getbhash(by)
    image = Image.open(io.BytesIO(by))
    tn = image.resize([16,16])
    rgbtn = tn.convert('RGB')
    iby = rgbtn.tobytes()
    #print("len=", len(iby))
    #bp = 0
    #for row in range(1,16):
    #    print("row=", row)
    #    for c in range(1,16):
    #        r = bp + 0
    #        g = bp + 1
    #        b = bp + 2
    #        bp = bp + 3
    #        print(" r=",iby[r]," g=",iby[g]," b=", iby[b])
        

    ifile.close()
    return [ crc, iby ]

def procsetfile(f:Path):
    log.info("PFILE: %s", f)
    rpos = psplit(f)
    ipos = finfo(f)
    dhash = gethash(rpos[0])
    fe = set.filent(dhash,ipos[0],rpos[1])
    ie = set.imgent(ipos[0],ipos[1])
    set.flist.append(fe)
    set.imap[ipos[0]] = ie

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
    set.dtop = Path(files)#.absolutePath() # standardizePath(os.path.abspath(files))

    if not setp.is_dir() :
        log.warn("set dosnt exist, create")
        os.mkdir(setp);

    if not set.dtop.is_dir() :
        log.fatal("top dir %s dosn't exist, exit", set.dtop.str())
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
    set.search(standardizePath(sys.argv[3]))
#    for ci in set.results :
#        print(ci)
    ffile = set.findfile(set.results[0].img.crc)
    if ( ffile != 0 ):
        print("File: " , set.printpath(ffile))

elif ( sys.argv[1] == "-c" and len(sys.argv) == 4 ):
    create(sys.argv[2], sys.argv[3] )

else:
    usage();
