package lukasync.job;

import lukasync.Lukasync;
import lukasync.client.EvoposClient;
import lukasync.client.MagentoClient;
import lukasync.util.JSONUtil;
import lukasync.util.LukaStore;
import org.json.JSONArray;
import org.json.JSONObject;

public class MagentoToEvoposJob extends Job<MagentoClient, EvoposClient> {

    public MagentoToEvoposJob (int jobId, MagentoClient source, EvoposClient destination) {
        super(jobId, source, destination);
    }

    @Override
    public JSONObject execute () {
        System.out.println("MagentoToEvoposJob starting.\n");

        try {
            copyNewSales();
        } catch (Throwable t) {
            addIssue("copyNewSales", t);
        }

        printFinish("MagentoToEvoposJob");

        return null;
    }

    private void copyNewSales () {
        String storeKey = "copyNewSales.latestCreatedAt";
        //JSONArray newSales = source.getNewSales(6, LukaStore.get(storeKey, jobId));
        JSONArray newSales = source.getNewSales(6, Lukasync.UPDATE_TIME); // TODO make this a global constant
        // TODO sort sales by created_at

        System.out.println("DEBUG: copyNewSales():");
        JSONUtil.prettyPrint(newSales);

        for (int i = 0; i < newSales.length(); i++) {
            // TODO wrap the following code in try catch to prevent one failing sale from interrupting the job
            JSONObject newSale = newSales.getJSONObject(i);

        }
    }


}
