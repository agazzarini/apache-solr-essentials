package org.gazzax.labs.solr.ase.ch2.urp;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

/**
 * Another example of update processor for splitting fields.
 * In bibliographic records there are a lot of information that are "compressed" in single fields.
 * Just to give you an example, there could be a title field which just contains a title but there could be some compound field
 * that is has a single string as value but contains a lot of information like the date, the language, the format; 
 * something like this
 * 
 * <field name="fixed_length_general_information">20041010s187u    it uua e       n    ita c</field>
 * 
 * So, in this case, you may want to separate those information in your solr schema, therefore having: 
 * 
 * <field name="language">ita</field>
 * <field name="year">2004</field>
 * 
 * and so on.
 * Obviously, the logic of this processor strongly depends on the format of the compound fields and on your index / search requirements.
 * In this example we will get the fixed_length_general_information field,  we will create the two fields above and finally we will remove the 
 * compound field.
 *
 */
public class SplitCompoundFieldProcessorFactory extends UpdateRequestProcessorFactory {

	@Override
	public UpdateRequestProcessor getInstance(
			final SolrQueryRequest request,
			final SolrQueryResponse response, 
			final UpdateRequestProcessor next) {
		return new SplitCompoundFieldProcessor(next);
	}
}
