
#ifdef LINUX

typedef  pthread_mutex_t * MUTEXHANDLE;
MUTEXHANDLE mutex_get();
void mutex_lock(MUTEXHANDLE);
void mutex_unlock(MUTEXHANDLE);
void mutex_release(MUTEXHANDLE);
void mutex_free(MUTEXHANDLE);

#else

typedef  HANDLE MUTEXHANDLE;
MUTEXHANDLE mutex_get();
void mutex_lock(MUTEXHANDLE);
void mutex_unlock(MUTEXHANDLE);
void mutex_release(MUTEXHANDLE);
void mutex_free(MUTEXHANDLE);

#endif