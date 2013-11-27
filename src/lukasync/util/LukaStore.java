package lukasync.util;

import lukasync.Lukasync;

import java.sql.*;

public class LukaStore {
    public static void put(String key, int serviceFlowId, String value) {
        Connection conn = getConnection();
        try {
            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE registry " +
                            "SET value = ? " +
                            "WHERE `key` = ? " +
                            "AND service_flow_id = ?"
            );

            updateStmt.setString(1, value);
            updateStmt.setString(2, key);
            updateStmt.setInt(3, serviceFlowId);

            int rowsUpdated = updateStmt.executeUpdate();

            if (rowsUpdated < 1) {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO registry " +
                                "(`key`, service_flow_id, value) " +
                                "VALUES (?, ?, ?)");

                stmt.setString(1, key);
                stmt.setInt(2, serviceFlowId);
                stmt.setString(3, value);

                System.out.println("DEBUG: LukaStore.put(): " + stmt.toString());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Error in SQL");
        }
    }

    public static String get(String key, int serviceFlowId, String defaultValue) {
        Connection conn = getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT value " +
                            "FROM registry " +
                            "WHERE `key` = ? " +
                            "AND service_flow_id = ?"
            );

            stmt.setString(1, key);
            stmt.setInt(2, serviceFlowId);

            System.out.println("DEBUG: LukaStore.get(): " + stmt.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            } else {
                return defaultValue;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Error in SQL");
        }
    }

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