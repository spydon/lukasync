package lukasync;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public class POS {

    private MetaConnection meta;

    public POS(MetaConnection conn) {
        this.meta = conn;
    }

    public JSONObject getContacts() {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
        try {
            Connection conn = meta.getConnection();
            PreparedStatement ps;
            if (meta.getLastUpdated().equals("0"))
                ps = conn.prepareStatement("SELECT id, name, getdate() as update_time from contacts WHERE id>0");
            else
                ps = conn.prepareStatement("SELECT id, name, getdate() as update_time from contacts WHERE id>0 AND modified_date>" + "'" + meta.getLastUpdated() + "'");
            ResultSet rs = ps.executeQuery();
            String updateTime = meta.getLastUpdated();
            while (rs.next()) {
                System.out.println(rs.getString("Name") + " " + rs.getInt("ID"));
                result.put(rs.getString("Name"), rs.getInt("ID"));
                updateTime = rs.getString("update_time");
            }
            meta.updateTime(updateTime);
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(new JSONObject(result).toString());
        return new JSONObject(result);
    }
}