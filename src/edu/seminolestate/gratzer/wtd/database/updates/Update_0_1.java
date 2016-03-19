package edu.seminolestate.gratzer.wtd.database.updates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.sqlite.SQLiteErrorCode;

import edu.seminolestate.gratzer.wtd.beans.Travelogue;
import edu.seminolestate.gratzer.wtd.beans.TravelogueImage;
import edu.seminolestate.gratzer.wtd.beans.User;
import edu.seminolestate.gratzer.wtd.database.DBUpdater.Updater;
import edu.seminolestate.gratzer.wtd.database.PasswordHasher;
import edu.seminolestate.gratzer.wtd.web.route.CRUDTravelogueImageRoute;

/**
 * Updates from version '0' to 1. This update creates all the initial tables and basic setup stuff.
 * @author Taylor
 * @date 2016-02-13
 */
public class Update_0_1 implements Updater {
	@Override
	public int getFrom() {
		return 0;
	}

	@Override
	public int getTo() {
		return 1;
	}

	@Override
	public void update(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30);
		
		// create tables
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (" + 
				"id INTEGER PRIMARY KEY, " + 
				"username TEXT UNIQUE, " + 
				"password TEXT, " +
				"admin BOOLEAN" +
				")");
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS travelogues (" + 
				"id INTEGER PRIMARY KEY, " + 
				"ownerid INTEGER, " + 
				"content TEXT, " + 
				"visitdate DATE, " +
				"FOREIGN KEY(ownerid) REFERENCES users(id) ON DELETE CASCADE" +
				")");
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS travel_images (" + 
				"id INTEGER PRIMARY KEY, " +
				"logid INTEGER, " +
				"filename TEXT," + 
				"FOREIGN KEY(logid) REFERENCES travelogues(id) ON DELETE CASCADE" +
				")");
		
		// create user(s)
		User userAdmin;
		try {
			userAdmin = new User(User.NO_ID, "admin", PasswordHasher.hashPassword("travel"), true).create(connection);
		} catch (SQLException e) {
			// ignore UNIQUE username constraint violation, throw back anything else
			if (e.getErrorCode() != SQLiteErrorCode.SQLITE_CONSTRAINT.code) {
				throw e;
			}
			
			userAdmin = new User(1).read(connection);
		}
		
		// TODO: transactions like https://stackoverflow.com/questions/9601030/transaction-in-java-sqlite3
		
		// create logs
		new Travelogue(
				Travelogue.NO_ID, 
				userAdmin.getId(), 
				"Le Tour Eiffel is the Notre Dame of the left bank and probably the most recognised structure in the world. Like so many in Paris, it isn’t just the building that’s impressive but the surrounding landscape. To the north is the grand Palais de Chaillot, built in 1937 for the Exposition Universelle. To the south, lies the Champs de Mars, a former military training ground with the Academie Militaire at its head. This was where Napoleon trained officers from the “wrong” class but with all the right qualities in the art of warfare. The Eiffel Tower stands proudly in the middle, like a gigantic, cast-iron exclamation mark.\n\nThere are many ways to photograph the tower: you can be on it, near it or far away and still make great pictures. It’s been photographed so often, though, that it can be difficult capturing a truly original image - and there are no lengths to which photographers will not go to find their own interpretation.", 
				new Date()).create(connection);
		
