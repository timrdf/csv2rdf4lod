package edu.rpi.tw.data.csv.valuehandlers;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;


/**
 * 
 */
public class LongMultiplierValueHandler extends DefaultValueHandler {

   private double multiplier = 1;
   
   public LongMultiplierValueHandler(double multiplier) {
      this.multiplier = multiplier;
   }
   
   @Override
   public URI getRange() {
      return (URI) ValueFactoryImpl.getInstance().createURI("http://www.w3.org/2001/XMLSchema#long");
   }

   @Override
   public void handleValue(Resource subjectR, URI predicate, String predicateLocalName, String value,
                           RepositoryConnection conn, String resourceURIbase,  CSVRecordTemplateFiller rec,RepositoryConnection conn2) {

      String tweaked = IntegerMultiplierValueHandler.tweak(value);
      try {
         long intVal = (long) this.multiplier * Long.parseLong(tweaked);
         conn.add(subjectR, predicate, vf.createLiteral(intVal));
      } catch (RepositoryException e) {
         e.printStackTrace();
      } catch (NumberFormatException e) {
         //e.printStackTrace();
         this.failedOnValue(value, subjectR, predicate);
      } 
   }
}