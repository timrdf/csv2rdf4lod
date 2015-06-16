package edu.rpi.tw.data.csv;

import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;

import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;

/**
 * 
 */
public interface ValueHandler extends NullInterpreter {

   /**
    * 
    * @return
    */
   public URI getRange();

   /**
    * @param subjectR
    * @param predicate
    * @param value
    * @param primary
    * @param objectURIbase - namespace to place any object URIs if creating any.
    * @param patternFiller - needed for crutch promotions.
    * @param conn2
    */
   public void handleValue(Resource subjectR, 
   							   URI predicate, String predicateLocalName, 
   							   String value, 
                           RepositoryConnection conn, 
                           String objectURIbase, CSVRecordTemplateFiller patternFiller, 
                           RepositoryConnection conn2);
   
   /**
    * Apply any context-free processing (e.g. cast to a decimal).
    * 
    * @param cellValue
    * @return a Value ready for assertion, derived from cellValue according to this ValueHandler.
    */
   public Value handleValue(String cellValue);
   
   /**
    * Return any predicates asserted in triples, excluding the `predicate` in
    * {@link #handleValue(Resource, URI, String, String, RepositoryConnection, String, CSVRecordTemplateFiller, RepositoryConnection)}
    * 
    * @return the predicates asserted while handling any values given.
    */
   public Set<URI> getAssertedPredicates();

   /**
    * 
    * @return
    */
	public Set<URI> getAssertedClasses();

	/**
	 * 
	 * @param rHandler - a ValueHandler that should also handle the given value.
	 */
	public void addChainedHandler(ValueHandler handler);
}