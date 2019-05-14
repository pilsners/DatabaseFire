package NerdyGadgets;

import java.sql.*;


public class Conn {

    static Connection connection = null;


    public static Connection getConnection() {
        if(connection != null) {
            return connection;
        }
        try {
            connection = DriverManager.getConnection("jdbc:mysql://www.jeltebroer.nl:3306/KBSB?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "kbs", "kbswachtwoord");
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0].getLineNumber());
        }
        return connection;
    }
}
