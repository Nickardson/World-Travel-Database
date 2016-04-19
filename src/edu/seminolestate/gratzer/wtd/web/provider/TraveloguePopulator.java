package edu.seminolestate.gratzer.wtd.web.provider;

import java.sql.SQLException;
import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.Travelogue;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
import edu.seminolestate.gratzer.wtd.web.PageProvider.Populator;
import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

public class TraveloguePopulator implements Populator {
	@Override
	public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		try {
			// TODO: verify that log id exists
			int id = Integer.parseInt(session.getParms().get("id"));
			Travelogue log = new Travelogue(id).read(Main.instance.dbConnection);
			
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
}