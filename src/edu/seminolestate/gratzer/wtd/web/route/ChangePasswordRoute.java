package edu.seminolestate.gratzer.wtd.web.route;

import java.sql.SQLException;
import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.User;
import edu.seminolestate.gratzer.wtd.database.PasswordHasher;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
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
 * @date 2016-02-13
 */
public class ChangePasswordRoute extends AbstractHandler {
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		LoginSession login = Server.getLoginSession(session);
		if (login == null) {
			return Server.newRedirectResponse("/login?error=mustlogin");
		}
		
		// create a user object with an unknown ID, and not admin with the given username/password
		String currentPassword = session.getParms().get("currentpassword");
		String newPassword = session.getParms().get("newpassword");
		String confirmNewPassword = session.getParms().get("confirmnewpassword");
		
		// confirm the new passwords match
		if (!newPassword.equals(confirmNewPassword)) {
			return Server.newRedirectResponse("/account?error=nomatch");
		}
		
		String hashedPassword = PasswordHasher.hashPassword(newPassword);
		
		try {
			User user = new User(login.getUserID()).read(Main.dbConnection);
			
			// confirm the given current password is correct
			if (!PasswordHasher.hashPassword(currentPassword).equals(user.getPassword())) {
				return Server.newRedirectResponse("/account?error=wrongcurrent");
			}
			
			// update the password
			user.setPassword(hashedPassword);
			user.update(Main.dbConnection);
			
			// TODO: invalidate session?
			return Server.newRedirectResponse("/");
		} catch (SQLException e) {
			e.printStackTrace();
			return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Internal server error: " + e.getMessage());
		}
	}
}