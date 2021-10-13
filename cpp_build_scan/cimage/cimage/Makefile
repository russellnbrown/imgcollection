#CC=g++-9 -std=c++17
CC=g++-10 --std=c++17
CFLAGS= -O3 -DLINUX -DFREEIMAGE_LIB
LDFLAGS=-L/home/usere/Sources/FreeImage/Dist -lfreeimage -lfreeimageplus -lm 

%.o: %.cpp
	$(CC) -c -o $@ $< $(CFLAGS)
 
cimage: cimage.o ImgCollectionBuilder.o ImgUtils.o logger.o Timer.o ImgCollectionSearch.o
	$(CC) -o cimage cimage.o ImgCollectionBuilder.o ImgUtils.o logger.o ImgCollectionSearch.o Timer.o $(LDFLAGS) -lpthread

clean:
	rm *.o cimage

