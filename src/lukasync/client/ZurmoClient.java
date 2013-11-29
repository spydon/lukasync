package lukasync.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import lukasync.Lukasync;
import lukasync.util.JSONUtil;
import lukasync.util.Rest;

import org.json.JSONArray;
import org.json.JSONObject;

public class ZurmoClient extends ServiceClient{
    //private static String DEFAULT_USER_COUNTRY = "Australia"; // TO ... DO(?) remove this...?

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

            if (Lukasync.PRINT_DEBUG) {
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

    public int getUserIdByUsername (String username) {
        JSONObject pagination = new JSONObject();
        pagination.put("page", 1);
        pagination.put("pageSize", 1);

        JSONObject search = new JSONObject();
        search.put("username", username.toLowerCase());

        JSONObject searchFilter = new JSONObject();
        searchFilter.put("pagination", pagination);
        searchFilter.put("search", search);

        searchFilter.put("sort", "username.asc");

        HashMap<String, String> headers = getDefaultHeaders();
        String searchFilterString = JSONUtil.jsonToURLEncoding(searchFilter);

        JSONObject response = Rest.jsonGet(baseUrl + "/users/user/api/list/filter/" + searchFilterString, headers);
        if (response == null) {

            throw new IllegalStateException("Response from Rest was null.");

        } else if (!response.getString("status").equals("SUCCESS")) {

            throw new IllegalArgumentException(
                    response.getString("message") +
                            " Errors: " +
                            response.getJSONObject("errors").toString()
            );

        } else if (response.getJSONObject("data").has("items")) {

            return response.getJSONObject("data").getJSONArray("items").getJSONObject(0).getInt("id");

        } else {

            return -1;

        }
    }

    public boolean createUser (JSONObject user) {
        return createUser(
                user.getString("username"),
                user.getString("password"),
                user.getString("firstName"),
                user.getString("lastName"),
                user.getString("mobilePhone"),
                user.getString("emailAddress"),
                user.getString("city"),
                user.getString("postalCode")
                //,
                //DEFAULT_USER_COUNTRY
        );
    }

    public boolean createUser(
            String username,
            String password,

            String firstName,
            String lastName,
            String mobilePhone,

            String emailAddress,

            String city,
            String postalCode
            //,
            //String country
    ) {
        JSONObject primaryEmail = new JSONObject();
        primaryEmail.put("emailAddress", emailAddress);
        primaryEmail.put("optOut", "0");

        JSONObject primaryAddress = new JSONObject();
        primaryAddress.put("city", city);
        primaryAddress.put("postalCode", postalCode);
        //primaryAddress.put("country", country);

        JSONObject data = new JSONObject();
        data.put("primaryEmail", primaryEmail);
        data.put("primaryAddress", primaryAddress);

        data.put("username", username.toLowerCase());
        data.put("password", password);
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("mobilePhone", normalizePhone(mobilePhone));

        JSONObject payload = new JSONObject();
        payload.put("data", data);

        HashMap<String, String> headers = getDefaultHeaders();

        JSONObject response = Rest.jsonPost(baseUrl + "/users/user/api/create/", headers, payload);
        return booleanResponse(response);
    }

    public boolean updateUser (int userId, JSONObject user) {
        return updateUser(
                userId,
                user.getString("password"),
                user.getString("firstName"),
                user.getString("lastName"),
                user.getString("mobilePhone"),
                user.getString("emailAddress"),
                user.getString("city"),
                user.getString("postalCode")
        );
    }

    public boolean updateUser (
            int userId,
            String password,

            String firstName,
            String lastName,
            String mobilePhone,

            String emailAddress,

            String city,
            String postalCode) {
        JSONObject primaryEmail = new JSONObject();
        primaryEmail.put("emailAddress", emailAddress);

        JSONObject primaryAddress = new JSONObject();
        primaryAddress.put("city", city);
        primaryAddress.put("postalCode", postalCode);

        JSONObject data = new JSONObject();
        data.put("primaryEmail", primaryEmail);
        data.put("primaryAddress", primaryAddress);

        data.put("password", password);
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("mobilePhone", normalizePhone(mobilePhone));

        JSONObject payload = new JSONObject();
        payload.put("data", data);

        HashMap<String, String> headers = getDefaultHeaders();

        JSONObject response = Rest.jsonPut(baseUrl + "/users/user/api/update/" + userId, headers, payload);
        return booleanResponse(response);
    }

    public int getContactIdByCustomerNo (String customerNo) {
        JSONObject pagination = new JSONObject();
        pagination.put("page", 1);
        pagination.put("pageSize", 1);

        JSONObject search = new JSONObject();
        search.put("officePhone", customerNo);

        JSONObject searchFilter = new JSONObject();
        searchFilter.put("pagination", pagination);
        searchFilter.put("search", search);

        searchFilter.put("sort", "officePhone.asc");

        HashMap<String, String> headers = getDefaultHeaders();
        String searchFilterString = JSONUtil.jsonToURLEncoding(searchFilter);

        JSONObject response = Rest.jsonGet(baseUrl + "/contacts/contact/api/list/filter/" + searchFilterString, headers);
        if (response == null) {

            throw new IllegalStateException("Response from Rest was null.");

        } else if (!response.getString("status").equals("SUCCESS")) {

            String errorSuffix = "";
            if (response.has("errors") && response.get("errors") != JSONObject.NULL) {
                errorSuffix = " Errors: " + response.getJSONObject("errors").toString();
            }

            throw new IllegalArgumentException(
                    response.getString("message") + errorSuffix
            );

        } else {

            if (response.getJSONObject("data").getInt("totalCount") < 1) {
                return -1;
            } else {
                return response.getJSONObject("data").getJSONArray("items").getJSONObject(0).getInt("id");
            }

        }
    }

    public boolean createContact(JSONObject contact) {
        JSONObject primaryEmail = contact.getJSONObject("primaryEmail");
        JSONObject primaryAddress = contact.getJSONObject("primaryAddress");

        return createContact(
                1,
                contact.getString("firstName"),
                contact.getString("lastName"),
                contact.getString("mobilePhone"),
                contact.getString("department"),
                contact.getString("customerNo"),
                primaryEmail.getString("emailAddress"),
                primaryEmail.getString("optOut"),
                primaryAddress.getString("street1"),
                primaryAddress.getString("street2"),
                primaryAddress.getString("city"),
                primaryAddress.getString("state"),
                primaryAddress.getString("postalCode"),
                primaryAddress.getString("country")
        );
    }

    public boolean createContact(
            int ownerId,

            String firstName,
            String lastName,
            String mobilePhone,
            String department,
            String customerNo,

            String emailAddress,
            String optOut,

            String street1,
            String street2,
            String city,
            String state,
            String postalCode,
            String country
    ) {
        JSONObject primaryEmail = new JSONObject();
        primaryEmail.put("emailAddress", emailAddress);
        primaryEmail.put("optOut", optOut);

        JSONObject primaryAddress = new JSONObject();
        primaryAddress.put("street1", street1);
        primaryAddress.put("street2", street2);
        primaryAddress.put("city", city);
        primaryAddress.put("state", state);
        primaryAddress.put("postalCode", postalCode);
        primaryAddress.put("country", country);

        JSONObject owner = new JSONObject();
        owner.put("id", ownerId);

        JSONObject status = new JSONObject();
        status.put("id", 6);

        JSONObject data = new JSONObject();
        data.put("primaryEmail", primaryEmail);
        data.put("primaryAddress", primaryAddress);
        data.put("owner", owner);
        data.put("state", status);

        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("mobilePhone", normalizePhone(mobilePhone));
        data.put("department", department);
        data.put("officePhone", customerNo);

        JSONObject payload = new JSONObject();
        payload.put("data", data);

        HashMap<String, String> headers = getDefaultHeaders();

        JSONObject response = Rest.jsonPost(baseUrl + "/contacts/contact/api/create/", headers, payload);
        return booleanResponse(response);
    }

    public boolean updateContact (
            int contactId,
            JSONObject contact
    ) {
        JSONObject primaryEmail = contact.getJSONObject("primaryEmail");
        JSONObject primaryAddress = contact.getJSONObject("primaryAddress");

        return updateContact(
                contactId,
                contact.getString("firstName"),
                contact.getString("lastName"),
                contact.getString("mobilePhone"),
                contact.getString("department"),
                contact.getString("customerNo"),
                primaryEmail.getString("emailAddress"),
                primaryEmail.getString("optOut"),
                primaryAddress.getString("street1"),
                primaryAddress.getString("street2"),
                primaryAddress.getString("city"),
                primaryAddress.getString("postalCode"),
                primaryAddress.getString("country")
        );
    }

    public boolean updateContact(
            int contactId,

            String firstName,
            String lastName,
            String mobilePhone,
            String department,
            String customerNo,

            String emailAddress,
            String optOut,

            String street1,
            String street2,
            String city,
            String postalCode,
            String country
    ) {
        JSONObject primaryEmail = new JSONObject();
        primaryEmail.put("emailAddress", emailAddress);
        primaryEmail.put("optOut", optOut);

        JSONObject primaryAddress = new JSONObject();
        primaryAddress.put("street1", street1);
        primaryAddress.put("street2", street2);
        primaryAddress.put("city", city);
        primaryAddress.put("postalCode", postalCode);
        primaryAddress.put("country", country);

        JSONObject data = new JSONObject();
        data.put("primaryEmail", primaryEmail);
        data.put("primaryAddress", primaryAddress);

        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("mobilePhone", normalizePhone(mobilePhone));
        data.put("department", department);
        data.put("officePhone", customerNo);

        JSONObject payload = new JSONObject();
        payload.put("data", data);

        HashMap<String, String> headers = getDefaultHeaders();

        JSONObject response = Rest.jsonPut(baseUrl + "/contacts/contact/api/update/" + contactId, headers, payload);
        return booleanResponse(response);
    }

    @SuppressWarnings("UnusedDeclaration")
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

    public int getOwnerIdByContactId (int contactId) {
        HashMap<String, String> headers = getDefaultHeaders();

        JSONObject response = Rest.jsonGet(baseUrl + "/contacts/contact/api/read/" + contactId, headers);
        if (response == null) {

            // this crash is ok because it means that we couldn't even reach the API, so we don't know anything.
            throw new IllegalStateException("Response from Rest was null.");

        } else if (!response.getString("status").equals("SUCCESS")) {

            return -1;

        } else {

            return response.getJSONObject("data").getJSONObject("owner").getInt("id");

        }
    }

    public boolean createNote(int ownerId, int contactId, String description, String dateUnsafe) {
        HashMap<String, String> headers = getDefaultHeaders();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        String dateSafe;
        try {
            dateSafe = sdf.format(sdf.parse(dateUnsafe));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        JSONObject relation = new JSONObject();
        relation.put("action", "add");
        relation.put("modelId", contactId);
        relation.put("modelClassName", "Contact");

        JSONArray contacts = new JSONArray();
        contacts.put(relation);

        JSONObject modelRelations = new JSONObject();
        modelRelations.put("activityItems", contacts);

        JSONObject owner = new JSONObject();
        owner.put("id", ownerId);

        JSONObject data = new JSONObject();
        data.put("description", description);
        data.put("occurredOnDateTime", dateSafe);
        data.put("modelRelations", modelRelations);
        data.put("owner", owner);

        JSONObject payload = new JSONObject();
        payload.put("data", data);

        if (Lukasync.PRINT_DEBUG) {
            System.out.println(payload.toString());
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

    private static String normalizePhone (String mobilePhone) {
        return (mobilePhone.length() > 24) ? mobilePhone.substring(0, 24) : mobilePhone;
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

            String errorSuffix = "";
            if (response.has("errors") && response.get("errors") != JSONObject.NULL) {
                errorSuffix = " Errors: " + response.getJSONObject("errors").toString();
            }
            throw new IllegalArgumentException(
                    response.getString("message") + errorSuffix
            );

        }
    }
}