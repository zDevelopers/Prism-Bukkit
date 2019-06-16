package me.botsko.prism.database;

import jdk.nashorn.internal.codegen.CompilerConstants;
import me.botsko.prism.database.sql.SQLPlayerQuery;
import me.botsko.prism.players.PrismPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

/**
 * Created for the Charlton IT Project.
 * Created by benjicharlton on 10/06/2019.
 */
public abstract class AbstractFuturePlayer implements Callable<PrismPlayer> {

    protected boolean done = false;
    protected PreparedStatement s;
    protected PrismPlayer p = null;

    protected PrismPlayer handleResult(ResultSet rs) throws SQLException {
        if (rs.next()) {
            p = new PrismPlayer(rs.getInt(1), SQLPlayerQuery.uuidFromDbString(rs.getString(3)), rs.getString(2));
            done = true;
            return p;
        }
        done = true;
        return p;
    }
}
