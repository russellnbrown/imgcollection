from PIL import Image,ImageDraw,ImageFont
import os
import random
import shutil
import sys

topDir = "C:/TestEnvironments/img/testfiles"
numDir = 5
numFil = 5
numShap = 30

def randcol():
    colour = ( random.randrange(50,255), random.randrange(50,255), random.randrange(50,255) )
    return colour

def fillimg(img):
    draw = ImageDraw.Draw(img)
    width = img.width
    height = img.height
    pad = 5
    for shp in range(1,numShap,1):
        swidth = random.randint(20,200)
        sheight = random.randint(20,200)
        sy = random.randint(pad, height-pad*2-sheight) 
        sx = random.randint(pad, width-pad*2-swidth)
        shape = random.randint(1,2)
        if ( shape == 1):      
            draw.rectangle([(sx,sy),(sx+sheight,sy+swidth)], randcol(), randcol() )
        elif ( shape == 2):      
            draw.ellipse([(sx,sy),(sx+sheight,sy+swidth)],  randcol()  )

def addtext(img):
    draw = ImageDraw.Draw(img)
    fnt = ImageFont.load_default()
    draw.text((10,10),"watermark",randcol(),fnt)

def mkfile(name,istest):
    width = random.randrange(800,1200)
    height = random.randrange(600,1000)
    type = random.choice(['png','jpg'])
    name = name + '.' + type
    background = ( random.randrange(50,255) )
    img = Image.new('RGB', (width,height), randcol() )
    print("making "+name)
    fillimg(img)
    img.save(name)
    #if this is a test file write it out in a few different ways
    if istest:
        if type=="png":
            otype="jpg"
        else:
            otype="png"
        cname = name + '_exactcopy.' + type
        xname = name + '_diffmt.' + otype
        tname = name + '_withtext.' + type
        print("Original: ", name)
        print("Exact copy: ", cname)
        print("Different format: ", xname)
        print("With text: ", tname)
        img.save(cname)
        img.save(xname)
        addtext(img)
        img.save(tname)

     
if len(sys.argv) != 4:
    print("nargd ",len(sys.argv))
    print ("Usage: python createimages.py <location> <number of dirs> <number of files>")
    exit(0)

topDir = sys.argv[1]
numDir = int(sys.argv[2])
numFil = int(sys.argv[3])

print ("Top Directory         : ", topDir )
print ("Number of directories : ", numDir )
print ("Number of files       : ", numFil )


random.seed()
if ( os.path.isdir(topDir) ):
    shutil.rmtree(topDir) 
os.mkdir(topDir)

for d in range(1, numDir+1, 1):
    dir = topDir + "/" + str(d)
    if os.path.isdir(dir):
        os.remove(dir)
    os.mkdir(dir)
    for f in range(1,numFil+1,1):
        fil = str(f)
        file = dir+"/"+"file_"+fil
        mkfile(file,False)
print("Last file re-written in a different formats for test purposes:")
mkfile(file,True)

