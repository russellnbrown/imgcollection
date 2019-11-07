
#include "common.h"

#ifdef LINUX


THREADHANDLE thread_start(THREADFUNC f, THREADPAR p)
{
	THREADHANDLE threadId;
	threadId = pthread_create(&threadId, NULL, f, p );
	return threadId;
}

void thread_wait(THREADHANDLE th)
{
	pthread_join(th, NULL);
}

#else

THREADHANDLE thread_start(THREADFUNC f, THREADPAR p)
{
	unsigned int threadId;
	_beginthreadex(NULL, 0, (void*)f, p, 0, &threadId);
	return (THREADHANDLE)threadId;
}

void thread_wait(THREADHANDLE th)
{
	HANDLE h = (HANDLE)th;
	WaitForSingleObject(h, INFINITE);
}

#endif
