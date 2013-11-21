package lukasync.job;

import lukasync.Lukasync;
import lukasync.client.EvoposClient;
import lukasync.client.MagentoClient;
import lukasync.util.JSONUtil;
import lukasync.util.LukaStore;
import org.json.JSONArray;
import org.json.JSONObject;

public class EvoposToMagentoJob extends Job<EvoposClient, MagentoClient> {

    public EvoposToMagentoJob (int jobId, EvoposClient source, MagentoClient destination) {
        super(jobId, source, destination);
    }

    @Override
    public JSONObject execute() {
        System.out.println("\nEvoposToMagentoJob starting.\n");

        try {
            copyNewUsers();
        } catch (Throwable t) {
            addIssue("copyNewUsers", t);
        }

        printFinish("EvoposToMagentoJob");

        return null;
    }

    private void copyNewUsers () {
        String storeKey = "copyNewUsers.lastModified";
        //JSONArray newUsers = source.getNewUsers(LukaStore.get(storeKey, jobId));
        JSONArray newUsers = source.getNewUsers(Lukasync.UPDATE_TIME);

        System.out.println("DEBUG: copyNewUsers():");
        JSONUtil.prettyPrint(newUsers);

        for (int i = 0; i < newUsers.length(); i++) {
            JSONObject newUser = newUsers.getJSONObject(i);

            String emailUser = newUser.getString("username").toLowerCase().replaceAll("\\s+", "");
            String rewrittenEmail = emailUser + "@theinnercirclevip.com";
            newUser.put("emailAddress", rewrittenEmail);

            destination.createCustomer(newUser);
            LukaStore.put(storeKey, jobId, newUser.getString("modified_at"));
        }

    }
}
