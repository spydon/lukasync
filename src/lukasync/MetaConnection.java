package lukasync;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class MetaConnection {
    private int id;
    private String type, address, dbName, username, password, lastUpdated;

    public MetaConnection(int id, String type, String address, String dbName, String username, String password, String lastUpdated) {
        this.id = id;
        this.type = type;
        this.address = address;
        this.dbName = dbName;
        this.username = username;
        this.password = password;
        this.lastUpdated = lastUpdated;
    }

    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(type + "://" + address + ";user=" + username
                    + ";password=" + password + ";databaseName=" + dbName + ";");
        } catch (SQLException e) {
            System.err.println("ERROR: The connection to " + getAddress() +
                    " timed out! \nCheck the internet connection or look for faulty lines in the CSV-file(" + Lukasync.CONF + ").");
            e.printStackTrace();
        }
        return conn;
    }

    public void updateTime(String time) {
        File config = new File(Lukasync.CONF);
        try {
            List<String> configArray = FileUtils.readLines(config);
            StringBuilder configString = new StringBuilder();
            for (String line : configArray) {
                if (line.startsWith(id + ","))
                    line = line.substring(0, line.lastIndexOf(",") + 1).concat(time);
                configString.append(line + "\n");
            }
            FileUtils.writeStringToFile(config, configString.toString());
        } catch (IOException e) {
            System.err.println("Couldn't set an updated time for ID: " + id + " in the config file");
        }
    }

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDbName() {
        return dbName;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }
}