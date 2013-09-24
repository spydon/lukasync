package lukasync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import org.json.JSONObject;

public class POS {
	
	private String baseURL, dbName, username, password; 
	private Connection conn;
	
	public POS(MetaConnection conn) {
		this.baseURL = conn.getAddress();
		this.dbName = conn.getDbName();
		this.username = conn.getUsername();
		this.password = conn.getPassword();
		this.conn = conn.getConnection();
	}
	
	public JSONObject query() {
		LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT ID, Name from contacts");
			System.out.println("1");
		    ResultSet rs = ps.executeQuery();
			System.out.println("2");
		    while(rs.next()) {
		    	System.out.println(rs.getString("Name") + " " + rs.getInt("ID"));
		    	result.put(rs.getString("Name"), rs.getInt("ID"));
		    }
		    rs.close();
		    ps.close();
		    conn.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		System.out.println(new JSONObject(result).toString());
		return new JSONObject(result);
	}
}