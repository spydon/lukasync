package lukasync.client;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lukasync.Lukasync;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

public abstract class ServiceClient {
    protected final int id;
    protected String type, address, dbName, username, password;
    protected JSONObject lastUpdated;

    public ServiceClient(JSONObject conf) {
        this.id = conf.getInt("id");
        this.type = conf.getString("type");
        this.address = conf.getString("address");
        this.dbName = conf.getString("dbName");
        this.username = conf.getString("username");
        this.password = conf.getString("password");
        this.lastUpdated = conf.getJSONObject("lastUpdated");
        init();
    }

    protected abstract void init();

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

//    public String getAddress() {
//        return address;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public String getDbName() {
//        return dbName;
//    }

//    public String getLastUpdated() {
//        return lastUpdated;
//    }
}