package me.botsko.prism.database.derby;

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
public class DerbyPrismDataSource extends SQLPrismDataSource {

    private File derby;

    public DerbyPrismDataSource(ConfigurationSection section) {
        super(section);
        name = "derby";
    }

    public static void updateDefaultConfig(ConfigurationSection section) {
        section.addDefault("username", "root");
        section.addDefault("password", "");
        section.addDefault("filePath", "prism");
    }
    public void setFile() {
        String path = Bukkit.getServer().getPluginManager().getPlugin("Prism").getDataFolder().getAbsolutePath();
        String fileName = this.section.getString("filePath", "prism");
        derby = new File(path, fileName);
    }

    @Override
    public DerbyPrismDataSource createDataSource() {
        HikariDataSource pool;
        final String dns = "jdbc:derby:" + derby;
        HikariConfig hikariConfig = loadHikariConfig(null,dns);
        pool = new HikariDataSource(hikariConfig);
        saveHikariConfig(hikariConfig);
        database = pool;
        return this;
    }
}

