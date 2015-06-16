package edu.rpi.tw.data.csv.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;

import edu.rpi.tw.data.csv.Conversion;
import edu.rpi.tw.data.csv.EnhancementParameters;
import edu.rpi.tw.data.csv.ValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.BooleanValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.DateTimeValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.DateValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.DecimalMultiplierValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.EnhancedLiteralValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.IntegerMultiplierValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.LongMultiplierValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.ResourceValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.StringThrasherResourceValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.URLValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.VerbatimLiteralValueHandler;
import edu.rpi.tw.data.csv.valuehandlers.YearValueHandler;

/**
 * 
 */
public class ValueHandlerFactory {

   private static Logger logger = Logger.getLogger(ValueHandlerFactory.class.getName());
   
   private static String XSD = "http://www.w3.org/2001/XMLSchema#";
   protected EnhancementParameters eParams;
   
   private static DecimalMultiplierValueHandler decimalHandler = new DecimalMultiplierValueHandler();
   private static IntegerMultiplierValueHandler intHandler     = new IntegerMultiplierValueHandler();
   private static DateValueHandler              dateHandler    = new DateValueHandler();
   private static BooleanValueHandler           boolHandler    = new BooleanValueHandler(null);
   
   
   protected HashMap<URI, ValueHandler> year2015HandlersTopicsTemplates = new HashMap<URI, ValueHandler>();
   protected ValueHandler defaultyear2015HandlersTopicsTemplates = new VerbatimLiteralValueHandler();
   
   /**
    * 
    * @param enrichmentParamsRep
    */
   public ValueHandlerFactory(EnhancementParameters enrichmentParams) {
      this.eParams = enrichmentParams;
   }
   
   /**
    * 2015  https://github.com/timrdf/csv2rdf4lod-automation/wiki/conversion:TemplatedTopicsEnhancement for literals
    * @param handler
    * @return
    */
   public ValueHandler getValueHandler(URI handler) {
      
      if( Conversion.value.equals(handler) ) {
         if( ! year2015HandlersTopicsTemplates.containsKey(Conversion.value)) {
            year2015HandlersTopicsTemplates.put(Conversion.value, new VerbatimLiteralValueHandler());
         }
         return year2015HandlersTopicsTemplates.get(Conversion.value);
      }else {
         return defaultyear2015HandlersTopicsTemplates;
      }
   }
   
