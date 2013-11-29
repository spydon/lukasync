package lukasync.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lukasync.Lukasync;
import lukasync.util.JSONUtil;
import lukasync.util.QueryBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

public class EvoposClient extends ServiceClient {

    private static final String SALES_QUERY_WHERE_BASE = "Sales_Transactions_Lines.gross >= 0 " +
            "AND Sales_Transactions_Header.sales_type = 'INVOICE' " +
            "AND part_no <> '.GADJUSTMENT' " +
            "AND soldto_id > 10 " +
            "AND LEN(customer_no) > 11";

    private final QueryBuilder contactQuery, userQuery, salesQuery, contactRelationQuery;

    public EvoposClient(JSONObject conf) {
        super(conf);
        this.userQuery = new QueryBuilder(
                "short_name as username, pass_code, first_name, last_name, mobile, email, home_address, postcode, " +
                        "date_signed, LSEmployees.modified_date",
                "Operators INNER JOIN LSEmployees ON LSEmployees.operator_id = Operators.id",
                "operators.id>0",
                "",
                ""
                );
        this.contactQuery = new QueryBuilder(
                "Contacts.customer_no, Contacts.name, firstname, surname, mobile_phone, email, address_1, address_2, town, state, code, "
                + "formatted_address, country, customer_No, send_mail, staff_id, date_created, Contacts_Persons.modified_date",
                "Contacts_Persons "
                + "INNER JOIN Contacts ON Contacts.id = Contacts_Persons.contact_id",
                "",
                "",
                "Contacts_Persons.modified_date asc"
                );
        this.contactRelationQuery = new QueryBuilder(
                "Customer_No, Short_Name as username, P.Modified_Date",
                "(SELECT X.Customer_No, H.SoldTo_ID, H.Modified_Date, H.Operator_ID, H.Transaction_No "
                + "FROM Sales_Transactions_Header H "
                + "INNER JOIN (SELECT Customer_No, SoldTo_ID, MIN(Sales_Transactions_Header.Modified_Date) As first_occurence "
                + "FROM Sales_Transactions_Header "
                + "INNER JOIN Contacts ON SoldTo_ID = Contacts.ID WHERE SoldTo_ID > 10 "
                + "GROUP BY SoldTo_ID, Customer_No) X ON H.SoldTo_ID = X.SoldTo_ID AND H.Modified_Date = X.first_occurence) P "
                + "INNER JOIN Operators ON Operator_ID = Operators.ID",
                "",
                "",
                "");
        this.salesQuery = new QueryBuilder(
                "Sales_Transactions_Lines.transaction_no, part_no, description, Sales_Transactions_Lines.gross, "
                + "Customer_No, Qty, Sales_Transactions_Header.sale_date, Sales_Transactions_Header.Modified_Date",
                "Sales_Transactions_Lines "
                + "INNER JOIN Sales_Transactions_Header ON Sales_Transactions_Lines.transaction_no = Sales_Transactions_Header.transaction_no "
                + "INNER JOIN Contacts ON soldto_id = Contacts.id",
                SALES_QUERY_WHERE_BASE,
                "",
                "Sales_Transactions_Header.modified_date, Sales_Transactions_Lines.transaction_no");
    }

    @Override
    protected void init() {}

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
                userQuery.setWhere("DATEADD(ss, 5, date_signed) > lsemployees.modified_date");
            else
                userQuery.setWhere("DATEADD(ss, 5, date_signed) < lsemployees.modified_date");

            userQuery.appendWhere("lsemployees.modified_date>" + "'" + updateTime + "'");
            System.out.println("getUsers(), userQuery: " + userQuery.getQuery());
            ps = conn.prepareStatement(userQuery.getQuery());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("username", rs.getString("username"));
                entry.put("password", rs.getString("pass_code"));
                entry.put("firstName", rs.getString("first_name"));
                entry.put("lastName", rs.getString("last_name"));
                entry.put("mobilePhone", rs.getString("mobile"));
                entry.put("emailAddress", rs.getString("email"));
                entry.put("city", rs.getString("home_address"));
                entry.put("postalCode", rs.getString("postcode"));
                entry.put("modified_at", rs.getString("modified_date"));
                entry.put("created_at", rs.getString("date_signed"));
//                entry.put("imported_at", rs.getString("imported_at"));
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

            contactQuery.setWhere("LEN(customer_no) > 11"); // XXX: Keep in sync with the contactRelationQuery length

            if(isNew)
                contactQuery.appendWhere("DATEADD(dd, 2, date_created) > Contacts_Persons.modified_date");
            else
                contactQuery.appendWhere("DATEADD(dd, 2, date_created) < Contacts_Persons.modified_date");

            updateTime = updateTime.equals("0") ? "1990-12-31" : updateTime;
            contactQuery.appendWhere("Contacts_Persons.modified_date>" + "'" + updateTime + "'");
            System.out.println("DEBUG: contactQuery - " + contactQuery.getQuery());
            ps = conn.prepareStatement(contactQuery.getQuery());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                JSONUtil.putString(entry, "firstName", rs.getString("firstname"));
                JSONUtil.putString(entry, "lastName", rs.getString("name"));
                JSONUtil.putString(entry, "mobilePhone", rs.getString("mobile_phone"));
                JSONUtil.putString(entry, "department", name);
                JSONUtil.putString(entry, "customerNo", rs.getString("customer_no"));

                JSONObject primaryEmail = new JSONObject();
                JSONUtil.putString(primaryEmail, "emailAddress", rs.getString("email"));
                primaryEmail.put("optOut", rs.getString("send_mail") == "1" ? "0" : "1");
                entry.put("primaryEmail", primaryEmail);

