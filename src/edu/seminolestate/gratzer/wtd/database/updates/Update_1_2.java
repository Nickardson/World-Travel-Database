package edu.seminolestate.gratzer.wtd.database.updates;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.seminolestate.gratzer.wtd.database.DBUpdater.Updater;

/**
 * Updates from version 1 to 2. Adds a new table, a new view, and adds a few columns to travelogue
 * @author Taylor
 * @date 2016-03-04
 */
public class Update_1_2 implements Updater {
	@Override
	public int getFrom() {
		return 1;
	}

	@Override
	public int getTo() {
		return 2;
	}

	@Override
	public void update(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30);
		
		// create locations table
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS locations (" + 
				"id INTEGER PRIMARY KEY," +
				"ownerid INTEGER," +
				"name TEXT," + 
				"latitude DOUBLE," + 
				"longitude DOUBLE," +
				"shared BOOLEAN," +
				"FOREIGN KEY(ownerid) REFERENCES users(id) ON DELETE CASCADE" +
				")");
		
		// add "shared" column to travelogues
		statement.executeUpdate("ALTER TABLE travelogues ADD COLUMN shared BOOLEAN");
		
		// add "countryid" column to travelogues
		statement.executeUpdate("ALTER TABLE travelogues ADD COLUMN locationid INTEGER REFERENCES locations(id) ON UPDATE CASCADE ON DELETE SET NULL");
		
		// Create a view of the users, with just the id and username
		statement.executeUpdate("CREATE VIEW IF NOT EXISTS usernames AS SELECT id, username FROM users");
	}
}
