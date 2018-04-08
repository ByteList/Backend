package de.gamechest.backend.web.socket.support;

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
                .append("id")
                .append("tab")
                .append("creator")
                .append("state").create();

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

    public String insert(int ticketId, String tab, String creator, String state) {
        String structure = this.structure.toValuesFormattedString();
        structure = structure
                .replace("id", String.valueOf(ticketId))
                .replace("tab", tab)
                .replace("creator", creator)
                .replace("state", state);

        return "INSERT INTO "+this.name+" VALUES"+structure;
    }
}
