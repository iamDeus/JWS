import java.sql.*;

import java.net.*;
import java.io.*;

/**
 * Running connection class
 * 
 * @version 1.0
 * @author Amduz
 */
public class ConnectionThreadInterface extends Time implements Runnable{
    //Thread management fields
    public static int threadsRunning = 0;
    public static int connectionsMade = 0;
    private boolean status = false;
    private final int threadID = connectionsMade;
    private final String THREAD_START = currentTime();
    //Client fields
    private Socket client = null;
    private BufferedReader input = null;
    private PrintWriter output = null;
    //Server fields
    private boolean serverBusy = false;
    //Database fields
    private Database mainDatabase = null;
    //Colour fields
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    
    
    /**
     * Main Constructor
     * 
     * @param client The connected socket.
     * @param database The database this thread is to interact with.
     */
    public ConnectionThreadInterface(Socket client, Database database){
        this.client = client;
        this.mainDatabase = database;
    }
    
    /**
     * Server Busy Constructor
     * 
     * @param client The connected socket.
     * @param serverBusy Server status. (flagged true when no space for more threads)
     */
    public ConnectionThreadInterface(Socket client, boolean serverBusy){
        this.client = client;
        this.serverBusy = serverBusy;
    }
    
    @Override
    public void run(){
        connectionsMade++;
        threadsRunning++;
        status = true;
        
        while(status){//allows to be switched on or off within loop
            try{
                System.out.println(currentTime() +"Thread running.["+threadID+"]");
                //Input/Output Stream initialisation
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                output = new PrintWriter(client.getOutputStream(), true);
                System.out.println(currentTime() +"Streams setup.["+threadID+"]");
                
                String statement = null;
                ResultSet rs = null;
                String htmlHeader = "";
                String htmlContent = "";

                if(serverBusy != true){//Disable database functionality if serverBusy flag set to true
                    
                    String clientRequestHeader = receive();
                    
                    //try{ Thread.sleep(10000); }catch(InterruptedException e){}//TESTING
                    
                    if(clientRequestHeader.contains("/getStudents")){//query database if correct request made HTTP/1.1 200 OK
                        htmlHeader = "HTTP/1.1 200 OK\n\n";
                        String studentType = "";
                        //seperating the student type from the request
                        studentType = clientRequestHeader.replaceFirst(" HTTP/1.1","").replaceFirst("GET /getStudents=","");
                        
                        //decidinig what statement to query
                        if(clientRequestHeader.contains("=") == false){//if type isnt specified, create statement for all
                            statement = "SELECT * FROM students"; //Statement retrieves ALL students
                        }else{
                            statement = "SELECT * FROM students "+"WHERE course=\'"+studentType+"\'"; //Statement which retrieves [TYPE] students
                        }
                        //receiving results
                        rs = mainDatabase.sendStatement(statement); //and receive ResultSet
                        System.out.println(ANSI_GREEN+currentTime() +"Result set received.["+threadID+"]"+ANSI_RESET);

                        //formatting results into html
                        htmlContent = formatToHTML(rs);
                    }else{//wrong request input responds with 404 error
                        htmlHeader = "HTTP/1.1 404 Not Found\n\n";
                        htmlContent = "<html>\n"
                                + "<body>\n"
                                + "<h1>Page not found!</h1>"
                                + "<h4>By Amadeusz Misiak</h4>"
                                + "<h5>" + currentTime() + "</h5>"
                                + "</body>\n"
                                + "</html>";
                    }                    
                    
                }else{ //HTTP/1.1 503 Service Unavailable
                    htmlHeader = "HTTP/1.1 503 Service Unavailable\n\n";
                    htmlContent = "<html>\n"
                            + "<body>\n"
                            + "<h1>Server currently busy!</h1>"
                            + "<h4>By Amadeusz Misiak</h4>"
                            + "<h5>" + currentTime() + "</h5>"
                            + "</body>\n"
                            + "</html>";
                }
                System.out.println(currentTime()+"SERVER RESPONSE:\n"+htmlHeader+htmlContent);
                
                //Once hmtlHeader and htmlContent filled out, send back to client and close streams
                send(htmlHeader, htmlContent);
                
            }catch(Exception e){
                System.out.println(ANSI_RED+currentTime() +"Exception in thread ["+threadID+"]: "+e+ANSI_RESET);
            }
            //thread status set to off
            status = false;
        }
        threadsRunning--;
    }
    
