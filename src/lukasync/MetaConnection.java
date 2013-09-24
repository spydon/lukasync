package lukasync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MetaConnection {
	private String type, address, dbName, username, password;

	public MetaConnection(String type, String address, String dbName, String username, String password) {
		this.type = type;
		this.address = address;
		this.dbName = dbName;
		this.username = username;
		this.password = password;		
	}
	
	public Connection getConnection() {
		Connection conn = null;
        try {
	        conn = DriverManager.getConnection(type + "://" + address + ";user=" + username
                    + ";password=" + password + ";databaseName=" + dbName + ";");
		} catch (SQLException e) {
			System.err.println("ERROR: The connection to " + getAddress() + 
					" timed out! \nCheck the internet connection or look for faulty lines in the CSV-file(" + Lukasync.CONF + ").");
			e.printStackTrace();
		}
        return conn;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getDbName() {
		return dbName;
	}
}