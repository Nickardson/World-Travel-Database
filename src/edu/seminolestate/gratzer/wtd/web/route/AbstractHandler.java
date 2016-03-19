package edu.seminolestate.gratzer.wtd.web.route;

import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;

/**
 * A nanolet handler which can be used for method-based overrides like (get(), post())
 * @author Taylor
 * @date 2016-02-14
 */
public class AbstractHandler extends DefaultHandler {

	@Override
	public String getText() {
		throw new IllegalStateException("Should not be called");
	}

	@Override
	public IStatus getStatus() {
		throw new IllegalStateException("Should not be called");
	}

	@Override
	public String getMimeType() {
		throw new IllegalStateException("Should not be called");
	}

}
