package org.gazzax.labs.solr.ase.ch2.handler;

import java.io.BufferedReader;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.handler.loader.ContentStreamLoader;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

public class FlatDataLoader extends ContentStreamLoader {

	@Override
	public void load(final SolrQueryRequest req, final SolrQueryResponse rsp,
			final ContentStream stream, final UpdateRequestProcessor processor)
			throws Exception {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(stream.getReader());
			String actLine = null;
			while ((actLine = reader.readLine()) != null) {
				// 1. Sanity check: line must have a fixed length, otherwise
				// skip
				if (actLine.length() != 107) {
					continue;
				}

				// 2. parse and create the document
				final SolrInputDocument document = new SolrInputDocument();
				document.setField("id", actLine.substring(0, 8));
				document.setField("isbn", actLine.substring(8,21).trim());
				document.setField("title", actLine.substring(21, 65).trim());
				document.setField("author", actLine.substring(65).trim());				
				
				AddUpdateCommand command = getAddCommand(req);
				command.solrDoc = document;
				
				processor.processAdd(command);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception ignore) {
					// TODO: handle exception
				}
			}
		}
	}

	private AddUpdateCommand getAddCommand(final SolrQueryRequest req) {
		final AddUpdateCommand addCmd = new AddUpdateCommand(req);
		
		addCmd.overwrite = req.getParams().getBool(UpdateParams.OVERWRITE, true);
		addCmd.commitWithin = req.getParams().getInt(UpdateParams.COMMIT_WITHIN, -1);
		return addCmd;
	}
}
