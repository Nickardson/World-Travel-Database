package edu.seminolestate.gratzer.wtd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import mjson.Json;

import org.sqlite.SQLiteConfig;

import edu.seminolestate.gratzer.wtd.beans.IBean;
import edu.seminolestate.gratzer.wtd.beans.Location;
import edu.seminolestate.gratzer.wtd.beans.Travelogue;
import edu.seminolestate.gratzer.wtd.beans.TravelogueImage;
import edu.seminolestate.gratzer.wtd.beans.User;
import edu.seminolestate.gratzer.wtd.beans.UserInfo;
import edu.seminolestate.gratzer.wtd.database.DBUpdater;
import edu.seminolestate.gratzer.wtd.database.DBUtil;
import edu.seminolestate.gratzer.wtd.database.updates.Update_0_1;
import edu.seminolestate.gratzer.wtd.database.updates.Update_1_2;
import edu.seminolestate.gratzer.wtd.ui.Tray;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
import edu.seminolestate.gratzer.wtd.web.PageProvider;
import edu.seminolestate.gratzer.wtd.web.PageProvider.Populator;
import edu.seminolestate.gratzer.wtd.web.PageProvider.RegexPopulator;
import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * Entry point for the World Travel Database server.
 * 
 * Application is written in Java (obviously), using SQLite for a single-file database.
 * Jade4J is used to render Jade templates for most pages.
 * The web server is created through a slightly modified NanoHTTPD
 * 
 * @author Taylor
 * @date 2016-02-14
 */
public class Main {
	private static Logger LOG = Logging.get(Main.class);
	
	public static Connection dbConnection = null;
	public static Server server;
	public static boolean isEclipse;
	public static Properties properties;
	
	public static void main(String[] args) throws Exception {
		LOG.info("Starting up Main.");
		
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
		
		properties = new Properties(defaultProps);
		in = new FileInputStream(configFile);
		properties.load(in);
		in.close();
		
		
		// a parameter can put application into eclipse mode, for immediate updates to the web files 
		isEclipse = false;
		if (args.length > 0) {
			if (args[0].equals("eclipse")) {
				LOG.info("Using src path for resources.");
				isEclipse = true;
			}
		}
		
		// load the JDBC class
		Class.forName("org.sqlite.JDBC");

		try {
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
			
			// Run updates
			LOG.info("Database Version: " + DBUtil.getUserVersion(dbConnection));
			LOG.info("Checking for Database Upgrade versions.");
			
			DBUpdater updater = new DBUpdater(dbConnection);
			updater.addUpdater(new Update_0_1());
			updater.addUpdater(new Update_1_2());
			
			updater.update();
			LOG.info("Database Version: " + DBUtil.getUserVersion(dbConnection));
			
			// @date 2016-03-20 registers a factory for JSON objects, which allows convertable beans, etc.
			Json.setGlobalFactory(new CustomJsonFactory());
			
			// Register data providers for Jade Pages
			LOG.info("Registering Page Populators");
			PageProvider.register(new Populator() {
				@Override
				public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
					LoginSession login = Server.getLoginSession(session);
					if (login != null) {
						// list travelogues owned by the current user
						try {
							PreparedStatement query = Main.dbConnection.prepareStatement("SELECT * FROM travelogues WHERE ownerid = ? ORDER BY visitdate DESC");
							query.setInt(1, login.getUserID());
							model.put("travelogues", IBean.executeQuery(Travelogue.class, query));
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}, "/resources/html/index.jade");
			
			PageProvider.register(new Populator() {
				@Override
				public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
					LoginSession login = Server.getLoginSession(session);
					
					if (login == null) {
						model.put("unauthorized", true);
						return;
					}
					
					try {
						User u = new User(login.getUserID()).read(Main.dbConnection);
						if (u != null && u.isAdmin()) {
							model.put("users", IBean.executeQuery(User.class, Main.dbConnection.prepareStatement("SELECT * FROM users")));
							model.put("travelogues", IBean.executeQuery(Travelogue.class, Main.dbConnection.prepareStatement("SELECT * FROM travelogues")));
							model.put("images", IBean.executeQuery(TravelogueImage.class, Main.dbConnection.prepareStatement("SELECT * FROM travel_images")));
							
							// @date 2016-03-20 Add locations to debug view 
							model.put("locations", IBean.executeQuery(Location.class, Main.dbConnection.prepareStatement("SELECT * FROM locations")));
							
							model.put("properties", Main.properties);
							
							try {
								model.put("ip", InetAddress.getLocalHost().toString());
							} catch (UnknownHostException ignored) {
							}
						} else {
							model.put("unauthorized", true);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}, "/resources/html/debug.jade");
			
			// @date 2016-03-20 reads from the view "userinfo", and provides the list of names
			PageProvider.register(new Populator() {
				@Override
				public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
					try {
						List<UserInfo> viewUsers = IBean.executeQuery(UserInfo.class, Main.dbConnection.prepareStatement("SELECT * FROM userinfo ORDER BY logcount DESC"));
						model.put("users", viewUsers);
					} catch (SQLException e) {
					}					
				}
			}, "/resources/html/users.jade");
			
			// @date 2016-03-20 A new page for shared travelogues
			PageProvider.register(new Populator() {
				@Override
				public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
					try {
						// TODO: verify that log id exists
						int id = Integer.parseInt(session.getParms().get("id"));
						Travelogue log = new Travelogue(id).read(dbConnection);
						
						LoginSession login = Server.getLoginSession(session);
						
						// if not the verified owner, is readonly, which means it won't bother showing the gear
						if (login == null || login.getUserID() != log.getOwnerid()) {
							model.put("readonly", true);
							
							// if log is not shared and user is not authorized, unauthorize them
							if (!log.isShared()) {
								model.put("unauthorized", true);
								return;
							}
						}
						
						if (session.getParms().containsKey("id")) {
							model.put("log", log);
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}, "/resources/html/travelogue.jade");
			
			// add login information to all pages
			PageProvider.registerPattern(new RegexPopulator() {
				private final Pattern PATTERN = Pattern.compile("\\/resources\\/html\\/[\\w]+.jade");
				@Override
				public Pattern getPattern() {
					return PATTERN;
				}
				
				@Override
				public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
					// add error parms
					if (session.getParms().get("error") != null) {
						model.put("error", session.getParms().get("error"));
					}
					
					try {
						String sessionID = session.getCookies().read("sessionid");
						if (sessionID != null) {
							LoginSession login = Main.server.getLoginSession(Integer.parseInt(sessionID));
							
							if (login != null) {
								model.put("userid", login.getUserID());
								model.put("username", new User(login.getUserID()).read(dbConnection).getUsername());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
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
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			LOG.severe("SQL Exception while starting up: " + e);
		}
	}

	public static void exit() {
		// TODO: exit at a good time
		
		try {
			Main.server.stop();
			Main.dbConnection.close();
		} catch (SQLException e) {
			LOG.severe("SQL Exception while closing server: " + e);
		}
		
		System.exit(0);
	}
}

//TODO: when redirecting users because not logged in, return to previous page