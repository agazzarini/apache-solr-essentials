package org.gazzax.labs.solr.ase.ch3.sp;

import java.io.IOException;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.lucene.document.Document;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.SolrIndexSearcher;
 
/**
 * A {@link SearchComponent} for gathering realtime prices from an external
 * resource (e.g. database, web service).
 * 
 * Note that it has to be declared in solrconfig.xml with &lt;searchComponent&gt;
 * 
 * <br/><pre> 	
	&lt;searchComponent name="prices" class="org.gazzax.labs.solr.ase.ch2.urp.RealTimePriceComponent"&gt;
		&lt;str name="datasource-jndi-name"&gt;jdbc/prices&lt;/str&gt;		
	&lt;/searchComponent&gt;
 * </pre><br/>
 * 
 * NOTE: this example should require a database where prices can be retrieved. For demonstration, 
 * it accepts a configuration parameter "dummy-mode", which defaults to true that returns random prices.
 * If you want to use that with a real database you must rewrite / subclass and override the {@link #getPrice(String)} method
 * in order to execute a query against a running database. Don't forget to set "dummy-mode" to false.
 * 
 * <br/><br/>
 * 
 * After doing that, the search component needs to be declared within the appropriate RequestHandler:
 * 
 * <br/>
 * 
 * <pre>
	&lt;requestHandler name=”/xyz” (other attributes follow) &gt;
		&lt;lst name=”defaults”&gt;
		…
		&lt;/lst&gt;
		&lt;arr name="last-components"&gt;
			&lt;str&gt;prices&lt;/str&gt;
		&lt;/arr&gt;	
	&lt;/requestHandler&gt;
 * </pre> 
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class RealTimePriceComponent extends SearchComponent {

	private DataSource datasource;

	// although an ideal approach could use a State pattern, in this example, for simplicity
	// we will use a boolean flag indicating a failure in obtaining a valid external resource reference
	// This is because we don't want to fail the whole search request in case the price database is down.
	// So if this flag is false this component basically will do nothing.
	private boolean hasBeenCorrectlyInitialised;
	
	/**
	 * Component initialisation.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void init(final NamedList args) {
		
		// 1.Retrieve the configuration parameters. 
		// First a "dummy" mode flag, which indicates we are running the 
		// example without a database.
		// If the dummy mode is set to true the get the datasource name
		// This component uses a database as external resource, but it could be
		// also a web service. In any case, this is the place where that
		// resource should be initialized.
		final SolrParams params = SolrParams.toSolrParams(args);
		final boolean isInDummyMode = params.getBool("dummy-mode", true);
		if (isInDummyMode) {
			hasBeenCorrectlyInitialised = true;
			return;
		}
		
		final String datasourceName = params.get("datasource-jndi-name", "jdbc/pricesdb");

		// In case you want give a try, this component actually stubs its
		// behaviour.
		// This is because otherwise we should have a database somewhere, we
		// should configure a datasource and so on...
		// The following code is therefore commented and should be used in case
		// you set up a valid datasource
		try {
			// 2. Obtain a reference to the naming context
			final Context ctx = new InitialContext();
	
			// 3. Lookup the datasource.
			datasource = (DataSource) ctx.lookup(datasourceName);
	
			// 4. Give a try by opening and immediately opening a connection.
			datasource.getConnection().close();
			
			// 5a. Mark this component as valid
			hasBeenCorrectlyInitialised = false;
		} catch (final Exception exception) {
			exception.printStackTrace();

			// 5b. Mark this component as invalid (no strictly needed)
			hasBeenCorrectlyInitialised = false;
		}
	}

	/**
	 * Prepare the response. Guaranteed to be called before any SearchComponent
	 * {@link #process(org.apache.solr.handler.component.ResponseBuilder)}
	 * method. Called for every incoming request.
	 * 
	 * The place to do initialization that is request dependent.
	 * 
	 * @param rb The {@link org.apache.solr.handler.component.ResponseBuilder}
	 * @throws IOException If there is a low-level I/O error.
	 */
	@Override
	public void prepare(final ResponseBuilder rb) throws IOException {
		// Nothing to be done here for this example. 
		// In case the datasource initialisation failed, this could be a place to
		// retry the initialisation.
	}

	/**
	 * Here we define the component core logic.
	 * For each document belonging to search results, we call an external service
	 * for gathering a corresponding up-to-date price.
	 * 
	 * @param rb The {@link org.apache.solr.handler.component.ResponseBuilder}
	 * @throws IOException If there is a low-level I/O error.
	 */
	@Override
	public void process(final ResponseBuilder builder) throws IOException {
		// Sanity check: if the component hasn't been properly initialised 
		// then it must immediately return.
		// A more ideal approach could retry the initialisation (in the prepare method).
		if (!hasBeenCorrectlyInitialised) {
			return;
		}
		
		// Get a SolrIndexSearcher reference 
		final SolrIndexSearcher searcher = builder.req.getSearcher();

		// This NamediLis will hold the component contribution (i.e. the component result).
		final NamedList<Double> contribution = new SimpleOrderedMap<Double>();
		for (final DocIterator it = builder.getResults().docList.iterator(); it.hasNext();) {

			// This is NOT the Solr ID of our records, but instead the Lucene internal document id
			// which is different
			int docId = it.nextDoc();
			final Document luceneDocument = searcher.doc(docId);
			
			// This is the Solr document Id 
			String id = luceneDocument.get("id");

			// Get the price of the item
			final Double itemPrice = getPrice(id);

			// Add the price of the item to the component contribution
			contribution.add(id, itemPrice);
		}

		// Add the component contribution to the response builder
		builder.rsp.add("prices", contribution);			
	}

	@Override
	public String getDescription() {
		return "Real time price component";
	}

	@Override
	public String getSource() {
		return null;
	}
	
	private final static Random RANDOMIZER = new Random();
	
	/**
	 * Returns the price associated with a given item.
	 * Note: this method actually returns random numbers :)
	 * This should be the place where the datasource or the external service should be used 
	 * for gathering prices.
	 * 
	 * @param id the record identifier.
	 * @return the price associated with a given item.
	 */
	Double getPrice(final String id) {
		int base = RANDOMIZER.nextInt(100);
		double factor = RANDOMIZER.nextDouble();
		factor = factor == 0 ? 1 : factor;
		return base * factor;
	}	
	
	public static void main(String[] args) {
		System.out.println(new RealTimePriceComponent().getPrice("1"));
	}
}