package org.gazzax.labs.solr.ase.ch3.index;

import static org.gazzax.labs.solr.ase.ch3.TestUtils.SOLR_URI;
import static org.gazzax.labs.solr.ase.ch3.TestUtils.sampleData;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
 
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.Test;

/**
 * This integration test demonstrates the usage of several implementations of {@link SolrServer}.
 * Each test method uses a different {@link SolrServer} implementation so you can see the 
 * configuration parameters required by each proxy.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class SolrServersITCase {
	SolrServer solr;
	
	/**
	 * Uses the {@link EmbeddedSolrServer} to index some data to Solr.
	 * 
	 * @throws Exception in case of I/O or index failure.
	 */
	@Test
	public void embeddedSolrServer() throws Exception {
		// 1. Create a new (local) container using "solr.solr.home" and "solr.data.dir" system property.
		// Note that we need to define a dedicated solr.data.dir for this test method because
		// otherwise we would end in a lock conflict (the embedded Solr instance is running) 
		System.setProperty("solr.solr.home", "src/solr/solr-home");
		System.setProperty("solr.data.dir", new File("target/solr-embedded-solr").getAbsolutePath());
		CoreContainer container = new CoreContainer();
		container.load();
		
		System.out.println(container.getAllCoreNames());
		
		// 2. Create a new instance of (Embedded)SolrServer 
		solr = new EmbeddedSolrServer(container, "example");
		
		// 3. Create some data
		final List<SolrInputDocument> albums = sampleData();
		
		// 4. Add those data
		solr.add(albums);
		
		// 5. Commit
		solr.commit();

		// 6. Verify
		verify();
	}
	
	/**
	 * Uses the {@link HttpSolrServer} to index some data to Solr.
	 * 
	 * @throws Exception in case of I/O or index failure.
	 */
	@Test
	public void httpSolrServer() throws Exception {
		// 1. Create a new instance of HttpSolrServer 
		solr = new HttpSolrServer(SOLR_URI);
		
		// 2. Create some data
		final List<SolrInputDocument> albums = sampleData();
		
		// 3. Add those data
		solr.add(albums);
		
		// 4. Commit
		solr.commit();
		
		// 5. Verify
		verify();
	}	
	
	/**
	 * Uses the {@link HttpSolrServer} to index some data to Solr.
	 * 
	 * @throws Exception in case of I/O or index failure.
	 */
	@Test
	public void loadBalancedHttpSolrServer() throws Exception {
		final String firstSolrUrl = SOLR_URI;
		final String secondSolrUrl = SOLR_URI;
		final String thirdSolrUrl = SOLR_URI;
		
		// 1. Create a new instance of HttpSolrServer 
		// Note that we are simply repeating the same server three times, in order to "simulate" a
		// scenario with three searchers.
		// In a real context we would have three different urls.
		solr = new LBHttpSolrServer(
				firstSolrUrl,
				secondSolrUrl,
				thirdSolrUrl);
		
		// 2. Create some data
		final List<SolrInputDocument> albums = sampleData();
		
		// 3. Add those data
		solr.add(albums);
		
		// 4. Commit
		solr.commit();
		
		// 5. Verify
		verify();
	}		
	
	/**
	 * Uses the {@link HttpSolrServer} to index some data to Solr.
	 * 
	 * @throws Exception in case of I/O or index failure.
	 */
	@Test
	public void concurrentUpdateSolrServer() throws Exception {
		final int bufferSize = 2; // commit each 2 albums
		final int threadsNo = 2; // Use two indexer threads
		
		// 1. Create a new instance of HttpSolrServer 
		// Note that we are simply repeating the same server three times, in order to "simulate" a
		// scenario with three searchers.
		// In a real context we would have three different urls.
		solr = new ConcurrentUpdateSolrServer(SOLR_URI, bufferSize, threadsNo);		
		
		// 2. Create some data
		final List<SolrInputDocument> albums = sampleData();
		
		// 3. Add those data
		solr.add(albums);
		
		// 4. Commit
		solr.commit();
		
		// 5. Verify
		verify();
	}		

	/**
	 * Cleanup the Solr index.
	 * 
	 * @throws Exception hopefully never, otherwise the corresponding test will fail.
	 */
	@After
	public void tearDown() throws Exception {
		solr.deleteByQuery("*:*");
		solr.commit();
	}
	
	/**
	 * Verifies that records have been effectively indexed.
	 * 
	 * @throws SolrServerException in case of Solr communication failure.
	 */
	void verify() throws SolrServerException {
		final SolrQuery query = new SolrQuery("*:*");
		query.setRequestHandler("/h1");
		
		final QueryResponse response = solr.query(query);
		assertEquals(sampleData().size(), (int)response.getResults().getNumFound());
	}
}
