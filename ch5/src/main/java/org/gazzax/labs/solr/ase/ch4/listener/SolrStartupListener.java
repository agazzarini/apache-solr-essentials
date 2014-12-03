package org.gazzax.labs.solr.ase.ch4.listener;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.params.EventParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.AbstractSolrEventListener;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrEventListener;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrRequestInfo;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrIndexSearcher;

/**
 * A {@link SolrEventListener} that preload sample data when Solr starts up.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class SolrStartupListener implements SolrEventListener {
	
	private String datafile;
	
	@Override
	public void init(final NamedList args) {
		this.datafile = (String) args.get("datafile");
	}

	@Override
	public void postCommit() {
		// Nothing to be done here
	}

	@Override
	public void postSoftCommit() {
		// Nothing to be done here
	}

	@Override
	public void newSearcher(
			final SolrIndexSearcher newSearcher, 
			final SolrIndexSearcher currentSearcher) {
		
		// 1. Check: we are only interested in startup events so there, current searcher must be null
		if (currentSearcher != null) {
			return;
		}
		
		LocalSolrQueryRequest request = null;
		
		try {
			
			// 2. Create the arguments map for the update request
			final NamedList<String> args = new SimpleOrderedMap<String>();
			args.add(UpdateParams.ASSUME_CONTENT_TYPE, "text/xml");
			addEventParms(currentSearcher, args);

			// 3. Create a new Solr (update) request
			request = new LocalSolrQueryRequest(newSearcher.getCore(), args);
			
			// 4. Fill the request with the (datafile) input stream
			final List<ContentStream> streams = new ArrayList<ContentStream>();
			streams.add(new ContentStreamBase() {
				@Override
				public InputStream getStream() throws IOException {
					return new FileInputStream(datafile);
				}
			});
			
			request.setContentStreams(streams);
			
			// 5. Creates a new Solr response
			final SolrQueryResponse response = new SolrQueryResponse();
			
			// 6. And finally call invoke the update handler
			SolrRequestInfo.setRequestInfo(new SolrRequestInfo(request, response));
			newSearcher.getCore().getRequestHandler("/update").handleRequest(request, response);		
		} finally {
			request.close();
		}
	}
	
	/**
	* Add the {@link org.apache.solr.common.params.EventParams#EVENT} with either the {@link org.apache.solr.common.params.EventParams#NEW_SEARCHER}
	* or {@link org.apache.solr.common.params.EventParams#FIRST_SEARCHER} values depending on the value of currentSearcher.
	* <p/>
	*
	* Note: this method has been copied from {@link AbstractSolrEventListener}. 
	* It wasn't possible to directly subclass that supertype because it requires a constructor that accepts a {@link SolrCore}, 
	* which we don't have in this event listener (at construction time).
	*
	* @param currentSearcher If null, add FIRST_SEARCHER, otherwise NEW_SEARCHER
	* @param args The named list to add the EVENT value to
	*/
	@SuppressWarnings("unchecked")
	void addEventParms(final SolrIndexSearcher currentSearcher, final NamedList args) {
		if (currentSearcher != null) {
			args.add(EventParams.EVENT, EventParams.NEW_SEARCHER);
		} else {
			args.add(EventParams.EVENT, EventParams.FIRST_SEARCHER);
		}
	}
}