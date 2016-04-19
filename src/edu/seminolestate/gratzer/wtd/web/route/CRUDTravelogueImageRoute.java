package edu.seminolestate.gratzer.wtd.web.route;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import edu.seminolestate.gratzer.wtd.Main;
import edu.seminolestate.gratzer.wtd.beans.TravelogueImage;
import edu.seminolestate.gratzer.wtd.database.PasswordHasher;
import edu.seminolestate.gratzer.wtd.web.LoginSession;
import edu.seminolestate.gratzer.wtd.web.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * A route which CRUDs a TravelogueImage
 * @author Taylor
 * @date 2016-02-13
 */
public class CRUDTravelogueImageRoute extends AbstractHandler {
	private static final String[] IMAGE_EXTENSIONS = new String[] {
		".png",
		".jpg",
		".jpeg",
		".gif",
		".webp",
		".webm"
	};
	
	public static final File IMG_DIR = new File("img");
	
	static {
		IMG_DIR.mkdir();
	}
	
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		LoginSession login = Server.getLoginSession(session);
		if (login == null) {
			return Server.newRedirectResponse("/login/?error=mustlogin");
		}
		
		Map<String, String> files = Server.processPost(session);
		
//		for (Entry<String, String> p : session.getParms().entrySet()) {
//			System.out.println("Parm: " + p.getKey() + ": " + p.getValue());
//		}
//		for (Entry<String, String> p : files.entrySet()) {
//			System.out.println("File: " + p.getKey() + ": " + p.getValue());
//		}
		
		if (session.getMethod() == Method.POST) {
			System.out.println("create image?");
			// CREATE image
			// "file": (file) the image file
			// "logid": (int) the id of the log to attach the file to
			
			// get logid
			String logidString = session.getParms().get("logid");
			int logid = Integer.parseInt(logidString);
			
			// TODO: allow admins to add always
			// TODO: adjust strictness
			// confirm that requesting user is allowed to add an image to this log.
//			try {
//				Travelogue log = new Travelogue(logid).read(Main.dbConnection);
//				
//				if (log.getOwnerid() != login.getUserID()) {
//					return NanoHTTPD.newFixedLengthResponse(Status.UNAUTHORIZED, NanoHTTPD.MIME_PLAINTEXT, "You do not own this log!");
//				}
//			} catch (SQLException e1) {
//				e1.printStackTrace();
//				return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal server error, could not determine ownership.");
//			}
			
			for (Entry<String, String> file : files.entrySet()) {
				if (file.getKey().startsWith("file")) {
					String filename = session.getParms().get(file.getKey());
					System.out.println("filename: " + filename);
					
					boolean isImage = false;
					String extension = null;
					for (String ext : IMAGE_EXTENSIONS) {
						if (filename.toLowerCase().endsWith(ext)) {
							isImage = true;
							extension = ext;
							break;
						}
					}
					
					if (!isImage)
						return NanoHTTPD.newFixedLengthResponse(Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "File upload must be an image.");
					
					File tempFile = new File(file.getValue());
					if (!tempFile.exists())
						return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal server error, file not copyable");
					
					String hash = PasswordHasher.md5File(tempFile);
					String imgName = hash + extension;
					
					File dest = new File(IMG_DIR, imgName);

					// print out dest
					System.out.println("Image destination: " + dest);
					
					try {
						Files.copy(tempFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
						
						TravelogueImage img = new TravelogueImage(TravelogueImage.NO_ID, logid, imgName);
						img.create(Main.dbConnection);
					} catch (IOException | SQLException e) {
						e.printStackTrace();
						return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal server error: " + e.getMessage());
					}
				}
			}
			return NanoHTTPD.newFixedLengthResponse("");
		} else if (session.getMethod() == Method.DELETE) {
			// DELETE image
			// "id": (int) the id of the image to delete
			
			// TODO: test the confirmation that user owns image
			String idString = urlParams.get("id");
			int id = Integer.parseInt(idString);
			
			try {
				TravelogueImage image = new TravelogueImage(id);
//				Travelogue parentLog = new Travelogue(image.getLogid());
				// TODO: adjust strictness
//				if (parentLog.getOwnerid() != login.getUserID()) {
//					return NanoHTTPD.newFixedLengthResponse(Status.UNAUTHORIZED, NanoHTTPD.MIME_PLAINTEXT, "You do not own this log!");
//				}
				
				image.delete(Main.dbConnection);
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
