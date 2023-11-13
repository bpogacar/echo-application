import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;


public class echoClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int numProbes;
    private int payloadSize;
    private String measurementType = "";

    public void startConnection(String ip, int port) {
        try {
            System.out.println("Starting Client");

            // initiate the client elements
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // enter the connection phase
            System.out.println("Entering Connection Phase");
            this.connectionPhase();
            
            // check if the client received the OK message from the server, if not there was not an error, otherwise proceed with the operation
            String response = in.readLine();
            if (!(response.equals("200 OK: Ready"))) {
                this.stopConnection();
            }
            System.out.println(response);

            // enter the measurement phase
            System.out.println("Entering Measurement Phase");
            this.measurementPhase();

            // if we are here, then the client did not stop during the measurement phase, meaning no error message was received, proceed with the termination phase
            System.out.println("Terminating Connection...");
            this.terminate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Client-side protocol for the connection setup phase; getting the correct inputs, creating the send message, and sending it to the connected server
    private void connectionPhase() {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String phase = "s";  // default to 's' since we are in the connection phase
        
        // get the measurement type from the user, ensuring one of the two desired inputs
        try {
            while (!(this.measurementType.equals("rtt")) && !(this.measurementType.equals("tput"))) {
                System.out.println("To measure throughput type 'rtt'. To measure throughput type 'tput'.");
                this.measurementType = input.readLine();
            }
        }
        catch(Exception e) {e.printStackTrace();}
       
        // get the number of probes from the user, ensuring a valid integer value. Save this value to the client for later.
        System.out.println("Specify the number of probes:");
        String numProbes = this.getIntegerString(input);
        this.numProbes = Integer.valueOf(numProbes);  // NOTE: this.numProbes is different from the local "numProbes" variable

        // get the number of bytes in the payload from the user, ensuring a valid integer value. Save this value to the client for later.
        System.out.println("Specify the number of bytes in the payload:");
        String mSize = this.getIntegerString(input);
        this.payloadSize = Integer.valueOf(mSize);

        // get the amount of delay from the user, ensuring a valid integer value
        System.out.println("Specify the desired amount of delay in milliseconds:");
        String delay = this.getIntegerString(input);

        // build message to send to the server in the specified format and then send it 
        String messageToServer = phase + " " + this.measurementType + " " + numProbes + " " + mSize + " " + delay;
        out.println(messageToServer);
    }

    // Client-side protocol for the measurement phase; sending the correct number of probe messages
    private void measurementPhase() {
        String phase = "m"; // default to 'm' since we are in the measurement phase
        double totalTime = 0;

        // create an appropraitly sized payload accounting for the fact that Java uses UTF-16 encoding instead of UTF-8...
        StringBuilder payloadBuilder = new StringBuilder();
        for (int i = 0; i < this.payloadSize; i++) {
            payloadBuilder.append("a");
        }
        byte[] payloadBytes = payloadBuilder.toString().getBytes(StandardCharsets.UTF_8);
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);

        // send numProbes messages to the server, containing the designated payload size
        for (int seqNum = 1; seqNum <= this.numProbes; seqNum++) {
            String message = phase + " " + seqNum + " " + payload;  // create message to send to the server with proper formatting
            Long startTime = System.currentTimeMillis(); // START TIMER
            out.println(message);
            try{  // print out the echo'd message recieved from the server, or terminate if it recieves an error message
                String response = in.readLine();

                Long endTime = System.currentTimeMillis(); // END TIMER
                Long duration = endTime - startTime;
                totalTime += duration;  // increase total time to find mean RTT
                if (duration == 0) {  // some smaller messages send too fast, resulting in division by 0, in this case I divide by 1 instead
                    duration = 1L;  // this edge case only seems to happen locally since on the server sending 1 byte of data I haven't gotten less than 3ms for duration
                }                  // it also only matters for printing the tput for EACH individual probe, the overall tput does not use this number
                double tput = payloadSize / duration; // define throughput in bytes per millisecond, which is equivalent to kilobytes per second

                // print the mean for the given measurement type
                if (this.measurementType.equals("rtt")) {
                    System.out.println("RTT: " + duration + " milliseconds.");
                } else if (this.measurementType.equals("tput")) { 
                    System.out.println("Throughput: " + tput + " KBps.");
                }

                // if at any point we recieve an error, terminate the connection
                if (response.equals("404 ERROR: Invalid Measurement Message")) {
                    this.stopConnection();
                }
                System.out.println(response);
            }   
            catch (Exception e) {e.printStackTrace();}
        }

        // record the appropriate measurement
        if (this.measurementType.equals("rtt")) {
            double RTT = totalTime / numProbes;   // to calculate rtt do: divide the totaltime by the number of probes to average the rtt for each
            System.out.println("The mean RTT is: " + RTT + " milliseconds.");
        } else if (this.measurementType.equals("tput")) {
            double TPUT = (payloadSize * numProbes) / totalTime;  // to calculate throughput do: payload * numprobes to get total payload, then divide by total time
            System.out.println("The mean Throughput is: " + TPUT + " KBps.");
        }
    }

    // Client-side protocol for the termination phase; ending the connection
    private void terminate() {
        String phase = "t"; // default phase to 't' since we are in the termination phase

        String message = phase; // create message to send to the server with proper formatting
        out.println(message);
        try{
            String response = in.readLine();
            System.out.println(response);
        }
        catch (Exception e) {e.printStackTrace();};
        this.stopConnection();   // we are done! terminate the connection (the server will have terminated regardless of 404 or 200 response message)
        System.out.println("Connection Closed.");
    }

    // get a string input from the user, but make sure that it is a valid integer before returning the string, keep repeating until we get one
    private String getIntegerString(BufferedReader input) {
        String str = "";
        boolean isInteger = false;
        while(!isInteger) {
            try {str = input.readLine();}
            catch(Exception e) {e.printStackTrace();}
            try {
                Integer.valueOf(str);
                isInteger = true;
            }
            catch (NumberFormatException e) {System.out.println("Please make sure the input is a valid integer");}
        }
        return str;
    }

    // stop the connection to the server
    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    // start up the client, acccepting valid IP and port number inputs
    public static void main(String[] args) {
        if (args.length == 0 || args.length == 1) {
            System.out.println("You need to include the ip and port as arguments.");
            return;
        } else if (args.length > 2) {
            System.out.println("You have too many arguements, only include the ip and port.");
            return;
        }

        echoClient server = new echoClient();
        String ip = args[0];
        try {
            int port = Integer.valueOf(args[1]);
            if (port < 58000 || port > 58999) {
                System.out.println("The port number needs to be between 58000 and 59000.");
                return;
            }
            server.startConnection(ip, port); 
        } catch (Exception e) {
            System.out.println("The port number has to come second, and needs to be an integer.");
        }
    }
}