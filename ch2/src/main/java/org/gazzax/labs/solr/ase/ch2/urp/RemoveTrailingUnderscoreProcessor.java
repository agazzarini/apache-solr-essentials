package org.gazzax.labs.solr.ase.ch2.urp;

import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

/**
 * An example of {@link UpdateRequestProcessor} that removes trailing underscores from a given field value.
 * 
 * @since 1.0
 */
public class RemoveTrailingUnderscoreProcessor extends UpdateRequestProcessor {
	private final String [] fields;
	
	/**
	 * Builds a new {@link RemoveTrailingUnderscoreProcessor} with the next processor in the chain.
	 * 
	 * @param next the next processor in the chain.
	 */
	public RemoveTrailingUnderscoreProcessor(final UpdateRequestProcessor next, final String [] fields) {
		super(next);
		this.fields = fields != null ? fields : new String[0];
	}
	
	/**
	 * Intercept the add document operation.
	 * Here this process gets a chance to change the incoming {@link SolrInputDocument}.
	 * 
	 * @param command the update command.
	 * @throws IOException in case of I/O failure.
	 */
	@Override
	public void processAdd(final AddUpdateCommand command) throws IOException {
		// 1. Retrieve the SolrInputDocument that contains data to be indexed.
		final SolrInputDocument document = command.getSolrInputDocument();
		
		// 2. Loop through the target fields
		for (final String fieldName : fields) {
			
			// 3. Get the field values (for simplicity we assume fields are monovalued and are strings)
			final String fieldValue = (String) document.getFieldValue(fieldName);
			
			// 4. Check and eventually change the value of that field.
			if (fieldValue != null && fieldValue.endsWith("_")) {
				document.setField(fieldName, fieldValue.substring(0, fieldValue.length() -1));
			}
		}
		
		// 5. IMPORTANT: forward the control to the next processor in the chain.
		super.processAdd(command);
	}
}
