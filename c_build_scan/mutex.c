#include "common.h"

#ifdef LINUX

MUTEXHANDLE mutex_get()
{
  MUTEXHANDLE t = malloc(sizeof(pthread_mutex_t ));
  memset(t,0,sizeof(pthread_mutex_t ));
  return t;
}

void mutex_lock(MUTEXHANDLE h)
{
	pthread_mutex_lock(h); 
}

void mutex_unlock(MUTEXHANDLE h)
{
	pthread_mutex_unlock(h); 
}

void mutex_free(MUTEXHANDLE h)
{

}

#else

#include <Windows.h>

MUTEXHANDLE mutex_get()
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