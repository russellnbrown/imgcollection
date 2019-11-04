
#include "common.h"

#ifdef LINUX

#else

THREADHANDLE thread_start(THREADFUNC f, THREADPAR p)
{
	HANDLE threadId;
	CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)f, p, 0, &threadId);
	return (THREADHANDLE)threadId;
}

void thread_wait(THREADHANDLE th)
{
	HANDLE h = (HANDLE)th;
	WaitForSingleObject(h, INFINITE);
}

#endif
