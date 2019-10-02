CC=g++-9 -std=c++17
CFLAGS= -O3

%.o: %.cpp
	$(CC) -c -o $@ $< $(CFLAGS)
 
cimage: cimage.o ImgCollectionBuilder.o ImgUtils.o logger.o Timer.o
	$(CC) -o cimage cimage.o ImgCollectionBuilder.o ImgUtils.o logger.o Timer.o -lfreeimageplus -lpthread

clean:
	rm *.o cimage

