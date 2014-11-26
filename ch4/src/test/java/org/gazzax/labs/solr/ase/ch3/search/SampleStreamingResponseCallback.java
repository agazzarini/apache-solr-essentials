package org.gazzax.labs.solr.ase.ch3.search;

import static org.junit.Assert.assertEquals;

import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.common.SolrDocument;

/**
 * A sample {@link StreamingResponseCallback} implementation.
 * Simply it makes some assertions about the incoming data.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
class SampleStreamingResponseCallback extends StreamingResponseCallback {

	private int howManyDocumentsInTheCurrentWindow = 0;
	private final int expectedNumFound;
	
	/**
	 * Builds a new {@link StreamingResponseCallback} with the expected number of results.
	 * 
	 * @param expectedNumFound the expected number of results.
	 */
	SampleStreamingResponseCallback(final int expectedNumFound) {
		this.expectedNumFound = expectedNumFound;
	}
	/**
	 * This method is called for each document in query response.
	 * 
	 * @param document a document that belongs to the current window / page of results.
	 */
	@Override
	public void streamSolrDocument(final SolrDocument document) {
		// Do something useful with the document. Here we are simply
		// counting the total number of returned documents.
		howManyDocumentsInTheCurrentWindow++;
	}

	/**
	 * Notifies the callback handler about query execution metadata.
	 * Note that this method is called once per document window (i.e. query execution).
	 * 
	 * @param numFound how many matching documents (in total).
	 * @param start the start offset of the first returned document.
	 * @param maxScore the max computed score among all matching documents.
	 */
	@Override
	public void streamDocListInfo(final long numFound, final long start, final Float maxScore) {
		// Make some assertion about metadata
		assertEquals(expectedNumFound, numFound);
		assertEquals(0, start);
	}
	
	/**
	 * Returns the number of documents included in the current window / page.
	 * 
	 * @return the number of documents included in the current window / page.
	 */
	int getCurrentWindowSize() {
		return howManyDocumentsInTheCurrentWindow;
	}
}