package me.botsko.prism.parameters;

import me.botsko.prism.actionlibs.QueryParameters;
import org.bukkit.command.CommandSender;

/**
 * Created for the Charlton IT Project.
 * Created by benjicharlton on 19/06/2019.
 */
public class CancelParameter extends SimplePrismParameterHandler {
    /**
     * @param name
     * @param aliases
     */
    public CancelParameter(String name, String... aliases) {
        super("cancel", "");
    }

    /**
     * @param query
     * @param alias
     * @param input
     * @param sender
     */
    @Override
    protected void process(QueryParameters query, String alias, String input, CommandSender sender) {
        query.setCancelled(true);
    }

}
