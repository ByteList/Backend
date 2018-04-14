package de.gamechest.backend.socket.support;

import de.gamechest.backend.socket.SupportState;
import de.gamechest.backend.socket.SupportTab;
import de.gamechest.backend.socket.support.minecraft.AnswersTable;
import de.gamechest.backend.socket.support.minecraft.MinecraftTable;
import de.gamechest.backend.sql.SqlLiteDatabase;
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
    private final AnswersTable answersTable;


    public SupportDatabase() {
        super("support");
        this.addNewTable(this.ticketsTable = new TicketsTable(this));

        this.addNewTable(this.minecraftTable = new MinecraftTable(this));
        this.addNewTable(this.answersTable = new AnswersTable(this));
    }

    public boolean createMinecraftTicket(int ticketId, String creator, String topic, String version, String serverId, String subject, String msg) {
        String ticketsCmd = this.ticketsTable.insert(ticketId, SupportTab.MINECRAFT.getTabShort(), creator, SupportState.OPEN.getStateString());
        String mcCmd = this.minecraftTable.insert(ticketId, topic, version, serverId, subject, msg);
        String mcAnswersCmd = this.answersTable.insert(ticketId, "system", "created");

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
                document.append("state", resultSet.getString("state"));
                document.append("tab", resultSet.getString("tab"));

                ResultSet tResultSet = this.executeQuery(this.minecraftTable.select(id));
                while (tResultSet.next()) {
                    document.append("subject", tResultSet.getString("subject"));
                    document.append("topic", tResultSet.getString("topic"));
                }
                tResultSet.close();
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
    public ArrayList<Document> getTicketIdsFromState(String state, String creator) {
        String cmd = this.ticketsTable.selectTicketsFromState(state, creator);
        ResultSet resultSet = this.executeQuery(cmd);
        ArrayList<Document> ids = new ArrayList<>();

        try {
            while (resultSet.next()) {
                Document document = new Document();
                int id = resultSet.getInt("ticket_id");
                document.append("id", id);
                document.append("creator", resultSet.getString("creator"));
                document.append("state", resultSet.getString("state"));
                document.append("tab", resultSet.getString("tab"));

                ResultSet tResultSet = this.executeQuery(this.minecraftTable.select(id));
                while (tResultSet.next()) {
                    document.append("subject", tResultSet.getString("subject"));
                    document.append("topic", tResultSet.getString("topic"));
                }
                tResultSet.close();
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

    public Document getTicketInformation(int ticketId) {
        Document document = new Document();
        String cmd = this.ticketsTable.selectTicket(ticketId);
        ResultSet resultSet = this.executeQuery(cmd);
        try {
            while (resultSet.next()) {
                document.append("id", ticketId);
                document.append("state", resultSet.getString("state"));
                document.append("creator", resultSet.getString("creator"));
                document.append("tab", resultSet.getString("tab"));
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

        if(!document.containsKey("id")) {
            document.append("id", "-2");
        }
        return document;
    }

    public Document getTicket(int ticketId) {
        Document document = new Document();
        String cmd = this.ticketsTable.selectTicket(ticketId);
        ResultSet resultSet = this.executeQuery(cmd);

        try {
            while (resultSet.next()) {
                String tab = resultSet.getString("tab");

                document.append("id", ticketId);
                document.append("state", resultSet.getString("state"));
                document.append("creator", resultSet.getString("creator"));
                document.append("tab", tab);
                ArrayList<Document> answers = new ArrayList<>();

                switch (SupportTab.getSupportTab(tab)) {
                    case DEFAULT:
                        break;
                    case MINECRAFT:
                        String[] keys = { "topic", "version", "server_id", "subject", "message" };
                        String[] keysAnswers = { "answer", "user", "message", "timestamp" };

                        ResultSet mcResultSet = this.executeQuery(this.minecraftTable.select(ticketId));
                        while (mcResultSet.next()) {
                            for (String key : keys) {
                                document.append(key, mcResultSet.getString(key));
                            }
                        }
                        mcResultSet.close();
                        ResultSet answerResultSet = this.executeQuery(this.answersTable.select(ticketId));
                        while (answerResultSet.next()) {
                            Document answer = new Document();
                            for (String key : keysAnswers) {
                                answer.append(key, answerResultSet.getString(key));
                            }
                            answers.add(answer);
                        }
                        answerResultSet.close();
                        break;
                    case WEBSITE:
                        break;
                    case TEAMSPEAK:
                        break;
                    case DISCORD:
                        break;
                    case ANYTHING:
                        break;
                }
                document.append("answers", answers);
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

        if(!document.containsKey("id")) {
            document.append("id", "-2");
        }
        return document;
    }

    public boolean answer(int ticketId, String user, String msg) {
        if(msg.startsWith("state:")) {
            String state = msg.replace("state:", "");
            try {
                SupportState supportState = SupportState.getSupportState(state);
                String stateUpdateCmd = this.ticketsTable.updateState(ticketId, supportState.getStateString());
                this.executeUpdate(stateUpdateCmd);
            } catch (NullPointerException ex) {
                return false;
            }
        } else {
            Document info = getTicketInformation(ticketId);
            SupportState supportState = SupportState.getSupportState(info.getString("state"));
            String stateUpdateCmd = this.ticketsTable.updateState(ticketId, SupportState.IN_PROGRESSING.getStateString());
            String stateUpdateAnswerCmd = this.answersTable.insert(ticketId, "system", "state:"+SupportState.IN_PROGRESSING.getStateString());

            switch (supportState) {
                case OPEN:
                    this.executeUpdate(stateUpdateCmd);
                    this.executeUpdate(stateUpdateAnswerCmd);
                    break;
                case IN_PROGRESSING:
                    break;
                case CLOSED:
                    this.executeUpdate(stateUpdateCmd);
                    this.executeUpdate(stateUpdateAnswerCmd);
                    break;
            }
        }

        String mcAnswersCmd = this.answersTable.insert(ticketId, user, msg);
        return this.executeUpdate(mcAnswersCmd);
    }
}
