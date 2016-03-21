package edu.seminolestate.gratzer.wtd.web.route;

import java.sql.SQLException;
import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.Location;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * A route which CRUDs a Location
 * @author Taylor
 * @date 2016-03-20
 */
public class CRUDLocationRoute extends AbstractHandler {
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		LoginSession login = Server.getLoginSession(session);
		if (login == null) {
			return Server.newRedirectResponse("/login/?error=mustlogin");
		}
		
		Server.processPost(session);
		
		if (session.getMethod() == Method.POST) {
			// CREATE location
			// "name": (string) the name of this location
			// "latitude": (int) 
			// "longitude": (int)
			// "shared": (boolean) whether the location is shared
			
			int ownerid = login.getUserID();
			String name = session.getParms().get("name");
			double latitude = Double.parseDouble(session.getParms().get("latitude"));
			double longitude = Double.parseDouble(session.getParms().get("longitude"));
			boolean shared = Boolean.parseBoolean(session.getParms().get("shared"));
			
			try {
				Location location = new Location(Location.NO_ID, ownerid, name, latitude, longitude, shared);
				location.create(Main.dbConnection);
				
				// print out the new ID to response
				return NanoHTTPD.newFixedLengthResponse(Integer.toString(location.getId()));
			} catch (SQLException e) {
				e.printStackTrace();
				return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal server error: " + e.getMessage());
			}
		} else if (session.getMethod() == Method.PUT) {
			// UPDATE location
			
			String idString = urlParams.get("id");
			if (idString != null) {
				int id = Integer.parseInt(idString);
				
				try {
					Location location = new Location(id).read(Main.dbConnection);
					System.out.println("Updating location " + location);
					// location must exist and be owned by the user
					// TODO: ensure that this properly checks for null entries
					if (location.getId() == Location.NO_ID) {
						return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "invalid log id");
					}
					
					// TODO: allow admins to edit always
					if (location.getOwnerid() != login.getUserID()) {
						return NanoHTTPD.newFixedLengthResponse(Status.FORBIDDEN, "text/plain", "not the owner of this log");
					}
					
					boolean changed = false;
					
					String name = session.getParms().get("name");
					if (name != null && name.length() > 0) {
						location.setName(name);
						changed = true;
					}
					
					if (session.getParms().containsKey("latitude")) {
						location.setLatitude(Double.parseDouble(session.getParms().get("latitude")));
						changed = true;
					}
					
					if (session.getParms().containsKey("longitude")) {
						location.setLongitude(Double.parseDouble(session.getParms().get("longitude")));
						changed = true;
					}
					
					if (session.getParms().containsKey("shared")) {
						location.setShared(Boolean.parseBoolean(session.getParms().get("shared")));
						changed = true;
					}
					
					if (changed) {
						location.update(Main.dbConnection);
						return NanoHTTPD.newFixedLengthResponse(Integer.toString(location.getId()));
					} else {
						return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "no change");
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
					return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "sql error");
				} catch (NumberFormatException e) {
					// @date 2016-03-12 Although the fields are not exposed to direct user input, catches number format with special message
					return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "input error");
				}
			} else {
				return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "no id provided");
			}
		} else if (session.getMethod() == Method.DELETE) {
			// DELETE location
			// "id": (int) the id of the location to delete
			
			// TODO: confirm user owns location
			String idString = urlParams.get("id");
			int id = Integer.parseInt(idString);
			
			try {
				new Location(id).delete(Main.dbConnection);
				return NanoHTTPD.newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT, "");
			} catch (SQLException e) {
				e.printStackTrace();
				return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Failed to delete");
			}
		} else {
			// unsupported method
			return NanoHTTPD.newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT, "Method not supported");
		}
	}
}
