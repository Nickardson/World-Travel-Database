package edu.seminolestate.gratzer.wtd.web.route;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
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
 * A route which CRUDs a Travelogue.
 * @author Taylor
 * @date 2016-02-13
 */
public class CRUDTravelogueRoute extends AbstractHandler {
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		LoginSession login = Server.getLoginSession(session);
		
		Server.processPost(session);
		
		if (session.getMethod() == Method.POST) {
			// CREATE log
			// ex: POST /crud/travelogue/
			
			String content = session.getParms().get("content");
			
			if (content.length() > 10_000) {
				return NanoHTTPD.newFixedLengthResponse(Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "content length");
			}
			
			Date date = null;
			
			String dateString = session.getParms().get("visitdate");
			System.out.println(dateString);
			if (dateString != null && dateString.length() != 0) {
				try {
					date = new Date(Long.parseLong(dateString));					
				} catch (NumberFormatException ignored) {
					ignored.printStackTrace();
					try {
						date = Travelogue.DATE_FORMAT.parse(dateString);
					} catch (ParseException e) {
						return NanoHTTPD.newFixedLengthResponse(Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "visitdate");
					}
				}
			}
			
			// TODO: test shared and locationid
			boolean shared = false;
			if (session.getParms().containsKey("shared")) {
				shared = Boolean.parseBoolean(session.getParms().get("shared"));
			}
			
			int locationid = Travelogue.NO_ID;
			if (session.getParms().containsKey("locationid")) {
				locationid = Integer.parseInt(session.getParms().get("locationid"));
			}
			
			Travelogue log = new Travelogue(Travelogue.NO_ID, login.getUserID(), content, date, shared, locationid);
			try {
				log.create(Main.instance.dbConnection);
				return NanoHTTPD.newFixedLengthResponse("success");
			} catch (SQLException e) {
				e.printStackTrace();
				return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "sql error");
			}
		} else if (session.getMethod() == Method.PUT) {
			// UPDATE log
			// ex: PUT /crud/travelogue/324
			//     (Optional) ?content=string
			//     (Optional) ?visitdate=string like "1996-03-12 15:32"
			
			String idString = urlParams.get("id");
			if (idString != null) {
				int id = Integer.parseInt(idString);
				
				try {
					Travelogue log = new Travelogue(id).read(Main.instance.dbConnection);
					System.out.println("Updating log " + log);
					// log must exist and be owned by the user
					// TODO: allow admins to edit always
					// TODO: ensure that this properly checks for null entries
					if (log != null) {
						if (log.getOwnerid() == login.getUserID()) {
							boolean changed = false;
							
							String content = session.getParms().get("content");
							if (content != null) {
								changed = true;
								log.setContent(content);
							}
							
							String visitdate = session.getParms().get("visitdate");
							if (visitdate != null) {
								changed = true;
								
								// try reading date as a number, then try reading as a formatted date.
								try {
									log.setVisitDate(new Date(Long.parseLong(visitdate)));
								} catch (NumberFormatException ignored) {
									try {
										log.setVisitDate(Travelogue.DATE_FORMAT.parse(visitdate));
									} catch (ParseException e) {
										log.setVisitDate(null);
										e.printStackTrace();
									}
								}
							}
							
							if (session.getParms().containsKey("shared")) {
								log.setShared(Boolean.parseBoolean(session.getParms().get("shared")));
								changed = true;
							}
							
							if (session.getParms().containsKey("locationid")) {
								log.setLocationid(Integer.parseInt(session.getParms().get("locationid")));
								changed = true;
							}
							
							if (session.getParms().containsKey("views")) {
								log.setViews(Integer.parseInt(session.getParms().get("views")));
								changed = true;
							}
							
							if (changed) {
								log.update(Main.instance.dbConnection);
								
								System.out.println("Success updating log " + log);
							} else {
								return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "no change");
							}
						} else {
							return NanoHTTPD.newFixedLengthResponse(Status.FORBIDDEN, "text/plain", "not the owner of this log");
						}
					} else {
						return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "invalid log id");
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
					return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "sql error");
				}
			} else {
				return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "no id provided");
			}
			
			return NanoHTTPD.newFixedLengthResponse("success");
		} else if (session.getMethod() == Method.DELETE) {
			// DELETE log
			// ex: DELETE /crud/travelogue/123
			
			String idString = urlParams.get("id");
			int id = Integer.parseInt(idString);
			
			try {
				Travelogue log = new Travelogue(id).read(Main.instance.dbConnection);
				if (log.getOwnerid() == login.getUserID()) {
					log.delete(Main.instance.dbConnection);
					return NanoHTTPD.newFixedLengthResponse("success");
				} else {
					return NanoHTTPD.newFixedLengthResponse(Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "Not the owner of this log.");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal server error, failed to delete.");
			}
		} else {
			return NanoHTTPD.newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT, "Method not supported.");
		}
	}
}
