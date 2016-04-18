package edu.seminolestate.gratzer.wtd.web;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import edu.seminolestate.gratzer.wtd.Logging;
import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.web.route.*;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Web server interface server
 * @author Taylor
 * @date 2016-02-14
 */
public class Server extends RouterNanoHTTPD {
	private static Logger LOG = Logging.get(Server.class);
	
	/**
	 * Maps sessionid to loginsessions
	 */
	private Map<Integer, LoginSession> loginSessions;
	
	private Random random = new Random();
	
	/**
	 * Whether server should cache pages
	 */
	private boolean isCaching;
	
	/**
	 * Creates the web server bound to the given port
	 * @param port The port to bind to
	 */
	public Server(int port) {
		// TODO: allow only one instance
		super(port);
		
		LOG.info("Web Port: " + port);
		
		isCaching = Main.properties.getProperty("web.caching").equals("true");
		LOG.info("web.caching?: " + isCaching);
		
		addMappings();
		loginSessions = new HashMap<>();
		
		setTempFileManagerFactory(new CustomTempFileManagerFactory());
	}
	
	/**
	 * Processes the given session.
	 * Does nothing if the request is not POST/PUT.
	 * If request is POST/PUT, gets the parameters and files
	 * @param session
	 * @return
	 */
	public static Map<String, String> processPost(IHTTPSession session) {
		Map<String, String> files = new HashMap<String, String>();
//		Method method = session.getMethod();
//		if (Method.PUT.equals(method) || Method.POST.equals(method)) {
		    try {
		        session.parseBody(files);
		    } catch (IOException ioe) {
		    } catch (ResponseException re) {
		    }
//		}
		
		return files;
	}
	
	/**
	 * Gets the LoginSession for the given session id.
	 * @param sessionID The session id
	 * @return The LoginSession
	 */
	public LoginSession getLoginSession(int sessionID) {
		return loginSessions.get(sessionID);
	}
	
	/**
	 * Creates a LoginSession for the given user id
	 * @param userID The ID of the user to be used in the session
	 * @return A login session
	 */
	public LoginSession generateLoginSession(int userID) {
		int sessionID;
		
		// it's unlikely to collide, but make sure that the session ID is unique.
		do {
			sessionID = random.nextInt();
		} while (loginSessions.containsKey(sessionID));
		
		LoginSession s = new LoginSession(sessionID, userID);
		loginSessions.put(sessionID, s);
		
		return s;
	}
	
	/**
	 * Remove a user session. Doesn't affect cookies on the user side
	 * @param parseInt The session id to remove
	 */
	public void deleteUserSession(int parseInt) {
		loginSessions.remove(parseInt);
	}
	
	/**
	 * Creates a redirect response to a given path
	 * @param text The text that is sent in the body of the response. Shown if the browser does not respect the redirect
	 * @param path The path that the user will be redirected to
	 * @return The response created.
	 */
	public static Response newRedirectResponse(String text, String path) {
		Response res = NanoHTTPD.newFixedLengthResponse(Status.REDIRECT, NanoHTTPD.MIME_HTML, text);
		res.addHeader("Location", path);
		return res;
	}
	
	/**
	 * Creates a redirect response to a given path, and has a default redirect message for if it fails.
	 * @param path The path that the user will be redirected to.
	 * @return The response created.
	 */
	public static Response newRedirectResponse(String path) {
		return Server.newRedirectResponse("You should be redirected to <a href=\"" + path + "\">" + path + "</a>.", path);
	}
	
	/**
	 * Gets the LoginSession from the HTTP session, or null if the cookie doesn't exist
	 * @param session The HTTP session to check for cookies
	 * @return The LoginSession, or null if it doesn't exist.
	 */
	public static LoginSession getLoginSession(IHTTPSession session) {
		String sessionid = session.getCookies().read("sessionid");
		if (sessionid != null) {
			LoginSession login = Main.server.getLoginSession(Integer.parseInt(sessionid));
			return login;
		} else {
			return null;
		}
	}
	
	@Override
	public void addMappings() {
		super.addMappings();
		
		
		// form routes
		addRoute("/form/register/", RegisterRoute.class);
		addRoute("/form/login/", LoginRoute.class);
		addRoute("/form/changepw/", ChangePasswordRoute.class);
		
		addRoute("/logout/", LogoutRoute.class);
		
		// CRUD routes
		addRoute("/crud/travelogue_image/?", CRUDTravelogueImageRoute.class);
		addRoute("/crud/travelogue_image/:id", CRUDTravelogueImageRoute.class);
		
		addRoute("/crud/travelogue/?", CRUDTravelogueRoute.class);
		addRoute("/crud/travelogue/:id", CRUDTravelogueRoute.class);
		
		addRoute("/crud/location/?", CRUDLocationRoute.class);
		addRoute("/crud/location/:id", CRUDLocationRoute.class);
		
		// 2016-04-17 added attraction
		addRoute("/crud/attraction/?", CRUDAttractionRoute.class);
		addRoute("/crud/attraction/:id", CRUDAttractionRoute.class);
		
		// ajax routes
		addRoute("/ajax/locations", LocationPresetRoute.class);
		
		// file serving routes
		addRoute("/", JadeRendererRoute.class);
		addRoute("/:path", JadeRendererRoute.class);
		addRoute("/img/(.)+", StaticPageHandler.class, new File("img").getAbsoluteFile());
		
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		Response res = super.serve(session);
		
		// disable the cache
		// TODO: cache is sometimes appropriate
		if (isCaching && session.getMethod().equals(Method.GET)) {
			res.addHeader("Cache-Control", "no-cache, no-store");
			res.addHeader("Pragma", "no-cache");
			res.addHeader("Expires", "0");
		}
		
		return res;
	}
}
