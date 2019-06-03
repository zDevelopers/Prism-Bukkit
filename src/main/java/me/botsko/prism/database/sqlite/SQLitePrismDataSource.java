package me.botsko.prism.database.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.botsko.prism.Prism;
import me.botsko.prism.database.sql.SQLPrismDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 8/04/2019.
 */
public class SQLitePrismDataSource extends SQLPrismDataSource {

    private File sqLiteFile;

    public SQLitePrismDataSource(ConfigurationSection section) {
        super(section);
        name = "sqlite";
    }

    public void setFile() {
        File dataFolder = Bukkit.getServer().getPluginManager().getPlugin("Prism").getDataFolder();
        String fileName = this.section.getString("filePath", "prism.db");
        sqLiteFile = new File(dataFolder, fileName);
    }

    public static void updateDefaultConfig(ConfigurationSection section) {
        section.addDefault("username", "root");
        section.addDefault("password", "");
        section.addDefault("filePath", "prism.db");
    }

    @Override
    public SQLitePrismDataSource createDataSource() {
        final String dns = "jdbc:sqlite:" + sqLiteFile;
        HikariConfig hConfig = loadHikariConfig("org.sqlist.JDBC",dns);
        HikariDataSource pool = new HikariDataSource(hConfig);
        database = pool;
        saveHikariConfig(hConfig);
        return this;
    }
}
