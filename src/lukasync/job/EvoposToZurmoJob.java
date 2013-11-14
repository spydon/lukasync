package lukasync.job;

import lukasync.Lukasync;
import lukasync.client.EvoposClient;
import lukasync.client.ZurmoClient;
import lukasync.util.JSONUtil;

import lukasync.util.LukaStore;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class EvoposToZurmoJob extends Job<EvoposClient, ZurmoClient> {

    //public static final String UPDATE_TIME = "2013-11-01";
    public static final String UPDATE_TIME = "1990-12-31";

    private ArrayList<String> issues;

    public EvoposToZurmoJob(EvoposClient source, ZurmoClient destination) {
        super(source, destination);

        issues = new ArrayList<>();
    }

    @Override
    public JSONObject execute() {
        // XXX Each task for the job is try-catched to keep them independent from each other
        System.out.println("\nEvoposToZurmoJob starting.\n");

        try {
            copyNewUsers();
        } catch (Throwable t) {
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));

            issues.add("\ncopyNewUsers:\n" + t.getMessage() + "\n" + errors.toString());
        }

        try {
            copyUpdatedUsers();
        } catch (Throwable t) {
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));

            issues.add("\ncopyUpdatedUsers:\n" + t.getMessage() + "\n" + errors.toString());
        }

        try {
            //copyNewContacts();
        } catch (Throwable t) {
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));

            issues.add("\ncopyNewContacts:\n" + t.getMessage() + "\n" + errors.toString());
        }

        try {
            copyUpdatedContacts();
        } catch (Throwable t) {
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));

            issues.add("\ncopyUpdatedContacts:\n" + t.getMessage() + "\n" + errors.toString());
        }

        try {
            //copyContactRelations();
        } catch (Throwable t) {
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));

            issues.add("\ncopyContactRelations:\n" + t.getMessage() + "\n" + errors.toString());
        }

        try {
            //copyNewTransactions();
        } catch (Throwable t) {
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));

            issues.add("\ncopyNewTransactions:\n" + t.getMessage() + "\n" + errors.toString());
        }

        if (Lukasync.PRINT_DEBUG) {
            System.out.println("\n\nEvoposToZurmoJob finished with " + issues.size() + " issues.");

            if (issues.size() > 0) {
                for (String issue : issues) {
                    System.out.println(issue);
                }
            }
        }

        return null;
    }

    /*
     * Tasks
     */

    private void copyNewUsers () {
        String storeKey = "copyNewUsers.lastModified";
        int sourceId = source.getId();
        //JSONArray newUsers = source.getNewUsers(LukaStore.get(storeKey, sourceId));
        JSONArray newUsers = source.getNewUsers(UPDATE_TIME);

        System.out.println("DEBUG: copyNewUsers():");
        JSONUtil.prettyPrint(newUsers);

        for (int i = 0; i < newUsers.length(); i++) {
            JSONObject newUser = newUsers.getJSONObject(i);
            destination.createUser(newUser);
            LukaStore.put(storeKey, sourceId, newUser.getString("modified_at"));
        }
    }

    private void copyUpdatedUsers () {
        String storeKey = "copyUpdatedUsers.lastModified";
        int sourceId = source.getId();
        //JSONArray updatedUsers = source.getUpdatedUsers(LukaStore.get(storeKey, sourceId));
        JSONArray updatedUsers = source.getUpdatedUsers(UPDATE_TIME);

        System.out.println("DEBUG: copyUpdatedUsers():");
        JSONUtil.prettyPrint(updatedUsers);

        for (int i = 0; i < updatedUsers.length(); i++) {
            JSONObject updatedUser = updatedUsers.getJSONObject(i);
            int userId = destination.getUserIdByUsername(updatedUser.getString("username"));

            destination.updateUser(userId, updatedUser);
            LukaStore.put(storeKey, sourceId, updatedUser.getString("modified_at"));
        }
    }

    private void copyNewContacts () {
        String storeKey = "copyNewContacts.lastModified";
        int sourceId = source.getId();
        //JSONArray newContacts = source.getNewContacts(LukaStore.get(storeKey, sourceId));
        JSONArray newContacts = source.getNewContacts(UPDATE_TIME);

        System.out.println("DEBUG: copyNewContacts():");
        JSONUtil.prettyPrint(newContacts);

        for (int i = 0; i < newContacts.length(); i++) {
            JSONObject newContact = newContacts.getJSONObject(i);
            destination.createContact(newContact);
            LukaStore.put(storeKey, sourceId, newContact.getString("modified_at"));
        }
    }

    private void copyUpdatedContacts () {
        String storeKey = "copyUpdatedContacts.lastModified";
        int sourceId = source.getId();
        //JSONArray updatedContacts = source.getUpdatedContacts(LukaStore.get(storeKey, sourceId));
        JSONArray updatedContacts = source.getUpdatedContacts(UPDATE_TIME);

        System.out.println("DEBUG: copyUpdatedContacts():");
        JSONUtil.prettyPrint(updatedContacts);

        for (int i = 0; i < updatedContacts.length(); i++) {
            JSONObject updatedContact = updatedContacts.getJSONObject(i);
            int contactId = destination.getContactIdByCustomerNo(updatedContact.getString("customer_no"));
            destination.updateContact(contactId, updatedContact);
            LukaStore.put(storeKey, sourceId, updatedContact.getString("modified_at"));
        }
    }

    private void copyContactRelations () {
        String storeKey = "copyContactRelations.lastModified";
        int sourceId = source.getId();
        //JSONArray newContactRelations = source.getNewContactRelations(LukaStore.get(storeKey, sourceId));
        JSONArray newContactRelations = source.getNewContactRelations(UPDATE_TIME);

        JSONUtil.prettyPrint(newContactRelations);

        for (int i = 0; i < newContactRelations.length(); i++) {
            JSONObject newContactRelation = newContactRelations.getJSONObject(i);

            if (Lukasync.PRINT_DEBUG) {
                System.out.println("DEBUG: copying contact relation: " + newContactRelation.toString());
            }

            String customerNo = newContactRelation.getString("customer_no");
            String username = newContactRelation.getString("username");

            int contactId = destination.getContactIdByCustomerNo(customerNo);
            int newOwnerId = destination.getUserIdByUsername(username);

            if (contactId == -1) {
                System.err.println("DEBUG: The contact could not be resolved from the relation.");
                continue;
            }

            if (newOwnerId == -1) {
                System.err.println("DEBUG: The owning user could not be resolved from the relation.");
                continue;
            }

            destination.transferContact(
                    contactId,
                    newOwnerId
            );
            LukaStore.put(storeKey, sourceId, newContactRelation.getString("modified_at"));
        }
    }

    private void copyNewTransactions () {
        String storeKey = "copyNewTransactions.lastModified";
        int sourceId = source.getId();
        //JSONArray saleLines = source.getNewSales(LukaStore.get(storeKey, sourceId));
        JSONArray saleLines = source.getNewSales(UPDATE_TIME); // TODO keep track of latest know transaction date

        StringBuilder sb = new StringBuilder();
        String modifiedDate = "";
        for (int i = 0; i < saleLines.length(); i++) {
            JSONObject currentSaleLine = saleLines.getJSONObject(i);

            System.out.println("DEBUG: currentSaleLine: " + currentSaleLine.toString());

            String candidateDate = currentSaleLine.getString("modified_at");
            modifiedDate = (modifiedDate.compareTo(candidateDate) > -1) ? candidateDate : modifiedDate;

            String qty = currentSaleLine.getString("qty");
            String partNo = currentSaleLine.getString("part_no");
            String description = currentSaleLine.getString("description");
            String gross = currentSaleLine.getString("gross");

            sb.append((int)Double.parseDouble(qty));
            sb.append(" x ");
            sb.append(partNo);
            sb.append(", ");
            sb.append(description);
            sb.append(", $");
            sb.append(gross);
            sb.append("<br />\n");

            String currentTransactionNo = currentSaleLine.getString("transaction_no");
            if (i + 1 < saleLines.length()) {
                JSONObject nextSaleLine = saleLines.getJSONObject(i + 1);
                String nextTransactionNo = nextSaleLine.getString("transaction_no");

                if (!currentTransactionNo.equals(nextTransactionNo)) {
                    createNoteForTransaction(sb, currentSaleLine, currentTransactionNo);
                    LukaStore.put(storeKey, sourceId, modifiedDate);
                }
            } else {
                createNoteForTransaction(sb, currentSaleLine, currentTransactionNo);
            }
        }
    }

    /*
     * Privates
     */

    private void createNoteForTransaction (StringBuilder sb, JSONObject currentSaleLine, String currentTransactionNo) {
        String soldAt = currentSaleLine.getString("sold_at");
        String headerString = "<span style=\"color: #080\">RECEIPT</span><br />\n" +
                "Transaction No: " + currentTransactionNo + "<br />\n" +
                "Date: " + soldAt + "<br />\n" +
                "<br />\n";

        sb.insert(0, headerString);

        int userId = 1;
        String customerNo = currentSaleLine.getString("customer_no");
        int contactId = destination.getContactIdByCustomerNo(customerNo);

        if (contactId > -1) {
            destination.createNote(userId, contactId, sb.toString(), soldAt);
        } else {
            System.err.println("createNoteForTransaction() couldn't match transaction with contact with customerNo " + customerNo + "!");
            System.err.println(sb.toString());
        }

        sb.setLength(0);
    }
}
