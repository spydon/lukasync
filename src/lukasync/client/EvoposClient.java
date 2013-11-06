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

    private final QueryBuilder contactQuery, userQuery, salesQuery, contactRelationQuery;

    public EvoposClient(JSONObject conf) {
        super(conf);
        this.contactQuery = new QueryBuilder(
                "Contacts.name, firstname, surname, mobile_phone, email, address_1, address_2, town, state, code, "
                + "formatted_address, country, customer_No, send_mail, staff_id, date_created, Contacts_Persons.modified_date, getdate() as imported_at",
                "Contacts_Persons INNER JOIN Contacts ON Contacts.id = Contacts_Persons.contact_id",
                "",
                "",
                "Contacts_Persons.modified_date asc"
                );
        this.userQuery = new QueryBuilder(
                "short_name, pass_code, first_name, last_name, mobile, email, home_address, postcode, date_signed, LSEmployees.modified_date, getdate() as imported_at",
                "Operators INNER JOIN LSEmployees ON LSEmployees.operator_id = Operators.id",
                "operators.id>0",
                "",
                ""
                );
        this.salesQuery = new QueryBuilder(
                "Sales_Transactions_Lines.transaction_no, part_no, description, Sales_Transactions_Lines.gross, "
                + "soldto_id, Sales_Transactions_Header.modified_date, getdate() as imported_at",
                "Sales_Transactions_Lines INNER JOIN Sales_Transactions_Header ON Sales_Transactions_Lines.transaction_no = Sales_Transactions_Header.transaction_no",
                "Sales_Transactions_Header.modified_date > 0 AND Sales_Transactions_Lines.gross >= 0 AND "
                + "Sales_Transactions_Header.sales_type = 'INVOICE' AND part_no <> '.GADJUSTMENT' AND soldto_id > 10",
                "Sales_Transactions_Lines.transaction_no, part_no, description, Sales_transactions_Lines.gross, soldto_id, Sales_Transactions_Header.modified_date ",
                "Sales_Transactions_Lines.transaction_no");

        this.contactRelationQuery = new QueryBuilder(
                "H.soldto_id, H.modified_date, H.operator_id, H.transaction_no, getdate() as imported_at FROM sales_transactions_header H",
                "INNER JOIN (SELECT soldto_id, MIN(modified_date) As first_occurence FROM Sales_Transactions_Header "
                + "WHERE soldto_id > 10 GROUP BY soldto_id) X ON H.soldto_id = X.soldto_id AND H.modified_date = X.first_occurence",
                "",
                "",
                "soldto_id");

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
                contactQuery.setWhere("DATEADD(ss, 5, date_created) > Contacts_Persons.modified_date");
            else
                contactQuery.setWhere("DATEADD(ss, 5, date_created) < Contacts_Persons.modified_date");

            contactQuery.appendWhere("modified_date>" + "'" + updateTime + "'");
            ps = conn.prepareStatement(contactQuery.getQuery());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("lastName", rs.getString("name"));
                entry.put("mobilePhone", rs.getString("mobile_mhone"));
                entry.put("companyName", name);

                JSONObject primaryEmail = new JSONObject();
                primaryEmail.put("emailAddress", rs.getString("email"));
                primaryEmail.put("optOut", rs.getString("send_mail"));
                entry.put("primaryEmail", primaryEmail);

                JSONObject primaryAddress = new JSONObject();
                primaryAddress.put("street1", rs.getString("address_1"));
                primaryAddress.put("street2", rs.getString("address_2"));
                primaryAddress.put("city", rs.getString("town"));
                primaryAddress.put("state", rs.getString("state"));
                primaryAddress.put("postalCode", rs.getString("code"));
                primaryAddress.put("country", rs.getString("country"));
                entry.put("primaryAddress", primaryAddress);
                entry.put("modified_at", rs.getString("modified_date"));
                entry.put("created_at", rs.getString("date_created"));
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
                userQuery.setWhere("DATEADD(ss, 5, date_signed) > LSEmployees.modified_date");
            else
                userQuery.setWhere("DATEADD(ss, 5, date_signed) < LSEmployees.modified_date");

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
                entry.put("modified_at", rs.getString("modified_date"));
                entry.put("created_at", rs.getString("date_signed"));
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

    public JSONArray getNewContactRelations(String updateTime) {
        JSONArray contactRelations = new JSONArray();
        try {
            Connection conn = getConnection();
            PreparedStatement ps;

            contactRelationQuery.appendWhere("H.modified_date>" + "" + updateTime + "");
            System.out.println("getNewContactRelations(), contactRelationQuery: " + contactRelationQuery.getQuery());
            ps = conn.prepareStatement(contactRelationQuery.getQuery());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("transaction_no", rs.getString("transaction_to"));
                entry.put("operator_id", rs.getString("operator_id"));
                entry.put("customer_id", rs.getString("soldto_id"));
                entry.put("modified_at", rs.getString("modified_date"));
                entry.put("imported_at", rs.getString("imported_at"));
                contactRelations.put(entry);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contactRelations;
    }

    public JSONArray getNewSales(String updateTime) {
        JSONArray sales = new JSONArray();
        try {
            Connection conn = getConnection();
            PreparedStatement ps;

            userQuery.appendWhere("Sales_Transactions_Header.modified_date>" + "" + updateTime + "");
            System.out.println("getNewSales(), salesQuery: " + salesQuery.getQuery());
            ps = conn.prepareStatement(salesQuery.getQuery());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("transaction_no", rs.getString("transaction_to"));
                entry.put("part_no", rs.getString("part_no"));
                entry.put("description", rs.getString("description"));
                entry.put("gross", rs.getString("gross"));
                entry.put("customer_id", rs.getString("soldto_id"));
                entry.put("modified_at", rs.getString("modified_date"));
                entry.put("imported_at", rs.getString("imported_at"));
                sales.put(entry);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
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