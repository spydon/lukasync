package lukasync.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lukasync.Lukasync;
import lukasync.QueryBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

public class EvoposClient extends ServiceClient {

    public EvoposClient(JSONObject conf) {
        super(conf);
    }

    @Override
    protected void init() {}

    public JSONArray getNewContacts(String updateTime) {
        JSONObject result = new JSONObject();
        JSONArray contacts = new JSONArray();
        try {
            Connection conn = getConnection();
            PreparedStatement ps;
            QueryBuilder query = new QueryBuilder("short_name, pass_code, first_name, last_name, mobile, email, home_address, postcode, getdate() as retrieval_time",
                    "Operators INNER JOIN LSEmployees ON LSEmployees.operator_id = Operators.ID",
                    "operators.id>0",
                    "",
                    ""
                    );
            if (!lastUpdated.equals("0"))
                query.appendWhere("modified_date>" + "'" + updateTime + "'");
            ps = conn.prepareStatement(query.getQuery());

            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                result.put("retrievalTime", rs.getString("update_time"));
                rs.beforeFirst();
            }
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("username", rs.getString("short_name"));
                entry.put("password", rs.getString("pass_code"));
                entry.put("firstName", rs.getString("first_name"));
                entry.put("lastName", rs.getString("last_name"));
                entry.put("mobileName", rs.getString("mobile"));
                entry.put("emailAddress", rs.getString("email"));
                entry.put("city", rs.getString("home_address"));
                entry.put("postalCode", rs.getString("postcode"));
                contacts.put(entry);
            }
            updateTime(updateTime);
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(new JSONObject(result).toString());
        return null;
    }

    private Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(type + "://" + address + ";user=" + username
                    + ";password=" + password + ";databaseName=" + dbName + ";");
        } catch (SQLException e) {
            System.err.println("ERROR: The connection to " + address +
                    " timed out! \nCheck the internet connection or look for faulty lines in the CSV-file(" + Lukasync.CONF + ").");
            e.printStackTrace();
        }
        return conn;
    }
}