package me.botsko.prism.database.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.botsko.prism.Prism;
import me.botsko.prism.database.InsertQuery;
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
        HikariConfig hConfig = loadHikariConfig("org.sqlite.JDBC", dns);
        hConfig.getDataSourceProperties().setProperty("characterEncoding", "UTF-8");
        hConfig.getDataSourceProperties().setProperty("encoding", "\"UTF-8\"");
        HikariDataSource pool = new HikariDataSource(hConfig);
        database = pool;
        saveHikariConfig(hConfig);
        return this;
    }
    protected String getActionTableCreateStatement(){
        return "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "actions` ("
                + "`action_id` int(10) NOT NULL AUTO_INCREMENT," + "`action` varchar(25) NOT NULL,"
                + "PRIMARY KEY (`action_id`)," + "UNIQUE KEY `action` (`action`)"
                + ")";
    };
    protected String getDataTableCreateStatement(){
        return "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "data` ("
                + "`id` bigint(20) NOT NULL AUTO_INCREMENT," + "`epoch` int(10) NOT NULL,"
                + "`action_id` int(10) NOT NULL," + "`player_id` int(10) NOT NULL,"
                + "`world_id` int(10) NOT NULL," + "`x` int(11) NOT NULL," + "`y` int(11) NOT NULL,"
                + "`z` int(11) NOT NULL," + "`block_id` mediumint(5) DEFAULT NULL,"
                + "`block_subid` mediumint(5) DEFAULT NULL," + "`old_block_id` mediumint(5) DEFAULT NULL,"
                + "`old_block_subid` mediumint(5) DEFAULT NULL," + "PRIMARY KEY (`id`)," + "KEY `epoch` (`epoch`),"
                + "KEY  `location` (`world_id`, `x`, `z`, `y`, `action_id`)"
                + ")";
    };

    protected String getDataExtraTableCreateStatement(){
        return "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "data_extra` ("
                + "`extra_id` bigint(20) NOT NULL AUTO_INCREMENT,"
                + "`data_id` bigint(20) NOT NULL," + "`data` text NULL," + "`te_data` text NULL,"
                + "PRIMARY KEY (`extra_id`)," + "KEY `data_id` (`data_id`)"
                + ")";
    };

    protected String getMetaTableCreateStatement(){
        return "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "meta` ("
                + "`id` int(10) NOT NULL AUTO_INCREMENT," + "`k` varchar(25) NOT NULL,"
                + "`v` varchar(255) NOT NULL," + "PRIMARY KEY (`id`)" + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";
    }

    protected String getPlayerTableCreateStatement(){
        return "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "players` ("
                + "`player_id` int(10) NOT NULL AUTO_INCREMENT," + "`player` varchar(255) NOT NULL,"
                + "`player_uuid` binary(16) NOT NULL," + "PRIMARY KEY (`player_id`),"
                + "UNIQUE KEY `player` (`player`)," + "UNIQUE KEY `player_uuid` (`player_uuid`)"
                + ")";
    }

    protected String getWorldTableCreateStatement(){
        return "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "worlds` ("
                + "`world_id` int(10) NOT NULL AUTO_INCREMENT," + "`world` varchar(255) NOT NULL,"
                + "PRIMARY KEY (`world_id`),"  + "UNIQUE KEY `world` (`world`)"
                + ")";
    }

    protected String getIDMAPTableCreateStatement(){
        return "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "id_map` (" + "`material` varchar(63) NOT NULL,"
                + "`state` varchar(255) NOT NULL," + "`block_id` mediumint(5) NOT NULL AUTO_INCREMENT,"
                + "`block_subid` mediumint(5) NOT NULL DEFAULT 0," + "PRIMARY KEY (`material`, `state`),"
                + "UNIQUE KEY (`block_id`, `block_subid`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
    }

    protected String getExtraDataFKStatement(){
        return "ALTER TABLE `" + getPrefix() + "data_extra` ADD CONSTRAINT `" + getPrefix()
                + "data_extra_ibfk_1` FOREIGN KEY (`data_id`) REFERENCES `" + getPrefix()
                + "data` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;";
    }
}
