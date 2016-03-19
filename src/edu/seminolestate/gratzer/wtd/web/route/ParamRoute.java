package edu.seminolestate.gratzer.wtd.web.route;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

public class ParamRoute extends AbstractHandler {
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		Map<String, String> files = Server.processPost(session);
		
		for (Entry<String, String> file : files.entrySet()) {
			System.out.println("Parm name: " + session.getParms().get(file.getKey()));
			System.out.println("Reading file: '" + file.getKey() + "' @ " + file.getValue());
			try {
				Scanner s = new Scanner(new File(file.getValue()));
				while (s.hasNextLine()) {
					System.out.println(s.nextLine());
				}
				s.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		StringBuilder b = new StringBuilder();
		
		b.append("Params:");
		for (Entry<String, String> x : session.getParms().entrySet()) {
			b.append(x.getKey());
			b.append(": ");
			b.append(x.getValue());
			b.append("\n");
		}
		b.append("\n");
		
		return NanoHTTPD.newFixedLengthResponse(b.toString());
	}
}
