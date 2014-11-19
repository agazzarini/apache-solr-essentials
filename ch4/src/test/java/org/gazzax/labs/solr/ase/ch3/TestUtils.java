package org.gazzax.labs.solr.ase.ch3;

import java.util.Random;

/**
 * Utility methods used in tests.
 * 
 * @author Andrea Gazzarini
 * @since 1.0
 */
public abstract class TestUtils {
	final static Random RANDOMIZER = new Random();
		
	/**
	 * Generates a (pseudo) random identifier.
	 * 
	 * @return a (pseudo) random identifier.
	 */
	public static String randomStringIdentifier() {
		return String.valueOf(RANDOMIZER.nextLong() + System.currentTimeMillis());
	}
}