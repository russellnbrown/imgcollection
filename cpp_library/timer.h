
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
// Timer
//
// used to provide timings of various operations
// call start(tag),stop[,start(tag2),stop],report
//
class Timer
{

private:

	struct timeritem
	{
		std::string name;
		std::chrono::duration<float> took;
	};
	static std::list<timeritem> items;
	static std::chrono::system_clock::time_point started;

public:

	static void start()
	{
		started = std::chrono::system_clock::now();
	}
	static void stop(const std::string &section)
	{
		timeritem itm;
		itm.name = section;
		itm.took = std::chrono::system_clock::now() - started;
		items.push_back(itm);
	}

	static std::string report()
	{
		std::stringstream ss;
		ss << "Timings:-\n";
		for (auto i : items)
		{
			ss << i.name << " = " << std::to_string(std::chrono::duration_cast<std::chrono::milliseconds>(i.took).count()) << std::endl;
		}
		return ss.str();
	}

};




