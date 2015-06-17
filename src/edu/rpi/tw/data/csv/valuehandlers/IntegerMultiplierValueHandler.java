package edu.rpi.tw.data.csv.valuehandlers;

import java.util.HashMap;
import java.util.logging.Logger;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;


/**
 * multiplier can be double in cases where multiplying value with non-integer value results in
 * integer value.
 */
public class IntegerMultiplierValueHandler extends DefaultValueHandler {

   private static Logger logger = Logger.getLogger(IntegerMultiplierValueHandler.class.getName());
   
   protected double                multiplier;
   protected HashMap<String,Value> codebook = new HashMap<String,Value>(); // TODO: get rid of this; use parent's
   protected URI range = ValueFactoryImpl.getInstance().createURI("http://www.w3.org/2001/XMLSchema#integer");

   /**
    * 
    * @param multiplier
    */
   public IntegerMultiplierValueHandler() {
      this(1.0,null,null); 
   }
   
   /**
    * 
    * @param multiplier
    */
   public IntegerMultiplierValueHandler(double multiplier) {
      this(multiplier,null,null); 
   }
   
   /**
    * 
    * @param multiplier - 
    * @param codebook - for cases when some integers do not represent integers.
    * @param range - 
    */
   public IntegerMultiplierValueHandler(double multiplier, HashMap<String,Value> codebook, String range) {
      this.multiplier = multiplier;
      if( codebook != null ) {
         this.codebook   = codebook;
      }
      if( range != null ) {
         this.range = ValueFactoryImpl.getInstance().createURI(range);
      }
   }
   
   @Override
   public URI getRange() {
      return range;
   }

   @Override
   public void handleValue(Resource subjectR, URI predicate, String predicateLocalName, String value,
                           RepositoryConnection conn, String resourceURIbase, CSVRecordTemplateFiller rec,
                           RepositoryConnection conn2) {
      try {
      	
         if( interpretsAsNull(value) ) return;
         
         //System.err.print(value + " supposedly an int: "+value+" -> tweak: "+tweak(value));
         
         
         // TODO: add if( null == value || value.length() == 0 ) return; like DecimalMultiplierValueHandler ?
         
         
         Value nonIntegerCode = this.codebook.get(value);
         //logger.finer("==null: " + (value == null) + " length: " + value.length() + " value:" + value + " interp as null: " + interpretsAsNull(value) + " codebooked: " + nonIntegerCode);
         if( nonIntegerCode != null ) {
            //System.err.println(" codebook: "+this.codebook.get(value));
            //primary.add(subjectR, predicate, this.resourceVH.promote(propertyLN, baseURI, value, null));
            conn.add(subjectR, predicate, this.codebook.get(value));
         }else {   
            //System.err.print(" non codebook: "+tweak(value));
            
            // Handle "$73.9" with multiplier "10"^^xsd:decimal
            long intVal = (long) (this.multiplier * Double.parseDouble(tweak(value)));
            
            //System.err.println(" non codebook multiplied: "+intVal);
            
            conn.add(subjectR, predicate, vf.createLiteral(""+intVal, getRange()));
         }
      } catch (RepositoryException e) {
         e.printStackTrace();
      } catch (NumberFormatException e) {
         
         this.failedOnValue(value, subjectR, predicate);
         try {
            conn.add(subjectR, predicate, vf.createLiteral(value));
         } catch (RepositoryException e1) {
            e1.printStackTrace();
         }
      } 
   }
   
   /**
    * 
    * @param supposedInt
    * @return
    */
   public static String tweak(String supposedInt) { // TODO: first $ do anything?
      //return supposedInt.replaceAll(",", "").replaceAll(" ", "").replaceAll("%","").replaceAll("$","").replaceAll("\\$", "");
      return supposedInt.replaceAll("[^0-9.+-]", "");
   }
   
   /**
    * 
    */
   @Override
   public Value handleValue(String cellValue) {
   	return vf.createLiteral(""+Integer.parseInt(cellValue.replaceAll("[^0-9.+-]", "")),getRange());
   }
}