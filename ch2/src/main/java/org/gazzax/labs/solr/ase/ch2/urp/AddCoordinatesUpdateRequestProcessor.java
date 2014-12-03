package org.gazzax.labs.solr.ase.ch2.urp;

import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link UpdateRequestProcessor} that uses the value of an address field (in the incoming documents) for 
 * obtaining the corresponding longitude and latitude.
 * 
 * The processor uses Google Maps API to retrieve the coordinates. Since Google stops rapid and subsequent requests coming from 
 * the same IP this processor accepts a "sleep-time" parameter which represents a "sleep" time in msecs between one requests
 * 
 * @author Andrea Gazzarini
 */
public class AddCoordinatesUpdateRequestProcessor extends UpdateRequestProcessor {
	private final static Logger LOGGER = LoggerFactory.getLogger(AddCoordinatesUpdateRequestProcessor.class);
	private final int sleepTimeInMsecs;
	private final GoogleGeoLocationService service;
	
	/**
	 * Builds a new {@link UpdateRequestProcessor} with the given data.
	 * 
	 * @param service the geolocation service provider.
	 * @param sleepTimeInMsecs the pause time between each geolocation request.
	 */
	public AddCoordinatesUpdateRequestProcessor(
			final GoogleGeoLocationService service,
			final int sleepTimeInMsecs,
			final UpdateRequestProcessor next) {
		super(next);
		this.sleepTimeInMsecs = sleepTimeInMsecs;
		this.service = service;
	}

	@Override
	public void processAdd(final AddUpdateCommand command) throws IOException  {
		final SolrInputDocument document = command.getSolrInputDocument();
		final String address = (String) document.getFieldValue("address");
		if (address != null && address.trim().length() !=  0) {
			try {
				final String id = String.valueOf(document.getFieldValue("id"));
				final String coordinates = service.getCoordinates(id, address);
				if (coordinates != null && coordinates.trim().length() != 0) {
					document.addField("coordinates", coordinates);
				} else {
					LOGGER.error("Document " + id + "  with address \"" + address+" \" hasn't been translated (null address)");
				}
			
				sleep();
			} catch (final Exception exception) {
				LOGGER.error("Unable to get coordinates for "+ document, exception);
			}
			super.processAdd(command);			
		}
	}
	
	/**
	 * Sleeps a little bit between requests...
	 * This is done because Google will stop subsequent rapid requests.
	 * Can be disabled by setting a value of 0 for this parameter.
	 */
	private void sleep() {
		try {
			Thread.sleep(sleepTimeInMsecs);
		} catch (final InterruptedException exception) {
			// Nothing to be done here
		}
	}
}