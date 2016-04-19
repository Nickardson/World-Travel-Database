package edu.seminolestate.gratzer.wtd.database.updates;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.seminolestate.gratzer.wtd.beans.Attraction;
import edu.seminolestate.gratzer.wtd.database.DBUpdater.Updater;

/**
 * @author Taylor
 * @date 2016-04-11
 */
public class Update_3_4 implements Updater {

	@Override
	public int getFrom() {
		return 3;
	}

	@Override
	public int getTo() {
		return 4;
	}

	@Override
	public void update(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30);
		
		// create attractions table
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS attractions (" + 
				"id INTEGER PRIMARY KEY," +
				"logid INTEGER," +	// Log id this place relates to
				"name TEXT," + 		// Name of the place
				"rating INTEGER," + // Rating, 0 for none, or 1-5 stars
				"comment TEXT," + 	// Comment about the place
				"type TEXT," + 		// The type ie (Hotel, Restaurant, Attractions)
				"FOREIGN KEY(logid) REFERENCES travelogues(id) ON DELETE CASCADE" +
				")");
		
		// create sample attractionss
		new Attraction(Attraction.NO_ID, 1, 
				"The Louvre", 
				5, 
				"Landmark art museum with vast collection", 
				"Museum").create(connection);
		new Attraction(Attraction.NO_ID, 1, 
				"Arc de Triomphe", 
				4, 
				"Triumphal arch & national monument", 
				"Culture").create(connection);
		
		new Attraction(Attraction.NO_ID, 2, 
				"Knight's Square", 
				3,
				"Renaissance square with Medici statue", 
				"Dining").create(connection);
		new Attraction(Attraction.NO_ID, 2, 
				"Piazza dei Miracoli", 
				5,
				"Green space surrounding the leaning tower", 
				"Park").create(connection);
		
		new Attraction(Attraction.NO_ID, 3, 
				"Syndey Snacks", 
				1,
				"Cheap and disgusting crab legs, would not recommend", 
				"Dining").create(connection);
		new Attraction(Attraction.NO_ID, 3, 
				"Taronga Zoo", 
				4,
				"Large zoo divided into different regions", 
				"Park").create(connection);
	}

}
