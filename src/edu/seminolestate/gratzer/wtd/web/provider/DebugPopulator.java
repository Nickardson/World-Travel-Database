package edu.seminolestate.gratzer.wtd.web.provider;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Map;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.IBean;
import edu.seminolestate.gratzer.wtd.beans.Location;
import edu.seminolestate.gratzer.wtd.beans.Travelogue;
import edu.seminolestate.gratzer.wtd.beans.TravelogueImage;
import edu.seminolestate.gratzer.wtd.beans.User;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
import edu.seminolestate.gratzer.wtd.web.Server;
import edu.seminolestate.gratzer.wtd.web.PageProvider.Populator;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

public class DebugPopulator implements Populator {
	@Override
	public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		LoginSession login = Server.getLoginSession(session);
		
		if (login == null) {
			model.put("unauthorized", true);
			return;
		}
		
		try {
			User u = new User(login.getUserID()).read(Main.instance.dbConnection);
			if (u != null && u.isAdmin()) {
				model.put("users", IBean.executeQuery(User.class, Main.instance.dbConnection.prepareStatement("SELECT * FROM users")));
				model.put("travelogues", IBean.executeQuery(Travelogue.class, Main.instance.dbConnection.prepareStatement("SELECT * FROM travelogues")));
				model.put("images", IBean.executeQuery(TravelogueImage.class, Main.instance.dbConnection.prepareStatement("SELECT * FROM travel_images")));
				
				// @date 2016-03-20 Add locations to debug view 
				model.put("locations", IBean.executeQuery(Location.class, Main.instance.dbConnection.prepareStatement("SELECT * FROM locations")));
				
				model.put("properties", Main.instance.getProperties());
				
				try {
					model.put("ip", InetAddress.getLocalHost().toString());
				} catch (UnknownHostException ignored) {
				}
			} else {
				model.put("unauthorized", true);
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
}