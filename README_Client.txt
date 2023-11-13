Hello there!! 

Make sure you have the echoClient.java file installed somewhere on your device.
Go into the command prompt or some equivalent terminal and navigate to the folder containing echoClient.java.
The basic commands for navigation are "cd" and "ls":
You can use 
$ ls
to see what files are available in the currrent directory that you are working in and from there you can do:
$ cd "filename"
to enter into one of the files (do not include the quotations).
Follow these basic steps to get into the right folder and then proceed.

To run the file first compile it using the most recent java version (as of 10/11/2023) via this command:
$ javac echoClient.java

Upon completion with no errors, you should see a new file called echoClient.class appear in the current folder.
If this does not happen try re-installing java and try again.

Then, proceed to run the file using two arguments:
$ java echoClient {arg 1} {arg 2}

{arg 1} should be the machine where echoServer is located. In my case this is csa2.bu.edu, if both the client and
server are being run on the same machine (i.e. your home computer) use "localhost" (with no quotations).
{arg 2} should be the port through which you will connect to the server, this should correspond to whichever 
port the server is currently running on* but will always be between [58000-59000).

* make sure the server is waiting for a connection first BEFORE running the client file

From there just follow the input prompts by typing into the terminal.

Other notes:
rtt = round trip time, which is how long it takes to send and recieve a message
tput = throughput, which is how much data can be sent each second in kilobytes
number of probes = number of times you want to repeat each message during the experiment

Good luck!!