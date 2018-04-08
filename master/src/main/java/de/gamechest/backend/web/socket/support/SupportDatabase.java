package de.gamechest.backend.web.socket.support;

import de.gamechest.backend.sql.SqlLiteDatabase;
import de.gamechest.backend.web.socket.SupportState;
import de.gamechest.backend.web.socket.SupportTab;
import de.gamechest.backend.web.socket.support.minecraft.MinecraftAnswersTable;
import de.gamechest.backend.web.socket.support.minecraft.MinecraftTable;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
}