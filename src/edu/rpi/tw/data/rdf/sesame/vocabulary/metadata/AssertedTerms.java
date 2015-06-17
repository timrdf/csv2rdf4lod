package edu.rpi.tw.data.rdf.sesame.vocabulary.metadata;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

/**
 * Struct created to return from static assertion methods.
 */
public class AssertedTerms {
	
	protected Set<URI> predicates = new HashSet<URI>();
	protected Set<URI> classes    = new HashSet<URI>();
	
	/**
	 * 
	 * @param predicate
	 */
	public void usedPredicate(URI predicate) {
		this.predicates.add(predicate);
	}
	
	/**
	 * 
	 * @param usedClass
	 */
	public void usedClass(URI usedClass) {
		this.classes.add(usedClass);
	}
	
	/**
	 * 
	 * @return
	 */
	public Set<URI> getPredicates() {
		return this.predicates;
	}
	
	/**
	 * 
	 * @return
	 */
	public Set<URI> getClasses() {
		return this.classes;
	}
}