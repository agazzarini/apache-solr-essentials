package org.gazzax.labs.solr.ase.ch6.shards;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 * A sample client that performs basic operations on a cluster composed by 2 shards.
 * Note that although this class is in test folder, it is not a test case but a simple main. 
 * 
 * So in order to execute it, just start the Solr cluster (i.e. the 2 shards) and run it as a 
 * normal Java application.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class Example {
	/**
	 * Entry point for this example.
	 * 
	 * @param args the command line arguments.
	 * @throws SolrServerException in case of Solr failure.
	 * @throws IOException in case of communication failure with Solr.
	 */
	public static void main(String[] args) throws SolrServerException, IOException {
		// 1. This is the first shard
		final SolrServer shard1 = new HttpSolrServer("http://127.0.0.1:8983/solr/shard1");
		
		// 2. This is the second shard
		final SolrServer shard2 = new HttpSolrServer("http://127.0.0.1:8984/solr/shard2");

		// 3. Index some data on the first shard (remember that distribuiting data is up to the client)
		final SolrInputDocument jazzBass = new SolrInputDocument();
		jazzBass.setField("id", "1a");
		jazzBass.setField("brand", "Fender");
		jazzBass.setField("model", "Standard Jazz");
		jazzBass.addField("artist", "Jaco Pastorius");
		jazzBass.addField("artist", "Marcus Miller");
		jazzBass.setField("series", "Jazz Bass");		
		shard1.add(jazzBass);
		 
		// 3. Index some data on the second shard 
		final SolrInputDocument precisionBass = new SolrInputDocument();
		precisionBass.setField("id", "2a");
		precisionBass.setField("brand", "Fender");
		precisionBass.setField("model", "Standard Precision");
		precisionBass.addField("artist", "Steva Harris");
		precisionBass.addField("artist", "Francis Rocco Prestia");
		precisionBass.setField("series", "Precision Bass");		
		shard2.add(precisionBass);
		
		// 4. commit changes
		shard1.commit();
		shard2.commit();
		
		// 5. Query
		final SolrQuery query = new SolrQuery("*:*");
		query.setParam("shards", "127.0.0.1:8983/solr/shard1,127.0.0.1:8984/solr/shard2");
		
		// Include also the [shard] transformer so we can know the owning shard of each document.
		query.setFields("*", "shard:[shard]");
		
		// Query can be sent to any shard
		final QueryResponse response = shard1.query(query);
		
		assertEquals(2, response.getResults().getNumFound());
		
		// 6. Prints out results
		for (final SolrDocument bass : response.getResults()) {
			System.out.println("Shard: " + bass.getFieldValue("shard"));
			System.out.println(bass.getFieldValue("brand") + ", " + bass.getFieldValue("model") + ", " + bass.getFieldValue("series"));
			System.out.println();
		}
		
		// 7. Shutdown
		shard1.shutdown();
		shard2.shutdown();
	}
}
