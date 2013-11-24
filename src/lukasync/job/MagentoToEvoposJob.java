package lukasync.job;

import lukasync.Lukasync;
import lukasync.client.EvoposClient;
import lukasync.client.MagentoClient;
import lukasync.util.JSONUtil;
import lukasync.util.LukaStore;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

        List<JSONObject> saleList = new ArrayList<JSONObject>();
        for (int i = 0; i < newSales.length(); i++) {
            saleList.add(newSales.getJSONObject(i));
        }

        Comparator<JSONObject> saleComparator = new Comparator<JSONObject>() {
            @Override
            public int compare (JSONObject o1, JSONObject o2) {
                return o1.getString("createdAt").compareTo(o2.getString("createdAt"));
            }
        };

        Collections.sort(saleList, saleComparator);

        System.out.println("DEBUG: copyNewSales():");
        JSONUtil.prettyPrint(newSales);

        for (JSONObject sale: saleList) {
            try {
                destination.insertNewSale(sale);
                LukaStore.put(storeKey, jobId, sale.getString("createdAt"));
            } catch (Throwable t) {
                System.err.println(t.getMessage());
                t.printStackTrace();
            }

        }
    }


}
