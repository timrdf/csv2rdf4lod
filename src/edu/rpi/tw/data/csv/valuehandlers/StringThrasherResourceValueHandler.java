package edu.rpi.tw.data.csv.valuehandlers;

import java.util.HashMap;
import java.util.logging.Logger;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;

import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;
import edu.rpi.tw.string.pmm.DefaultPrefixMappings;
import edu.rpi.tw.string.pmm.PrefixMappings;

/**
 * 
 * This class is essentially a punt from trying to use the composite architecture of the converter.
 * https://github.com/timrdf/csv2rdf4lod-automation/issues/368
 *
 */
public class StringThrasherResourceValueHandler extends ResourceValueHandler {

   private static Logger logger = Logger.getLogger(ResourceValueHandler.class.getName());
   private PrefixMappings pmap = new DefaultPrefixMappings();
   
   protected HashMap<String,String> searchReplace = new HashMap<String,String>();
   protected String delimiter = null;
   protected Value  predicate;
   

   /**
    * 
    * @param idol
    * @param searchReplace - regex(es) to search, values to replace it with.
    * @param delimiter - delimit the value that was search/replaced.
    */
	public StringThrasherResourceValueHandler(ResourceValueHandler   idol,
														   HashMap<String,String> searchReplace,
														   String                 delimiter,
														   Value                  predicate) {
	   super(idol.valuesAreURISafe,
	   		idol.codebook,
	   		idol.subjectSameAsLinks,
	   		idol.subjectSameAsCaseInsensitive,
	   		idol.vocabNS,
	   		idol.type,
	   		idol.externalTypesMap,
	   		idol.objectTemplates, 
	   		idol.objectLabelProperties, 
	   		idol.objectLabelTemplate, 
	   		idol.humanRedirects,
	   		idol.objectSameAsLinks,
	   		idol.linksViaRepository,
	   		idol.objectSameAsCaseInsensitive,
	   		idol.objectSameAsDirectReference,
	   		idol.additionalObjectDescriptions,
	   		idol.triplesFromSearches,
	   		idol.inverses,
	   		idol.omitResourceLabels);
	   
	   if( searchReplace != null ) {
	   	this.searchReplace = searchReplace;
	   }
	   if( delimiter != null && delimiter.length() > 0 ) {
	   	this.delimiter = delimiter;
	   }else {

	   }
	   if( predicate instanceof URI ) {
	      this.predicate = predicate;
	   }else {
	      if( null != predicate ) {
	         logger.warning("Template predicates are not implemented for chained enhancements: "+predicate.stringValue());
	      }else {
	         logger.warning("Template predicates are not implemented for chained enhancements.");
	      }
	   }
   }
	
   @Override
   public void handleValue(Resource subjectR,                        // Subject
   								URI predicate, String predicateLocalName, // Predicate
   								String                  value,            // Object
                           RepositoryConnection    conn, 
                           String                  objectURIbase, 
                           CSVRecordTemplateFiller templateFiller,   // context
                           RepositoryConnection    ancilConn) {
   	
   	String replaced = value;
   	for( String regex : searchReplace.keySet() ) {                           // e.g. "^doi:"
   		logger.finer("THRASHER Should regex column " + predicateLocalName 
   				+ " value: ]"+value+
   				"[ regex: ]"+regex+
   				"[ replacement: ]"+searchReplace.get(regex)+"[");               // e.g. ""
   		replaced = replaced.replaceAll(regex, searchReplace.get(regex));
   		logger.finer("replaced value: ]"+replaced+"[");
   	}
   	URI p = this.predicate != null ? (URI) this.predicate : predicate;
   	// TODO: fill predicate template.
   	if( this.delimiter != null ) {
         String[] vals = replaced.split(delimiter);
         for( int val = 0; val < vals.length; val++ ) {
      		logger.finest("THRASHER deferring handling of SUB value to ResourceValueHandler, predicate "+p.stringValue() + " " + vals[val]);             
      		super.handleValue(subjectR, p, predicateLocalName, vals[val], conn, objectURIbase, templateFiller, ancilConn);
         }
   	}else {
   		logger.finest("THRASHER deferring of FULL value handling to ResourceValueHandler, predicate "+p.stringValue() + " " + replaced);
   		super.handleValue(subjectR, p, predicateLocalName, replaced, conn, objectURIbase, templateFiller, ancilConn);
   	}
   }
}