                JSONObject primaryAddress = new JSONObject();
                JSONUtil.putString(primaryAddress, "street1", rs.getString("address_1"));
                JSONUtil.putString(primaryAddress, "street2", rs.getString("formatted_address"));
                JSONUtil.putString(primaryAddress, "city", rs.getString("town"));
                JSONUtil.putString(primaryAddress, "state", rs.getString("state"));
                JSONUtil.putString(primaryAddress, "postalCode", rs.getString("code"));
                JSONUtil.putString(primaryAddress, "country", rs.getString("country"));
                entry.put("primaryAddress", primaryAddress);
                JSONUtil.putString(entry, "modified_at", rs.getString("modified_date"));
//                JSONUtil.putString(entry, "created_at", rs.JSONUtil.putString("date_created"));
//                JSONUtil.putString(entry, "imported_at", rs.getString("imported_at"));
//                JSONUtil.putString(entry, "companyName", name);
                lukasync.util.JSONUtil.prettyPrint(entry);
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

            contactRelationQuery.setWhere("LEN(customer_no) > 11"); // XXX: Keep in sync with the contactQuery length

            updateTime = updateTime.equals("0") ? "1990-12-31" : updateTime;
            contactRelationQuery.appendWhere("P.modified_date>" + "'" + updateTime + "'");
            System.out.println("getNewContactRelations(), contactRelationQuery: " + contactRelationQuery.getQuery());
            ps = conn.prepareStatement(contactRelationQuery.getQuery());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("customer_no", rs.getString("customer_no"));
                entry.put("username", rs.getString("username"));
                entry.put("modified_at", rs.getString("modified_date"));
//                entry.put("imported_at", rs.getString("imported_at"));
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

            updateTime = updateTime.equals("0") ? "1990-12-31" : updateTime;
            salesQuery.setWhere(SALES_QUERY_WHERE_BASE);
            salesQuery.appendWhere("Sales_Transactions_Header.modified_date>" + "'" + updateTime + "'");
            System.out.println("getNewSales(), salesQuery: " + salesQuery.getQuery());
            ps = conn.prepareStatement(salesQuery.getQuery());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("transaction_no", rs.getString("transaction_no"));
                entry.put("part_no", rs.getString("part_no"));
                entry.put("qty", rs.getString("qty"));
                entry.put("description", rs.getString("description"));
                entry.put("gross", rs.getString("gross"));
                entry.put("customer_no", rs.getString("customer_no"));
                entry.put("sold_at", rs.getString("sale_date"));
                entry.put("modified_at", rs.getString("modified_date"));
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

    public void insertNewSale(JSONObject sale) {
        insertNewSale(sale.getString("username"), sale.getString("gross"), sale.getString("createdAt"));
    }

    public void insertNewSale(String username, String gross, String saleDate) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps;

            ps = conn.prepareStatement(
                    "INSERT INTO Sales_Transactions_Header "
                        + "(Transaction_No, "
                        + "Sub_Type_ID, "
                        + "Type_Description, "
                        + "Sales_Sub_Type, "
                        + "Sales_Type, "
                        + "Operator_ID, "
                        + "Operator_Name, "
                        + "SoldTo_Name, "
                        + "Sale_Date, "
                        + "Net, "
                        + "Gross, "
                        + "Branch_ID, "
                        + "Draw_No, "
                        + "Modified_User) "
                    + "VALUES "
                        + "(?," //Transaction_No, has to be calculated
                        + "9,"
                        + "'ONLINE',"
                        + "'ONLINE',"
                        + "'ONLINE',"
                        + "?,"  //Operator_Id, has to be calculated
                        + "?,"  //Operator_Name, from JSON
                        + "'ONLINE',"
                        + "?,"  //Sale_Date, from JSON
                        + "?,"  //Net, has to be calculated
                        + "?,"  //Gross, from JSON
                        + "0,"
                        + "1,"
                        + "'lukasync')");
            ps.setInt(1, getLastTransactionNo()+1);     //"transaction_no"
            ps.setInt(2, getUserId(username));
            ps.setString(3, username);
            ps.setString(4, saleDate); //"sale_date"
            ps.setDouble(5, Double.parseDouble(gross)*0.9);//"net"
            ps.setDouble(6, Double.parseDouble(gross));//"gross"

//            System.out.println(ps);
            int result = ps.executeUpdate();
            if(result != 1)
                throw new IllegalArgumentException("Not a valid number of users modified - " + result);
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getUserId(String username) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps;
            QueryBuilder q = new QueryBuilder("id", "operators", "Short_Name = '" + username + "'", "", "");
            ps = conn.prepareStatement(q.getQuery());

            ResultSet result = ps.executeQuery();

            if(!result.next()) {
                ps.close();
                conn.close();
                throw new IllegalArgumentException("Not a valid username");
            } else {
                int userId = result.getInt("id");
                ps.close();
                conn.close();
                return userId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; //Will never reach this point
    }

    private int getLastTransactionNo() {
        try {
            Connection conn = getConnection();
            PreparedStatement ps;
            QueryBuilder q = new QueryBuilder(
                    "TOP 1 transaction_no",
                    "Sales_Transactions_Header",
                    "transaction_no > 1337000000 AND transaction_no < 1338000000",
                    "",
                    "transaction_no desc");
            ps = conn.prepareStatement(q.getQuery());

            ResultSet result = ps.executeQuery();

            if(!result.next()) {
                ps.close();
                conn.close();
                throw new IllegalArgumentException("Could not fetch last transaction number");
            } else {
                int transactionNumber = result.getInt("transaction_no");
                ps.close();
                conn.close();
                return transactionNumber;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; //Will never reach this point
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