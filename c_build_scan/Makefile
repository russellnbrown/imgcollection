CC=cc
CFLAGS= 

%.o: %.c
	$(CC) -c -o $@ $< $(CFLAGS)
 
c_build_search: c_build_search.o utils.o Set.o SetBuilder.o SetSearch.o Logger.o ImgUtils.o hashmap.o
	$(CC) -o c_build_search utils.o c_build_search.o SetBuilder.o SetSearch.o Set.o Logger.o ImgUtils.o hashmap.o -lfreeimage

clean:
	rm *.o c_build_search


#c_build_search.c
#SetBuilder.c
#Set.c
#SetSearch.c
#utils.c
