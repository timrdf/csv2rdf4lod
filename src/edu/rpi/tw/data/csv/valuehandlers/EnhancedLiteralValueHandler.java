package edu.rpi.tw.data.csv.valuehandlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.Conversion;
import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;
import edu.rpi.tw.data.rdf.sesame.vocabulary.metadata.AssertedTerms;
import edu.rpi.tw.string.NameFactory;
import edu.rpi.tw.string.pmm.DefaultPrefixMappings;
import edu.rpi.tw.string.pmm.PrefixMappings;

/**
 * Applies templates and codebook. Asserts sameAs links for subjectR.
 * 
 * Templates are applied, then the codebook. If no templates are provided, one identity template is used.
 */
public class EnhancedLiteralValueHandler extends VerbatimLiteralValueHandler {
   
   private static Logger logger = Logger.getLogger(EnhancedLiteralValueHandler.class.getName());
   
   protected static PrefixMappings pmap = new DefaultPrefixMappings();
   
   //protected HashMap<String,Value>                  codebook            = new HashMap<String,Value>(); // Removed 2011 Apr 26 b/c this is already in super class.
   protected HashMap<String,HashSet<URI>>           subLinks        = new HashMap<String,HashSet<URI>>();
   protected boolean                                caseInsensitive = false;
   protected Set<String>                            templates       = new HashSet<String>();
   
   /** for a given regex, what predicate-objects should be asserted?   (see SubjectAnnotationViaObjectSearchQuerylet) */
   protected HashMap<String,HashMap<Value,Set<Value>>> triplesFromSearches = new HashMap<String,HashMap<Value,Set<Value>>>();
   
   /**
    * 
    * @param codebook
    */
   public EnhancedLiteralValueHandler(HashMap<String,Value> codebook) {
      this(codebook, null, false, null, null);
   }  
   
   /**
    * 
    * @param codebook
    * @param subLinks
    * @param templates
    * @param triplesFromSearches
    */
   public EnhancedLiteralValueHandler(HashMap<String,Value>        codebook, 
                                      HashMap<String,HashSet<URI>> subLinks,
                                      boolean                      caseInsensitive,
                                      List<String>                 templates,
                                      HashMap<String, HashMap<Value,Set<Value>>> triplesFromSearches) {
      super();
      if( codebook  != null ) this.codebook  = codebook;

      if( subLinks != null ) {
         if ( caseInsensitive ) {
            this.caseInsensitive = true;
            this.subLinks = ResourceValueHandler.desensitizeCase(subLinks);
         }else { 
            this.subLinks = subLinks;
         }
      }
      if( templates != null && templates.size() > 0 ) {
         this.templates = new HashSet<String>(templates);
      }else {
         this.templates.add("[.]"); // Add the trivial template for just the value.
      }
      if( triplesFromSearches != null) {
      	this.triplesFromSearches = triplesFromSearches;
      }
      
      /*HashMap<Value,String> po = new HashMap<Value,String>(); 
      po.put(FOAF.topic,            "[\\1]");
      po.put(FOAF.isPrimaryTopicOf, "http://dbpedia.org/resource/[\\1]");
      po.put(FOAF.homepage,         "[/sd][\\1]");
      triplesFromSearches.put(".*(A[^ ]*C) .*", po);*/
   }
   
