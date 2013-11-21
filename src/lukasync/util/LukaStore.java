package lukasync.util;

import lukasync.Lukasync;

import java.sql.*;

public class LukaStore {
    public static void put(String key, int serviceFlowId, String value) {
        Connection conn = getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO registry " +
                            "(`key`, service_flow_id, value) " +
                            "VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE " +
                            "value = VALUES(value)");

            stmt.setString(1, key);
            stmt.setInt(2, serviceFlowId);
            stmt.setString(3, value);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Error in SQL");
        }
    }

    public static String get(String key, int serviceFlowId) {
        Connection conn = getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT value " +
                            "FROM registry " +
                            "WHERE `key` = ?" +
                            "AND service_flow_id = ?"
            );

            stmt.setString(1, key);
            stmt.setInt(2, serviceFlowId);
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