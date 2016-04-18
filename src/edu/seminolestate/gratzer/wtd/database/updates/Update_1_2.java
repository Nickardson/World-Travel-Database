package edu.seminolestate.gratzer.wtd.database.updates;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.seminolestate.gratzer.wtd.beans.Location;
import edu.seminolestate.gratzer.wtd.beans.Travelogue;
import edu.seminolestate.gratzer.wtd.beans.User;
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
		
		// Create a view of users, showing usernames and how many logs that user has created
		statement.executeUpdate("CREATE VIEW IF NOT EXISTS userinfo AS " +
				"SELECT username, COUNT(travelogues.id) as 'logcount' FROM users " + 
					"LEFT OUTER JOIN travelogues ON users.id = travelogues.ownerid " + 
						"GROUP BY username");
		
		User userAdmin = new User().read(connection, "admin");
		

		// add new locations
		Location locationEiffel = new Location(Location.NO_ID, userAdmin.getId(), "Eiffel Tower, Paris, France", 48.8582, 2.2945, true).create(connection);
		Location locationOpera = new Location(Location.NO_ID, userAdmin.getId(), "Sydney Opera House, Australia", -33.856783, 151.215321, true).create(connection);
		Location locationLeaning = new Location(Location.NO_ID, userAdmin.getId(), "Leaning Tower of Pisa, Italy", 43.723006, 10.396502, true).create(connection);

		// apply to the existing travelogues, ONLY if they are still the same
		Travelogue travelEiffel = new Travelogue(1).read(connection);
		Travelogue travelOpera = new Travelogue(3).read(connection);
		Travelogue travelLeaning = new Travelogue(2).read(connection);

		if (travelEiffel.getOwnerid() == userAdmin.getId()) {
			travelEiffel.setLocationid(locationEiffel.getId());
			travelEiffel.update(connection);
		}
		
		if (travelOpera.getOwnerid() == userAdmin.getId()) {
			travelOpera.setLocationid(locationOpera.getId());
			travelOpera.update(connection);
		}
		
		if (travelLeaning.getOwnerid() == userAdmin.getId()) {
			travelLeaning.setLocationid(locationLeaning.getId());
			travelLeaning.update(connection);
		}
	}
}
