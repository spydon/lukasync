package lukasync.client;

import org.json.JSONObject;

public abstract class ServiceClient {
    protected final int id;
    protected String name, type, connectionType, address, databaseName, username, password;

    public ServiceClient(JSONObject conf) {
        this.id = conf.getInt("id");
        this.name = conf.getString("name");
        this.type = conf.getString("type");
        this.connectionType = conf.getString("connectionType");
        this.address = conf.getString("address");

        if (conf.has("databaseName")) {
            this.databaseName = conf.getString("databaseName");
        }

        this.username = conf.getString("username");
        this.password = conf.getString("password");
        init();
    }

    protected abstract void init();
}