package edu.seminolestate.gratzer.wtd.database.updates;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.seminolestate.gratzer.wtd.database.DBUpdater.Updater;

/**
 * Adds a 7th column, "views" to travelogues.
 * @author Taylor
 * @date 2016-04-03
 * Created update 3
 */
public class Update_2_3 implements Updater {

	@Override
	public int getFrom() {
		return 2;
	}

	@Override
	public int getTo() {
		return 3;
	}

	@Override
	public void update(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30);
		
		// add "views" column
		statement.executeUpdate("ALTER TABLE travelogues ADD COLUMN views INTEGER CHECK(views >= 0)");
	}

}
