import java.net.*;
import java.io.*;

/**
 * Main server class
 * 
 * @version 1.0
 * @author Amduz
 */
public class SJWS extends Time{
    //Server specifications
    public static final int SERVER_PORT = 7879;
    public static final int CONNECTIONS_MAX = 3;
    //Threads
    public static Thread currentConnections[] = new Thread[CONNECTIONS_MAX];
    //Database
    public static Database mainDatabase = null;
    //Output colours
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    
    /**
     * Main listening Thread
     * 
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException{
        
        //Database Setup
        try{
            mainDatabase = new Database();
            mainDatabase.connect();
        }catch(Exception sqle){
            System.out.println(ANSI_RED+currentTime() +"Connection to database couldn't be established: "+sqle+ANSI_RESET);
            System.exit(1);
        }
        
        //socket declaration/definition
        ServerSocket ss = null;
        
        try{//ServerSocket open
            ss = new ServerSocket(SERVER_PORT);
            System.out.println(currentTime() +"Port open.");
        }catch(IOException ioe){
            System.out.println(ANSI_RED+currentTime() +"Couldn't open port. (Port may be already opened): "+ioe+ANSI_RESET);
            System.exit(1);
        }
        
        //Begin listening
        boolean listening = true;
        while(listening){
            listen(ss);//listen on ServerSocket ss
        }
    }
    
    /**
     * Listening to TCP method
     * 
     * @param socket ServerSocket on which the server listens for new connection requests
     */
    public static void listen(ServerSocket socket){
        Socket currentClient = null;
        
        System.out.println(ANSI_BLUE+currentTime() + "Server listening on port: "+ socket.getLocalPort()+ANSI_RESET);
        try{//waiting for a connection
            currentClient = socket.accept();
            System.out.println(ANSI_GREEN+currentTime() +"Successfully connected to: "+currentClient+ANSI_RESET);
        }catch(IOException ioe){
            System.out.println(ANSI_RED+currentTime() +"Failed to accept connection: "+ioe+ANSI_RESET);
        }
        
        //if server not full, create a new connection thread
        if(ConnectionThreadInterface.getThreadsRunning() < CONNECTIONS_MAX){
            createConnection(currentClient);
        }else{//When server full, respond that server busy
            respondBusy(currentClient);
        }
        
        System.out.println("Current running connections: "+ConnectionThreadInterface.getThreadsRunning());
    }
    
    /**
     * Busy reply only method
     * 
     * @param client client socket to respond to
     */
    public static void respondBusy(Socket client){
        try{
            //begins a thread with the busy flag set to true
            ConnectionThreadInterface busy = new ConnectionThreadInterface(client, true);
            Thread busyThread = new Thread(busy);
            busyThread.start();
            
        }catch(Exception e){
            System.out.println(ANSI_RED+currentTime() +"Error creating busyThread: "+e+ANSI_RESET);
        }
    }
    
    /**
     * General connection method
     *
     * @param clientclient socket to respond to
     */
    public static void createConnection(Socket client){
        try{
            //Creates a the normal thread which enables all database functions
            ConnectionThreadInterface connection = new ConnectionThreadInterface(client, mainDatabase);
            //Each active thread added to the allocated thread array
            currentConnections[ConnectionThreadInterface.getThreadsRunning()] = new Thread(connection);
            currentConnections[ConnectionThreadInterface.getThreadsRunning()].start();
            System.out.println(currentTime() +"Thread created.");
        }catch(Exception e){
            System.out.println(ANSI_RED+currentTime() +"Error creating connectionThread: "+e+ANSI_RESET);
        }
    }
    
}
