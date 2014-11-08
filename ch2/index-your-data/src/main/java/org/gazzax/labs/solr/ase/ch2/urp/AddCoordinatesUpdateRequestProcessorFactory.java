package org.gazzax.labs.solr.ase.ch2.urp;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

/**
 * An {@link UpdateRequestProcessor} that uses the value of an address field (in the incoming documents) for 
 * obtaining the corresponding longitude and latitude.
 * 
 * The processor uses Google Maps API to retrieve the coordinates. Since Google stops rapid and subsequent requests coming from 
 * the same IP this processor accepts a "sleep-time" parameter which represents a "sleep" time in msecs between one requests
 * 
 * @author Andrea Gazzarini
 */
public class AddCoordinatesUpdateRequestProcessorFactory extends UpdateRequestProcessorFactory {
	static final String SLEEP_TIME_PARAM_NAME = "sleep-time";
	static final int DEFAULT_SLEEP_TIME = 1000;

	int sleepTimeInMsecs = DEFAULT_SLEEP_TIME;
	
	GoogleGeoLocationService service;
	
	@Override
	public UpdateRequestProcessor getInstance(
			final SolrQueryRequest request, 
			final SolrQueryResponse response, 
			final UpdateRequestProcessor next) {
		return new AddCoordinatesUpdateRequestProcessor(service, sleepTimeInMsecs, next);
	}
	
	@SuppressWarnings({ "rawtypes"})
	@Override
	public void init(final NamedList args) {
		sleepTimeInMsecs = SolrParams.toSolrParams(args).getInt(SLEEP_TIME_PARAM_NAME, DEFAULT_SLEEP_TIME);
		service = new GoogleGeoLocationService();
		service.init();
	}
}