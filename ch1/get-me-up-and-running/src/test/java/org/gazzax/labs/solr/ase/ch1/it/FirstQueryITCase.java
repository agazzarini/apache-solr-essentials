package org.gazzax.labs.solr.ase.ch1.it;

import static org.junit.Assert.assertEquals;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A simple test case that demonstrates the basic interaction between SOLR client and server.
 * A client creates an input document, sends to SOLR and then executes a query.
 * <br/><br/>
 * 
 * You can run it in three modes:
 * 
 * <ul>
 * 	<li>Main class: Run As - Java Application</li>
 * 	<li>Unit Test: Run As - JUnit</li>
 * 	<li>Integration tests: Run as - Maven install (on the project)</li>
 * </ul>
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class FirstQueryITCase {
	
	private SolrServer client;

	/**
	 * Sample and simple scenario where we index one document and make a query.
	 * 
	 * @throws Exception hopefully never otherwise the test fails.
	 */
	@Test
	public void indexAndQuery() throws Exception {
		// This is the (input) Data Transfer Object between your client and SOLR.
		final SolrInputDocument input = new SolrInputDocument();
		
		// 1. Populates with (at least required) fields
		input.setField("id", 1);
		input.setField("title", "Apache SOLR Essentials");
		input.setField("author", "Andrea Gazzarini");
		input.setField("isbn", "972-2-5A619-12A-X");
		
		// 2. Adds the document
		client.add(input);
		
		// 3. Makes index changes visible
		client.commit();
		
		// 4. Builds a new query object with a "select all" query. 
		final SolrQuery query = new SolrQuery("*:*");
		
		// 5. Executes the query
		final QueryResponse response = client.query(query);
		
		assertEquals(1, response.getResults().getNumFound());
		
		// 6. Gets the (output) Data Transfer Object.
		final SolrDocument output = response.getResults().iterator().next();
		final String id = (String) output.getFieldValue("id");
		final String title = (String) output.getFieldValue("title");
		final String author = (String) output.getFieldValue("author");
		final String isbn = (String) output.getFieldValue("isbn");
		
		// 7.1 In case we are running as a Java application print out the query results.
		System.out.println("It works! I found the following book: ");
		System.out.println("--------------------------------------");
		System.out.println("ID: " + id);
		System.out.println("Title: " + title);
		System.out.println("Author: " + author);
		System.out.println("ISBN: " + isbn);
		
		// 7. Otherwise asserts the query results using standard JUnit procedures.
		assertEquals("1", id);
		assertEquals("Apache SOLR Essentials", title);
		assertEquals("Andrea Gazzarini", author);
		assertEquals("972-2-5A619-12A-X", isbn);
	}
	
	/**
	 * Entry point for running the example as a Java application.
	 * 
	 * @param args the command line arguments. Not used here.
	 * @throws Exception never, otherwise something went wrong.
	 */
	public static void main(String[] args) throws Exception {
		final FirstQueryITCase test = new FirstQueryITCase();

		test.setUp();
		test.indexAndQuery();
		test.tearDown();
	}
	
	/**
	 * Setup things for this test case.
	 */
	@Before
	public void setUp() {
		client = new HttpSolrServer("http://127.0.0.1:8983/solr/biblo");
	}
	
	/**
	 * Shutdown SOLR client.
	 */
	@After 
	public void tearDown() {
		client.shutdown();
	}
}