package org.gazzax.labs.solr.ase.ch3.index;

import static org.gazzax.labs.solr.ase.ch3.TestUtils.randomStringIdentifier;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
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
 * Note that the {@link CloudSolrServer} test is marked as ignored because we 
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
		// 1. Create a new (local) container using "solr.solr.home" system property.
		CoreContainer container = new CoreContainer();
		
		// 2. Create a new instance of (Embedded)SolrServer 
		solr = new EmbeddedSolrServer(container, "example");
		
		// 3. Create some data
		final List<SolrInputDocument> albums = createSampleData();
		
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
		solr = new HttpSolrServer("http://127.0.0.1:8080/solr/example");
		
		// 2. Create some data
		final List<SolrInputDocument> albums = createSampleData();
		
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
		final String firstSolrUrl = "http://127.0.0.1:8080/solr/example";
		final String secondSolrUrl = "http://127.0.0.1:8080/solr/example";
		final String thirdSolrUrl = "http://127.0.0.1:8080/solr/example";
		
		// 1. Create a new instance of HttpSolrServer 
		// Note that we are simply repeating the same server three times, in order to "simulate" a
		// scenario with three searchers.
		// In a real context we would have three different urls.
		solr = new LBHttpSolrServer(
				firstSolrUrl,
				secondSolrUrl,
				thirdSolrUrl);
		
		// 2. Create some data
		final List<SolrInputDocument> albums = createSampleData();
		
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
		final String solrUrl = "http://127.0.0.1:8080/solr/example";
		final int bufferSize = 2; // commit each 2 albums
		final int threadsNo = 2; // Use two indexer threads
		
		// 1. Create a new instance of HttpSolrServer 
		// Note that we are simply repeating the same server three times, in order to "simulate" a
		// scenario with three searchers.
		// In a real context we would have three different urls.
		solr = new ConcurrentUpdateSolrServer(solrUrl, bufferSize, threadsNo);		
		
		// 2. Create some data
		final List<SolrInputDocument> albums = createSampleData();
		
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
	}
	
	/**
	 * Verifies that records have been effectively indexed.
	 * 
	 * @throws SolrServerException in case of Solr communication failure.
	 */
	void verify() throws SolrServerException {
		final SolrQuery query = new SolrQuery("*:*");
		query.setRequestHandler("/fielded");
		
		final QueryResponse response = solr.query(query);
		assertEquals(createSampleData().size(), (int)response.getResults().getNumFound());
	}
	
	/**
	 * Creates some sample data (albums) that will be indexed.
	 * 
	 * @return some sample data to index.
	 */
	List<SolrInputDocument> createSampleData() {
		// The outcoming of this method is a list of SolrInputDocuments representing data to be indexed.
		final List<SolrInputDocument> albums = new ArrayList<SolrInputDocument>();
		
		// SolrInputDocument is the data transfer object that represents a document / record on Solr.
		final SolrInputDocument weatherReport = new SolrInputDocument();
		
		// "set" set a given value. Any existing value (single or multiple) will be removed.
		weatherReport.setField("id", randomStringIdentifier());
		weatherReport.setField("title", "Heavy Weather");
		weatherReport.setField("artist", "Weather Report");

		// "genre" is a multivalued field so we must use "add" in order to set two values.
		weatherReport.addField("genre", "Jazz");
		weatherReport.addField("genre", "Fusion");
		
		weatherReport.setField("released", "1977");
		
		albums.add(weatherReport);
		
		final SolrInputDocument octavarium = new SolrInputDocument();
		octavarium.setField("id", randomStringIdentifier());
		octavarium.setField("title", "Octavarium");
		octavarium.setField("artist", "Dream Theater");

		octavarium.addField("genre", "Progressive Rock");
		octavarium.addField("genre", "Progressive Metal");
		
		octavarium.setField("released", "2005");
		octavarium.setField("old_price", 12.50);
		octavarium.setField("new_price", 11.00);
		
		albums.add(octavarium);
		
		final SolrInputDocument delicateSoundOfThunder = new SolrInputDocument();
		delicateSoundOfThunder.setField("id", randomStringIdentifier());
		delicateSoundOfThunder.setField("title", "Delicate Sound of Thunder");
		delicateSoundOfThunder.setField("artist", "Pink Floyd");
		delicateSoundOfThunder.addField("genre", "Progressive Rock");
		delicateSoundOfThunder.addField("genre", "Pop");
		delicateSoundOfThunder.addField("genre", "Rock");
		
		delicateSoundOfThunder.setField("released", "1988");
	
		albums.add(delicateSoundOfThunder);
		
		final SolrInputDocument ummagumma = new SolrInputDocument();
		ummagumma.setField("id", randomStringIdentifier());
		ummagumma.setField("title", "Ummagumma");
		ummagumma.setField("artist", "Pink Floyd");
		ummagumma.addField("genre", "Progressive Rock");
		ummagumma.addField("genre", "Rock");
		
		ummagumma.setField("released", "1969");
	
		albums.add(ummagumma);
		
		return albums;
	}
}
