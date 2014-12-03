/**
 * This package contains several examples of {@link org.apache.solr.update.processor.UpdateRequestProcessor}.
 * Specifically:
 * 
 * <ul>
 *	<li><b>RemoveTrailingUnderscoreProcessor</b>: removes trailing underscores from fields and therefore change the stored value of a field (before indexing the document)</li>
 *	<li><b>SplitCompoundFieldProcessor</b>: another way to manipulate fields in documents. It uses a field value to populate several fields</li>
 *	<li><b>AddCoordinatesProcessor</b>: uses an address field withing the document and calls a Google geolocation service for gathering the lat and lon coordinates.</li>
 * </ul>
 */
package org.gazzax.labs.solr.ase.ch2.urp;