   /**
    * @param col - 1-based.
    * @return 
    */
   public ValueHandler getValueHandler(int col) {
      
      HashMap<String,Value>        codebook           = eParams.getCodebook(col);                 // CodebookQuerylet
      HashMap<String,HashSet<URI>> subjectSameAsLinks = eParams.getSubjectSameAsLinks(col);       // SubjectSameAsLinksQuerylet
      List<String>                 objectTemplates    = eParams.getObjectTemplates(col);          // ObjectTemplateQuerylet
      HashMap<String,        // conversion:regex                                                  // SubjectAnnotationViaObjectSearchQuerylet
              HashMap<Value, // conversion:predicate (templates)
                      Set<Value>>> subAnn             = eParams.getSubjectAnnotationsViaObjectSearches(col);

      //logger.finest("#getValueHandler: "+ columnIndex + " subAnn " + subAnn);
      //logger.fine("#getValueHandler: "+ (codebook == null) + " " + (subLinks == null) + " " + 
      //                                  (objectTemplates == null));
      logger.finest(col + " has " + subjectSameAsLinks.size() + " lodlinks");
      
      ValueHandler literalVH = codebook.size()       + subjectSameAsLinks.size() + 
                               objectTemplates.size() + subAnn.size() > 0 && !eParams.isChained(col)
                               ? new EnhancedLiteralValueHandler(codebook,
                                                                 subjectSameAsLinks, 
                                                                 eParams.lodLinkCaseInsensitive(col), 
                                                                 objectTemplates,subAnn) 
                               : new VerbatimLiteralValueHandler();
      
      ValueHandler handler = literalVH; // Is reassigned below based on enhancement parameters.
      
      URI    range  = eParams.getRange(col);
      String rangeS = range != null ? range.stringValue() : "";
      
      //System.err.println("getValueHandler("+columnIndex+"): "+range);
      
      Set<Double> multipliers = eParams.getMultipliers(col);
      double multiplier = 1;
      for( Double d : multipliers ) {
         multiplier *= d;
      }
      //System.err.println("MULT: @" +columnIndex+" "+multiplier);
      
      if( "g".equals(rangeS) ) {                                             // Latitude
         handler = literalVH;
         
      }else if( "G".equals(rangeS) ) {                                       // Longitude
         handler = literalVH;
          
      }else if( (XSD+"date").equals(rangeS) ) {                              // Date
         HashMap<String,URI> patterns  = eParams.getDatePatterns(col);
         handler = new DateValueHandler(patterns.keySet());
         
      }else if( (XSD+"dateTime").equals(rangeS) ) {                          // Date
         HashMap<String,URI> patterns = eParams.getDateTimePatterns(col);
         int    timezone = eParams.getDateTimeTimeZone(col);
         handler = new DateTimeValueHandler(patterns,timezone);
         
      }else if( (XSD+"gYear").equals(rangeS) ) {                             // Year
         handler = new YearValueHandler(eParams.getCodebook(col));
         
      }else if( (XSD+"boolean").toString().equals(rangeS) ) {                // Boolean
         handler = new BooleanValueHandler(eParams.getCodebook(col));
         
      }else if( (XSD+"integer").toString().equals(rangeS) ||
                (XSD+"nonNegativeInteger").equals(rangeS) ||
                (XSD+"positiveInteger").equals(rangeS)) {                    // Integer
         handler = new IntegerMultiplierValueHandler(multiplier, 
                                                     eParams.getCodebook(col),
                                                     rangeS);
      }else if( (XSD+"long").toString().equals(rangeS) ) {                   // Boolean
            handler = new LongMultiplierValueHandler(multiplier);
         
      }else if( (XSD+"decimal").toString().equals(rangeS) ) {                // Decimal
         handler = new DecimalMultiplierValueHandler(multiplier, 
                                                     eParams.getCodebook(col));
         
      }else if( (XSD+"float").toString().equals(rangeS) ) {                  // Float ## NOTE: types to decimal
         handler = new DecimalMultiplierValueHandler(multiplier, 
                                                     eParams.getCodebook(col),XSD+"float");
         
      }else if( (XSD+"double").toString().equals(rangeS) ) {                 // Double ## NOTE: types to decimal
         handler = new DecimalMultiplierValueHandler(multiplier, 
                                                     eParams.getCodebook(col),XSD+"double");

      }else if( (XSD+"anyURI").toString().equals(rangeS) ) {                 // URL
         handler = URLValueHandler.getInstance();
         // TODO: map this to ResourceValueHandler.
         
      }else if( "h".equals(rangeS) ) {                                       // chunk of HTML
         handler = literalVH;
         
      }else if( RDFS.LITERAL.stringValue().equals(rangeS) ) {                // Literal
         handler = literalVH;
      }else if( RDFS.RESOURCE.stringValue().equals(rangeS) ||
                        ResourceValueHandler.isURI(rangeS)) {                // Resource
         // Moved from first check to last to permit conversion:range foaf:Person
         // https://github.com/timrdf/csv2rdf4lod-automation/issues/326
         
         HashMap<String, Set<URI>> externalSuperClasses = new HashMap<String, Set<URI>>();
         for( String local : eParams.getExternalSuperClasses().keySet() ) {
            externalSuperClasses.put(local, eParams.getExternalSuperClasses().get(local));
         }
         
         if( !RDFS.RESOURCE.stringValue().equals(rangeS) ) {
            // https://github.com/timrdf/csv2rdf4lod-automation/issues/326
            Set<URI> types = new HashSet<URI>();
            types.add(ValueFactoryImpl.getInstance().createURI(rangeS));
            externalSuperClasses.put(null, types);
         }
         
         ResourceValueHandler rHandler = new ResourceValueHandler(
				eParams.isColumnURISafe(col),                   // URISafeQuerylet
				eParams.getCodebook(col),                       // CodebookQuerylet
				subjectSameAsLinks,                             // SubjectSameAsLinksQuerylet
				eParams.lodLinkCaseInsensitive(col),            // ObjectSameAsLinksQuerylet
				eParams.getNamespaceOfVocab(),                  // 
				eParams.getObjectTypeLocalName(col),            // ColumnIndexesQuerylet
				externalSuperClasses,                           // SubClassOfQuerylet
				eParams.getObjectTemplates(col),                // ObjectTemplateQuerylet
				eParams.getObjectLabelProperties(col),          // ObjectLabelPropertyQuerylet
				eParams.getObjectLabelTemplate(col),            // ObjectLabelTemplateQuerylet
				//eParams.getPromotionTemplate(columnIndex),
				eParams.getSubjectHumanRedirects(),             // HumanRedirectQuerylet
				
				eParams.getObjectSameAsLinks(col),              // ObjectSameAsLinksQuerylet
				eParams.getLODLinksRepository(col),             // ObjectSameAsLinksQuerylet
				eParams.lodLinkCaseInsensitive(col),            // ObjectSameAsLinksQuerylet
				eParams.referenceLODLinkedURIsDirectly(col),    // ObjectSameAsLinksQuerylet         https://github.com/timrdf/csv2rdf4lod-automation/issues/234
				eParams.getConstantAdditionalDescriptions(col), // ResourceAnnotationsQuerylet       <- instantiated by eParams, populated by CSVtoRDf#visitHeader
				subAnn,                                         // SubjectAnnotationViaObjectSearchQuerylet
				eParams.getInverses(col),                       // InverseOfQuerylet
				eParams.isColumnUnlabeled(col));                // UnlabeledQuerylet
         
         if( eParams.getInterpretWithRegexColumnsInChain().contains(col) ) {
         	rHandler = new StringThrasherResourceValueHandler(rHandler,
         																	  eParams.getInterpretWithRegexesInChain(col),
         																	  eParams.getObjectDelimiterInChain(col),
         																	  eParams.getEquivalentPropertyInChain(col));
         }
         if( eParams.isChained(col) ) {
         	handler.addChainedHandler(rHandler);
         }else {
         	handler = rHandler;
         }
      }

      // TODO: is this consistent with how Codebook was implemented?
      handler.setInterpretAsNulls(eParams.getInterpetAsNullStrings(col));
      
      //System.err.println(columnIndex + " " + handler.getRange().toString()+" " + multiplier);
      return handler;
   }
   
   /**
    * For cases where the value is a template with explicit casts, e.g. "xsd:decimal(3.14)".
    * This DOES NOT use the enhancement parameters, and they should be used if they can.
    * 
    * @param cellValue
    * @return
    */
   public static ValueHandler getValueHandler(String cellValue) {
      if( cellValue.startsWith("xsd:decimal(") ) {
         return decimalHandler;
      }else if( cellValue.startsWith("xsd:integer(") ) {
         return intHandler;
      }else if( cellValue.startsWith("xsd:date(") ) {
         return dateHandler;
      }else if( cellValue.startsWith("xsd:boolean(") ) {
         return boolHandler;
      }
      return null;
   }
}