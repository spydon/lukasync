package lukasync.job;

import lukasync.client.EvoposClient;
import lukasync.client.ZurmoClient;
import lukasync.util.JSONUtil;

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

        copyContactRelations();
//        copyNewTransactions();

        return null;
    }

    private void copyNewUsers () {
        JSONArray newUsers = source.getNewUsers("0");
        System.out.println("DEBUG: copyNewUsers():");
        JSONUtil.prettyPrint(newUsers);

        for (int i = 0; i < newUsers.length(); i++) {
            destination.createUser(newUsers.getJSONObject(i));
        }
    }

    private void copyUpdatedUsers () {
        JSONArray updatedUsers = source.getUpdatedUsers("0");
        System.out.println("DEBUG: copyUpdatedUsers():");
        JSONUtil.prettyPrint(updatedUsers);

        for (int i = 0; i < updatedUsers.length(); i++) {
            destination.createUser(updatedUsers.getJSONObject(i));
        }
    }

    private void copyContactRelations () {
        JSONArray newContactRelations = source.getNewContactRelations("0");
        JSONUtil.prettyPrint(newContactRelations);

        for (int i = 0; i < newContactRelations.length(); i++) {
            destination.transferContact(
                    destination.getContactIdByCustomerNo(newContactRelations.getJSONObject(i).getString("customer_no")),
                    destination.getUserIdByUsername(newContactRelations.getJSONObject(i).getString("username")));
        }
    }

    private void copyNewContacts () {
        JSONArray newContacts = source.getNewContacts("0");
        System.out.println("DEBUG: copyNewContacts():");
        JSONUtil.prettyPrint(newContacts);

        for (int i = 0; i < newContacts.length(); i++) {
            destination.createContact(newContacts.getJSONObject(i));
        }
    }

    private void copyUpdatedContacts () {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void copyNewTransactions () {
        //To change body of created methods use File | Settings | File Templates.
    }
}
