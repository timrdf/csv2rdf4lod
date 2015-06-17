package edu.rpi.tw.data.csv.valuehandlers;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;

/**
 * Does nothing but assert value as literal.
 */
public class VerbatimLiteralValueHandler extends DefaultValueHandler {

   /** Singleton 
   private static VerbatimLiteralValueHandler singleton = new VerbatimLiteralValueHandler();
   private VerbatimLiteralValueHandler() {  
   }
   public static VerbatimLiteralValueHandler getInstance() {
      return singleton;
   }*/
   
   public VerbatimLiteralValueHandler() {
      super();
   }
   
   @Override
   public URI getRange() {
      return RDFS.LITERAL;
   }
   
   /**
    * @param subjectR
    * @param predicate
    * @param value
    * @param primary
    * @param resourceURIbase
    * @param pmap
    * @param objectLabels
    */
   @Override
   public void handleValue(Resource subjectR, URI predicate, String predicateLocalName, String value, 
                           RepositoryConnection conn, String resourceURIbase, 
                           CSVRecordTemplateFiller templateFiller, RepositoryConnection conn2) {
      try {
         //System.err.println(subjectR.stringValue()+" "+predicate.stringValue()+" "+value);
         if( subjectR  == null ) System.err.println("s "+subjectR);
         if( predicate == null ) System.err.println("   p "+predicate);
         if( value     == null ) System.err.println("       o "+value);
         conn.add(subjectR, predicate, vf.createLiteral(value));
      } catch (RepositoryException e) {
         e.printStackTrace();
      }
      
      handleChain(subjectR, predicate, predicateLocalName, value, conn, resourceURIbase, templateFiller, conn2);
   }
}