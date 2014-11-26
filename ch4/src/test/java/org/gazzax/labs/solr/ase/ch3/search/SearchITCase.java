package org.gazzax.labs.solr.ase.ch3.search;

import static org.gazzax.labs.solr.ase.ch3.TestUtils.SOLR_URI;
import static org.gazzax.labs.solr.ase.ch3.TestUtils.sampleData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This integration test demonstrates the usage of Solrj for issuing queries.
 * The example will use a {@link ConcurrentUpdateSolrServer} for indexing some sample data and 
 * an {@link HttpSolrServer} as proxy for issuing queries.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class SearchITCase {
	private static SolrServer INDEXER;
	private static SolrServer SEARCHER;
	
	/**
	 * Indexes a set of sample documents.
	 * 
	 * @throws SolrServerException in case of Solr internal failure,
	 * @throws IOException in case of I/O failure.
	 */
	@BeforeClass
	public static void indexData() throws SolrServerException, IOException {
		// 1. Create a proxy indexer 
		INDEXER = new ConcurrentUpdateSolrServer(SOLR_URI, 2, 1);

		// 2. Index sample data
		INDEXER.add(sampleData());
		
		// 3. Commit changes
		INDEXER.commit();
		
		// 4. Create a proxy searcher
		SEARCHER = new HttpSolrServer(SOLR_URI);
	}
	
	/**
	 * Removes all documents from Solr.
	 * 
	 * @throws Exception hopefully never, otherwise a second run of this test will fail.
	 */
	@AfterClass
	public static void cleanUp() throws Exception {
		INDEXER.deleteByQuery("*:*");
		INDEXER.commit();
		INDEXER.shutdown();
		
		SEARCHER.shutdown();
	}
	
	/**
	 * Selects all documents using an handler configured with SolrQueryParser
	 * 
	 * @throws Exception hopefully never, otherwise the test fails.
	 */
	@Test
	public void selectAll() throws Exception {
		// 1. Prepare the Query object
		// The query string can be directly injected in the constructor
		final SolrQuery query = new SolrQuery("*:*");
		query.setRequestHandler("/h1");
		
		// These settings will override the "defaults" section
		query.setFacet(false);
		query.setHighlight(false);
		query.setSort("released", ORDER.desc);
		
		// We are asking 5 documents per page
		query.setRows(5);
		
		// 2. Send the query request and get the corresponding response.
		final QueryResponse response = SEARCHER.query(query);
		
		// 3. Get the result object, containing documents and metadata.
		final SolrDocumentList documents = response.getResults();
		
		// If not explicitly requested (or set in the handler) the start is set to 0
		assertEquals(0, documents.getStart());
		
		// Total number of documents found must be equals to all documents we previously indexed
		assertEquals(sampleData().size(), documents.getNumFound());
		
		// Page size must be 5, as requested
		assertEquals(5, documents.size());
	}
	
	/**
	 * Demonstrates how to ask for faceting and iterate over response facets.
	 * 
	 * @throws Exception hopefully never, otherwise the test fails.
	 */
	@Test
	public void facets() throws Exception {
		// 1. Prepare the Query object
		// The query string can be directly injected in the constructor
		final SolrQuery query = new SolrQuery("*:*");
		query.setRequestHandler("/h1");
		
		// These settings will override the "defaults" section
		// Note that this handler sets facet to true, so the following line is
		// not actually needed
		query.setFacet(true);
		query.addFacetField("genre", "released");
		
		// We don't want highlighting here
		// Since the HL component is disabled by default, also this line is not needed.
		query.setHighlight(false);
		
		// We are only interested in facets, so skip don't include any 
		// document in the response.
		query.setRows(0);
		
		// 2. Send the query request and get the corresponding response.
		final QueryResponse response = SEARCHER.query(query);
		
		// 3. Get the result object, containing documents and metadata.
		final SolrDocumentList documents = response.getResults();
		
		// If not explicitly requested (or set in the handler) the start is set to 0
		assertEquals(0, documents.getStart());
		
		// Total number of documents found must be equals to all documents we previously indexed
		assertEquals(sampleData().size(), documents.getNumFound());
		
		// Page size must be 0, as requested
		assertEquals(0, documents.size());
		
		final FacetField genre = response.getFacetField("genre");
		assertNotNull(genre);
		 
		// This is something that should never appear within a TestCase :) 
		// however is useful to demonstrate how to iterate over facet values
		for (final Count count : genre.getValues()) {
			// e.g. Jazz : 19
			// e.g. Fusion: 11
			System.out.println(count.getName() + " : " + count.getCount());
		}
	}
	
	/**
	 * Illustrates how to execute a query with a response callback.
	 * 
	 * @throws Exception hopefully never, otherwise the test fails.
	 */
	@Test
	public void withCallback() throws Exception {
		final int expectedWindowSize = 2;
		
		// 1. Prepare the Query object
		// The query string can be directly injected in the constructor. 
		// Note that the /h2 request handler uses a dismax query parser and 
		// therefore the query string is composed by just search term (no fields).
		final SolrQuery query = new SolrQuery("rock jazz");
		query.setRequestHandler("/h2");
		query.setFacet(false);
		query.setRows(expectedWindowSize);
		
		// 2. creates a callback handler
		final SampleStreamingResponseCallback callbackHandler = new SampleStreamingResponseCallback(4);
		
		// 3. Note that in this case we are not invoking "query" but "queryAndStreamResponse"
		// That requires the callback handler previously defined. 
		// That method still returns a QueryResponse but, as its javadoc says, 
		// it is not supposed to be used because streamed documents are removed from there.
		@SuppressWarnings("unused")
		final QueryResponse dontUseMe = SEARCHER.queryAndStreamResponse(query, callbackHandler);
		
		// 4. Assert window result
		assertEquals(expectedWindowSize, callbackHandler.getCurrentWindowSize());
	}	
}