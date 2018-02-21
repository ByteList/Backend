package de.gamechest.backend.updater;

import de.bytelist.jenkinsapi.JenkinsAPI;

import java.io.*;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Created by ByteList on 21.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class Updater {

    public Updater(String currentVersion, Logger logger) {
        JenkinsAPI jenkinsAPI = new JenkinsAPI("apiUser", "Uf6UYSqSrgOGby01fSIe7dAkd1eSzVYggqH");

        String loginCheck = jenkinsAPI.getLoginCorrect("https://vs.bytelist.de/jenkins/");
        if(!loginCheck.equals(JenkinsAPI.CORRECT_LOGIN_VARIABLE)) {
            System.err.println("Cannot check for updates:");
            System.err.println(loginCheck);
            return;
        }

        int currentBuildNumber = Integer.parseInt(currentVersion.replace(".", ":").split(":")[2]);
        int lastStableBuildNumber = Integer.parseInt(jenkinsAPI.getBuildNumber("https://vs.bytelist.de/jenkins/job/GameChest-Backend/lastSuccessfulBuild/"));

        if(currentBuildNumber < lastStableBuildNumber) {
            System.out.println("Update found! Current build: "+currentBuildNumber+" - New build: "+lastStableBuildNumber);
            System.out.println("Start downloading...");
            try {
                if(!new File(".", "tempUpdate/").exists()) {
                    new File(".", "tempUpdate/").mkdir();
                }
                String path = "https://vs.bytelist.de/jenkins/job/GameChest-Backend/lastSuccessfulBuild/artifact/";
                System.out.println(downloadFile(jenkinsAPI, path+"master/target/GameChestBackend.jar", "./tempUpdate/GameChestBackend.jar"));
                System.out.println("Download successful! Moving files to their location...");

                System.out.println(moveFile(new File("./tempUpdate/", "GameChestBackend.jar"), new File(".", "GameChestBackend-Updated.jar")));

                if(new File("./", "tempUpdate/").listFiles().length == 0) {
                    new File("./", "tempUpdate/").delete();
                }
                System.out.println("Update successful! Restarting backend...");
                for (Handler handler : logger.getHandlers()) {
                    handler.close();
                }
                Thread shutdownHook = new Thread(() -> {
                    String[] param = {"sh", "update.sh"};
                    try {
                        Runtime.getRuntime().exec(param);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                shutdownHook.setDaemon(true);
                Runtime.getRuntime().addShutdownHook(shutdownHook);
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No update found.");
        }
    }

    private String downloadFile(JenkinsAPI jenkinsAPI, String url, String downloadedFile) throws IOException {

        InputStream in = jenkinsAPI.getInputStream(url);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int n;
        System.out.println("Please wait...");
        System.out.println("File size: " + jenkinsAPI.getContentLength(url) + " bytes");
        try {
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] response = out.toByteArray();

        FileOutputStream fos = new FileOutputStream(downloadedFile);
        fos.write(response);
        fos.close();
        return "Downloaded " + url + " to " + downloadedFile;
    }

    private String moveFile(File file, File to) {
        if(file.renameTo(to)) {
            return "Moved file "+file.getName()+" to "+to.getPath();
        }
        return "Error while moving "+file.getName() + " to "+to.getPath();
    }
}
