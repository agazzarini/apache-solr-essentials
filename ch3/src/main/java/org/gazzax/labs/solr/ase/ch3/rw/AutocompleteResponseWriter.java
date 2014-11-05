package org.gazzax.labs.solr.ase.ch3.rw;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;

/**
 * A custom response writer for an autocomplete feature.
 * 
 * The output of this writer is a short JSON like this:
 *
 * <br/>
 * <pre>
 * 	{
		 query:'Li',
		 suggestions:['Liberia','Libyan Arab Jamahiriya','Liechtenstein','Lithuania']
	}
 * </pre>
 * 
 * An interesting extension on this component could return also the type of a given 
 * label, therefore allowing a client to show the label and the corresponding type like:
 * 
 * Alain Caron (Bassist)
 * Another Brick in the wall (Song)
 * Antony Jackson (Bassist)
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public class AutocompleteResponseWriter implements QueryResponseWriter {	
	private final static Set<String> FIELDS = new HashSet<String>(1);
	static 
	{
		FIELDS.add("label");
	}

	/**
	 * Here the writer creates its output.
	 * 
	 * @param writer the character stream writer.
	 * @param request the current {@link SolrQueryRequest}
	 * @param response the output response.
	 * @throws IOException in case of I/O failure.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void write(
			final Writer writer, 
			final SolrQueryRequest request, 
			final SolrQueryResponse response) throws IOException {
		
		// 1. Get a reference to values that compound the current response
		final NamedList elements = response.getValues();
		
		// 2. Use a StringBuilder to build the output 
		final StringBuilder builder = new StringBuilder("{")
			.append("query:'")
			.append(request.getParams().get(CommonParams.Q))
			.append("',");
		
		// 3. Get a reference to the object which hold the query result
		final Object value = elements.getVal(1);		
		if (value instanceof ResultContext)
		{
			final ResultContext context = (ResultContext) value;
		
			// The ordered list (actually the page subset) of matched documents
			final DocList ids = context.docs;
			if (ids != null)
			{
				final SolrIndexSearcher searcher = request.getSearcher();
				final DocIterator iterator = ids.iterator();
				builder.append("suggestions:[");
				
				// 4. Iterate over documents
				for (int i = 0; i < ids.size(); i++)
				{
					// 5. For each document we need to get the corresponding "label" attribute
					final Document document = searcher.doc(iterator.nextDoc(), FIELDS);
					if (i > 0)  { builder.append(","); }
					
					// 6. Append the label value to writer output
					builder
						.append("'")
						.append(((String) document.get("label")).replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\""))
						.append("'");
				}
				builder.append("]").append("}");
			}
		}
		
		// 7. and finally write out the built character stream by means of output writer.
		writer.write(builder.toString());
	}

	@Override
	public String getContentType(final SolrQueryRequest request, final SolrQueryResponse response) 
	{
		 return "application/json;  charset=UTF-8";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void init(final NamedList args) 
	{
		// Nothing to be done here
	}
}