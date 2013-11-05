package lukasync.client;

import org.json.JSONObject;

public abstract class ServiceClient {
    protected final int id;
    protected String name, type, connectionType, address, dbName, username, password;
    protected JSONObject lastUpdated;

    public ServiceClient(JSONObject conf) {
        this.id = conf.getInt("id");
        this.name = conf.getString("name");
        this.type = conf.getString("type");
        this.connectionType = conf.getString("connectionType");
        this.address = conf.getString("address");
        this.dbName = conf.getString("dbName");
        this.username = conf.getString("username");
        this.password = conf.getString("password");
        this.lastUpdated = conf.getJSONObject("lastUpdated");
        init();
    }

    protected abstract void init();
}