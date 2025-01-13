package org.sevensrequiem.globalBans;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public class Database
{
    public static void main(String[] args)
    {
        Main instance;
        Connection connection = null;
        ResultSet resultSet = null;
        Statement statement = null;
        instance = Main.getInstance();

        try
        {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + instance.getDataFolder() + "/bans.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // Create the bans table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS bans (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "ip TEXT NOT NULL, " +
                                    "admin TEXT NOT NULL, " +
                                    "identifier TEXT NOT NULL, " +
                                    "game TEXT NOT NULL, " +
                                    "expires DATETIME DEFAULT NULL, " +
                                    "reason TEXT NOT NULL, " +
                                    "date_banned DATETIME DEFAULT CURRENT_TIMESTAMP)";
            statement.executeUpdate(createTableSQL);

            resultSet = statement.executeQuery("SELECT * FROM bans");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void Download() {
        Bans bansInstance = new Bans(Main.getInstance());
        String jsonResponse = bansInstance.RetrieveGlobalBanList();

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.getInstance().getDataFolder() + "/bans.db");
             Statement statement = connection.createStatement()) {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String ip = jsonObject.getString("ip");
                String admin = jsonObject.getString("admin");
                String identifier = jsonObject.getString("identifier");
                String game = jsonObject.getString("game");
                String expires = jsonObject.optString("expires", null);
                String reason = jsonObject.getString("reason");
                String dateBanned = jsonObject.optString("date_banned", "CURRENT_TIMESTAMP");

                String insertSQL = "INSERT INTO bans (ip, admin, identifier, game, expires, reason, date_banned) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                    preparedStatement.setString(1, ip);
                    preparedStatement.setString(2, admin);
                    preparedStatement.setString(3, identifier);
                    preparedStatement.setString(4, game);
                    if (expires != null) {
                        preparedStatement.setString(5, expires);
                    } else {
                        preparedStatement.setNull(5, java.sql.Types.NULL);
                    }
                    preparedStatement.setString(6, reason);
                    preparedStatement.setString(7, dateBanned);
                    preparedStatement.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

public boolean CompareUserIP(String ip) {
    try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.getInstance().getDataFolder() + "/bans.db");
         Statement statement = connection.createStatement()) {
        ResultSet resultSet = statement.executeQuery("SELECT * FROM bans WHERE ip = '" + ip + "'");
        while (resultSet.next()) {
            return true;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}

public String GetBanReason(String ip) {
    try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.getInstance().getDataFolder() + "/bans.db");
         Statement statement = connection.createStatement()) {
        ResultSet resultSet = statement.executeQuery("SELECT * FROM bans WHERE ip = '" + ip + "'");
        while (resultSet.next()) {
            return resultSet.getString("reason");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return null;
}
}