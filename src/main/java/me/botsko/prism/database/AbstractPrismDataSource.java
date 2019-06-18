package me.botsko.prism.database;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.zaxxer.hikari.HikariDataSource;
import me.botsko.prism.Prism;

/**
 * Created for the Charlton IT Project.
 * Created by benjicharlton on 18/06/2019.
 */
public abstract class AbstractPrismDataSource implements PrismDataSource {

    protected HikariDataSource database;

    public void addMetrics(HealthCheckRegistry registry){
        if(database!=null)database.setHealthCheckRegistry(registry);
        else Prism.log("Could not set Db metrics - db unconfigured");
    }
}
