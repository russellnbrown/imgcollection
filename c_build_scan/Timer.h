//////////////////////////////////////////////////////////////////////////////
// Timer.h
// =======
// High Resolution Timer.
// This timer is able to measure the elapsed time with 1 micro-second accuracy
// in both Windows, Linux and Unix system 
//
//  AUTHOR: Song Ho Ahn (song.ahn@gmail.com)
// CREATED: 2003-01-13
// UPDATED: 2017-03-30
//
// Copyright (c) 2003 Song Ho Ahn
//////////////////////////////////////////////////////////////////////////////


#if defined(WIN32) || defined(_WIN32)   // Windows system specific
#include <windows.h>
#else          // Unix based system specific
#include <sys/time.h>
#endif


typedef struct _Timer
{

	double startTimeInMicroSec;                 // starting time in micro-second
	double endTimeInMicroSec;                   // ending time in micro-second
	int    stopped;                             // stop flag 
#if defined(WIN32) || defined(_WIN32)
	LARGE_INTEGER frequency;                    // ticks per second
	LARGE_INTEGER startCount;                   //
	LARGE_INTEGER endCount;                     //
#else
	struct timeval startCount;                         //
	struct timeval endCount;                           //
#endif
}Timer;

Timer*  timer_createx();
void	timer_free();
void	timer_start(Timer*);                             // start timer
void	timer_stop(Timer*);                              // stop the timer
double	timer_getElapsedTime(Timer*);                    // get elapsed time in second
double	timer_getElapsedTimeInSec(Timer*);               // get elapsed time in second (same as getElapsedTime)
double	timer_getElapsedTimeInMilliSec(Timer*);          // get elapsed time in milli-second
double	timer_getElapsedTimeInMicroSec(Timer*);          // get elapsed time in micro-second

