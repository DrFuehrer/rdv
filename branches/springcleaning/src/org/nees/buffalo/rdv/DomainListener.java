package org.nees.buffalo.rdv;

/**
 * A listener interface to notify listeners when the domain
 * has changed.
 * 
 * @author  Jason P. Hanley
 * @since   1.0
 */
public interface DomainListener {
	/**
	 * Called when the domain has been changed
	 * 
	 * @param domain  the new value of the domain
	 * @since         1.0
	 */
	public void domainChanged(double domain);
}
