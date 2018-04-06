package de.gamechest.backend.sql;

import java.sql.*;

/**
 * Created by ByteList on 06.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class SqlLite {

    private Connection connection;

    public SqlLite(String db) throws Exception {
        Class.forName("org.sqlite.JDBC");
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+db+".db");
            System.out.println("SqlLite - "+db+" started!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean close() {
        if(this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                return false;
            }
        }
        return true;
    }

    public boolean createTableIfNotExists(String table) {
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS "+table);

            statement.close();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public boolean executeUpdate(String cmd) {
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate(cmd);

            statement.close();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public ResultSet executeQuery(String cmd) {
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            return statement.executeQuery(cmd);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public boolean executeSupportInsert(int ticketId, String topic, String owner) {
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("INSERT INTO support VALUES('"+ticketId+"', '"+topic+"', '"+owner+"')");

            statement.close();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public boolean executeSupportInsertMinecraft(int ticketId, String player, String uuid, String version, String sid, String subject, String msg) {
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("INSERT INTO mc VALUES('"+ticketId+"', '"+player+"', '"+uuid+"', '"+version+"', '"+sid+"', '"+subject+"', '"+msg+"', '0')");
            statement.executeUpdate("INSERT INTO mc-answers VALUES('"+ticketId+"', '0', 'state:open:system', '"+(System.currentTimeMillis()-1000)+"')");

            statement.close();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

}
