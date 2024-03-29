package de.gamechest.backend.socket.support.tables;

import de.gamechest.backend.socket.support.SupportTab;
import de.gamechest.backend.socket.support.SupportDatabase;
import de.gamechest.backend.sql.SqlLiteTable;
import de.gamechest.backend.sql.SqlLiteTableStructure;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ByteList on 15.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class DiscordTable implements SqlLiteTable {

    private final SupportDatabase database;

    @Getter
    private SupportTab supportTab;
    @Getter
    private String name;
    @Getter
    private SqlLiteTableStructure structure;

    public DiscordTable(SupportDatabase database) {
        this.database = database;

        this.supportTab = SupportTab.DISCORD;
        this.name = this.supportTab.getTabShort();

        this.structure = new SqlLiteTableStructure()
                .append("ticket_id")
                .append("topic")
                .append("name")
                .append("channel")
                .append("subject")
                .append("message").create();

        database.executeUpdate("CREATE TABLE IF NOT EXISTS "+this.name+" "+this.structure.toStatementFormattedString());
    }

    @Override
    public int count() {
        ResultSet resultSet = this.database.executeQuery("SELECT COUNT(ticket_id) AS rowcount FROM "+this.name);
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

    public String insert(int ticketId, String topic, String name, String channel, String subject, String msg) {
        String structure = this.structure.toValuesFormattedString();
        structure = structure
                .replace("ticket_id", String.valueOf(ticketId))
                .replace("topic", topic)
                .replace("name", name)
                .replace("channel", channel)
                .replace("subject", subject)
                .replace("message", msg);

        return "INSERT INTO "+this.name+" VALUES"+structure;
    }

    public String select(int ticketId) {
        return "SELECT * FROM "+this.name+" WHERE ticket_id = '"+ticketId+"'";
    }
}
