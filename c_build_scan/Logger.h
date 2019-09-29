
void logto(char* fname);
enum LogLevel { Raw, Debug, Info, Warn, Error, Fatal };
void logger(enum LogLevel ll, char *fmt, ...);

