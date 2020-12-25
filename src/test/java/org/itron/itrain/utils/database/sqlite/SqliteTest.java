package org.itron.itrain.utils.database.sqlite;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * @author Shadowalker
 */
@Slf4j
public class SqliteTest {

    public static void main(String args[]) {
        Connection conn = null;
        ResultSet resultSet = null;
        Statement statement = null;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:sqlite.db");
            statement = conn.createStatement();
            resultSet = statement.executeQuery("select * from user");


            while (resultSet.next()) {
                String username = resultSet.getString("username");
                log.info("username:{}", username);
            }


        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();

        } catch (SQLException sqlex) {
            System.out.println(sqlex.getMessage());
            sqlex.printStackTrace();
        } finally {
            try {

                resultSet.close();
                statement.close();
                conn.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
