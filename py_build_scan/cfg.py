from typing import Dict, List
from pathlib import Path
import struct
import time

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


dtop:Path
dlist:List[dirent] = []
flist:List[filent] = []
imap:Dict[int,imgent] = {}


# https://www.geeksforgeeks.org/python-how-to-time-the-program/
class Timer: 
	
    def __init__(self, func = time.perf_counter): 
        self.elapsed = 0.0
        self._func = func 
        self._start = None

    # starting the module 
    def start(self): 
        if self._start is not None: 
            raise RuntimeError('Already started') 
        self._start = self._func() 

    # stopping the timmer 
    def stop(self): 
        if self._start is None: 
            raise RuntimeError('Not started') 
        end = self._func() 
        self.elapsed += end - self._start 
        self._start = None

    def reset(self): 
        self.elapsed = 0.0

    def __enter__(self): 
        self.start() 
        return self

    def __exit__(self, *args): 
        self.stop() 
