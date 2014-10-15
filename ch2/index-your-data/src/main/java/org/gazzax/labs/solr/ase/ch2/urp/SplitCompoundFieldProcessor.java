package org.gazzax.labs.solr.ase.ch2.urp;

import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

/**
 * An {@link UpdateRequestProcessor} that splits a compound field value in order to create 
 * several fields.
 * 
 */
public class SplitCompoundFieldProcessor extends UpdateRequestProcessor {

	private final static String COMPOUND_FIELD_NAME = "fixed_length_general_information";
	
	/**
	 * Builds a new {@link SplitCompoundFieldProcessor}.
	 * 
	 * @param next the next processor in the chain.
	 */
	public SplitCompoundFieldProcessor(final UpdateRequestProcessor next) {
		super(next);
	}
	
	@Override
	public void processAdd(final AddUpdateCommand command) throws IOException {
		// 1. Get the Solr (Input) document
		final SolrInputDocument document = command.getSolrInputDocument();
		
		// 2. Get the value of the compound field 
		final String compoundValue = (String) document.getFieldValue(COMPOUND_FIELD_NAME);

		// 3. Split the value and create the other fields
		if (compoundValue != null) {
			
			// 4. Create and populate the "year" field.
			if (compoundValue.length() >=4) {
				final String year = compoundValue.substring(0, 4);
				document.setField("year", year);
			}
			
			// 5. Create and populate the "language" field.
			if (compoundValue.length() >=39) {
				final String language = compoundValue.substring(36, 39);
				document.setField("language", language);				
			}
			
			// 6. Remove the compound field.
			document.remove(COMPOUND_FIELD_NAME);
		}
		
		// 7. IMPORTANT: forward the control to the next processor in the chain.
		super.processAdd(command);
	}
}
