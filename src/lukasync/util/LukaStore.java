package lukasync.util;

import lukasync.Lukasync;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LukaStore {
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

    private Connection getConnection () {
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
    */
}