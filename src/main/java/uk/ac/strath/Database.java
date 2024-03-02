package uk.ac.strath;

import java.sql.*;

public class Database {
    private Connection conn;

    public Database(String url, String user, String password) throws SQLException {
        conn = DriverManager.getConnection(url, user, password);
    }

    public User getUser(String username) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM `users` WHERE `username`=?;");

        pstmt.setString(1, username);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String password = rs.getString("password");

            return new User(username, password);
        }

        return null;
    }
}
