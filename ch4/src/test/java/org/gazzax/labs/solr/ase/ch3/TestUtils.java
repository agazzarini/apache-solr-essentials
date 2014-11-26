package org.gazzax.labs.solr.ase.ch3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.solr.common.SolrInputDocument;
 
/** 
 * Utility stuff used in tests.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public abstract class TestUtils {
	public final static String SOLR_URI = "http://127.0.0.1:8983/solr/example/";
	final static Random RANDOMIZER = new Random();
		
	/**
	 * Generates a (pseudo) random identifier.
	 * 
	 * @return a (pseudo) random identifier.
	 */
	public static String randomStringIdentifier() {
		return String.valueOf(RANDOMIZER.nextLong() + System.currentTimeMillis());
	}
	
	// list of SolrInputDocuments representing data to be indexed.
	final static List<SolrInputDocument> ALBUMS = new ArrayList<SolrInputDocument>();
	static
	{
		// SolrInputDocument is the data transfer object that represents a document / record on Solr.
		final SolrInputDocument weatherReport = new SolrInputDocument();
		
		// "set" set a given value. Any existing value (single or multiple) will be removed.
		weatherReport.setField("id", randomStringIdentifier());
		weatherReport.setField("title", "Heavy Weather");
		weatherReport.setField("artist", "Weather Report");

		// "genre" is a multivalued field so we must use "add" in order to set two values.
		weatherReport.addField("genre", "Jazz");
		weatherReport.addField("genre", "Fusion");
		
		weatherReport.setField("released", "1977");
		
		ALBUMS.add(weatherReport);
		
		final SolrInputDocument octavarium = new SolrInputDocument();
		octavarium.setField("id", randomStringIdentifier());
		octavarium.setField("title", "Octavarium");
		octavarium.setField("artist", "Dream Theater");

		octavarium.addField("genre", "Progressive Rock");
		octavarium.addField("genre", "Progressive Metal");
		
		octavarium.setField("released", "2005");
		
		ALBUMS.add(octavarium);
		
		final SolrInputDocument delicateSoundOfThunder = new SolrInputDocument();
		delicateSoundOfThunder.setField("id", randomStringIdentifier());
		delicateSoundOfThunder.setField("title", "Delicate Sound of Thunder");
		delicateSoundOfThunder.setField("artist", "Pink Floyd");
		delicateSoundOfThunder.addField("genre", "Progressive Rock");
		delicateSoundOfThunder.addField("genre", "Pop");
		delicateSoundOfThunder.addField("genre", "Rock");
		delicateSoundOfThunder.setField("released", "1988");
	
		ALBUMS.add(delicateSoundOfThunder);
		
		final SolrInputDocument ummagumma = new SolrInputDocument();
		ummagumma.setField("id", randomStringIdentifier());
		ummagumma.setField("title", "Ummagumma");
		ummagumma.setField("artist", "Pink Floyd");
		ummagumma.addField("genre", "Progressive Rock");
		ummagumma.addField("genre", "Rock");
		ummagumma.setField("released", "1969");

		final SolrInputDocument rockTheNations = new SolrInputDocument();
		rockTheNations.setField("id", randomStringIdentifier());
		rockTheNations.setField("title", "Rock the Nations");
		rockTheNations.setField("artist", "Saxon");
		rockTheNations.addField("genre", "Rock");
		rockTheNations.setField("released", "1987");
		
		ALBUMS.add(rockTheNations);		

		final SolrInputDocument theStoryOfRock = new SolrInputDocument();
		theStoryOfRock.setField("id", randomStringIdentifier());
		theStoryOfRock.setField("title", "The Story of Rock");
		theStoryOfRock.setField("artist", "Steve Vai");
		theStoryOfRock.addField("genre", "Rock");
		theStoryOfRock.addField("genre", "Instrumental Rock");
		theStoryOfRock.addField("genre", "Progressive Rock");
		theStoryOfRock.setField("released", "2012");
		
		ALBUMS.add(theStoryOfRock);		

		final SolrInputDocument jazzAlbum1 = new SolrInputDocument();
		jazzAlbum1.setField("id", randomStringIdentifier());
		jazzAlbum1.setField("title", "A Modern Jazz Symposium of Music and Poetry");
		jazzAlbum1.setField("artist", "Charles Mingus");
		jazzAlbum1.addField("genre", "Jazz");
		jazzAlbum1.setField("released", "1957");

		ALBUMS.add(jazzAlbum1);

		final SolrInputDocument jazzAlbum2 = new SolrInputDocument();
		jazzAlbum2.setField("id", randomStringIdentifier());
		jazzAlbum2.setField("title", "Where Jazz meets Poetry");
		jazzAlbum2.setField("artist", "United Artists");
		jazzAlbum2.addField("genre", "Jazz");
		jazzAlbum2.setField("released", "1976");

		ALBUMS.add(jazzAlbum2);
	}
	
	/**
	 * Returns the sample data (albums) that will be indexed.
	 * 
	 * @return some sample data to index.
	 */
	public static List<SolrInputDocument> sampleData() {
		return ALBUMS;
	}	
}