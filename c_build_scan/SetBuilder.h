/*
 * Copyright (C) 2019 russell brown
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

 //
 // SetBuilder.h - methods to create the 'Image Database'
 //

void setbuild_create(char*, char*, BOOL);
int setbuild_winscan(const char*);
uint32_t setbuild_processDirectory(const char*);
int setbuild_processImageFile(uint32_t, const char*);
void setbuild_waitthreads();
void setbuild_startthreads();
THREADRETURN setbuild_threadRun(THREADPAR);
void setbuild_addToThreadQ(uint32_t, const char*);