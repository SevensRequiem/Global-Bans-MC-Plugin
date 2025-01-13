package org.sevensrequiem.globalBans;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {

    public static void main(String[] args) {
        Timer timer = new Timer();

        Main instance;
        instance = Main.getInstance();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Database databaseInstance = new Database();
                instance.getLogger().info("Downloading BanList");
                databaseInstance.Download();
                instance.getLogger().info("Downloading Complete");
            }
        };

        // Schedule task to run after 5 minutes
        timer.schedule(task, 300000);

        // Schedule task to run periodically every 10 seconds
        timer.scheduleAtFixedRate(task, 10000, 10000);
    }
}