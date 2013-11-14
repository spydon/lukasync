package lukasync.client;

import org.json.JSONObject;

public class MagentoClient extends ServiceClient {

    public MagentoClient(JSONObject conf) {
        super(conf);
    }

    @Override
    protected void init() {

    }
}