package lukasync.job;

import lukasync.JSONUtil;
import lukasync.client.EvoposClient;
import lukasync.client.ZurmoClient;

import org.json.JSONArray;
import org.json.JSONObject;

public class EvoposToZurmoJob extends Job<EvoposClient, ZurmoClient> {

    public EvoposToZurmoJob(EvoposClient source, ZurmoClient destination) {
        super(source, destination);
    }

    @Override
    public JSONObject execute() {
        copyNewUsers();
        copyUpdatedUsers();

        copyNewContacts();
        copyUpdatedContacts();

        copyNewTransactions();

        return null;
    }

    private void copyNewUsers () {
        JSONArray newUsers = source.getNewUsers("0");
        System.out.println("DEBUG: copyNewUsers():");
        JSONUtil.prettyPrint(newUsers);
    }

   private void copyUpdatedUsers () {
       JSONArray updatedUsers = source.getUpdatedUsers("0");
       System.out.println("DEBUG: copyUpdatedUsers():");
       JSONUtil.prettyPrint(updatedUsers);

       for (int i = 0; i < updatedUsers.length(); i++) {
           destination.createUser(updatedUsers.getJSONObject(i));
       }
   }

    private void copyNewContacts () {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void copyUpdatedContacts () {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void copyNewTransactions () {
        //To change body of created methods use File | Settings | File Templates.
    }
}
