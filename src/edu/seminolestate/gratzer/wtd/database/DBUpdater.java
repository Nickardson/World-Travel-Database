package edu.seminolestate.gratzer.wtd.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages updates to a Database.
 * @author Taylor
 * @date 2016-02-13
 * Created
 * 
 * @date 2016-03-03
 * Added transactions and rollbacks to the update process, so if an update fails the database will be returned to a good state.
 */
public class DBUpdater {
	private Connection connection;
	private List<Updater> updaters = new ArrayList<Updater>();
	
	/**
	 * An updater.
	 * Updaters strictly only upgrade from one exact "from" version to their "to" version.
	 */
	public static interface Updater {
		/**
		 * @return The database version this Updater is capable of converting.
		 */
		public int getFrom();
		
		/**
		 * @return The database version this Updater converts to.
		 */
		public int getTo();
		
		/**
		 * Implements the upgrade process
		 * @param connection The database connection to update
		 * @throws SQLException
		 */
		public void update(Connection connection) throws SQLException;
	}
	
	/**
	 * Creates a DBUpdater, which requires Updaters to be added.
	 * @param connection The database connection to update
	 */
	public DBUpdater(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Registers an updater.
	 * @param up The updater to add to the list of updaters.
	 */
	public void addUpdater(Updater up) {
		this.updaters.add(up);
	}
	
	/**
	 * Tries to update the database version using the Updaters given to this class, potentially using several updaters in the process.
	 * Updating stops when there is no updater capable of upgrading the current version.
	 * @throws SQLException
	 */
	public void update() throws SQLException {
		int currentVersion = DBUtil.getUserVersion(connection);
		
		int bestUpdaterVersion;
		Updater bestUpdater;
		
		do {
			bestUpdaterVersion = Integer.MIN_VALUE;
			bestUpdater = null;

			// find the updater which can upgrade this current "from" version to the largest "to" version
			for (Updater updater : updaters) {
				int to = updater.getTo();
				if (updater.getFrom() == currentVersion && to > currentVersion) {
					if (to > bestUpdaterVersion) {
						bestUpdaterVersion = to;
						bestUpdater = updater;
					}
				}
			}
			
			// if an updater is found, use it to update
			if (bestUpdater != null) {
				System.out.println("Updating from " + bestUpdater.getFrom() + " to " + bestUpdater.getTo());
				
				final boolean oldAutoCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);

				try {
					DBUtil.setUserVersion(connection, bestUpdaterVersion);
					bestUpdater.update(connection);
					currentVersion = bestUpdaterVersion;
				} catch(Exception e) {
					System.out.println("Exception while updating! Reverting back...");
					connection.rollback();
					
					System.out.println("Version of database is now: " + DBUtil.getUserVersion(connection));
					break;
				} finally {
					connection.commit();
					connection.setAutoCommit(oldAutoCommit);
				}
			}
		} while (bestUpdater != null);
	}
}
