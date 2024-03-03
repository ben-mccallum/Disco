package uk.ac.strath;

import java.sql.*;

public class Database {
    private Connection conn;

    public Database(String url, String user, String password) throws SQLException {
        conn = DriverManager.getConnection(url, user, password);
    }

    public void addUser(String username, String password) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `users` (`username`, `password`) VALUES (?,?);");

        pstmt.setString(1, username);
        pstmt.setString(2, password);


        pstmt.executeUpdate();
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
