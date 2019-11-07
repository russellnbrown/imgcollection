
#ifdef LINUX

#include <pthread.h>

typedef pthread_t THREADHANDLE;
typedef void* THREADPAR;
typedef void* THREADRETURN;
typedef void* (*THREADFUNC) (void* par);
#define THREADRETURNOK 0

THREADHANDLE thread_start(THREADFUNC, THREADPAR);
void thread_wait(THREADHANDLE);

#else

typedef unsigned int * THREADHANDLE;
typedef void* THREADPAR;
typedef void THREADRETURN;
typedef void (*THREADFUNC) (void* par);
#define THREADRETURNOK

THREADHANDLE thread_start(THREADFUNC, THREADPAR);
void thread_wait(THREADHANDLE);

#endif