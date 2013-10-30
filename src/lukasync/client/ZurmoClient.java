package lukasync.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import lukasync.Lukasync;
import lukasync.Rest;

import org.json.JSONArray;
import org.json.JSONObject;

public class ZurmoClient extends ServiceClient{
    private String sessionId;
    private String token;
    private String baseUrl;

    public ZurmoClient(JSONObject conf) {
        super(conf);
    }

    @Override
    protected void init() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("ZURMO-AUTH-USERNAME", this.username);
        headers.put("ZURMO-AUTH-PASSWORD", this.password);
        headers.put("ZURMO-API-REQUEST-TYPE", "REST");

        this.baseUrl = this.address;
        JSONObject response = Rest.jsonPost(baseUrl + "/zurmo/api/login", headers);

        if (response != null && response.getString("status").equals("SUCCESS")) {
            JSONObject data = response.getJSONObject("data");
            String sessionId = data.getString("sessionId");
            String token = data.getString("token");

            if (Lukasync.printDebug) {
                System.out.println("\nDEBUG: ZurmoClient built with credentials:");
                System.out.println("  sessionId: " + sessionId);
                System.out.println("  token: " + token);
            }

          this.sessionId = sessionId;
          this.token = token;

        } else {
            voidResponse(response);
        }
    }

    public boolean createNote(int userId, int contactId, String description, Date date2) {
        HashMap<String, String> headers = getDefaultHeaders();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = sdf.format(date2).toString();

        JSONObject relation = new JSONObject();
        relation.put("action", "add");
        relation.put("modelId", contactId);
        relation.put("modelClassName", "Contact");

        JSONArray contacts = new JSONArray();
        contacts.put(relation);

        JSONObject modelRelations = new JSONObject();
        modelRelations.put("activityItems", contacts);

        JSONObject owner = new JSONObject();
        owner.put("id", userId);

        JSONObject data = new JSONObject();
        data.put("description", description);
        data.put("occurredOnDateTime", date);
        data.put("modelRelations", modelRelations);
        data.put("owner", owner);

        JSONObject payload = new JSONObject();
        payload.put("data", data);

        if (Lukasync.printDebug) {
            //System.out.println(payload.toString());
            //System.out.println(JSONUtil.jsonToURLEncoding(payload));
        }

        JSONObject response = Rest.jsonPost(baseUrl + "/notes/note/api/create/", headers, payload);
        return booleanResponse(response);
    }

    @SuppressWarnings("unused")
    @Deprecated
    private void createNoteRelation(int noteId, int contactId) {
        HashMap<String, String> headers = getDefaultHeaders();

        JSONObject relation = new JSONObject();
        relation.put("action", "add");
        relation.put("modelId", contactId);
        relation.put("modelClassName", "Contact");

        JSONArray notes = new JSONArray();
        notes.put(relation);

        JSONObject modelRelations = new JSONObject();
        modelRelations.put("activityItems", notes);

        JSONObject data = new JSONObject();
        data.put("modelRelations", modelRelations);

        JSONObject payload = new JSONObject();
        payload.put("data", data);

        String requestURL = baseUrl + "/notes/note/api/update/" + noteId;

        JSONObject response = Rest.jsonPut(requestURL, headers, payload);
        voidResponse(response);
    }

    public boolean createContact(
            int ownerId,

            String firstName,
            String lastName,
            String mobilePhone,
            String companyName,

            String emailAddress,
            boolean optOut,

            String street1,
            String street2,
            String city,
            String postalCode,
            String country
    ) {
        //int state,String description,
        HashMap<String, String> headers = getDefaultHeaders();

        JSONObject data = new JSONObject();
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("mobilePhone", mobilePhone);
        data.put("companyName", companyName);

        JSONObject primaryEmail = new JSONObject();
        primaryEmail.put("emailAddress", emailAddress);
        primaryEmail.put("optOut", optOut);
        data.put("primaryEmail", primaryEmail);

        JSONObject primaryAddress = new JSONObject();
        primaryAddress.put("street1", street1);
        primaryAddress.put("street2", street2);
        primaryAddress.put("city", city);
        primaryAddress.put("postalCode", postalCode);
        primaryAddress.put("country", country);
        data.put("primaryAddress", primaryAddress);

        JSONObject owner = new JSONObject();
        owner.put("id", ownerId);
        data.put("owner", owner);

        JSONObject payload = new JSONObject();
        payload.put("data", data);

        JSONObject response = Rest.jsonPost(baseUrl + "/contacts/contact/api/create/", headers, payload);
        return booleanResponse(response);
    }

    public boolean updateContact(
            int contactId,

            Integer ownerId,

            String firstName,
            String lastName,
            String mobilePhone,
            String companyName,

            String emailAddress,
            Boolean optOut,

            String street1,
            String street2,
            String city,
            String postalCode,
            String country
    ) {
        // short circuit if nothing is to be updated
        if (ownerId == null &&
                firstName == null &&
                lastName == null &&
                mobilePhone == null &&
                companyName == null &&
                emailAddress == null &&
                optOut == null &&
                street1 == null &&
                street2 == null &&
                city == null &&
                postalCode == null &&
                country == null) {
            return false;
        }

        HashMap<String, String> headers = getDefaultHeaders();

        JSONObject data = new JSONObject();

        if (firstName != null) {

            data.put("firstName", firstName);

        }

        if (lastName != null) {

            data.put("lastName", lastName);

        }

        if (mobilePhone != null) {

            data.put("mobilePhone", mobilePhone);

        }

        if (companyName != null) {

            data.put("companyName", companyName);

        }

        updateContactEmailCheck(emailAddress, optOut, data);

        updateContactAddressCheck(street1, street2, city, postalCode, country, data);

        if (ownerId != null) {

            JSONObject owner = new JSONObject();
            owner.put("id", ownerId);
            data.put("owner", owner);

        }

        JSONObject payload = new JSONObject();
        payload.put("data", data);

        JSONObject response = Rest.jsonPut(baseUrl + "/contacts/contact/api/update/" + contactId, headers, payload);
        return booleanResponse(response);
    }

    private void updateContactEmailCheck (String emailAddress, Boolean optOut, JSONObject outData) {
        if (emailAddress != null
                || optOut != null) {

            JSONObject primaryEmail = new JSONObject();

            if (emailAddress != null) {

                primaryEmail.put("emailAddress", emailAddress);

            }

            if (optOut != null) {

                primaryEmail.put("optOut", optOut);

            }

            outData.put("primaryEmail", primaryEmail);
        }
    }

    private void updateContactAddressCheck (String street1,
                                            String street2,
                                            String city,
                                            String postalCode,
                                            String country,
                                            JSONObject outData) {
        if (street1 != null
                || street2 != null
                || city != null
                || postalCode != null
                || country != null) {

            JSONObject primaryAddress = new JSONObject();

            if (street1 != null) {

                primaryAddress.put("street1", street1);

            }

            if (street2 != null) {

                primaryAddress.put("street2", street2);

            }

            if (city != null) {

                primaryAddress.put("city", city);

            }

            if (postalCode != null) {

                primaryAddress.put("postalCode", postalCode);

            }

            if (country != null) {

                primaryAddress.put("country", country);

            }

            outData.put("primaryAddress", primaryAddress);
        }
    }

    public boolean transferContact (int contactId, int ownerId) {
        HashMap<String, String> headers = getDefaultHeaders();

        JSONObject owner = new JSONObject();
        owner.put("id", ownerId);

        JSONObject data = new JSONObject();
        data.put("owner", owner);

        JSONObject payload = new JSONObject();
        payload.put("data", data);

        JSONObject response = Rest.jsonPut(baseUrl + "/contacts/contact/api/update/" + contactId, headers, payload);

        return booleanResponse(response);
    }

    public boolean createUser(
            String username,
            String password,

            String firstName,
            String lastName,
            String mobilePhone,
            String department,

            String emailAddress,
            int optOut,

            String city,
            String postalCode,
            String country,

            String stateName,
            int order
    ) {
        //int state,String description,
        HashMap<String, String> headers = getDefaultHeaders();
        JSONObject payload = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("username", username.toLowerCase());
        data.put("password", password);
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("mobilePhone", mobilePhone);
        data.put("department", department);

        JSONObject primaryEmail = new JSONObject();
        primaryEmail.put("emailAddress", emailAddress);
        primaryEmail.put("optOut", optOut);
        data.put("primaryEmail", primaryEmail);

        JSONObject primaryAddress = new JSONObject();
        primaryAddress.put("city", city);
        primaryAddress.put("postalCode", postalCode);
        primaryAddress.put("country", country);
        data.put("primaryAddress", primaryAddress);

        JSONObject state = new JSONObject();
        state.put("name", stateName);
        state.put("order", order);
        data.put("state",state);

        payload.put("data", data);

        JSONObject response = Rest.jsonPost(baseUrl + "/users/user/api/create/", headers, payload);
        return booleanResponse(response);
    }

    private HashMap<String, String> getDefaultHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("ZURMO-SESSION-ID", sessionId);
        headers.put("ZURMO-TOKEN", token);
        headers.put("ZURMO-API-REQUEST-TYPE", "REST");
        return headers;
    }

    private static boolean booleanResponse(JSONObject response) {
        voidResponse(response);

        return true;
    }

    private static void voidResponse (JSONObject response) {
        if (response == null) {

            throw new IllegalStateException();

        } else if (!response.getString("status").equals("SUCCESS")) {

            throw new IllegalArgumentException(
                    response.getString("message") +
                            " Errors: " +
                            response.getJSONObject("errors").toString()
            );

        }
    }
}