CC=cc
CFLAGS= -O3 -DLINUX

%.o: %.c
	$(CC) -c -o $@ $< $(CFLAGS)
 
c_build_search: c_build_search.o utils.o Set.o SetBuilder.o SetSearch.o Logger.o ImgUtils.o Timer.o bst.o mutex.o thread.o
	$(CC) -o c_build_search utils.o c_build_search.o SetBuilder.o SetSearch.o Set.o Logger.o ImgUtils.o Timer.o bst.o mutex.o thread.o -lfreeimage -lpthread

clean:
	rm *.o c_build_search


#c_build_search.c
#SetBuilder.c
#Set.c
#SetSearch.c
#utils.c
