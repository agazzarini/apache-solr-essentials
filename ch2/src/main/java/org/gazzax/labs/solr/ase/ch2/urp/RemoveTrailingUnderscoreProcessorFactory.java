package org.gazzax.labs.solr.ase.ch2.urp;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

/**
 * This is the factory in charge to create concrete instances of a given update request processor.
 * In solr schema you're always declaring factories, not concrete types.
 * 
 * @author agazzarini
 * @since 1.0
 */
public class RemoveTrailingUnderscoreProcessorFactory extends UpdateRequestProcessorFactory {

	private String [] fields;
	
	@Override
	public void init(@SuppressWarnings("rawtypes") final NamedList args) {
		// 1. Solr utility to transform a generic named list in a set of SolrParams
		final SolrParams parameters = SolrParams.toSolrParams(args);
		
		// 2. Retrieve the names of the fields that will be checked by the processor
		fields = parameters.getParams("fields");
	}
	
	/**
	 * A {@link UpdateRequestProcessorFactory} must implement this method in order to 
	 * create a concrete processor instance.
	 * 
	 * According with the decorator pattern, each processor will get a reference to the next processor in the chain.
	 * Once its work has been done, your processor has to forward the control to the next processor, 
	 * otherwise the index chain will be interrupted and most probably no data will be indexed.
	 * 
	 * TDB: factory lifecycle within Solr.
	 * 
	 * @see http://en.wikipedia.org/wiki/Decorator_pattern
	 */
	@Override
	public UpdateRequestProcessor getInstance(
			final SolrQueryRequest request,
			final SolrQueryResponse response, 
			final UpdateRequestProcessor next) {
		return new RemoveTrailingUnderscoreProcessor(next, fields);
	}

}
