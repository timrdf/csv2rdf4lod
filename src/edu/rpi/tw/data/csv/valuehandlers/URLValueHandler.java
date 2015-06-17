package edu.rpi.tw.data.csv.valuehandlers;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;

/**
 * 
 */
public class URLValueHandler extends DefaultValueHandler {

   /** Singleton */
   private static URLValueHandler singleton = new URLValueHandler();
   private URLValueHandler() {  
   }
   public static URLValueHandler getInstance() {
      return singleton;
   }

   @Override
   public URI getRange() {
      return RDFS.RESOURCE;
   }
   
   @Override
   public void handleValue(Resource subjectR, URI predicate, String predicateLocalName, String value, 
                           RepositoryConnection conn, String resourceURIbase, CSVRecordTemplateFiller rec, RepositoryConnection conn2) {  
      
      // The value is an rdfs:Resource's name (a URI).
      //System.err.println(getClass().getSimpleName()+": "+value + " " + valueR.stringValue());
      if( value != null && value.length() > 0 ) {
         try {
            Resource valueR = vf.createURI(value.replaceAll(" ",""));
            conn.add(subjectR, predicate,  valueR);
            conn2.add(valueR, RDFS.LABEL, vf.createLiteral(value));
         } catch (RepositoryException e) {
            e.printStackTrace();
         }   
      }else { // There is no value
         try {
            this.failedOnValue(value, subjectR, predicate);
            conn.add(subjectR, predicate,vf.createLiteral(value));
         } catch (RepositoryException e) {
            e.printStackTrace();
         }
      }
   }
}// TODO: really cast to a URI, or make a typed literal? Greg says URI, Lee says typed literal.