package edu.seminolestate.gratzer.wtd.web.route;

import java.sql.SQLException;
import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.Attraction;
import edu.seminolestate.gratzer.wtd.beans.Travelogue;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * A route which CRUDs an attraction
 * @author Taylor
 * @date 2016-04-17
 */
public class CRUDAttractionRoute extends AbstractHandler {
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		LoginSession login = Server.getLoginSession(session);
		if (login == null) {
			return Server.newRedirectResponse("/login/?error=mustlogin");
		}
		
		Server.processPost(session);
		
		if (session.getMethod() == Method.POST) {
			// CREATE attraction
			// "name": (string)
			// "logid": (int) 
			// "rating": (int)
			// "comment": (string)
			// "type": (string)
			
//			int ownerid = login.getUserID();
			String name = session.getParms().get("name");
			int logid = Integer.parseInt(session.getParms().get("logid"));
			int rating = Integer.parseInt(session.getParms().get("rating"));
			String comment = session.getParms().get("comment");
			String type = session.getParms().get("type");
			
			try {
				Attraction attraction = new Attraction(Attraction.NO_ID, logid, name, rating, comment, type);
				attraction.create(Main.dbConnection);
				
				// print out the new ID to response
				return NanoHTTPD.newFixedLengthResponse(Integer.toString(attraction.getId()));
			} catch (SQLException e) {
				e.printStackTrace();
				return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal server error: " + e.getMessage());
			}
		} else if (session.getMethod() == Method.PUT) {
			// UPDATE attraction
			
			String idString = urlParams.get("id");
			if (idString != null) {
				int id = Integer.parseInt(idString);
				
				try {
					Attraction attraction = new Attraction(id).read(Main.dbConnection);
					System.out.println("Updating Attraction " + attraction);
					// location must exist and be owned by the user
					// TODO: ensure that this properly checks for null entries
					if (attraction.getId() == Attraction.NO_ID) {
						return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "invalid log id");
					}
					
					Travelogue ownerLog = new Travelogue(attraction.getLogid()).read(Main.dbConnection);
					// TODO: allow admins to edit always
					if (ownerLog.getOwnerid() != login.getUserID()) {
						return NanoHTTPD.newFixedLengthResponse(Status.FORBIDDEN, "text/plain", "not the owner of this log");
					}
					
					boolean changed = false;
					
					String name = session.getParms().get("name");
					if (name != null && name.length() > 0) {
						attraction.setName(name);
						changed = true;
					}
										
					if (session.getParms().containsKey("logid")) {
						attraction.setLogid(Integer.parseInt(session.getParms().get("logid")));
						changed = true;
					}
					
					if (session.getParms().containsKey("rating")) {
						attraction.setRating(Integer.parseInt(session.getParms().get("rating")));
						changed = true;
					}
					
					if (session.getParms().containsKey("comment")) {
						attraction.setComment(session.getParms().get("comment"));
						changed = true;
					}
					
					if (session.getParms().containsKey("type")) {
						attraction.setType(session.getParms().get("type"));
						changed = true;
					}
					
					if (changed) {
						attraction.update(Main.dbConnection);
						return NanoHTTPD.newFixedLengthResponse(Integer.toString(attraction.getId()));
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
			// DELETE attraction
			// "id": (int) the id of the attraction to delete
			
			String idString = urlParams.get("id");
			int id = Integer.parseInt(idString);
			
			try {
				System.out.println("Deleting attraction " + id);
				new Attraction(id).delete(Main.dbConnection);
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
