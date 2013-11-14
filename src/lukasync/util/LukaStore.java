package lukasync.util;

import lukasync.Lukasync;

import java.sql.*;


public class LukaStore {
    public static void put(String key, int serviceId, String value) {
        Connection conn = getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO registry (`key`, service_id, value) " +
                            "VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "value = VALUES(value)");

            stmt.setString(1, key);
            stmt.setInt(2, serviceId);
            stmt.setString(3, value);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Error in SQL");
        }
    }

    public static String get(String key, int serviceId) {
        Connection conn = getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT value FROM registry " +
                            "WHERE `key` = ?" +
                            "AND service_id = ?"
            );

            stmt.setString(1, key);
            stmt.setInt(2, serviceId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Error in SQL");
        }
    }

    /*
    public void createCustomer (int serviceId, String externalId, JSONObject contact) {
//        // XXX imported_at and modified_at are present in contact
//
//        // check if entity_service exists, abort if it does
//        if (customerExists(serviceId, externalId)) {
//            throw new IllegalArgumentException("Customer already exists!");
//        }
//
//        // create entity_service, keep id
//        int entityServiceId =
//        // create customer, pointing out user and entity_service
//
//        /*
//        Connection conn = getConnection();
//
//        String[] columns = {
//                "entity_service_id",
//                "user_id",
//                "first_name",
//                "last_name",
//                "email",
//                "mobile",
//                "address",
//                "city",
//                "state",
//                "postcode",
//                "country"
//        };
//
//        Object[] values = {
//
//        };
//        String statementString;

    }

    private String getCreateStatementString (String table,
                                             String[] columns,
                                             String[] values) {
        if (table.equals("")) {
            throw new IllegalArgumentException("Table must be provided");
        }

        if (columns.length < 1) {
            throw new IllegalArgumentException("At least one column must be used");
        }

        if (values.length < 1) {
            throw new IllegalArgumentException("At least one value must be inserted");
        }

        if (columns.length != values.length) {
            throw new IllegalArgumentException("Column count and value count must be equal");
        }

        String columnsSQLString = columns[0];
        for (int i = 1; i < columns.length; i++) {
            columnsSQLString += ", " + columns[i];
        }

        String valuesSQLString = values[0];
        for (int i = 1; i < values.length; i++) {
            valuesSQLString += ", " + values[i];
        }

        String sqlString = "INSERT INTO " +
                table + " " +
                columnsSQLString + " " +
                "values (" + valuesSQLString + ")";

        System.out.println("DEBUG: LukaStore.getCreateStatement(): " + sqlString);
        return sqlString;
    }
    */

    private static Connection getConnection () {
        try {
            return DriverManager.getConnection(
                    Lukasync.DB,
                    Lukasync.USER,
                    Lukasync.PASS
            );
        } catch (SQLException e) {
            System.out.println("DEBUG: LukasStore.getConnection failed:");
            e.printStackTrace();
        }

        return null;
    }
}