
typedef  void*  MUTEXHANDLE;
MUTEXHANDLE mutex_get();
void mutex_lock(HANDLE);
void mutex_release(HANDLE);
void mutex_free(HANDLE);
