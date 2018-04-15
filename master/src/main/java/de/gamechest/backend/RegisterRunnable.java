package de.gamechest.backend;

import de.gamechest.database.DatabaseManager;
import de.gamechest.database.DatabasePlayerObject;
import de.gamechest.database.webregister.DatabaseWebRegisterObject;

import java.util.UUID;

/**
 * Created by ByteList on 15.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class RegisterRunnable implements Runnable {

    private final Backend backend;
    private final DatabaseManager parentDatabaseManager;

    RegisterRunnable(Backend backend) {
        this.backend = backend;
        this.parentDatabaseManager = this.backend.getDatabaseManager().getParentDatabaseManager();
    }

    @Override
    public void run() {
        while (backend.isRunning) {
            this.parentDatabaseManager.getDatabaseWebRegister().getAllByState("0").forEach(document -> {
                UUID uuid = UUID.fromString(document.getString(DatabaseWebRegisterObject.UUID.getName()));
                String mail = document.getString(DatabaseWebRegisterObject.MAIL_ADDRESS.getName());
                String verifyCode = document.getString(DatabaseWebRegisterObject.VERIFY_CODE.getName());

                this.parentDatabaseManager.getAsync().getPlayer(uuid, dbPlayer -> {
                    String name = dbPlayer.getDatabaseElement(DatabasePlayerObject.LAST_NAME).getAsString();
                    if(this.backend.getMailClient().sendRegisterMail(mail, name, verifyCode)) {
                        this.parentDatabaseManager.getDatabaseWebRegister().setDatabaseObject(uuid, DatabaseWebRegisterObject.STATE, "1");
                    }
                });
            });
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
