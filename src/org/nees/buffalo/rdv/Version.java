package org.nees.buffalo.rdv;

/**
 * A class to encapsulate the version of the application.
 * The version is displayed in the format of major.minor.release.build.
 * 
 * @author  Jason P. Hanley
 * @since   1.0
 */
public final class Version {
	/**
	 * The major version number.
	 * 
	 * @since  1.0
	 */
	public static final int major = 1;
	
	/**
	 * The minor version number
	 * 
	 * @since  1.0 
	 */
	public static final int minor = 2;
	
	/**
	 * The release number.
	 * 
	 * @since  1.0
	 */
	public static final int release = 0;
	
	/**
	 * The build string.
	 *
	 * @since  1.2
	 */
	public static final String build = "_beta1";
	
	/**
	 * This class can not be instantiated and it's constructor
	 * always throws an exception.
	 */
	private Version() {
		throw new UnsupportedOperationException("This class can not be instantiated.");
	}
}
