package lukasync.job;

import lukasync.Lukasync;
import lukasync.client.EvoposClient;
import lukasync.client.ZurmoClient;
import lukasync.util.JSONUtil;

import lukasync.util.LukaStore;
import org.json.JSONArray;
import org.json.JSONObject;

public class EvoposToZurmoJob extends Job<EvoposClient, ZurmoClient> {

    public EvoposToZurmoJob(int jobId, EvoposClient source, ZurmoClient destination) {
        super(jobId, source, destination);
    }

    @Override
    public JSONObject execute() {
        // XXX Each task for the job is try-catched to keep them independent from each other
        System.out.println("\nEvoposToZurmoJob starting.\n");

        try {
            copyNewUsers();
        } catch (Throwable t) {
            addIssue("copyNewUsers", t);
        }

        try {
            copyUpdatedUsers();
        } catch (Throwable t) {
            addIssue("copyUpdatedUsers", t);
        }

        try {
            copyNewContacts();
        } catch (Throwable t) {
            addIssue("copyNewContacts", t);
        }

        try {
            copyUpdatedContacts();
        } catch (Throwable t) {
            addIssue("copyUpdatedContacts", t);
        }

        try {
            copyContactRelations();
        } catch (Throwable t) {
            addIssue("copyContactRelations", t);
        }

        try {
            copyNewTransactions();
        } catch (Throwable t) {
            addIssue("copyNewTransactions", t);
        }

        printFinish("EvoposToZurmoJob");

        return null;
    }

    /*
     * Tasks
     */

    private void copyNewUsers () {
        String storeKey = "copyNewUsers.lastModified";
        String updateTime = LukaStore.get(storeKey, jobId, Lukasync.INITIAL_IMPORT_UPDATE_TIME);
        JSONArray newUsers = source.getNewUsers(updateTime);

        System.out.println("DEBUG: copyNewUsers():");
        JSONUtil.prettyPrint(newUsers);

        for (int i = 0; i < newUsers.length(); i++) {
            JSONObject newUser = newUsers.getJSONObject(i);
            destination.createUser(newUser);
            LukaStore.put(storeKey, jobId, newUser.getString("modified_at"));
        }
    }

    private void copyUpdatedUsers () {
        String storeKey = "copyUpdatedUsers.lastModified";
        String updateTime = LukaStore.get(storeKey, jobId, Lukasync.INITIAL_IMPORT_UPDATE_TIME);
        JSONArray updatedUsers = source.getUpdatedUsers(updateTime);

        System.out.println("DEBUG: copyUpdatedUsers():");
        JSONUtil.prettyPrint(updatedUsers);

        for (int i = 0; i < updatedUsers.length(); i++) {
            JSONObject updatedUser = updatedUsers.getJSONObject(i);
            int userId = destination.getUserIdByUsername(updatedUser.getString("username"));

            destination.updateUser(userId, updatedUser);
            LukaStore.put(storeKey, jobId, updatedUser.getString("modified_at"));
        }
    }

    private void copyNewContacts () {
        String storeKey = "copyNewContacts.lastModified";
        String updateTime = LukaStore.get(storeKey, jobId, Lukasync.INITIAL_IMPORT_UPDATE_TIME);
        JSONArray newContacts = source.getNewContacts(updateTime);

        System.out.println("DEBUG: copyNewContacts():");
        JSONUtil.prettyPrint(newContacts);

        for (int i = 0; i < newContacts.length(); i++) {
            JSONObject newContact = newContacts.getJSONObject(i);
            destination.createContact(newContact);
            LukaStore.put(storeKey, jobId, newContact.getString("modified_at"));
        }
    }

    private void copyUpdatedContacts () {
        String storeKey = "copyUpdatedContacts.lastModified";
        String updateTime = LukaStore.get(storeKey, jobId, Lukasync.INITIAL_IMPORT_UPDATE_TIME);
        JSONArray updatedContacts = source.getUpdatedContacts(updateTime);

        System.out.println("DEBUG: copyUpdatedContacts():");
        JSONUtil.prettyPrint(updatedContacts);

        for (int i = 0; i < updatedContacts.length(); i++) {
            JSONObject updatedContact = updatedContacts.getJSONObject(i);
            int contactId = destination.getContactIdByCustomerNo(updatedContact.getString("customer_no"));
            destination.updateContact(contactId, updatedContact);
            LukaStore.put(storeKey, jobId, updatedContact.getString("modified_at"));
        }
    }

    private void copyContactRelations () {
        String storeKey = "copyContactRelations.lastModified";
        String updateTime = LukaStore.get(storeKey, jobId, Lukasync.INITIAL_IMPORT_UPDATE_TIME);
        JSONArray newContactRelations = source.getNewContactRelations(updateTime);

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
            LukaStore.put(storeKey, jobId, newContactRelation.getString("modified_at"));
        }
    }

    private void copyNewTransactions () {
        String storeKey = "copyNewTransactions.lastModified";
        String updateTime = LukaStore.get(storeKey, jobId, Lukasync.INITIAL_IMPORT_UPDATE_TIME);
        JSONArray saleLines = source.getNewSales(updateTime);

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
                    LukaStore.put(storeKey, jobId, modifiedDate);
                }
            } else {
                createNoteForTransaction(sb, currentSaleLine, currentTransactionNo);
            }
        }
    }

    /*
     * Privates
     */

    /*
     * This method does not bork out because it is ok for it to fail.
     * The reason is that we don't want it to get stuck on faulty transaction-to-contact relations.
     */
    private void createNoteForTransaction (StringBuilder sb, JSONObject currentSaleLine, String currentTransactionNo) {
        String soldAt = currentSaleLine.getString("sold_at");
        String headerString = "<span style=\"color: #080\">RECEIPT</span><br />\n" +
                "Transaction No: " + currentTransactionNo + "<br />\n" +
                "Date: " + soldAt + "<br />\n" +
                "<br />\n";

        sb.insert(0, headerString);

        String customerNo = currentSaleLine.getString("customer_no");
        int contactId = destination.getContactIdByCustomerNo(customerNo);
        int ownerId = destination.getOwnerIdByContactId(contactId);

        if (contactId > -1 && ownerId > -1) {
            destination.createNote(ownerId, contactId, sb.toString(), soldAt);
        } else if (contactId < 0){
            System.err.println("createNoteForTransaction() couldn't match transaction with contact with customerNo " + customerNo + "!");
            System.err.println(sb.toString());
        } else if (ownerId < 0){
            System.err.println("createNoteForTransaction() couldn't fetch owner for contact with id " + contactId + "!");
            System.err.println(sb.toString());
        }

        sb.setLength(0);
    }
}
