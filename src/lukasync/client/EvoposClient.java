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

    private final QueryBuilder contactQuery, userQuery;

    public EvoposClient(JSONObject conf) {
        super(conf);
        this.contactQuery = new QueryBuilder(
                "Contacts.Name, FirstName, SurName, Mobile_Phone, Email, Address_1, Address_2, Town, State, Code, "
                + "Formatted_Address, Country, Customer_No, Send_Mail, Staff_ID, Date_Created, Contacts_Persons.Modified_Date, GETDATE() as Imported_At",
                "Contacts_Persons INNER JOIN Contacts ON Contacts.ID = Contacts_Persons.Contact_ID",
                "",
                "",
                "Contacts_Persons.Modified_Date asc"
                );
        this.userQuery = new QueryBuilder("short_name, pass_code, first_name, last_name, mobile, email, home_address, postcode, date_signed, LSEmployees.Modified_Date, getdate() as Imported_At",
                "Operators INNER JOIN LSEmployees ON LSEmployees.operator_id = Operators.ID",
                "operators.id>0",
                "",
                ""
                );
    }

    @Override
    protected void init() {}

    public JSONArray getNewContacts(String updateTime) {
        return getContacts(true, updateTime);
    }

    public JSONArray getUpdatedContacts(String updateTime) {
        return getContacts(false, updateTime);
    }

    private JSONArray getContacts(boolean isNew, String updateTime) {
        JSONArray contacts = new JSONArray();
        try {
            Connection conn = getConnection();
            PreparedStatement ps;
            if(isNew)
                contactQuery.setWhere("DATEADD(ss, 5, Date_Created) > Contacts_Persons.Modified_Date");
            else
                contactQuery.setWhere("DATEADD(ss, 5, Date_Created) < Contacts_Persons.Modified_Date");

            contactQuery.appendWhere("modified_date>" + "'" + updateTime + "'");
            ps = conn.prepareStatement(contactQuery.getQuery());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("lastName", rs.getString("Name"));
                entry.put("mobilePhone", rs.getString("Mobile_Phone"));
                entry.put("companyName", name);

                JSONObject primaryEmail = new JSONObject();
                primaryEmail.put("emailAddress", rs.getString("Email"));
                primaryEmail.put("optOut", rs.getString("Send_Mail"));
                entry.put("primaryEmail", primaryEmail);

                JSONObject primaryAddress = new JSONObject();
                primaryAddress.put("street1", rs.getString("Address_1"));
                primaryAddress.put("street2", rs.getString("Address_2"));
                primaryAddress.put("city", rs.getString("Town"));
                primaryAddress.put("state", rs.getString("state"));
                primaryAddress.put("postalCode", rs.getString("code"));
                primaryAddress.put("country", rs.getString("country"));
                entry.put("primaryAddress", primaryAddress);
                entry.put("modified_at", rs.getString("Modified_Date"));
                entry.put("created_at", rs.getString("Date_Created"));
                entry.put("imported_at", rs.getString("imported_at"));
                entry.put("companyName", name);
                contacts.put(entry);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contacts;
    }

    public JSONArray getNewUsers(String updateTime) {
        return getUsers(true, updateTime);
    }

    public JSONArray getUpdatedUsers(String updateTime) {
        return getUsers(false, updateTime);
    }

    private JSONArray getUsers(boolean isNew, String updateTime) {
        JSONArray contacts = new JSONArray();
        try {
            Connection conn = getConnection();
            PreparedStatement ps;

            if(isNew)
                userQuery.setWhere("DATEADD(ss, 5, Date_Signed) > LSEmployees.Modified_Date");
            else
                userQuery.setWhere("DATEADD(ss, 5, Date_Signed) < LSEmployees.Modified_Date");

            userQuery.appendWhere("LSEmployees.Modified_Date>" + "" + updateTime + "");
            System.out.println("getUsers(), userQuery: " + userQuery.getQuery());
            ps = conn.prepareStatement(userQuery.getQuery());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("username", rs.getString("short_name"));
                entry.put("password", rs.getString("pass_code"));
                entry.put("firstName", rs.getString("first_name"));
                entry.put("lastName", rs.getString("last_name"));
                entry.put("mobile", rs.getString("mobile"));
                entry.put("email", rs.getString("email"));
                entry.put("city", rs.getString("home_address"));
                entry.put("postalCode", rs.getString("postcode"));
                entry.put("modified_at", rs.getString("Modified_Date"));
                entry.put("created_at", rs.getString("Date_Signed"));
                entry.put("imported_at", rs.getString("imported_at"));
                contacts.put(entry);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contacts;
    }

    private Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(connectionType + "://" + address + ";user=" + username
                    + ";password=" + password + ";databaseName=" + databaseName + ";");
        } catch (SQLException e) {
            System.err.println("ERROR: The connection to " + address +
                    " timed out! \nCheck the internet connection or look for faulty lines in the CSV-file(" + Lukasync.DB + ").");
            e.printStackTrace();
        }
        return conn;
    }
}