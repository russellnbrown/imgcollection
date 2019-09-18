from typing import Dict, List
import sys
import os
import io
import struct
import logging
import zlib
import set
import time
from PIL import Image,ImageDraw,ImageFont
from pathlib import Path,PurePosixPath
import cfg

log:logging.Logger
log = logging.getLogger("pimage")

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
    #log.debug("DIR: %s HASH: %d", rpos, dhash)

#
# getbhash - returns crc32 of a byte array ( the file content )
#
def getbhash(key):
    hash = zlib.crc32(key)
    return hash

#
# gethash - returns crc32 of a string
#
def gethash(pstr):
    b = pstr.encode('utf-8')
    hash = zlib.crc32(b)
    return hash
    hash:long = 5381;
    l = len(pstr)
    for cx in pstr:
        hash = ((hash << 5) + hash) + ord(cx)
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

#
# procsetfile - called by file walker. creates a 'fileent' & 'imgent' and adds to the relevant list
#
def procsetfile(f:Path):
    fi = finfo(f)
    #log.debug("Processing file %s", fi)
    fe = cfg.filent(fi.dhash, fi.ihash, fi.name)
    ie = cfg.imgent(fi.ihash, fi.tmb)
    cfg.flist.append(fe)
    cfg.imap[ie.ihash] = ie

#
# file_walker - goes through all files in a directory calling procsetfile or procsetdir
# depending on it it is a file or a directory. calls itself recursivly for 
#
def file_walker(walk):
    procsetdir(walk)
    
    for item in walk.iterdir():
        if item.is_dir():
            file_walker(item)
        else:
            procsetfile(item)

#
# create - creates & saves the image collection
#
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

    etime = cfg.Timer()
    etime.start()
    file_walker(filesp)
    etime.stop()
    scantime = etime.elapsed_ms()

    etime.start()
    set.save(setp)
    etime.stop()
    savetime = etime.elapsed_ms()

    log.info("Timers: scan=" +  str(scantime)  + ", save=" +  str(savetime)  )


def search(set1, file):

    etime = cfg.Timer()

    #load set
    etime.start()
    set.load(Path(set1))
    etime.stop()
    loadtime = etime.elapsed_ms()

    # get imgfileinfo for file to be searched
    f = standardizePath(file)
    fi = finfo(Path(f))

    # search collection for matching files
    etime.start()
    results = set.search(fi)
    etime.stop()
    searchtime = etime.elapsed_ms()


    # print out results
    if ( len(results) == 0 ):
        print("No images found")
        exit(0)
    for ci in results :
        fi = set.findfile(ci.img.ihash)
        if ( fi != 0 ):
            log.info("Img " + str(ci.img.ihash) + ", cls=" + str(ci.close) + ", file=" + set.findfile(ci.img.ihash).fname)
        else:
            log.info("Img " + str(ci.img.ihash) + ", cls=" + str(ci.close) + ", file=?")

    log.info("Timinigs: load=" + str(loadtime) + ", search=" + str(searchtime) )

# - pftt   
def usage():
    log.info("Usage: pimage.py -c <set> <top> <files> | -s <set> <file>")
    exit(0)

#
# Start Here
#

setlogging()

if ( len(sys.argv) < 3 ):
    usage()

# check command args to see what to do...

# search - load the specified iimg collection
if ( sys.argv[1] == "-s" and len(sys.argv) == 4 ):
    search(sys.argv[2], sys.argv[3] )
# create
elif ( sys.argv[1] == "-c" and len(sys.argv) == 4 ):
    create(sys.argv[2], sys.argv[3] )

else:
    usage();
