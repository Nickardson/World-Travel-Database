package edu.seminolestate.gratzer.wtd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import mjson.Json;

import org.sqlite.SQLiteConfig;

import edu.seminolestate.gratzer.wtd.database.DBUpdater;
import edu.seminolestate.gratzer.wtd.database.DBUtil;
import edu.seminolestate.gratzer.wtd.database.updates.Update_0_1;
import edu.seminolestate.gratzer.wtd.database.updates.Update_1_2;
import edu.seminolestate.gratzer.wtd.database.updates.Update_2_3;
import edu.seminolestate.gratzer.wtd.database.updates.Update_3_4;
import edu.seminolestate.gratzer.wtd.ui.Tray;
import edu.seminolestate.gratzer.wtd.web.PageProvider;
import edu.seminolestate.gratzer.wtd.web.Server;
import edu.seminolestate.gratzer.wtd.web.provider.AllPopulator;
import edu.seminolestate.gratzer.wtd.web.provider.DebugPopulator;
import edu.seminolestate.gratzer.wtd.web.provider.IndexPopulator;
import edu.seminolestate.gratzer.wtd.web.provider.TraveloguePopulator;
import edu.seminolestate.gratzer.wtd.web.provider.UsersPopulator;
import fi.iki.elonen.NanoHTTPD;

/**
 * Entry point for the World Travel Database server.
 * 
 * Application is written in Java (obviously), using SQLite for a single-file database.
 * Jade4J is used to render Jade templates for most pages.
 * The web server is created through a slightly modified NanoHTTPD
 * 
 * @author Taylor
 * @date 2016-02-14
 * @date 2016-04-03
 * Added Update_2_3 to update list.
 */
public class Main {
	private static Logger LOG = Logging.get(Main.class);
	
	public Connection dbConnection = null;
	public Server server;
	public boolean isEclipse;
	
	public PageProvider pageProvider;
	
	public static Main instance;
	public static void main(String[] args) throws Exception {
		instance = new Main(args);
	}
	
	public Main(String[] args) throws Exception {
		// load the JDBC class
		Class.forName("org.sqlite.JDBC");

		LOG.info("Starting up Main.");
		Properties properties = getProperties();
		
		// a parameter can put application into eclipse mode, for immediate updates to the web files 
		isEclipse = false;
		if (args.length > 0) {
			if (args[0].equals("eclipse")) {
				LOG.info("Using src path for resources.");
				isEclipse = true;
			}
		}
	
		LOG.info("Creating Web Server");
		server = new Server(Integer.parseInt(properties.getProperty("web.port")));
		
		// Create connection to database
		LOG.info("Connecting to Database");
		
		// initial database config
		SQLiteConfig config = new SQLiteConfig();
		config.enforceForeignKeys(true);
		
		dbConnection = DriverManager.getConnection("jdbc:sqlite:" + properties.getProperty("database.name"), config.toProperties());
		Statement statement = dbConnection.createStatement();
		statement.setQueryTimeout(Integer.parseInt(properties.getProperty("database.timeoutseconds")));

		LOG.info("Turning on PRAGMA foreign_keys");
		statement.executeUpdate("PRAGMA foreign_keys = 1");
		ResultSet r = statement.executeQuery("PRAGMA foreign_keys");
		r.next();
		boolean isPragmaForeignKeys = r.getInt(1) == 1;
		LOG.info("PRAGMA FOREIGN_KEYS: " + isPragmaForeignKeys);
		if (!isPragmaForeignKeys) {
			LOG.severe("Foreign keys are not on!");
		}
		r.close();
		
		update();
		
		// @date 2016-03-20 registers a factory for JSON objects, which allows convertable beans, etc.
		Json.setGlobalFactory(new CustomJsonFactory());
		
		pageProvider = getPageProvider();
		
		// start web server
		LOG.info("Starting Web Server");
		server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

		// create tray icon
		LOG.info("Creating Tray");
  		try {
  			new Tray();
  		} catch (Exception e) {
  			LOG.severe("Could not create tray icon, does platform support it?");
  		}
	}

	private Properties _properties = null;
	public Properties getProperties() throws IOException {
		if (_properties != null)
			return _properties;
		
		// read properties file
		Properties defaultProps = new Properties();
		InputStream in = Main.class.getResourceAsStream("/resources/config.properties");
		defaultProps.load(in);
		in.close();

		File configFile = new File("worldtravel.properties");
		if (!configFile.exists()) {
			LOG.info("Config file does not exist, creating.");
			defaultProps.store(new FileOutputStream(configFile), "Configuration for the World Travel Database.");
		} else {
			LOG.info("Config file exists.");
		}
		
		Properties properties = new Properties(defaultProps);
		in = new FileInputStream(configFile);
		properties.load(in);
		in.close();
		
		_properties = properties;
		
		return properties;
	}

	private void update() throws SQLException {
		// Run updates
		LOG.info("Database Version: " + DBUtil.getUserVersion(dbConnection));
		LOG.info("Checking for Database Upgrade versions.");
		
		DBUpdater updater = new DBUpdater(dbConnection);
		updater.addUpdater(new Update_0_1());
		updater.addUpdater(new Update_1_2());
		updater.addUpdater(new Update_2_3());
		updater.addUpdater(new Update_3_4());
		
		updater.update();
		LOG.info("Database Version: " + DBUtil.getUserVersion(dbConnection));
	}
	
	private PageProvider getPageProvider() {
		// Register data providers for Jade Pages
		LOG.info("Registering Page Populators");
		
		PageProvider provider = new PageProvider();
		// provide the front page owned by the logged in user
		provider.register(new IndexPopulator(), "/resources/html/index.jade");
		
		// debug page
		provider.register(new DebugPopulator(), "/resources/html/debug.jade");
		
		// @date 2016-03-20 reads from the view "userinfo", and provides the list of names
		provider.register(new UsersPopulator(), "/resources/html/users.jade");
		
		// @date 2016-03-20 A new page for shared travelogues
		provider.register(new TraveloguePopulator(), "/resources/html/travelogue.jade");
		
		// add login information to all pages
		provider.registerPattern(new AllPopulator());
		
		return provider;
	}

	public void exit() {
		// TODO: exit at a good time
		
		try {
			server.stop();
			dbConnection.close();
		} catch (SQLException e) {
			LOG.severe("SQL Exception while closing server: " + e);
		}
		
		System.exit(0);
	}
}

//TODO: when redirecting users because not logged in, return to previous page