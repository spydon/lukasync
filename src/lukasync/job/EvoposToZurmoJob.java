package lukasync.job;

import lukasync.client.EvoposClient;
import lukasync.client.ZurmoClient;

import org.json.JSONObject;

public class EvoposToZurmoJob extends Job<EvoposClient, ZurmoClient> {

    public EvoposToZurmoJob(EvoposClient source, ZurmoClient destination, JSONObject jobMeta) {
        super(source, destination, jobMeta);
    }

    @Override
    public JSONObject execute() {
        // TODO Auto-generated method stub
        return null;
    }
}