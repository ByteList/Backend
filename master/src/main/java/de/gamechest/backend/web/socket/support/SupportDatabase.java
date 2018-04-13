package de.gamechest.backend.web.socket.support;

import de.gamechest.backend.sql.SqlLiteDatabase;
import de.gamechest.backend.web.socket.SupportState;
import de.gamechest.backend.web.socket.SupportTab;
import de.gamechest.backend.web.socket.support.minecraft.MinecraftAnswersTable;
import de.gamechest.backend.web.socket.support.minecraft.MinecraftTable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by ByteList on 08.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class SupportDatabase extends SqlLiteDatabase {

    private final TicketsTable ticketsTable;
    private final MinecraftTable minecraftTable;
    private final MinecraftAnswersTable minecraftAnswersTable;


    public SupportDatabase() {
        super("support");
        this.addNewTable(this.ticketsTable = new TicketsTable(this));

        this.addNewTable(this.minecraftTable = new MinecraftTable(this));
        this.addNewTable(this.minecraftAnswersTable = new MinecraftAnswersTable(this));
    }

    public boolean createMinecraftTicket(int ticketId, String creator, String topic, String version, String serverId, String subject, String msg) {
        String ticketsCmd = this.ticketsTable.insert(ticketId, SupportTab.MINECRAFT.getTabShort(), creator, SupportState.OPEN.getStateString());
        String mcCmd = this.minecraftTable.insert(ticketId, topic, version, serverId, subject, msg);
        String mcAnswersCmd = this.minecraftAnswersTable.insert(ticketId, "system", "created");

        if(this.executeUpdate(ticketsCmd) && this.executeUpdate(mcCmd)) {
            return this.executeUpdate(mcAnswersCmd);
        }
        return false;
    }

    public ArrayList<Document> getTicketIds(String creator) {
        String cmd = this.ticketsTable.selectTickets(null, creator);
        ResultSet resultSet = this.executeQuery(cmd);
        ArrayList<Document> ids = new ArrayList<>();

        try {
            while (resultSet.next()) {
                Document document = new Document();
                int id = resultSet.getInt("ticket_id");
                document.append("id", id);
                document.append("state", resultSet.getInt("state"));
                document.append("tab", resultSet.getInt("tab"));

                ResultSet tResultSet = this.executeQuery(this.minecraftTable.selectSubject(id));
                while (tResultSet.next()) {
                    document.append("subject", tResultSet.getString("subject"));
                    document.append("topic", tResultSet.getString("topic"));
                }
                ids.add(document);
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
        return ids;
    }
}
