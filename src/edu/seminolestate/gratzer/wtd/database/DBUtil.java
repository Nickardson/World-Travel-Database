package edu.seminolestate.gratzer.wtd.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.WeakHashMap;

import edu.seminolestate.gratzer.wtd.Main;

/**
 * Utilties for managing databases
 * @author Taylor
 * @date 2016-02-13
 */
public class DBUtil {
	/**
	 * Gets whether the database table has the given column
	 * @param connection The connection to the database
	 * @param table The name of the table
	 * @param column The name of the column in the table
	 * @return Whether the column exists
	 * @throws SQLException
	 */
	public static boolean doesTableHaveColumn(Connection connection, String table, String column) throws SQLException {
		// build a table_info query for the given table
		// (PreparedStatement does not support parameterized table names)
		PreparedStatement pragma = connection.prepareStatement("PRAGMA table_info(" + table + ")");
		
		// check if any row (representing a column) has a "name" value equal to our desired column 
		ResultSet rs = pragma.executeQuery();
		while (rs.next()) {
			String columnName = rs.getString("name");
			if (columnName.equalsIgnoreCase(column)) {
				return true;
			}
		}
		rs.close();
		
		return false;
	}
	
	private static WeakHashMap<Connection, Integer> versionCache = new WeakHashMap<Connection, Integer>();
	
	/**
	 * Gets the version field of the database.
	 * @param connection The connection to the database
	 * @return The version of the database
	 * @throws SQLException
	 * @date 2016-03-20 Added a cache to fix problems with version not updating until after transactions are finished
	 */
	public static int getUserVersion(Connection connection) throws SQLException {
		if (versionCache.containsKey(connection)) {
			return versionCache.get(connection);
		}
		
		PreparedStatement get = connection.prepareStatement("PRAGMA user_version");
		ResultSet rs = get.executeQuery();
		
		int result = 0;
		while (rs.next()) {
			result = rs.getInt(1);
		}
		rs.close();
		
		versionCache.put(connection, result);
		
		return result;
	}
	
	/**
	 * Sets the version field of the database.
	 * @param connection The connection to the database
	 * @param version The new version of the database
	 * @throws SQLException
	 * @date 2016-03-20 Added a cache to fix problems with version not updating until after transactions are finished 
	 */
	public static void setUserVersion(Connection connection, int version) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("PRAGMA user_version = " + version);
		statement.executeUpdate();
		
		versionCache.put(connection, version);
	}

	/**
	 * Gets the rowid of the last insert.
	 * This should only be called directly after an insert operation to the given on the main thread.
	 * @return The row ID of the last inserted row.
	 * @throws SQLException
	 */
	public static int getLastRowID(Connection connection) throws SQLException {
		Statement s = connection.createStatement();
		ResultSet rs = s.executeQuery("SELECT last_insert_rowid() FROM users");
		rs.next();
		int newID = rs.getInt(1);
		rs.close();
		s.close();
		return newID;
	}

	/**
	 * Renames the existing table to a new table name
	 * Creates a new table from the given SQL statement.
	 * Copies data from the temporary table
	 * Deletes the old table (under the new name)
	 * @param table The name of the current (and new) table.
	 * @param statement The statement to execute updates on.
	 * @param sql The SQL schema of the new table
	 * @throws SQLException
	 */
	public static void migrateTable(String table, Statement statement, String sql) throws SQLException {
		final boolean oldAutoCommit = Main.instance.dbConnection.getAutoCommit();
		Main.instance.dbConnection.setAutoCommit(false);
		
		try {
			statement.executeUpdate("ALTER TABLE " + table + " RENAME TO _temp" + table);
			statement.executeUpdate(sql);
			
			// TODO: confirm copied data
			System.out.println("Insert from migrate: " + statement.executeUpdate("INSERT INTO " + table + " SELECT * FROM _temp" + table));
			
			statement.executeUpdate("DROP TABLE _temp" + table);
		} catch (Exception e) {
			e.printStackTrace();
			Main.instance.dbConnection.rollback();
		} finally {
			Main.instance.dbConnection.commit();
			Main.instance.dbConnection.setAutoCommit(oldAutoCommit);
		}
	}
	
	// PRAGMA schema.user_version = integer ;
}
