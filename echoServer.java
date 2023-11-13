import java.net.*;
import java.util.concurrent.TimeUnit;
import java.io.*;


public class echoServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int numProbes;
    private int delay;

    public void start(int port) {
        System.out.println("Server Started");
        try {
            System.out.println("Waiting for Client to connect ... ");
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            System.out.println("Connection Established");
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // enter the connection phase
            this.connectionPhase();

            // enter the measurement phase
            this.measurementPhase();

            // enter the termination phase
            this.terminationPhase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Server-side protocol for the connection phase, recieve a valid connection message and set up server properties according to the information
    // respond back OK when completed, ERROR if the message was invalid
    private void connectionPhase() {
        try{
            // recieve connection message from client and parse it into a list of tokens
            String message = in.readLine();
            String[] fromClient = message.split(" ");

            // check that the message phase is indeed for connection setup and contains the right number of arguements
            if (!fromClient[0].equals("s") || fromClient.length != 5) {
                out.println("404 ERROR: Invalid Connection Setup Message");
                this.stop();
            }

            // the validity of the tokens was already checked by the client before sending, so now we can add the variables to the server
            this.numProbes = Integer.valueOf(fromClient[2]);
            this.delay = Integer.valueOf(fromClient[4]);

            // done! we have completed the connection phase with no erorrs, return the correct message to the client
            out.println("200 OK: Ready");
         }
        catch (Exception e) {e.getStackTrace();}
    }

    // Server-side protocol for the measurement phase, recieve probe messages from the client, validate them, and then echo them back
    private void measurementPhase() {
        int iter = 1;
        while(iter <= this.numProbes) {     // recieve a certain number of messages equal to the number of probes specified in the connection message
            try{
                // recieve connection message from client and parse it into a list of tokens
                String message = in.readLine();
                String[] fromClient = message.split(" ");

                // retrieve the sequence number from the message
                int seqNumber = Integer.valueOf(fromClient[1]);

                // check that the message phase is indeed for measurement phase, contains the right number of arguements, and the correct sequence number
                if (!fromClient[0].equals("m") || fromClient.length != 3 || iter != seqNumber) {
                    out.println("404 ERROR: Invalid Measurement Message");
                    this.stop();
                }

                // if get here, we didn't send the error message, so echo back the probe after waiting the designated delay time
                TimeUnit.MILLISECONDS.sleep(this.delay);
                out.println(message);

                // increment iter
                iter++;
            }
            catch (Exception e) {e.getStackTrace();}
        }
    }

    // Server-side protocol for the termination phase, recieve terminate message from the client, respond OK or ERROR, and close
    private void terminationPhase() {
        try{
            // recieve connection message from client and parse it into a list of tokens
            String message = in.readLine();
            // if (message.equals("")) {   // for SOME REASON there is a blank message that gets read first before the terminate message, so this loop gets rid of that
            //     message = in.readLine();
            // }   
            String[] fromClient = message.split(" ");

            // check that the message phase is indeed for the termination phase and contains the right number of arguements
            if (!fromClient[0].equals("t") || fromClient.length != 1) {
                out.println("404 ERROR: Invalid Connection Termination Message");
            } else {
                out.println("200 OK: Closing Connection");
            }
            
            // regardless of the message sent back to the client, terminate the connection
            this.stop();
        }
        catch (Exception e) {e.getStackTrace();}
    }

    // stop the server
    public void stop() {
        try {
            // in.close();
            // out.close();
            // clientSocket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // start up the server with a valid port number as an arguement
    public static void main(String[] args) {
        try {
            if (args.length > 1) { 
                System.out.println("You need to have ONLY the port number as a single argument.");
            }
            echoServer server = new echoServer();
            int port = Integer.valueOf(args[0]);
            if (port < 58000 || port > 58999) {
            System.out.println("The port number needs to be between 58000 and 59000.");
            return;
            }
            server.start(port);
        } catch (Exception e) {
            System.out.println("You need to include the port number as an argument.");
        }
    }

}

