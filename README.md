# ClassLoadLogger

An implementation of Java Agent to record class loading events.

Compile and export the files as a jar, and execute your program with the agent.
The agentt accepts a log file name.

        java -javaagent:ClassLoadLogger.jar=logfile.txt YourApp [args for your app]

