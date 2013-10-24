package lukasync;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class ZurmoClient {
    private final String sessionId;
    private final String token;
    private final String baseUrl;

    private ZurmoClient(String baseUrl, String sessionId, String token) {
        this.baseUrl = baseUrl;
        this.sessionId = sessionId;
        this.token = token;
    }

    public static ZurmoClient build(MetaConnection conn) {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("ZURMO-AUTH-USERNAME", conn.getUsername());
        headers.put("ZURMO-AUTH-PASSWORD", conn.getPassword());
        headers.put("ZURMO-API-REQUEST-TYPE", "REST");
        String baseUrl = conn.getAddress();
        JSONObject response = Rest.jsonPost(baseUrl + "/zurmo/api/login", headers);
        if(response != null && response.getString("status").equals("SUCCESS")) {
            JSONObject data = response.getJSONObject("data");
            String sessionId = data.getString("sessionId");
            String token = data.getString("token");
            return new ZurmoClient(baseUrl, sessionId, token);
        } else {
            //TODO
            throw new IllegalArgumentException();
        }
    }

    public boolean createNote(int userId, String userName, int contactId, String description, Date date2) {
        HashMap<String, String> headers = getDefaultHeaders();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = sdf.format(date2).toString();
        JSONObject payload = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("description", description);
        data.put("occurredOnDateTime", date);
        JSONObject owner = new JSONObject();
        owner.put("id", userId);
        owner.put("username", userName);
        data.put("owner", owner);
        payload.put("data", data);
        System.out.println(payload.toString());
        System.out.println(JSONUtil.jsonToURLEncoding(payload));
        JSONObject response = Rest.jsonPost(baseUrl + "/notes/note/api/create/", headers, payload);
        if(response == null) {
            throw new IllegalStateException();
        } else if(!response.getString("status").equals("SUCCESS")) {
            throw new IllegalArgumentException(response.toString());
        } else {
            JSONObject responseData = response.getJSONObject("data");
            int noteId = responseData.getInt("id");
            createNoteRelation(noteId, contactId);
        }
        return true;
    }

    private void createNoteRelation(int noteId, int customerId) {
        HashMap<String, String> headers = getDefaultHeaders();
        JSONObject payload = new JSONObject();
        JSONObject modelRelations = new JSONObject();
        JSONArray notes = new JSONArray();
        JSONObject relation = new JSONObject();
        relation.put("action", "add");
        relation.put("modelId", noteId);
        notes.put(relation);
        modelRelations.put("notes", notes);
        payload.put("modelRelations", modelRelations);
        JSONObject response = Rest.jsonPost(baseUrl + "/contacts/contact/api/update/" + customerId, headers, payload);
        if(response == null || !response.getString("status").equals("SUCCESS")) {
            throw new IllegalArgumentException();
        }
    }

    public boolean createContact(
    		int ownerId,
            String username,

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
        JSONObject payload = new JSONObject();
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
        owner.put("username", username);
        data.put("owner", owner);

        payload.put("data", data);
        JSONObject response = Rest.jsonPost(baseUrl + "/contacts/contact/api/create/", headers, payload);
        if(response == null) {
            throw new IllegalStateException();
        } else if(!response.getString("status").equals("SUCCESS")) {
            throw new IllegalArgumentException(response.toString());
        }
        return true;
    }

//    private void createContactRelation(int contactId, int ownerId) {
//        HashMap<String, String> headers = getDefaultHeaders();
//        JSONObject payload = new JSONObject();
//        JSONObject modelRelations = new JSONObject();
//        JSONArray notes = new JSONArray();
//        JSONObject relation = new JSONObject();
//        relation.put("action", "add");
//        relation.put("modelId", contactId);
//        notes.put(relation);
//        modelRelations.put("notes", notes);
//        payload.put("modelRelations", modelRelations);
//        JSONObject response = Rest.jsonPost(baseUrl + "/contacts/contact/api/update/" + customerId, headers, payload);
//        if(response == null || !response.getString("status").equals("SUCCESS")) {
//            throw new IllegalArgumentException();
//        }
//    }

    private HashMap<String, String> getDefaultHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        System.out.println(sessionId + " " + token);
        headers.put("ZURMO-SESSION-ID", sessionId);
        headers.put("ZURMO-TOKEN", token);
        headers.put("ZURMO-API-REQUEST-TYPE", "REST");
        return headers;
    }
}