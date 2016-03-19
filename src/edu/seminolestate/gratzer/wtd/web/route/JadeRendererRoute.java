package edu.seminolestate.gratzer.wtd.web.route;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.exceptions.JadeException;
import de.neuland.jade4j.template.TemplateLoader;
import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.web.JadeHelper;
import edu.seminolestate.gratzer.wtd.web.PageProvider;
import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * Can serve up .jade files, along with other internal web resources
 * @author Taylor
 * @date 2016-02-14
 */
public class JadeRendererRoute extends AbstractHandler {
	private static JadeConfiguration config;
	
	static {
		// Create Jade Configuration, disable caching in development environment
		config = new JadeConfiguration();
		config.setCaching(!Main.isEclipse);
		
		Map<String, Object> def = new HashMap<String, Object>();
		def.put("_", new JadeHelper());
		
		config.setSharedVariables(def);
		
		if (Main.isEclipse) {
			// eclipse, use the relative source path
			config.setTemplateLoader(new TemplateLoader() {
				@Override
				public Reader getReader(String name) throws IOException {
					return new FileReader(new File("src" + name));
				}
				
				@Override
				public long getLastModified(String name) throws IOException {
					return new File("src" + name).lastModified();
				}
			});
		} else {
			// not eclipse, use internal .jar resources
			config.setTemplateLoader(new TemplateLoader() {
				@Override
				public Reader getReader(String name) throws IOException {
					return new BufferedReader(new InputStreamReader(Main.class.getResource(name).openStream()));
				}
				
				@Override
				public long getLastModified(String name) throws IOException {
					return 0;
				}
			});
		}
	}
	
	private static String[] getPathArray(String uri) {
        String array[] = uri.split("/");
        ArrayList<String> pathArray = new ArrayList<String>();

        for (String s : array) {
            if (s.length() > 0)
                pathArray.add(s);
        }

        return pathArray.toArray(new String[]{});

    }
	
	private boolean hasResource(String path) {
		return Server.class.getResource(path) != null;
	}
	
	private Response serve(String path) {
		return serve(path, null, null, null);
	}
	
	private Response serve(String path, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		InputStream resource = null;
        try {
        	if (hasResource(path)) {
        		if (Main.isEclipse) {
        			resource = new FileInputStream(new File("src", path));
        		} else {
        			resource = Server.class.getResourceAsStream(path.toString());
        		}
        		
            	String mime = NanoHTTPD.getMimeTypeForFile(path);
        		
        		if (mime.equalsIgnoreCase("text/jade")) {
        			resource.close();
            		try {
        				Map<String, Object> model = new HashMap<String, Object>();
        				
        				PageProvider.populate(model, path, uriResource, urlParams, session);
        				
        				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html", config.renderTemplate(config.getTemplate(path), model));
        			} catch (JadeException | IOException e) {
        				// e.printStackTrace();
        				return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML, "Internal jade server error: " + e.getMessage());
        			}
        		}

        		return NanoHTTPD.newChunkedResponse(Status.OK, mime, resource);
        	} else {
        		return new Error404UriHandler().get(null, null, null);
        	}
        } catch (IOException ioe) {
        	ioe.printStackTrace();
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.REQUEST_TIMEOUT, "text/plain", null);
        }
	}
	
	private static String[] indexNames = new String[] {
		"index.html",
		"index.htm",
		"index.jade"
	};
	
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		// get the url being requested
		String baseUri = uriResource.getUri();
        String realUri = RouterNanoHTTPD.normalizeUri(session.getUri());
        
        for (int index = 0; index < Math.min(baseUri.length(), realUri.length()); index++) {
            if (baseUri.charAt(index) != realUri.charAt(index)) {
                realUri = RouterNanoHTTPD.normalizeUri(realUri.substring(index));
                break;
            }
        }
        
        String[] spl = realUri.split("/");
        //realUri=spl[0];
        
        for (int i = 1; i < spl.length; i++) {
        	urlParams.put("arg" + i, spl[i]);        	
        }
        
        // build a internal resource path
        StringBuilder pathBuilder = new StringBuilder("/resources/html");
        for (String pathPart : getPathArray(realUri)) {
        	pathBuilder.append('/');
        	pathBuilder.append(pathPart);
        }
        
        String path = pathBuilder.toString();
        
        
        
        if (!path.contains(".")) {
        	// for directories, try to serve an index file
        	for (String indexName : indexNames) {
        		if (hasResource(path + "/" + indexName)) {
            		return serve(path + "/" + indexName, uriResource, urlParams, session);
            	}
        	}
        	
        	// "localhost:8080/test" will check "/test.jade"
        	if (hasResource(path + ".jade")) {
        		return serve(path + ".jade", uriResource, urlParams, session);
        	}
        	
        	// indexing resources is non-trivial, for now serve up a file not found
        	return NanoHTTPD.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "File not found.");
        } else {
        	return serve(path);
        }
	}
}