Hello there!! 

Make sure you have the echoServer.java file installed somewhere on your device.
Go into the command prompt or some equivalent terminal and navigate to the folder containing echoServer.java
(For more information on how to do this see README_Client.txt)

To run the file first compile it using the most recent java version (as of 10/11/2023) via this command:
$ javac echoServer.java
(For troubleshooting see README_Client.txt)

Then, proceed to run the file using one argument:
$ java echoServer {arg 1}

{arg 1} should be the port through which you will connect to the client, this should correspond to whichever 
port the client will run on but will always be between [58000-59000).

Once this is done the server will wait for inputs from the client and run properly, you do not need to do anything else.

Good luck!!