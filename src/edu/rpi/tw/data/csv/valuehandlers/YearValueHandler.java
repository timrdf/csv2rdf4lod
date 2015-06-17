package edu.rpi.tw.data.csv.valuehandlers;

import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;

/**
 * http://www.w3.org/TR/xmlschema-2/#gYear
 */
public class YearValueHandler extends DefaultValueHandler {

   protected HashMap<String,Value> codebook = new HashMap<String,Value>();
   
   /**
    * 
    */
   public YearValueHandler() {
      this(null);
   }
   
   /**
    * 
    * @param codebook
    */
   public YearValueHandler(HashMap<String,Value> codebook) {
      super();
      if( codebook != null ) this.codebook = codebook;
   }

   @Override
   public URI getRange() {
      return ValueFactoryImpl.getInstance().createURI("http://www.w3.org/TR/xmlschema-2/#gYear");
   }

   @Override
   public void handleValue(Resource subjectR, URI predicate, String predicateLocalName, String value,
                           RepositoryConnection conn, String resourceURIbase, CSVRecordTemplateFiller rec,
                           RepositoryConnection conn2) {
      try {
         Value nonIntegerCode = this.codebook.get(value);
         if( nonIntegerCode != null ) {
            conn.add(subjectR, predicate, this.codebook.get(value));
         }else {   
            //int intVal = Integer.parseInt(value.replaceAll(",", "").replaceAll(" ", ""));
            int intVal = Integer.parseInt(IntegerMultiplierValueHandler.tweak(value));
            conn.add(subjectR, predicate, vf.createLiteral(""+intVal,getRange()));
         }
      } catch (RepositoryException e) {
         e.printStackTrace();
      } catch (NumberFormatException e) {
         //e.printStackTrace();
         this.failedOnValue(value, subjectR, predicate);
         try {
            conn.add(subjectR, predicate, vf.createLiteral(value));
         } catch (RepositoryException e1) {
            e1.printStackTrace();
         }
      } 
   }
}