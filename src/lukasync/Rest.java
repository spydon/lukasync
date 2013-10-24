package lukasync;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

public class Rest {

	public static JSONObject jsonPost(String urlStr, HashMap<String, String> headers) {
		return jsonPost(urlStr, headers, null);
	}

	public static JSONObject jsonPost(String urlStr, HashMap<String, String> headers, JSONObject payload) {
		try {
			URI uri = new URI(urlStr);
			HttpPost post = new HttpPost(uri);
			if(!headers.containsKey("Content-Type"))
				post.addHeader("Content-Type", "application/json");
			if(!headers.containsKey("Accept"))
				post.addHeader("Accept", "application/json");

			for(String key : headers.keySet())
				if(!key.equalsIgnoreCase("accept") || !key.equalsIgnoreCase("content-type"))
					post.addHeader(key, headers.get(key));
			if(payload != null) {
				System.out.println(JSONUtil.jsonToURLEncoding(payload));
				System.out.println(URLEncoder.encode(JSONUtil.jsonToURLEncoding(payload), "ISO-8859-1"));
				StringEntity entity = new StringEntity(URLEncoder.encode(JSONUtil.jsonToURLEncoding(payload), "ISO-8859-1"));
				post.setEntity(entity);
			}
			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(post);
//			debugResponse(response);
			if(response.getStatusLine().getStatusCode() == 200) {
				StringWriter writer = new StringWriter();
				IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
				return new JSONObject(writer.toString());
			} else {
				throw new IllegalArgumentException(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private static void debugResponse(HttpResponse response) {
		System.out.println("\nDEBUG: ");
		System.out.println(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
		for(Header header : response.getAllHeaders())
			System.out.println(header);
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(writer.toString());
	}

}