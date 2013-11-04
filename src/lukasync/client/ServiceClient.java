package lukasync.client;

import org.json.JSONObject;

public abstract class ServiceClient {
    protected final int id;
    protected String name, type, address, dbName, username, password;
    protected JSONObject lastUpdated;

    public ServiceClient(JSONObject conf) {
        this.id = conf.getInt("id");
        this.name = conf.getString("name");
        this.type = conf.getString("type");
        this.address = conf.getString("address");
        this.dbName = conf.getString("dbName");
        this.username = conf.getString("username");
        this.password = conf.getString("password");
        this.lastUpdated = conf.getJSONObject("lastUpdated");
        init();
    }

    protected abstract void init();

//    @DEPRECATED
//    public void updateTime(String time) {
//        File config = new File(Lukasync.CONF);
//        try {
//            List<String> configArray = FileUtils.readLines(config);
//            StringBuilder configString = new StringBuilder();
//            for (String line : configArray) {
//                if (line.startsWith(id + ","))
//                    line = line.substring(0, line.lastIndexOf(",") + 1).concat(time);
//                configString.append(line + "\n");
//            }
//            FileUtils.writeStringToFile(config, configString.toString());
//        } catch (IOException e) {
//            System.err.println("Couldn't set an updated time for ID: " + id + " in the config file");
//        }
//    }

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