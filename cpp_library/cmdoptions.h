
#pragma once
#include <string>
#include <vector>
#include <map>
#include <sstream>
#include <optional>

using namespace std;


class CmdOptions
{
public:


	CmdOptions(int argc, char* argv[])
	{
		string setmap = "";
		for (int c = 0; c < argc; c++)
		{
			string item = string(argv[c]);
			string next = "";
			if ( c < (argc-1) )
				next = string(argv[c + 1]);

			all.push_back(item);
			if (item[0] == '-')
			{
				if (next.length() >0 && next[0] == '-' )
					next="";
				flags[item] = next;
			}
		}
	}

	optional<string> getflag(string flag) { if (flags.find(flag) != flags.end()) return flags[flag]; return nullopt; }
	vector<string> get() {	return all; }

private:

	map<string, string> flags;
	vector<string> all;

};

