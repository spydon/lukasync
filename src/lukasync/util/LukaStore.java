package lukasync.util;

import lukasync.main.Lukasync;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LukaStore {
    public void createContact (JSONObject contact) {
        Connection conn = getConnection();

        String[] columns = {
                "entity_service_id",
                "user_id",
                "first_name",
                "last_name",
                "email",
                "mobile",
                "address",
                "city",
                "state",
                "postcode",
                "country"
        };

        String[] values = {
                // TODO How to handle that there might be strings or ints? instanceof in the helper method?
        };
        // TODO set up shiz for calling getCreateStatementString()
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
}