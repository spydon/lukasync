package lukasync.job;

import lukasync.client.ServiceClient;
import lukasync.client.ZurmoClient;

import org.json.JSONObject;

public class MagentoToZurmoJob extends Job<ServiceClient, ZurmoClient> {

    public MagentoToZurmoJob(ServiceClient source, ZurmoClient destination) {
        super(source, destination);
    }

    @Override
    public JSONObject execute() {
        // TODO Auto-generated method stub
        return null;
    }
}
