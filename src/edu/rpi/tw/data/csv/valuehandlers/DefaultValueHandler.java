package edu.rpi.tw.data.csv.valuehandlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;

import edu.rpi.tw.data.csv.Conversion;
import edu.rpi.tw.data.csv.ValueHandler;
import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;
import edu.rpi.tw.string.pmm.DefaultPrefixMappings;
import edu.rpi.tw.string.pmm.PrefixMappings;

/**
 * 
 */
public abstract class DefaultValueHandler implements ValueHandler {
   
   private static Logger logger = Logger.getLogger(DefaultValueHandler.class.getName());
   
   protected static ValueFactory   vf             = ValueFactoryImpl.getInstance();
   private   static PrefixMappings pmap           = new DefaultPrefixMappings();
   protected static String         conversionNULL = Conversion.NULL.stringValue();
   
   //
   protected HashMap<String,Value> codebook        = new HashMap<String,Value>();
   protected Set<String>           interpretAsNull = null;

   // Error handling
   protected HashMap<String,Integer> badValues         = null;
   protected HashMap<URI,Integer>    errorPredicates   = new HashMap<URI,Integer>();
   protected HashMap<URI,Integer>    stumblePredicates = new HashMap<URI,Integer>();
   
   //
   protected Set<URI> assertedPredicates = new HashSet<URI>();
   protected Set<URI> assertedClasses    = new HashSet<URI>();
   
   protected Set<ValueHandler> chainedHandlers = new HashSet<ValueHandler>();
   
   /**
    * 
    */
   public DefaultValueHandler() {
      super();
      this.badValues = new HashMap<String,Integer>();
   }
   
   /**
    * @param codebook - 
    */
   public DefaultValueHandler(HashMap<String,Value> codebook) {
      this();
      this.codebook = codebook;
   }
   
   /**
    * 
    */
   @Override
   public void setInterpretAsNulls(Set<String> interpretAsNulls) {
      this.interpretAsNull = interpretAsNulls;
   }
   
   /**
    * 
    * @param value - value of cell that may become object of a triple.
    * @return true iff 'value' should not produce a triple.
    */
   @Override
   public boolean interpretsAsNull(String value) { 
      //logger.finest("value: " + value + " codebook size: "+codebook.size());
      if( codebook != null && codebook.containsKey(value) ) {
         value = codebook.get(value).stringValue();
         logger.finest("codebooked value: " + value);
      }
      //logger.finest("as null: " + (interpretAsNull != null && interpretAsNull.contains(value)));
      return conversionNULL.equals(value) || interpretAsNull != null && interpretAsNull.contains(value) || //;
             value == null; // added this 2011-Apr-20 to eliminate "blank cells"
   }
   
   /**
    * 
    * @param value
    * @param pattern
    * @param subjectR
    * @param predicate
    */
   protected void stumbledOnValue(String value, String pattern, Resource subjectR, URI predicate) {
      this.stumbledOnValue(value, pattern, subjectR, predicate, getClass().getSimpleName());
   }
   
   /**
    * 
    * @param value
    * @param pattern
    * @param subjectR
    * @param predicate
    */
   protected void stumbledOnValue(String value, String pattern, Resource subjectR, URI predicate, String handlerTag) {
      if( value != null && value.trim().length() > 0 ) {
         int count = 0;
         if( !this.stumblePredicates.containsKey(predicate) ) { // TODO: fails can't be seen after these.
            this.stumblePredicates.put(predicate, 1);
         }else {
            count = this.stumblePredicates.get(predicate);
            this.stumblePredicates.put(predicate, count+1);
         }
         if(count <= 5) 
         System.err.println("   "+handlerTag +" stumbled: \""+value+"\" !~ \""+pattern+"\" @ :"+
                                           pmap.bestLabelFor(subjectR.stringValue())+" :"+predicate.getLocalName());
      }
   }
   
   /**
    * 
    * @param value
    * @param numPatterns
    * @param subjectR
    * @param predicate
    * @param handlerTag
    */
   protected void failedOnValue(String value, Resource subjectR, URI predicate) {
      failedOnValue(value, 1, subjectR, predicate);
   }
   
   /**
    * 
    * @param value
    * @param numPatterns
    * @param subjectR
    * @param predicate
    */
   protected void failedOnValue(String value, int numPatterns, Resource subjectR, URI predicate) {
      failedOnValue(value, numPatterns, subjectR, predicate, getClass().getSimpleName());
   }
   
   /**
    * 
    * @param value
    * @param numPatterns
    * @param subjectR
    * @param predicate
    * @param handlerTag
    */
   protected void failedOnValue(String value, int numPatterns,
   									  Resource subjectR, URI predicate, String handlerTag) {
      if( value != null && value.trim().length() > 0 ) {
         int count = 0;
         if( !this.errorPredicates.containsKey(predicate) ) {
            this.errorPredicates.put(predicate, 1);
         }else {
            count = this.errorPredicates.get(predicate);
            this.errorPredicates.put(predicate, count+1);
         }
         if(count <= 5) 
         System.err.println(handlerTag +" FAILED: \""+value+"\" !~ "+numPatterns+" patterns @ :"+
               pmap.bestLocalNameFor(subjectR.stringValue())+" :"+predicate.getLocalName());
      }
   }
   
   /**
    * 
    * @param badValue
    * @param datatype
    * @param predicate
    */
   protected void gotBadValue(String badValue, String datatype, URI predicate) {
      if( !this.badValues.containsKey(badValue) ) {
         this.badValues.put(badValue, 1);
      }else {
         int count = this.badValues.get(badValue) + 1;
         this.badValues.put(badValue, count);
         if( count < 5 ) {
            System.err.println("\""+badValue+"\" is not friendly to "+datatype+" "+predicate.stringValue());
         }
      }
   }
   
   @Override
   /**
    * This was added for https://github.com/timrdf/csv2rdf4lod-automation/issues/279
    * Done only in DecimalValueHandler for now.
    */
   public Value handleValue(String cellValue) {
      System.err.println("ERROR: DefaultValueHandler's subclass did not define Value #handlValue(String)");
      return null;
   }
   
   @Override
   public void addChainedHandler(ValueHandler handler) {
   	this.chainedHandlers.add(handler);
   }
   
   /**
    * 
    * @param subjectR
    * @param predicate
    * @param predicateLocalName
    * @param value
    * @param conn
    * @param resourceURIbase
    * @param templateFiller
    * @param conn2
    */
   protected void handleChain(Resource subjectR, URI predicate, String predicateLocalName, String value, 
							         RepositoryConnection conn, String resourceURIbase, 
							         CSVRecordTemplateFiller templateFiller, RepositoryConnection conn2) {
   	
      // https://github.com/timrdf/csv2rdf4lod-automation/issues/368
      for( ValueHandler handler : chainedHandlers ) {
         handler.handleValue(subjectR, predicate, predicateLocalName, value, conn, resourceURIbase, templateFiller, conn2);
      }
   }
   
   @Override
   public Set<URI> getAssertedPredicates() {
   	return this.assertedPredicates;
   }
   
   @Override
   public Set<URI> getAssertedClasses() {
   	return this.assertedClasses;
   }
}