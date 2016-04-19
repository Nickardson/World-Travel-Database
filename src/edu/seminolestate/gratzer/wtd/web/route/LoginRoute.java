package edu.seminolestate.gratzer.wtd.web.route;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.IBean;
import edu.seminolestate.gratzer.wtd.beans.User;
import edu.seminolestate.gratzer.wtd.database.PasswordHasher;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
import edu.seminolestate.gratzer.wtd.web.PathedCookie;
import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * A route which creates a login session and assigns a session cookie
 * @author Taylor
 * @date 2016-02-13
 */
public class LoginRoute extends AbstractHandler {
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		String paramUsername = session.getParms().get("username");
		String paramPassword = session.getParms().get("password");
		
		try {
			// get use
			PreparedStatement query = Main.instance.dbConnection.prepareStatement("SELECT * FROM users WHERE username = ?");
			query.setString(1, paramUsername);
			User user = IBean.executeQuery(User.class, query).get(0);
			
			if (user != null && user.getPassword().equals(PasswordHasher.hashPassword(paramPassword))) {
				LoginSession login = Main.instance.server.generateLoginSession(user.getId());
				session.getCookies().set(new PathedCookie("sessionid", Integer.toString(login.getSessionID()), 5, "/"));
				
				return Server.newRedirectResponse("/");
			} else {
				// TODO: redirect back to referred rather than hardcode
				return Server.newRedirectResponse("/login?error=wrong");
			}
		} catch (SQLException e) {
			e.printStackTrace();			
			return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Internal SQL error, please contact your server administrator.");
		} catch (IndexOutOfBoundsException e) {
			// @date 2016-03-20 fix poor error message on invalid username 
			return Server.newRedirectResponse("/login?error=wrong");
		}
	}
}
