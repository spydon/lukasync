package lukasync;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class JSONUtil {
    public static String jsonToURLEncoding(JSONObject json) {
        String output = "";
        String[] keys = JSONObject.getNames(json);
        for (String currKey : keys)
            output += jsonToURLEncodingAux(json.get(currKey), currKey);

        return output.substring(0, output.length() - 1);
    }

    private static String jsonToURLEncodingAux(Object json, String prefix) {
        String output = "";
        if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject) json;
            String[] keys = JSONObject.getNames(obj);
            for (String currKey : keys) {
                String subPrefix = prefix + "[" + currKey + "]";
                output += jsonToURLEncodingAux(obj.get(currKey), subPrefix);
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArr = (JSONArray) json;
            int arrLen = jsonArr.length();

            for (int i = 0; i < arrLen; i++) {
                String subPrefix = prefix + "[" + i + "]";
                Object child = jsonArr.get(i);
                output += jsonToURLEncodingAux(child, subPrefix);
            }
        } else {
            try {
                output =
                        URLEncoder.encode(prefix, "ISO-8859-1") +
                        "=" +
                        URLEncoder.encode(json.toString(), "ISO-8859-1") +
                        "&";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                // this should never be possible
                throw new IllegalStateException();
            }
        }

        return output;
    }
}
