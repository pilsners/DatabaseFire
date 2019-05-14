package NerdyGadgets;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.Permission;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;


public class Main {
    private static int serverID;
    private static int databaseLookback;

    public static void main(String[] args) {

        if(getConfig()){
            setFile();
            Conn.getConnection();

            Timer t = new Timer();
            t.schedule(new Fire(), 0, 5000);
        }else {
            System.out.println("Please make sure your serverStatus.config is correctly configured!");
        }


    }

    static class Fire extends TimerTask{
        public void run(){
            Timestamp curTime = new Timestamp(new Date().getTime());
            try{
                System.out.println("Processor load: " + getProcessCpuLoad());
                PreparedStatement p = Conn.connection.prepareStatement("INSERT INTO ComponentStatus (componentID, uptime, processorStatus, availableStorage, lastUpdate) VALUES ("+serverID+", 90," + getProcessCpuLoad() + ", 5, '" + curTime + "')");
                p.executeUpdate();
                ResultSet rs = Conn.connection.createStatement().executeQuery("SELECT COUNT(componentID) Amount FROM ComponentStatus WHERE componentID = " + serverID);
                if(rs.next()){
                    if(rs.getInt("Amount") > databaseLookback){

                        PreparedStatement rm = Conn.connection.prepareStatement("INSERT INTO ComponentStatus_archive SELECT * FROM ComponentStatus WHERE componentID = "+serverID+" ORDER BY lastUpdate ASC LIMIT 1");
                        rm.executeUpdate();
                        PreparedStatement rm2 = Conn.connection.prepareStatement("DELETE FROM ComponentStatus WHERE componentID = "+serverID+" ORDER BY lastUpdate ASC LIMIT 1");
                        rm2.executeUpdate();

                    }
                }
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }catch(Exception f){
                System.out.println(f.getMessage());
            }
        }
    }


    public static double getProcessCpuLoad() {
        String list = new String();
        ProcessBuilder builder = new ProcessBuilder(System.getProperty("user.dir") + "/getData.sh");

        try{
            Process process = builder.start();


            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            list = reader.readLine();

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return Double.parseDouble(list);
    }

    public static void setFile(){
        File f = new File("getData.sh");

        if(!f.exists()){
            List<String> lines = Arrays.asList("grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage}'");
            try{

                Files.write(Paths.get("getData.sh"), lines, StandardOpenOption.CREATE);
            }catch (IOException e) {
                System.out.println(e.getMessage());
            }


        }


    }

    public static boolean getConfig(){
        File conf = new File("serverStatus.config");

        Properties prop = new Properties();
        InputStream is = null;
        try{
            is = new FileInputStream("serverStatus.config");
        }catch (FileNotFoundException e){
            List<String> lines = Arrays.asList("serverID=", "databaseLookback=20");
            try{
                conf.setExecutable(true);
                conf.setReadable(true);
                conf.setWritable(true);
                Files.write(Paths.get("serverStatus.config"), lines, StandardOpenOption.CREATE);
                System.out.println("Please fill in the config file!");
            }catch (IOException ef){

                System.out.println(ef.getMessage());
            }

            return false;
        }

        try {
            prop.load(is);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }

        try{
            serverID = Integer.parseInt(prop.getProperty("serverID"));
            databaseLookback = Integer.parseInt(prop.getProperty("databaseLookback"));
        }catch(NumberFormatException e){
            System.out.println("Make sure your put an interger in the serverStatus.conf serverID or databaseLookback property");
            return false;

        }

        return true;

    }
}
