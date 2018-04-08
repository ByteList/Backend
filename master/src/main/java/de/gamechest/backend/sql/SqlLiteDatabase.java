package de.gamechest.backend.sql;

import de.gamechest.backend.Callback;
import lombok.Getter;

import java.sql.*;
import java.util.HashMap;

/**
 * Created by ByteList on 08.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public abstract class SqlLiteDatabase {

    private HashMap<String, SqlLiteTable> tables;
    @Getter
    private String name;

    private Connection connection;

    public SqlLiteDatabase(String name) {
        this.tables = new HashMap<>();
        this.name = name;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+name+".db");
            System.out.println("SqlLite - "+name+" started!");
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

    public SqlLiteTable getTable(String name) {
        return this.tables.get(name);
    }

    public boolean addNewTable(SqlLiteTable sqlLiteTable) {
        if(!this.tables.containsKey(sqlLiteTable.getName())) {
            this.tables.put(sqlLiteTable.getName(), sqlLiteTable);
            return true;
        }
        return false;
    }

    public boolean removeTable(String name, Callback<SqlLiteTable> lastAction) {
        if(this.tables.containsKey(name)) {
            SqlLiteTable sqlLiteTable = this.tables.get(name);
            lastAction.run(sqlLiteTable);
            this.tables.remove(name);
            return true;
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
}