   /**
    * @param subjectR           - the URI of the subject of triples to assert for the given CSV cell value.
    * @param predicate          - the URI of the predicate of triples to assert for the given CSV cell value.
    * @param predicateLocalName - 
    * @param value              - the CSV cell value (might be a delimited segment of the full value if delimit_object)
    * @param primary               - the RepositoryConnection to assert the "main" triples.
    * @param resourceURIbase    - the namespace within which to name objects if need to promote them to URIs.
    * @param templateFiller     - something that populates templates with values from other CSV cells in this CSV row.
    * @param conn2              - the RepositoryConnection to assert the "ancillary" triples (this is done for readability when serializing).
    */
   @Override
   public void handleValue(Resource subjectR, URI predicate, String predicateLocalName, String value, 
                           RepositoryConnection conn,                                   String resourceURIbase, 
                           CSVRecordTemplateFiller templateFiller, RepositoryConnection conn2) {
      /**
       * value  - String from cell
       * tValue - Result of applying template
       * cValue - Result of applying codebook
       * 
       * ctValue - Result of applying codebook to Result of applying template.
       * tcValue - Result of applying template to Result of applying codebook.
       */
      
      logger.finest("         "+value+" (codebook)--> "+codebook.get(value));
      String cValue = codebook.containsKey(value) ? codebook.get(value).stringValue() : value;
      logger.finest("         "+value+" (codebook)--> "+cValue);

      /*This was used before trying to codebook "" to "N/A"
         if( value == null || value.length() < 1 || Conversion.NULL.equals(codebook.get(value))) {
         logger.finest("skipping null value.");
         return;
      }*/
      
      // If codebook maps the input value to some non-null, lengthy value, should proceed.
      if( Conversion.NULL_String.equals(cValue) || Conversion.NULL.equals(cValue) ) {
         logger.finest("skipping null value."); // TODO: should be calling this.interpretsAsNull(value).
         return;
      }

      


      
      
      
      // Filling templates, THEN applying codebook. 
      // INCONSISTENT with https://github.com/timrdf/csv2rdf4lod-automation/wiki/Order-of-operations-for-multiple-enhancements
      for( String template : this.templates ) {
         
         //String tValue = templateFiller.fillTemplate(template); // TODO: lose column after first? pushColumn call.
         // replace ^^ with \/ to handle delimits_object.
         String tValue = templateFiller.fillTemplate(template, cValue, CSVRecordTemplateFiller.AS_LITERAL);
         // delimits_object ",_"    with cell     "R._A._MARTIN,_J._C._WYNN,_G._A._ABRAMS"
         // value: R._A._MARTIN
         // value: J._C._WYNN
         // value: G._A._ABRAMS
         
         
         logger.finest("         "+value+" ("+template+")--> "+tValue);
         try {
            Value interpretation = this.codebook.get(tValue); // TODO: we're codebooking the template here - OPPOSITE of what we said before.
            // TODO: check if interpretation is a template that fills. If it is, assert that value instead.
            // (not implementing it now b/c there's no use case and it'll slow down processing.)
            // https://github.com/timrdf/csv2rdf4lod-automation/issues/346
            // See ResourceValueHandler's promotionApproach = "9" for how to implement it.
            if( interpretation != null ) {
            	//
            	// The templated value codebook'd to something.
            	//
            	
               //System.err.println("      interpretation: "+codebook.get(value));
            	
            	// TODO: Fill codebook'ed value as a template? (What if it becomes a URI? We're not a literal value handler any more...)
            	// ^^ https://github.com/timrdf/csv2rdf4lod-automation/issues/346
            	
               //primary.add(subjectR, predicate, this.resourceVH.promote(propertyLN, baseURI, value, null));
               
               conn.add(subjectR, predicate, interpretation);            // Not the tValue; it's interpretation
               
               // Link subject to an external resource using owl:sameAs
               if( this.subLinks.get(this.caseInsensitive ? interpretation.stringValue().toLowerCase() : interpretation) != null ) {
                  for( URI external : this.subLinks.get(this.caseInsensitive ? interpretation.stringValue().toLowerCase() : interpretation) ) {
                     conn.add(subjectR, OWL.SAMEAS, external);
                  }
               }
            }else {
            	//
            	// The templated value did not codebook to anything.
               //
            	conn.add(subjectR, predicate, vf.createLiteral(tValue)); // Just the tValue
               
               // Link subject to an external resource using owl:sameAs
               if( this.subLinks.get(this.caseInsensitive ? tValue.toLowerCase() : tValue) != null ) {
                  for( URI external : this.subLinks.get(this.caseInsensitive ? tValue.toLowerCase() : tValue) ) {
                     logger.finest(getClass().getCanonicalName() + " " + (this.caseInsensitive ? tValue.toLowerCase() : tValue) + "is in subLinks "+external);
                     conn.add(subjectR, OWL.SAMEAS, external);
                  }
               }else {
                  logger.finest(getClass().getCanonicalName() + " " + (this.caseInsensitive ? tValue.toLowerCase() : tValue) + " not in subLinks "+this.caseInsensitive);
               }
            }
            
            //
            // Regex the literal value and assert new POs on subjectR for each captured group.
            //
            AssertedTerms terms = assertObjectSearchDescriptions(subjectR, tValue, this.triplesFromSearches, templateFiller, conn); // TODO: OK to change to cellValue instead of templated?
            super.assertedPredicates.addAll(terms.getPredicates());
            super.assertedClasses.addAll(terms.getClasses());
            
         }catch (RepositoryException e) {
            e.printStackTrace();
         }  
      }
      
      handleChain(subjectR, predicate, predicateLocalName, value, conn, resourceURIbase, templateFiller, conn2);
   }
   
