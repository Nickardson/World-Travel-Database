package edu.seminolestate.gratzer.wtd.web.route;

import java.sql.SQLException;
import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.User;
import edu.seminolestate.gratzer.wtd.database.PasswordHasher;
import edu.seminolestate.gratzer.wtd.web.PathedCookie;
import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * A route which can register a user
 * Takes the parameters "username" and "password", registers a user and gives the user a session cookie
 * @author Taylor
 */
public class RegisterRoute extends AbstractHandler {
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		// create a user object with an unknown ID, and not admin with the given username/password
		String username = session.getParms().get("username");
		String password = session.getParms().get("password");
		
		if (password.length() < 6) {
			return Server.newRedirectResponse("/register?error=short");
		}
		
		String hashedPassword = PasswordHasher.hashPassword(password);
		
		User newUser = new User(User.NO_ID, username, hashedPassword, false);
		try {
			User checkUser = new User(User.NO_ID, username, hashedPassword, false).read(Main.dbConnection, username);
			if (checkUser.getId() == User.NO_ID) {
				System.out.println("Registering new user: " + checkUser);
				
				newUser.create(Main.dbConnection);
				newUser.read(Main.dbConnection, username);
				
				int sessionID = Main.server.generateLoginSession(newUser.getId()).getSessionID();
				session.getCookies().set(new PathedCookie("sessionid", Integer.toString(sessionID), 5, "/"));
				
				return Server.newRedirectResponse("/");
			} else {
				return Server.newRedirectResponse("/register?error=exists");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Internal server error: " + e.getMessage());
		}
	}
}