    public void send(String htmlHeader, String htmlContent){
        try{
            output.write(htmlHeader + htmlContent);
            output.flush();
            System.out.println(ANSI_YELLOW+currentTime()+"Response sent.["+threadID+"]"+ANSI_RESET);
            //close streams
            output.close();
            input.close();
        }catch(IOException ioe){
            System.out.println(ANSI_RED+currentTime() +"Response couldn't be sent. ["+threadID+"]: "+ioe+ANSI_RESET);
        }
        
    }
    
    /**
     * Receive method.
     * 
     * the input is used to receive the request
     * 
     * @param input
     * @return
     */
    public String receive(){
        String clientRequestHeader = "";
        try{
            String line;
            //first load up request
            clientRequestHeader = input.readLine();
            System.out.println(ANSI_GREEN+"["+clientRequestHeader+"]"+ANSI_RESET);
            //then read all header information until none
            while ((line = input.readLine()).length() > 0) {
               System.out.println(ANSI_GREEN+"[" + line + "]"+ANSI_RESET);
            }
            System.out.println(ANSI_GREEN+currentTime() +"Client request received.["+threadID+"]"+ANSI_RESET);
        }catch(IOException ioe){
            System.out.println(ANSI_RED+currentTime() +"Couldn't receive client request. ["+threadID+"]: "+ioe+ANSI_RESET);
        }
        return clientRequestHeader;
    }
    
    /**
     * Formatting method
     * 
     * @param rs The ResultSet object to be formatted into html
     * @return a html String
     */
    public static String formatToHTML(ResultSet rs){
        String html = "";
        String fields = "";//the specific table fields retrieved
        
        try{
            while(rs.next()){// add each field to table 
                fields = fields.concat(
                          "  <tr>\n"
                        + "    <td>"+rs.getString("name")+"</td>\n"
                        + "    <td>"+rs.getString("course")+"</td>\n"
                        + "    <td>"+rs.getDate("dob")+"</td>\n"
                        + "  </tr>\n");
            }
            
            if(fields.equals("")){//if fields empty because wrong student type requested
                //write default Invalid! response
                html = "<html>\n"
                        + "<body>\n"
                        + "<h1>City University</h1>"
                        + "<h3>Student Information</h3>"
                        + "<h4>By Amadeusz Misiak</h4>"
                        + "<h5>" + currentTime() + "</h5>\n"
                        + "<p>Invalid student type requested.</p>"
                        + "</body>\n"
                        + "</html>";
            } else {//otherwise fill out table with fields
                html = "<html>\n"
                        + "<body>\n"
                        + "<h1>City University</h1>"
                        + "<h3>Student Information</h3>"
                        + "<h4>By Amadeusz Misiak</h4>"
                        + "<h5>" + currentTime() + "</h5>"
                        + " <table>\n"
                        + "  <tr>\n"
                        + "    <th>Full Name</th>\n"
                        + "    <th>Course</th>\n"
                        + "    <th>DOB</th>\n"
                        + "  </tr>\n"
                        + fields
                        + "</table> \n\n"
                        + "</body>\n"
                        + "</html>";
            }
            
            
        }catch(SQLException sqle){
            System.out.println(ANSI_RED+currentTime()+"Error formatting to html: "+sqle+ANSI_RESET);
        }
        return html;
    }
    
    /*
    Setters & Getters
    */
    public static int getThreadsRunning(){
        return threadsRunning;
    }
    public static int getConnectionsMade(){
        return connectionsMade;
    }
    public boolean getStatus(){
        return status;
    }
    public int getThreadID(){
        return threadID;
    }
    public String getThreadStart(){
        return THREAD_START;
    }
    public Socket getSocket(){
        return client;
    }
    
    public void setStatus(boolean currentStatus){
        System.out.println("Thread ["+this.threadID+"] status changed to "+currentStatus);
        status = currentStatus; 
    }
    public void setClient(Socket currentClient){
        System.out.println("Thread ["+this.threadID+"] client set to "+currentClient);
        client = currentClient;
    }
}
