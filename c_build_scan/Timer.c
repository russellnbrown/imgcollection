//////////////////////////////////////////////////////////////////////////////
// Timer.c
// =========
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

#include "common.h"

Timer *timer_createx()
{
	Timer* t = (Timer*)malloc(sizeof(Timer));
#if defined(WIN32) || defined(_WIN32)
    QueryPerformanceFrequency(&t->frequency);
    t->startCount.QuadPart = 0;
    t->endCount.QuadPart = 0;
#else
	t->startCount.tv_sec = t->startCount.tv_usec = 0;
	t->endCount.tv_sec = t->endCount.tv_usec = 0;
#endif

	t->stopped = 0;
	t->startTimeInMicroSec = 0;
	t->endTimeInMicroSec = 0;

	return t;
}



void timer_free(Timer *t)
{
}



///////////////////////////////////////////////////////////////////////////////
// start timer.
// startCount will be set at this point.
///////////////////////////////////////////////////////////////////////////////
void timer_start(Timer *t)
{
    t->stopped = 0; // reset stop flag
#if defined(WIN32) || defined(_WIN32)
    QueryPerformanceCounter(&t->startCount);
#else
    gettimeofday(&t->startCount, NULL);
#endif
}



///////////////////////////////////////////////////////////////////////////////
// stop the timer.
// endCount will be set at this point.
///////////////////////////////////////////////////////////////////////////////
void timer_stop(Timer* t)
{
	t->stopped = 1; // set timer stopped flag

#if defined(WIN32) || defined(_WIN32)
    QueryPerformanceCounter(&t->endCount);
#else
    gettimeofday(&t->endCount, NULL);
#endif
}



///////////////////////////////////////////////////////////////////////////////
// compute elapsed time in micro-second resolution.
// other getElapsedTime will call this first, then convert to correspond resolution.
///////////////////////////////////////////////////////////////////////////////
double timer_getElapsedTimeInMicroSec(Timer* t)
{
#if defined(WIN32) || defined(_WIN32)
    if(!t->stopped)
        QueryPerformanceCounter(&t->endCount);

	t->startTimeInMicroSec = t->startCount.QuadPart * (1000000.0 / t->frequency.QuadPart);
	t->endTimeInMicroSec = t->endCount.QuadPart * (1000000.0 / t->frequency.QuadPart);
#else
    if(!t->stopped)
		gettimeofday(&t->endCount, NULL);

	t->startTimeInMicroSec = (t->startCount.tv_sec * 1000000.0) + t->startCount.tv_usec;
	t->endTimeInMicroSec = (t->endCount.tv_sec * 1000000.0) + t->endCount.tv_usec;
#endif

    return t->endTimeInMicroSec - t->startTimeInMicroSec;
}



///////////////////////////////////////////////////////////////////////////////
// divide elapsedTimeInMicroSec by 1000
///////////////////////////////////////////////////////////////////////////////
double timer_getElapsedTimeInMilliSec(Timer* t)
{
    return timer_getElapsedTimeInMicroSec(t) * 0.001;
}



///////////////////////////////////////////////////////////////////////////////
// divide elapsedTimeInMicroSec by 1000000
///////////////////////////////////////////////////////////////////////////////
double timer_getElapsedTimeInSec(Timer* t)
{
    return timer_getElapsedTimeInMicroSec(t) * 0.000001;
}



///////////////////////////////////////////////////////////////////////////////
// same as getElapsedTimeInSec()
///////////////////////////////////////////////////////////////////////////////
double timer_getElapsedTime(Timer* t)
{
    return timer_getElapsedTimeInSec(t);
}
