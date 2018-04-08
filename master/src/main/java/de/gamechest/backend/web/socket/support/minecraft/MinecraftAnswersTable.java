package de.gamechest.backend.web.socket.support.minecraft;

import de.gamechest.backend.sql.SqlLiteTable;
import de.gamechest.backend.sql.SqlLiteTableStructure;
import de.gamechest.backend.web.socket.support.SupportDatabase;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ByteList on 08.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
@Data
public class MinecraftAnswersTable implements SqlLiteTable {

    private final SupportDatabase database;

    private String name;
    private SqlLiteTableStructure structure;

    public MinecraftAnswersTable(SupportDatabase database) {
        this.database = database;

        this.name = "mcanswers";
        this.structure = new SqlLiteTableStructure()
                .append("ticket_id")
                .append("answer")
                .append("user")
                .append("message")
                .append("timestamp").create();

        database.executeUpdate("CREATE TABLE IF NOT EXISTS "+name+" "+structure.toStatementFormattedString());
    }

    @Override
    public int count() {
        ResultSet resultSet = this.database.executeQuery("SELECT COUNT(id) AS rowcount FROM "+this.name);
        int count = -1;
        try {
            while (resultSet.next()) {
                count = resultSet.getInt("rowcount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    public int countAnswers(int ticketId) {
        ResultSet resultSet = this.database.executeQuery("SELECT COUNT(id) AS rowcount FROM "+this.name+" WHERE id = "+String.valueOf(ticketId));
        int count = -1;
        try {
            while (resultSet.next()) {
                count = resultSet.getInt("rowcount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    public String insert(int ticketId, String user, String msg) {
        String structure = this.structure.toValuesFormattedString();
        structure = structure
                .replace("ticket_id", String.valueOf(ticketId))
                .replace("answer", String.valueOf(countAnswers(ticketId)+1))
                .replace("user", user)
                .replace("message", msg)
                .replace("timestamp", String.valueOf(System.currentTimeMillis()/1000));

        return "INSERT INTO "+this.name+" VALUES"+structure;
    }
}
