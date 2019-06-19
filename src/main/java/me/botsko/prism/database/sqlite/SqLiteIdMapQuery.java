package me.botsko.prism.database.sqlite;

import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.sql.SQLIdMapQuery;
import org.apache.commons.lang.Validate;

import java.sql.*;

/**
 * Created for the Addstar Project.
 * Created by Narimm on 18/06/2019.
 */
public class SqLiteIdMapQuery extends SQLIdMapQuery {

    private static final String automap = "INSERT INTO <prefix>id_map(material, state, block_id,) VALUES (?, ?, (SELECT IFNULL(MAX(id), 0) + 1 FROM <prefix>id_map));";

    public SqLiteIdMapQuery(PrismDataSource dataSource) {
        super(dataSource);
    }

    public int mapAutoId(String material, String state) {
        Validate.notNull(material, "Material cannot be null");
        Validate.notNull(state, "State cannot be null");
        //SQLITE cannot autoincrement a non primary key....
        String query = automap.replace("<prefix>", prefix);

        if (state.equals("0") || state.equals("[]"))
            state = "";

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, material);
                st.setString(2, state);

                boolean success = st.executeUpdate() > 0;

                SQLWarning warning = st.getWarnings();

                while (warning != null) {
                    dataSource.getLog().warn("sql Warning: " + warning.getMessage());
                    warning = warning.getNextWarning();
                }

                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int autoInc = rs.getInt(1);

                    if (!success) {
                        dataSource.getLog().info("Failed id map: material=" + material + ", " + "state=" + state);
                    }

                    return autoInc;

					/*if(success) {
						return autoInc;
					}
					else {

						try (PreparedStatement undoInc = conn.prepareStatement(unauto.replace("<prefix>", prefix))) {
							st.setInt(1, autoInc - 1);
							st.executeUpdate();
						}
					}*/
                }
            }
        } catch (final SQLException e) {
            dataSource.getLog().error("Database connection error: ", e);
            e.printStackTrace();
        }

        return 0;
    }
}
