package edu.rpi.tw.data.csv.valuehandlers;

import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.Conversion;
import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;


/**
 * (case insensitive): 'yes', 'no', 'true', 'false', '0', and '1'.
 */
public class BooleanValueHandler extends DefaultValueHandler {

   public static final Value trueV  = vf.createLiteral(true);
   public static final Value falseV = vf.createLiteral(false);
   
   protected HashMap<String,Value> codebook = new HashMap<String,Value>();
   
   /**
    * 
    * @param interpetAsTrueStrings
    * @param interpetAsFalseStrings
    */
   public BooleanValueHandler(HashMap<String,Value> codebook) {
      if( codebook != null ) this.codebook = codebook;
   }

   @Override
   public URI getRange() {
      return vf.createURI("http://www.w3.org/2001/XMLSchema#boolean");
   }

   @Override
   public void handleValue(Resource subjectR, URI predicate, String predicateLocalName, String value,
                           RepositoryConnection conn, String resourceURIbase, CSVRecordTemplateFiller rec, RepositoryConnection conn2) {
      
      if( Conversion.NULL.equals(this.codebook.get(value)) ) return;
      
      //System.err.println("trying boolean: "+value+" (" +isTrue(value) + "-" + isFalse(value)+") "+codebook.get(value)+" ("+isTrue(codebook.get(value)) + "-" + isFalse(codebook.get(value))+")");
      try {
         if( isTrue(value) ) {
            conn.add(subjectR, predicate, trueV);
         }else if( isFalse(value) ) {
            conn.add(subjectR, predicate, falseV);
         }else if( codebook.get(value) != null ) {
            conn.add(subjectR, predicate, codebook.get(value) );
         }else {
            this.failedOnValue(value, 8 + codebook.size(), subjectR, predicate);
            conn.add(subjectR, predicate, vf.createLiteral(value));
         }
      } catch (RepositoryException e) {
         e.printStackTrace();
      }
   }
   
   /**
    * 
    * @param value
    * @return
    */
   public boolean isTrue(String value) {
      if( this.codebook.size() == 0 ) {
         return isDefaultTrue(value);
      }else if( this.codebook.get(value) != null && isDefaultTrue(this.codebook.get(value).stringValue()) ) {
         return true;
      }
      return false;
   }
   
   /**
    * 
    * @param value
    * @return
    */
   public boolean isFalse(String value) {
      if( this.codebook.size() == 0 ) {
         return isDefaultFalse(value);
      }else if( this.codebook.get(value) != null && isDefaultFalse(this.codebook.get(value).stringValue()) ) {
         return true;
      }
      return false;
   }
   
   /**
    * 
    * @param value
    * @return
    */
   protected boolean isDefaultTrue(String value) {
      return "yes".equalsIgnoreCase(value) || 
             "y".equalsIgnoreCase(value)   ||
             "1".equals(value)             || 
             "true".equalsIgnoreCase(value);
   }
   
   /**
    * 
    * @param value
    * @return
    */
   protected boolean isDefaultFalse(String value) {
      return "no".equalsIgnoreCase(value) || 
             "n".equalsIgnoreCase(value)   ||
             "0".equals(value)             || 
             "false".equalsIgnoreCase(value);
   }
   
   @Override
   public String toString() {
      return this.codebook.toString();
   }
   
   @Override
   public Value handleValue(String cellValue) {
   	if( cellValue.startsWith("xsd:boolean(") ) {
   		cellValue.replaceAll("^xsd:boolean\\(", "");
   		cellValue.replaceAll("\\)$", "");
   	}
   	if( isTrue(cellValue) ) {
   		return trueV;
   	}else if( isFalse(cellValue) ) {
   		return falseV;
   	}else {
   		return null;
   	}
   }
}