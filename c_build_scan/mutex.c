#include "common.h"

#ifdef LINUX

#else

#include <Windows.h>

HANDLE mutex_get()
{
	PSRWLOCK lock = malloc(sizeof(RTL_SRWLOCK));
	InitializeSRWLock(lock);
	return lock;
}

void mutex_lock(HANDLE h)
{
	AcquireSRWLockExclusive(h);
}

void mutex_unlock(HANDLE h)
{
	ReleaseSRWLockExclusive(h);
}

void mutex_free(HANDLE h)
{
	ReleaseSRWLockExclusive(h);
	free((RTL_SRWLOCK*)h);
}

#endif