		new Travelogue(
				Travelogue.NO_ID, 
				userAdmin.getId(), 
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum id volutpat eros. Vivamus lectus lorem, fermentum in venenatis quis, venenatis id ipsum. Aliquam volutpat dui in nisl auctor vestibulum. Sed eleifend diam in ipsum viverra, vitae fermentum diam sodales. Aenean tristique turpis at condimentum commodo. Donec viverra odio massa, id varius nulla tincidunt nec. Maecenas mollis nisi eu neque rhoncus, ut semper neque luctus. In hendrerit viverra suscipit. Quisque vulputate cursus libero, ac scelerisque sapien molestie et.\n\nEtiam id eleifend orci. Proin ullamcorper lectus et tellus porttitor, non elementum neque mattis. Integer consequat quam tellus, non suscipit arcu sodales ullamcorper. Vestibulum molestie vestibulum porta. Vivamus tempus, tellus non vehicula ornare, eros lorem mattis enim, nec vulputate nunc justo et massa. Duis ultricies, nulla ac pretium dictum, ligula sem rhoncus diam, eget consequat ligula velit ut odio. Curabitur rutrum tortor et volutpat tempus. Vestibulum vehicula tortor convallis mauris lacinia, sit amet pellentesque nisi scelerisque. In id sem ut ligula rutrum consequat. Maecenas mauris enim, efficitur ac sem sed, congue vestibulum nisi. Proin consectetur ipsum a orci posuere elementum. Suspendisse scelerisque urna eget urna ultrices viverra. Nullam luctus lobortis augue a placerat.\n\nNulla quis cursus tellus, in venenatis orci. Aliquam molestie sed felis eu ullamcorper. Nulla sed finibus diam. In felis enim, bibendum vitae ex eu, fermentum lacinia erat. Vivamus sodales felis sit amet nisl condimentum euismod. Etiam lacinia nunc magna, et placerat nisl ullamcorper a. Pellentesque luctus nunc vitae lorem sodales, ut porta justo suscipit. Sed at volutpat lacus. Etiam quis sapien a mauris feugiat placerat. Aliquam rhoncus velit vitae justo varius, at fermentum risus fermentum. Nulla elementum orci nec mi convallis convallis. Curabitur ut nisi quis ex suscipit tempor. Pellentesque vel mauris quis nulla lacinia finibus eu eget sapien. Vestibulum sed risus et mi molestie mollis quis non tellus.", 
				new Date(System.currentTimeMillis() - 86_400_000L * 300)).create(connection);
		
		new Travelogue(
				Travelogue.NO_ID, 
				userAdmin.getId(), 
				"Fusce iaculis, mauris nec aliquet eleifend, metus felis eleifend mi, sit amet aliquam ex dolor eget mauris. Proin urna est, auctor dictum tortor at, bibendum gravida mi. Nullam viverra mauris ac sapien vestibulum aliquet. Sed non tellus egestas, consequat velit ut, lobortis quam. Etiam aliquet lacus sem, vel egestas quam pellentesque at. Etiam imperdiet nisl et lacus molestie, eu ultricies urna faucibus. Cras ac eros purus. Phasellus fringilla interdum enim, vitae euismod metus mattis at. Nulla luctus, ligula et dictum cursus, leo ipsum vulputate justo, eget congue lectus libero sed lectus. Quisque at ex tincidunt, dignissim arcu ac, luctus dolor. Praesent mollis egestas bibendum. Sed tortor dui, interdum faucibus orci vel, sagittis rhoncus augue.\n\nNulla ornare maximus eros. In imperdiet mollis magna, eu feugiat dui facilisis ut. Nunc eget dolor nec tortor vulputate viverra. In at pulvinar felis. Phasellus nec nisl laoreet, consectetur nisi eget, luctus augue. Nullam velit nunc, vehicula sit amet felis nec, hendrerit cursus ex. Etiam pulvinar dictum pellentesque. Phasellus sed auctor ante. Suspendisse cursus enim enim, quis varius eros vestibulum sed. Proin libero neque, eleifend ac egestas ac, faucibus vel mi. Donec lacinia condimentum vulputate. Vivamus aliquam sem eu magna accumsan vestibulum. Nullam neque quam, vehicula eget nisi et, viverra gravida purus. Nullam porta aliquam hendrerit.", 
				new Date(System.currentTimeMillis() - 86_400_000L * 200)).create(connection);
		
		// copy the images to the IMG_DIR, and add to the log
		try {
			String[] imgs = new String[] {
				"a0990b1f185fd97e10698abc3a482945.jpg",
				"ab55d75410da8ca680e2ebbc0ea3119c.jpg",
				"da5d6e38916691c7465df2d6ffdc45f7.jpg",
				"950c0139867999a90a55673df10fad02.jpg",
				"f5c92854990a913d22dad0dc72ed2065.jpg",
				"9c1b36b9b96c98d6baa7d8ffba09d9fc.jpg",
				"e720542557ae61b7181cf6c526a5e47f.jpg"
			};
			
			int[] logids = new int[] {
				1,
				1,
				1,
				2,
				2,
				3,
				3
			};
			
			for (int i = 0; i < imgs.length; i++) {
				System.out.println("Copying image: " + imgs[i]);
				Files.copy(Update_0_1.class.getResourceAsStream("/resources/img/" + imgs[i]), new File(CRUDTravelogueImageRoute.IMG_DIR, imgs[i]).toPath(), StandardCopyOption.REPLACE_EXISTING);
				new TravelogueImage(TravelogueImage.NO_ID, logids[i], imgs[i]).create(connection);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
