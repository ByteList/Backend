package de.gamechest.backend.socket.support;

import de.gamechest.backend.socket.support.tables.*;
import de.gamechest.backend.sql.SqlLiteDatabase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static de.gamechest.backend.util.HtmlUtf8Characters.convertToHtmlCharacters;

/**
 * Created by ByteList on 08.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class SupportDatabase extends SqlLiteDatabase {

    private final TicketsTable ticketsTable;
    private final AnswersTable answersTable;

    private final MinecraftTable minecraftTable;
    private final WebsiteTable websiteTable;
    private final TeamspeakTable teamspeakTable;
    private final DiscordTable discordTable;
    private final AnythingTable anythingTable;

    private final String[] keysAnswers = { "answer", "user", "message", "timestamp" };

    private final String[] mcKeys = { "topic", "version", "server_id", "subject", "message" };
    private final String[] webKeys = { "topic", "url", "subject", "message" };
    private final String[] tsKeys = { "topic", "name", "uid", "subject", "message" };
    private final String[] disKeys = { "topic", "name", "channel", "subject", "message" };
    private final String[] anyKeys = { "topic", "subject", "message" };

    public SupportDatabase() {
        super("support");
        this.addNewTable(this.ticketsTable = new TicketsTable(this));
        this.addNewTable(this.answersTable = new AnswersTable(this));

        this.addNewTable(this.minecraftTable = new MinecraftTable(this));
        this.addNewTable(this.websiteTable = new WebsiteTable(this));
        this.addNewTable(this.teamspeakTable = new TeamspeakTable(this));
        this.addNewTable(this.discordTable = new DiscordTable(this));
        this.addNewTable(this.anythingTable = new AnythingTable(this));
    }

    public boolean createMinecraftTicket(int ticketId, String creator, String topic, String version, String serverId, String subject, String msg) {
        String ticketsCmd = this.ticketsTable.insert(ticketId, SupportTab.MINECRAFT.getTabShort(), creator, SupportState.OPEN.getStateString());
        String ticketCmd = this.minecraftTable.insert(ticketId, topic, version, serverId, subject, msg);
        String answersCmd = this.answersTable.insert(ticketId, "system", "created");

        if(this.executeUpdate(ticketsCmd) && this.executeUpdate(ticketCmd)) {
            return this.executeUpdate(answersCmd);
        }
        return false;
    }

    public boolean createWebsiteTicket(int ticketId, String creator, String topic, String url, String subject, String msg) {
        String ticketsCmd = this.ticketsTable.insert(ticketId, SupportTab.WEBSITE.getTabShort(), creator, SupportState.OPEN.getStateString());
        String ticketCmd = this.websiteTable.insert(ticketId, topic, url, subject, msg);
        String answersCmd = this.answersTable.insert(ticketId, "system", "created");

        if(this.executeUpdate(ticketsCmd) && this.executeUpdate(ticketCmd)) {
            return this.executeUpdate(answersCmd);
        }
        return false;
    }

    public boolean createTeamspeakTicket(int ticketId, String creator, String topic, String name, String uid, String subject, String msg) {
        String ticketsCmd = this.ticketsTable.insert(ticketId, SupportTab.TEAMSPEAK.getTabShort(), creator, SupportState.OPEN.getStateString());
        String ticketCmd = this.teamspeakTable.insert(ticketId, topic, name, uid, subject, msg);
        String answersCmd = this.answersTable.insert(ticketId, "system", "created");

        if(this.executeUpdate(ticketsCmd) && this.executeUpdate(ticketCmd)) {
            return this.executeUpdate(answersCmd);
        }
        return false;
    }

    public boolean createDiscordTicket(int ticketId, String creator, String topic, String name, String channel, String subject, String msg) {
        String ticketsCmd = this.ticketsTable.insert(ticketId, SupportTab.DISCORD.getTabShort(), creator, SupportState.OPEN.getStateString());
        String ticketCmd = this.discordTable.insert(ticketId, topic, name, channel, subject, msg);
        String answersCmd = this.answersTable.insert(ticketId, "system", "created");

        if(this.executeUpdate(ticketsCmd) && this.executeUpdate(ticketCmd)) {
            return this.executeUpdate(answersCmd);
        }
        return false;
    }

    public boolean createAnythingTicket(int ticketId, String creator, String topic, String subject, String msg) {
        String ticketsCmd = this.ticketsTable.insert(ticketId, SupportTab.ANYTHING.getTabShort(), creator, SupportState.OPEN.getStateString());
        String ticketCmd = this.anythingTable.insert(ticketId, topic, subject, msg);
        String answersCmd = this.answersTable.insert(ticketId, "system", "created");

        if(this.executeUpdate(ticketsCmd) && this.executeUpdate(ticketCmd)) {
            return this.executeUpdate(answersCmd);
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
                String tab = resultSet.getString("tab");

                document.append("id", id);
                document.append("tab", tab);
                document.append("state", resultSet.getString("state"));

                switch (SupportTab.getSupportTab(tab)) {

                    case DEFAULT:
                        break;
                    case MINECRAFT:
                        ResultSet mcResultSet = this.executeQuery(this.minecraftTable.select(id));
                        while (mcResultSet.next()) {
                            document.append("subject", convertToHtmlCharacters(mcResultSet.getString("subject")));
                            document.append("topic", mcResultSet.getString("topic"));
                        }
                        mcResultSet.close();
                        break;
                    case WEBSITE:
                        ResultSet webResultSet = this.executeQuery(this.websiteTable.select(id));
                        while (webResultSet.next()) {
                            document.append("subject", convertToHtmlCharacters(webResultSet.getString("subject")));
                            document.append("topic", webResultSet.getString("topic"));
                        }
                        webResultSet.close();
                        break;
                    case TEAMSPEAK:
                        ResultSet tsResultSet = this.executeQuery(this.teamspeakTable.select(id));
                        while (tsResultSet.next()) {
                            document.append("subject", convertToHtmlCharacters(tsResultSet.getString("subject")));
                            document.append("topic", tsResultSet.getString("topic"));
                        }
                        tsResultSet.close();
                        break;
                    case DISCORD:
                        ResultSet disResultSet = this.executeQuery(this.discordTable.select(id));
                        while (disResultSet.next()) {
                            document.append("subject", convertToHtmlCharacters(disResultSet.getString("subject")));
                            document.append("topic", disResultSet.getString("topic"));
                        }
                        disResultSet.close();
                        break;
                    case ANYTHING:
                        ResultSet anyResultSet = this.executeQuery(this.anythingTable.select(id));
                        while (anyResultSet.next()) {
                            document.append("subject", convertToHtmlCharacters(anyResultSet.getString("subject")));
                            document.append("topic", convertToHtmlCharacters(anyResultSet.getString("topic")));
                        }
                        anyResultSet.close();
                        break;
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
    public ArrayList<Document> getTicketIdsFromState(String state, String creator) {
        String cmd = this.ticketsTable.selectTicketsFromState(state, creator);
        ResultSet resultSet = this.executeQuery(cmd);
        ArrayList<Document> ids = new ArrayList<>();

        try {
            while (resultSet.next()) {
                Document document = new Document();
                int id = resultSet.getInt("ticket_id");
                String tab = resultSet.getString("tab");
                document.append("id", id);
                document.append("tab", tab);
                document.append("creator", resultSet.getString("creator"));
                document.append("state", resultSet.getString("state"));

                switch (SupportTab.getSupportTab(tab)) {

                    case DEFAULT:
                        break;
                    case MINECRAFT:
                        ResultSet mcResultSet = this.executeQuery(this.minecraftTable.select(id));
                        while (mcResultSet.next()) {
                            document.append("subject", convertToHtmlCharacters(mcResultSet.getString("subject")));
                            document.append("topic", mcResultSet.getString("topic"));
                        }
                        mcResultSet.close();
                        break;
                    case WEBSITE:
                        ResultSet webResultSet = this.executeQuery(this.websiteTable.select(id));
                        while (webResultSet.next()) {
                            document.append("subject", convertToHtmlCharacters(webResultSet.getString("subject")));
                            document.append("topic", webResultSet.getString("topic"));
                        }
                        webResultSet.close();
                        break;
                    case TEAMSPEAK:
                        ResultSet tsResultSet = this.executeQuery(this.teamspeakTable.select(id));
                        while (tsResultSet.next()) {
                            document.append("subject", convertToHtmlCharacters(tsResultSet.getString("subject")));
                            document.append("topic", tsResultSet.getString("topic"));
                        }
                        tsResultSet.close();
                        break;
                    case DISCORD:
                        ResultSet disResultSet = this.executeQuery(this.discordTable.select(id));
                        while (disResultSet.next()) {
                            document.append("subject", convertToHtmlCharacters(disResultSet.getString("subject")));
                            document.append("topic", disResultSet.getString("topic"));
                        }
                        disResultSet.close();
                        break;
                    case ANYTHING:
                        ResultSet anyResultSet = this.executeQuery(this.anythingTable.select(id));
                        while (anyResultSet.next()) {
                            document.append("subject", convertToHtmlCharacters(anyResultSet.getString("subject")));
                            document.append("topic", convertToHtmlCharacters(anyResultSet.getString("topic")));
                        }
                        anyResultSet.close();
                        break;
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

    private Document getTicketInformation(int ticketId) {
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
                        ResultSet mcResultSet = this.executeQuery(this.minecraftTable.select(ticketId));
                        while (mcResultSet.next()) {
                            for (String key : mcKeys) {
                                document.append(key, convertToHtmlCharacters(mcResultSet.getString(key)));
                            }
                        }
                        mcResultSet.close();
                        break;
                    case WEBSITE:
                        ResultSet webResultSet = this.executeQuery(this.websiteTable.select(ticketId));
                        while (webResultSet.next()) {
                            for (String key : webKeys) {
                                document.append(key, convertToHtmlCharacters(webResultSet.getString(key)));
                            }
                        }
                        webResultSet.close();
                        break;
                    case TEAMSPEAK:
                        ResultSet tsResultSet = this.executeQuery(this.teamspeakTable.select(ticketId));
                        while (tsResultSet.next()) {
                            for (String key : tsKeys) {
                                document.append(key, convertToHtmlCharacters(tsResultSet.getString(key)));
                            }
                        }
                        tsResultSet.close();
                        break;
                    case DISCORD:
                        ResultSet disResultSet = this.executeQuery(this.discordTable.select(ticketId));
                        while (disResultSet.next()) {
                            for (String key : disKeys) {
                                document.append(key, convertToHtmlCharacters(disResultSet.getString(key)));
                            }
                        }
                        disResultSet.close();
                        break;
                    case ANYTHING:
                        ResultSet anyResultSet = this.executeQuery(this.anythingTable.select(ticketId));
                        while (anyResultSet.next()) {
                            for (String key : anyKeys) {
                                document.append(key, convertToHtmlCharacters(anyResultSet.getString(key)));
                            }
                        }
                        anyResultSet.close();
                        break;
                }

                ResultSet answerResultSet = this.executeQuery(this.answersTable.select(ticketId));
                while (answerResultSet.next()) {
                    Document answer = new Document();
                    for (String key : keysAnswers) {
                        answer.append(key, convertToHtmlCharacters(answerResultSet.getString(key)));
                    }
                    answers.add(answer);
                }
                answerResultSet.close();
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
