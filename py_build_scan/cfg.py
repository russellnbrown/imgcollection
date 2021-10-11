#
# Copyright (C) 2019 russell brown
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

from typing import Dict, List
from pathlib import Path
import struct
import math
from datetime import datetime
from time import perf_counter

tns:int = 16
tnm:int = tns*tns*3

#
# dirent - class to hold information about a directory
#
class dirent(object):
    dhash:int = 0       # hash of path, used as key by filent
    dpath:str = ""      # the path ( relative to top )
    dmod:datetime
    def __init__(self, _dh:int, _dp:str, _dmt:str):
        self.dhash=_dh
        self.dpath=_dp
        #self.dmod = datetime.strptime(_dmt,"%c")
        self.dmod = datetime.strptime(_dmt,"%d/%m/%Y %I:%M:%S %p")

    def __str__(self):
        return "dilent(dh="+str(self.dhash)+", dp="+str(self.dpath)+", mod="+str(self.dmod)+")"

#
# fileent - class to hold information about a file
#
class filent(object):
	dhash:int=0     # the key to the directory it is in ( see dirent )
	ihash:int=0     # the key to its image ( see imgent )
	fname:str=""    # the file anme
	def __init__(self, _dh:int, _ih:int, _fn:str):
		self.dhash=_dh
		self.ihash=_ih
		self.fname=_fn

	def __str__(self):
		return "filent(dh="+str(self.dhash)+", ih="+str(self.ihash)+", fn="+str(self.fname)+")"

# 
# imgent - class to hold information about an image
#
class imgent(object):
    ihash:int=0     # its key ( used in fileent )
    tmb:bytearray   # RGB thumbnail
    def __init__(self,  _ihash:int, _tn:bytearray):
        self.ihash = _ihash
        self.tmb = _tn

    def __str__(self):
        bl = len(self.tmb)
        return "imgent(ih="+str(self.ihash)+", tlen=" + str(bl) +")"

#
# comapreditem - class to hold comparison result between an image and the search image
#
class compareditem(object):
    img:imgent      # key to imgent
    close:float=0   # how close a match it is

    def __init__(self,  _img:imgent, _close:float):
        self.img = _img
        self.close = _close

    def __str__(self):
        return "ci(im="+str(self.img.ihash)+", cl="+str(self.close)+")"

#
# These structures form the 'database'
#
dtop:Path                       # top level directory - absolute path, all dirent rel to this
dlist:List[dirent] = []         # list of dirents
flist:List[filent] = []         # list of fileents
imap:Dict[int,imgent] = {}      # map of imgent ( allows us to easilly detect dups )


# https://www.geeksforgeeks.org/python-how-to-time-the-program/
class Timer: 
	
    def __init__(self): 
        self.elapsed = 0.0
        self.started = 0.0
        self.end = 0.0

    # starting the module 
    def start(self): 
        self.started = perf_counter() 

    # stopping the timmer 
    def stop(self): 
        self.end = perf_counter() 
        self.elapsed = self.end - self.started

    def elapsed_ns(self):
        return self.elapsed

    def elapsed_ms(self):
        ms = self.elapsed*1000.0
        return int(ms)


