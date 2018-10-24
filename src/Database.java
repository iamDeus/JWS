import java.sql.*;
/**
 * Database handling class
 * 
 * @author Amadeusz Misiak
 */
public class Database extends Time {
    //Database fields
    private static Connection con = null;
    private String driver = "com.mysql.jdbc.Driver";
    private String host = "city.ac.telecom2.net";
    private String port = "63006";
    private String url = "jdbc:mysql://"+host+":"+port+"/";
    private String db = "cityuniversity";
    private String user = "javastudent";
    private String pass = "kdjf878324jkdf";
    public static boolean accessT = true;
    //Output colours
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    
    /**
     * Default Constructor.
     * 
     * Loads the sql driver according to default specifications
     */
    public Database(){
        try{
            Class.forName(driver).newInstance();
            System.out.println(currentTime()+"Database driver loaded.");
        }catch(Exception e){
            System.out.println(ANSI_RED+currentTime()+"Couldn't load database driver: "+e+ANSI_RESET);
        }
    }
    
    /**
     * Custom Database Constructor.
     * 
     *
     * @param host Database host
     * @param port Database port
     * @param database the database at host
     * @param user Username for access
     * @param pass Password for access
     */
    public Database(String host, int port, String database, String user, String pass){
        try{
            Class.forName(driver).newInstance();
            System.out.println(currentTime()+"Database driver loaded.");
        }catch(Exception e){
            System.out.println(ANSI_RED+currentTime()+"Couldn't load database driver: "+e+ANSI_RESET);
        }
        this.host = host;
        this.port = String.valueOf(port);
        this.db = database;
        this.user = user;
        this.pass = pass;
    }
    
    /**
     * Connect method.
     * 
     * Establishes a running connection with the database server
     * 
     */
    public void connect(){
        try{
            con = DriverManager.getConnection(url+db, user, pass);
            System.out.println(currentTime()+"Connection to database made.");
        }catch(SQLException sqle){
            System.out.println(ANSI_RED+currentTime()+"Couldn't connect to database: "+sqle+ANSI_RESET);
        }
        
    }
    
    /**
     * Abstraction query method.
     * 
     * creates a statement from string, then queries it and ensures thread safety
     * 
     * @param statement The String statement to be used to query the database
     * @return Returns the queried ResultSet
     */
    public ResultSet sendStatement(String statement){
        ResultSet rs = null;
        
        try{
            while(rs==null){//while rs not populated with results try again until 
                //the accessT flag has been released and rs is populated
                if(accessT){
                    accessT = false;
                    Statement st = con.createStatement();
                    System.out.println(currentTime()+"Statement created. >"+statement);
                    
                    rs = st.executeQuery(statement);
                    System.out.println(currentTime()+"Statement sent and results received. >"+rs.toString());
                    accessT = true;
                }
            }
        }catch(SQLException sqle){
            System.out.println(ANSI_RED+currentTime()+"Problem sending statement: "+sqle+ANSI_RESET);
        }
        return rs;
    }
    
    /*
    Getters & Setters
    */
    public static Connection getConnection(){
        return con;
    }
    public String getDriver(){
        return this.driver;
    }
    public String getHost(){
        return this.host;
    }
    public int getPort(){
        return Integer.parseInt(this.port);
    }
    public String getUrl(){
        return this.url;
    }
    public String getDatabase(){
        return this.db;
    }
    public String getUser(){
        return this.user;
    }
    public String getPass(){
        return this.pass;
    }
    
    public void setHost(String host){
        this.host = host;
    }
    public void setPort(int port){
        this.port = String.valueOf(port);
    }
    public void setDatabase(String db){
        this.db = db;
    }
    public void setUser(String user){
        this.user = user;
    }
    public void setPass(String pass){
        this.pass = pass;
    }
    
}