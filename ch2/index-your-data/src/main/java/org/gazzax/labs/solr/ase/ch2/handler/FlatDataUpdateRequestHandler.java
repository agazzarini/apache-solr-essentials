package org.gazzax.labs.solr.ase.ch2.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.UpdateRequestHandler;
import org.apache.solr.handler.loader.ContentStreamLoader;

public class FlatDataUpdateRequestHandler extends UpdateRequestHandler {
	@SuppressWarnings("rawtypes")
	protected Map<String,ContentStreamLoader> createDefaultLoaders(NamedList args) {
		Map<String,ContentStreamLoader> registry = new HashMap<String,ContentStreamLoader>();
	    registry.put("text/plain", new FlatDataLoader());
	    return registry;
	}
}
