package edu.seminolestate.gratzer.wtd;

import mjson.Json;
import mjson.Json.DefaultFactory;
import mjson.Json.Factory;

public class CustomJsonFactory extends DefaultFactory implements Factory {
	@Override
	public Json make(Object anything) {
		if (anything instanceof IJsonable) {
			return ((IJsonable) anything).toJSON();
		} else {
			return super.make(anything);			
		}
	}
}
