package me.botsko.prism.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.botsko.prism.database.SelectQuery;
import me.botsko.prism.database.sql.SQLPrismDataSource;
import me.botsko.prism.database.sql.SQLSelectQueryBuilder;
import org.bukkit.configuration.ConfigurationSection;


/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 5/04/2019.
 */
public class MySQLPrismDataSource extends SQLPrismDataSource {

    private boolean nonStandardSQL;

    public MySQLPrismDataSource(ConfigurationSection section) {
        super(section);
        nonStandardSQL = this.section.getBoolean("useNonStandardSql", true);
        name = "mysql";
    }

    public static void updateDefaultConfig(ConfigurationSection section) {
        section.addDefault("hostname", "127.0.0.1");
        section.addDefault("username", "root");
        section.addDefault("password", "");
        section.addDefault("databaseName", "minecraft");
        section.addDefault("prefix", "prism_");
        section.addDefault("port", "3306");
        section.addDefault("useNonStandardSql", true);
    }
    @Override
    public MySQLPrismDataSource createDataSource() {
        try {
            HikariDataSource pool;
            final String dns = "jdbc:mysql://" + this.section.getString("hostname") + ":"
                    + this.section.getString("port") + "/" + this.section.getString("databaseName")
                    + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
            HikariConfig hConfig = loadHikariConfig("com.mysql.jdbc.Driver", dns);
            pool = new HikariDataSource(hConfig);
            database = pool;
            createSettingsQuery();
            saveHikariConfig(hConfig);
            return this;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setFile() {
        //not required here.
    }

    @Override
    public SelectQuery createSelectQuery() {
        if (nonStandardSQL) {
            return new MySQLSelectQueryBuilder(this);
        } else {
            return new SQLSelectQueryBuilder(this);
        }

    }
}
