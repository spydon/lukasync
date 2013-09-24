package lukasync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class Rest {
	
	private String sessionId, baseURL, username, password; 
	
	public Rest(MetaConnection conn) {
		this.baseURL = conn.getAddress();
		this.username = conn.getUsername();
		this.password = conn.getPassword();
		this.sessionId = login();
		System.out.println(sessionId);
	}

	/**
	 * This method logs in to the SugarCRM with a POST and returns the session
	 * id.
	 * 
	 * @return {@code String} session id
	 */

	public String login() {
		JSONObject jso = new JSONObject();
		jso.put("user_name", username);
		jso.put("password", password);
		jso.put("version","1");

		JSONObject jso2 = new JSONObject();
		jso2.put("user_auth", jso);
		jso2.put("application_name", "lukasync");
		
		String payload = jso2.toString();
		System.out.println(payload);

		System.out.println(baseURL
				+ "?method=login&input_type=JSON&response_type=JSON&rest_data=" + payload);
		//System.out.println(data);
		JSONObject jsondata = httpPost(baseURL
				+ "?method=login&input_type=JSON&response_type=JSON&rest_data=" + payload);
		String sessionid = jsondata.getString("id");	

		return sessionid;
	}
	
	public JSONObject query(String module, String query) {
		JSONObject jso = new JSONObject();
		jso.put("session", sessionId);
		jso.put("module_name", module);
		jso.put("query", query);
		//jso.put("order_by", "");
		//jso.put("offset", 0);
		//jso.put("select_fields", "[\"id\",\"name\"]");
		//:,"link_name_to_fields_array":[{"name":"contacts","value":["id","email1","name","title","phone_work","description"]}],"max_results":20,"deleted":"FALSE"}method=logout&input_type=JSON&response_type=JSON&rest_data={"session":"iov5a257lk5acsg9l3ll6kuej3"}
		System.out.println(baseURL);
		System.out.println(jso.toString());
		JSONObject jsondata = httpPost(baseURL + "?method=get_entry_list&input_type=JSON&response_type=JSON&rest_data=" + jso.toString());
		System.out.println(jsondata.toString());
		return jsondata;
	}

	public JSONObject httpPost(String urlStr) {
		String res = null;
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setAllowUserInteraction(false);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			if (conn.getResponseCode() != 200) {
				throw new IOException(conn.getResponseCode() + " " + conn.getResponseMessage());
			}

			// Buffer the result into a string
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			conn.disconnect();
			res = sb.toString();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JSONObject(res);
	}
}