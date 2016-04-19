package edu.seminolestate.gratzer.wtd.web.provider;

import java.util.Map;
import java.util.regex.Pattern;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.User;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
import edu.seminolestate.gratzer.wtd.web.PageProvider.RegexPopulator;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

public class AllPopulator implements RegexPopulator {
	private final Pattern PATTERN = Pattern.compile("\\/resources\\/html\\/[\\w]+.jade");
	@Override
	public Pattern getPattern() {
		return PATTERN;
	}
	
	@Override
	public void populate(Map<String, Object> model, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		// add error parms
		if (session.getParms().get("error") != null) {
			model.put("error", session.getParms().get("error"));
		}
		
		try {
			String sessionID = session.getCookies().read("sessionid");
			if (sessionID != null) {
				LoginSession login = Main.instance.server.getLoginSession(Integer.parseInt(sessionID));
				
				if (login != null) {
					model.put("userid", login.getUserID());
					model.put("username", new User(login.getUserID()).read(Main.instance.dbConnection).getUsername());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
