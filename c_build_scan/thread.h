
typedef void* THREADHANDLE;
typedef void* THREADPAR;
typedef void THREADRETURN;
typedef void (*THREADFUNC) (const void* par);

THREADHANDLE thread_start(THREADFUNC, THREADPAR);
void thread_wait(THREADHANDLE);
