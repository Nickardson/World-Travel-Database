package edu.seminolestate.gratzer.wtd.web.route;

import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * A route which removes the user session and cookie, and redirects to the front page.
 * @author Taylor
 * @date 2016-02-13
 */
public class LogoutRoute extends AbstractHandler {
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		Response res = Server.newRedirectResponse("/");
		
		String idString = session.getCookies().read("sessionid");
		if (idString != null) {
			Main.server.deleteUserSession(Integer.parseInt(idString));
			session.getCookies().delete("sessionid");
		}
		
		return res;
	}
}
