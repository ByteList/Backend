package de.gamechest.backend.socket.support.tables;

import de.gamechest.backend.socket.support.SupportDatabase;
import de.gamechest.backend.sql.SqlLiteTable;
import de.gamechest.backend.sql.SqlLiteTableStructure;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ByteList on 08.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
@Data
public class TicketsTable implements SqlLiteTable {

    private final SupportDatabase database;

    private String name;
    private SqlLiteTableStructure structure;

    public TicketsTable(SupportDatabase database) {
        this.database = database;

        this.name = "tickets";
        this.structure = new SqlLiteTableStructure()
                .append("ticket_id")
                .append("tab")
                .append("creator")
                .append("notify")
                .append("state").create();

        database.executeUpdate("CREATE TABLE IF NOT EXISTS "+name+" "+structure.toStatementFormattedString());
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

    public int countNotify(String creator, String notifyValue) {
        ResultSet resultSet = this.database.executeQuery("SELECT COUNT(notify) AS rowcount FROM "+this.name+" WHERE creator = '"+creator+"' AND notify = '"+notifyValue+"'");
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

    public String insert(int ticketId, String tab, String creator, String state) {
        String structure = this.structure.toValuesFormattedString();
        structure = structure
                .replace("ticket_id", String.valueOf(ticketId))
                .replace("tab", tab)
                .replace("creator", creator)
                .replace("state", state)
                .replace("notify", "0");

        return "INSERT INTO "+this.name+" VALUES"+structure;
    }

    public String selectTickets(String tab, String creator) {
        String t = "tab = '"+tab+"'";
        String c = "creator = '"+creator+"'";
        return "SELECT * FROM "+this.name+" WHERE "+(creator != null ? c : "")+(tab != null ? (creator != null ? " AND " : "")+t : "");
    }

    public String selectTicketsFromState(String state, String creator) {
        String s = "state = '"+state+"'";
        String c = "creator = '"+creator+"'";
        return "SELECT * FROM "+this.name+" WHERE "+(creator != null ? c : "")+(state != null ? (creator != null ? " AND " : "")+s : "");
    }

    public String selectTicket(int ticketId) {
        return "SELECT * FROM "+this.name+" WHERE ticket_id = '"+ticketId+"'";
    }

    public String updateState(int ticketId, String state) {
        return "UPDATE "+this.name+" SET state = '"+state+"' WHERE ticket_id = '"+ticketId+"';";
    }

    public String updateNotify(int ticketId, String value) {
        return "UPDATE "+this.name+" SET notify = '"+value+"' WHERE ticket_id = '"+ticketId+"';";
    }
}
