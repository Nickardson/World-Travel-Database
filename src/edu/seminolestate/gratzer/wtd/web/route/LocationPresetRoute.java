package edu.seminolestate.gratzer.wtd.web.route;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import mjson.Json;
import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.IBean;
import edu.seminolestate.gratzer.wtd.beans.Location;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * A route which responds with a JSON table of locations which the current user is privileged to access.
 * @author Taylor
 * @date 2016-03-20
 */
public class LocationPresetRoute extends AbstractHandler {
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		try {
			LoginSession login = Server.getLoginSession(session);
			int userID = -1;
			
			// optionally use login to make query allow non-shared entries
			if (login != null) {
				userID = login.getUserID();
			}
			
			PreparedStatement query = Main.instance.dbConnection.prepareStatement("SELECT * FROM locations WHERE shared OR ownerid = ?");
			query.setInt(1, userID);
			List<Location> locations = IBean.executeQuery(Location.class, query);
			
			return NanoHTTPD.newFixedLengthResponse(Json.object().set("locations", locations).toString());
		} catch (SQLException e) {
			e.printStackTrace();			
			return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Internal SQL error, please contact your server administrator.");
		}
	}
}
