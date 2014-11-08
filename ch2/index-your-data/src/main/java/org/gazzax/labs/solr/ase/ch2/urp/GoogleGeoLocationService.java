package org.gazzax.labs.solr.ase.ch2.urp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.PreDestroy;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Google geolocation service.
 * Please refer to 
 * <a href="http://code.google.com/apis/maps/documentation/geocoding/">Google Geocoding API</a>
 * 
 * <br/>
 * 
 * In order to detect the coordnates we check, in the response, the following fragment:
 * 
 * <pre>
		<location>
			<lat>42.3780421</lat>
			<lng>12.2785659</lng>
		</location>
 * </pre>
 * 
 * @author Andrea Gazzarini
 */
public class GoogleGeoLocationService {
	private final static Logger LOGGER = LoggerFactory.getLogger(GoogleGeoLocationService.class);
	
	private static String URL = "http://maps.googleapis.com/maps/api/geocode/xml?sensor=false&address=";
	
	private CloseableHttpClient client;
	private SAXParser parser;
	
	private final String LATITUDE_TAG_NAME = "lat";
	private final String LONGITUDE_TAG_NAME = "lng";
	
	private StringBuilder coordinates = new StringBuilder();

	private DefaultHandler responseParser =  new DefaultHandler() {
		private boolean isProcessingLatitude;
		private boolean isProcessingLongitude;		
		private boolean coordinatesHasBeenCollected;
		
		@Override
		public void startDocument() throws SAXException {
			reset();
		};
		
		@Override
		public void endDocument() throws SAXException  {
			// Nothing to be done here...
		};
		
		@Override
		public void startElement(
				final String uri, 
				final String localName, 
				final String qName, 
				final Attributes attributes) throws SAXException {
			if (!coordinatesHasBeenCollected) {
				isProcessingLatitude = LATITUDE_TAG_NAME.equals(qName);
				isProcessingLongitude = LONGITUDE_TAG_NAME.equals(qName);
			}
		};
		
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			if (!coordinatesHasBeenCollected) {
				if (isProcessingLatitude || isProcessingLongitude) {
					coordinates.append(new String(ch, start, length).trim());
				}
			}
		};
		
		public void endElement(
				final String uri, 
				final String localName, 
				final String qName) throws SAXException {
			if (!coordinatesHasBeenCollected) {
				if (isProcessingLatitude)
				{
					coordinates.append(",");
				} else if (isProcessingLongitude)
				{
					coordinatesHasBeenCollected = true;
				}
			}
		};
		
		/**
		 * Resets the state of this parser.
		 */
		private void reset() {
			coordinates.setLength(0);
			isProcessingLatitude = false;
			isProcessingLongitude = false;			
			coordinatesHasBeenCollected = false;
		}
	};
	
	@SuppressWarnings("deprecation")
	public String getCoordinates(final String identifier, final String address) throws IOException {
		HttpGet method = null;
		CloseableHttpResponse response = null;
		try {
			method = new HttpGet(URL + URLEncoder.encode(address, "UTF-8"));
		} catch (final UnsupportedEncodingException exception) {
			method = new HttpGet(URL + URLEncoder.encode(address));
		}
		
		try {
			response = client.execute(method);
			final HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			switch (statusCode) {
				case HttpStatus.SC_OK:					
					parser.parse(new ByteArrayInputStream(EntityUtils.toByteArray(entity)), responseParser);
				
					String result = coordinates.toString();
					
					LOGGER.debug("Document " + identifier + "  with address \"" + address+" \" has been translated to " + result);
					
					return result;
				default:
					String message = "Google Geocoding Service : unable to parse geolocation data for " + identifier;
					LOGGER.error(message);
					throw new  IOException(message);
			}
		} catch (final Exception exception)
		{
			LOGGER.error("Google Geocoding Service : unable to parse geolocation data for " + identifier);
			throw new IOException(exception);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	/**
	 * Initializes this service.
	 */
	public void init() {
		try {
			client = HttpClients.createDefault();
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}
	
	/**
	 * Shutdown this service
	 */
	@PreDestroy
	public void shutdown() {
		try {
			client.close();
		} catch (final IOException exception) {
			LOGGER.error("I/O Failure while closing the HTTP client", exception);
		}
	}
}