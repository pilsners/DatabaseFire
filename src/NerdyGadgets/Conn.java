package NerdyGadgets;

import java.sql.*;


public class Conn {

    static Connection connection = null;


    public static Connection getConnection(String username, String password) {
        if(connection != null) {
            return connection;
        }
        try {
            connection = DriverManager.getConnection("jdbc:mysql://"+Main.DBServerAddress+":3306/KBSB?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", username, username);
        } catch (SQLClientInfoException e){
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0].getLineNumber());
            System.out.println("ERROR: The program was terminated because of an error in with the database connection");
            System.exit(0);
        }
        return connection;
    }
}