   /**
    * Look through the string object and assert some triples on the subject if matches are found.
    * 
    *  ".*A[^ ]* .*" -> <sioc:topic,"http://dbpedia.org/resource/[.]">
    *                   <foaf:topic,"[.]">
    *                   
    * This method is called by both 
    * EnhancedLiteralValueHandler#handleValue and 
    *        ResourceValueHandler#handleValue.
    * 
    * @param subjectR            - the subject URI to assert descriptions about.
    * @param cellValue           - templated
    * @param triplesFromSearches - map of regex -> <predicate, object> template to search/assert.
    * @param templateFiller      - fills the cell value based on tabular context.
    * @param conn                - the Sesame Repository connection to assert triples to.
    */
   public static AssertedTerms assertObjectSearchDescriptions(
   										    Resource                                  subjectR,
                                     String                                    cellValue,
                                     HashMap<String,HashMap<Value,Set<Value>>> triplesFromSearches,
                                     CSVRecordTemplateFiller                   templateFiller,
                                     RepositoryConnection                      conn) {
   	
   	AssertedTerms terms = new AssertedTerms();

   	// TODO: If conversion:range is xsd:decimal, DecimalMultiplierValueHandler is used and does not call this.
   	//    Same goes for any other range that doesn't lead to this class.
   	logger.finest("triplesFromSearches: " + cellValue + " " + triplesFromSearches);
	  
      for( String searchRegex : triplesFromSearches.keySet() ) {
         logger.finest("regex:   " + searchRegex);
         Pattern pattern = Pattern.compile(searchRegex);                                    // conversion:regex
         Matcher matcher = pattern.matcher(cellValue);
         while( matcher.find() ) {
            for( Value p : triplesFromSearches.get(searchRegex).keySet() ) {                // conversion:predicate
               for( Value searchOtemplate : triplesFromSearches.get(searchRegex).get(p) ) { // conversion:object
                  String oTemplate = searchOtemplate.stringValue();
                  try {
                     logger.finest("o template: " + searchOtemplate +" to be filled with " + matcher.groupCount() + " matches.");
                     for( int i = 1; i <= matcher.groupCount(); i++ ) {
                        logger.finest("o template captured group "+ i + ": "+ matcher.group(i));
                        logger.finest("o template: " + searchOtemplate);
                        // https://github.com/timrdf/csv2rdf4lod-automation/wiki/Using-template-variables-to-construct-new-values
                        oTemplate = oTemplate.replaceAll("\\[\\\\"   +i+"\\]",    matcher.group(i));                         // [.]
                        oTemplate = oTemplate.replaceAll("\\[_\\\\"  +i+"_\\]",   matcher.group(i).toLowerCase());           // [_._]
                        oTemplate = oTemplate.replaceAll("\\[\\^\\\\"+i+"\\^\\]", matcher.group(i).toUpperCase());           // [^.^]
                        logger.finest("[><]: ."+ matcher.group(i) + ". -> ." + NameFactory.trimChars(matcher.group(i)," ")+".");
                        oTemplate = oTemplate.replaceAll("\\[>\\\\"  +i+"\\<]",NameFactory.trimChars(matcher.group(i)," ")); // []
                        logger.finest("\\]\\"+i+"\\]");
                        logger.finest("o template: " + searchOtemplate + " expands: " + templateFiller.doesExpand(oTemplate));
                     }
                     Value o = templateFiller.tryExpand(oTemplate, searchOtemplate);
                     //System.err.println("expanded to " + o);
                     if( p instanceof URI ) {
                     	conn.add(subjectR, ((URI)p), o);
                     	terms.usedPredicate((URI)p);
                     	if( p.equals(RDF.TYPE) ) {
                     		terms.usedClass((URI)o);
                     	}
                     }else {
                     	//System.err.println("Applying object_search with templated predicate:" + p.stringValue());
                        URI pR = vf.createURI(templateFiller.fillTemplate(p.stringValue(),CSVRecordTemplateFiller.AS_RESOURCE));
                        conn.add(subjectR, pR, o);
                        terms.usedPredicate((URI)pR);
                     }
                  }catch( Exception e) {
                  	System.err.println("Object search error:");
                     System.err.println(":"+pmap.bestLocalNameFor(subjectR.stringValue()) + " :\"" + cellValue + "\"");
                     System.err.println("regex:            " + searchRegex);
                     System.err.println("predicate:        " + p.stringValue());
                     System.err.println("object:           " + triplesFromSearches.get(searchRegex).get(p));
                     System.err.println("templated object: " + oTemplate);
                     e.printStackTrace();
                  } // try
               } // for Otemplates (i.e. conversion:object)
            } // for predicates    (i.e. conversion:predicate)
         } // for regex matches against cell value.
      } // for searchRegex         (i.e. conversion:regex)
      return terms;
   }
   
}