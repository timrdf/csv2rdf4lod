package edu.rpi.tw.data.csv.valuehandlers;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.logging.Logger;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.Conversion;
import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;

/**
 * TODO: DecimalMultiplierValueHandler FAILED: "9.66556E-05"
 * TODO: interpret as null failed: "-"
 */
public class DecimalMultiplierValueHandler extends DefaultValueHandler {

   private static Logger logger = Logger.getLogger(DecimalMultiplierValueHandler.class.getName());
   
   private double multiplier = 1;
   
   private URI range = ValueFactoryImpl.getInstance().createURI("http://www.w3.org/2001/XMLSchema#decimal");
   
   /**
    * 
    */
   public DecimalMultiplierValueHandler() {
      this(1.0);
   }
   
   /**
    * 
    * @param multiplier
    */
   public DecimalMultiplierValueHandler(double multiplier) {
   	this(multiplier, null, "http://www.w3.org/2001/XMLSchema#decimal");
   }
   
   /**
    * 
    * @param multiplier
    * @param codebook
    */
   public DecimalMultiplierValueHandler(double multiplier, HashMap<String,Value> codebook) {
   	this(multiplier, codebook, "http://www.w3.org/2001/XMLSchema#decimal");
   }
   
   /**
    * 
    * @param multiplier
    * @param codebook
    * @param range
    */
   public DecimalMultiplierValueHandler(double multiplier, HashMap<String,Value> codebook, String range) {
      super(codebook);
      this.range = ValueFactoryImpl.getInstance().createURI(range);
      this.multiplier = multiplier;
   }
   
   /**
    * 
    */
   @Override
   public URI getRange() {
      return this.range;
   }

   @Override
   public void handleValue(Resource subjectR, URI predicate, String predicateLocalName, String value,
                           RepositoryConnection conn, String resourceURIbase, CSVRecordTemplateFiller rec, 
                           RepositoryConnection conn2) {
      try {
      	
         if( interpretsAsNull(value) ) return;
         if( Conversion.NULL.equals(predicate) ) return;
         
      	if( super.codebook.containsKey(value) ) {
      		//System.err.println("DEC MULT CODEBOOKS " + value + " to " + super.codebook.get(value));
            conn.add(subjectR, predicate, this.codebook.get(value));
      	}else if( null == value || value.length() == 0 ) {
      		return;
      	}else {
      	
	         double doubleVal = multiplier * Double.parseDouble(tweak(value));
	
	         /*if( value != null && value.indexOf("e") > 0 ) {
	            logger.finest("DECIMAL TWEAK: "+value+" --> " +DecimalMultiplierValueHandler.tweak(value)+"  -->  " +doubleVal);
	         }*/
	         
	         String myformat = "###############################.#####################";
	         DecimalFormat df = new DecimalFormat(myformat);
	         
	         //System.err.println(doubleVal + " " + df.format(doubleVal));
	         //
	         // This wasn't the original, but fixed a problem before (is no creating "2.38E7"^^xsd:decimal"): 
	         // primary.add(subjectR, predicate, vf.createLiteral(doubleVal));
	         // primary.add(subjectR, predicate, vf.createLiteral(""+doubleVal,getRange()));
	         conn.add(subjectR, predicate, vf.createLiteral(""+df.format(doubleVal),getRange()));
      	}
      } catch (RepositoryException e) {
         e.printStackTrace();
      } catch (NumberFormatException e) {
         e.printStackTrace();
         System.err.println(value.replaceAll("[^0-9.+-e]", ""));
         this.failedOnValue(value, subjectR, predicate);
      } 
   }
   
   /**
    * 
    * @param supposedDecimal
    * @return
    */
   public static String tweak(String supposedDecimal) {
      //return supposedInt.replaceAll(",", "").replaceAll(" ", "").replaceAll("%","").replaceAll("$","").replaceAll("\\$", "");
      return supposedDecimal.replaceAll("[^0-9.+-e]", "").replaceAll(",","");
   }
   
   
   /**
    * 
    */
   @Override
   public Value handleValue(String cellValue) {
      // TODO: code above should call this?
      
      if( cellValue.startsWith("xsd:decimal(") ) {
         cellValue = cellValue.substring(12,cellValue.length()-1);
      }
      //System.err.println("strip: " + cellValue);
      cellValue = tweak(cellValue);
      
      double doubleVal = multiplier * Double.parseDouble(tweak(cellValue));
      
      String myformat = "###############################.#####################";
      DecimalFormat df = new DecimalFormat(myformat);
      
      //System.err.println("done: " + cellValue);
      return vf.createLiteral(""+df.format(doubleVal),getRange());
   }
}