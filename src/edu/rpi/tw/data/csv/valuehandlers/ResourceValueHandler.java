package edu.rpi.tw.data.csv.valuehandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.Conversion;
import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;
import edu.rpi.tw.data.csv.querylets.column.ColumnEnhancementQuerylet;
import edu.rpi.tw.data.rdf.sesame.vocabulary.DCTerms;
import edu.rpi.tw.data.rdf.sesame.vocabulary.FOAF;
import edu.rpi.tw.data.rdf.sesame.vocabulary.PROVO;
import edu.rpi.tw.data.rdf.sesame.vocabulary.metadata.AssertedTerms;
import edu.rpi.tw.string.NameFactory;
import edu.rpi.tw.string.pmm.DefaultPrefixMappings;
import edu.rpi.tw.string.pmm.PrefixMappings;
//import org.apache.commons.validator.UrlValidator;

/**
 * Cast Resource Promotion     - if value has "://", then just case it as URI in its own right.
 * Typed Resource Promotion    - appends the type local name into the URI of the value.
 * Predicate-scoped Resource Promotion - 
 * Codebook Resource Promotion - use the value to look up another value that should be used (lookup is not from row).
 * Crutch Resource Promotion   - use additional values from row to construct URI within local namespace.
 * Template Resource Promotion - populate a URI template with values from row to construct an external URI namespace.
 * Default Resource Promotion  - uses "value" as the type.
 */
public class ResourceValueHandler extends DefaultValueHandler {

   public static String in = "      ";
 
   private static Logger logger = Logger.getLogger(ResourceValueHandler.class.getName());
   private PrefixMappings pmap = new DefaultPrefixMappings();
   
   //private static String[] validator_schemes = {"http","https"};
   //public  static UrlValidator uriValidator  = new UrlValidator(validator_schemes,UrlValidator.NO_FRAGMENTS);
   //public  static UrlValidator fragValidator = new UrlValidator(validator_schemes);

   protected String                                vocabNS                = null;   
   protected Set<URI>                              inverses               = new HashSet<URI>();
   
   /** The canonical template if others need to know. This is one of <code>objectTemplates</code>. */
   protected String                                objectTemplate         = null;
   protected String                                objectLabelTemplate    = null;
   
   /** When handling a value, many templates may be applied. */
   protected Set<String>                           objectTemplates        = null;
   protected Set<URI>                              objectLabelProperties  = new HashSet<URI>();
   
   protected boolean                               valuesAreURISafe       = false;
   
   // Typing the object
   protected String                                type                   = "value"; // Type label      (e.g. "FOIA Request")
   protected String                                typeLN                 = "value"; // Type local name (e.g. "FOAI_Request")
   protected URI                                   typeR                  = null;
   protected HashMap<String,Set<URI>>              externalTypesMap       = new HashMap<String,Set<URI>>();
   
   /**
    * The links via graph contains Resources that the conversion may reference according to the cell value
    * and assumption of inverse functionality of the value. While the `objectSameAsLinks` attribute allows
    * this direct lookup, the entire graph is needed to implement conversion:keys (the OWL key-like identity inference).
    */
   protected Repository                            linksViaRepository = null;
   
   protected HashMap<String,HashSet<URI>>          subjectSameAsLinks               = new HashMap<String,HashSet<URI>>();
   protected boolean                               subjectSameAsCaseInsensitive     = false;

   /**
    * "local value" -> { :external_uri_1, :extenral_uri_2 }
    */
   protected HashMap<String,HashSet<URI>>          objectSameAsLinks                = new HashMap<String,HashSet<URI>>();
   protected boolean                               objectSameAsCaseInsensitive      = false;
   protected boolean                               objectSameAsDirectReference      = false;
   protected Set<URI>                              objectSameAsTranscludeAttributes = new HashSet<URI>();
   protected int                                   numSameAsAssertions              = 0;
   
   protected HashMap<String,
   						HashMap<Value,Set<Value>>>    triplesFromSearches = new HashMap<String,HashMap<Value,Set<Value>>>();
   
   protected HashMap<URI,Set<Value>>               additionalObjectDescriptions = new HashMap<URI,Set<Value>>();

   protected Set<String>                           humanRedirects = new HashSet<String>();
   
   protected boolean omitResourceLabels = false;
   
   /**
    * A very-null ResourceValueHandler. Not recommended.
    */
   public ResourceValueHandler(String objectTemplate) {
   	this(false,null,null,false,null,null,null,listOf(objectTemplate),null,null,null,null,null,false,false,null,null,null,false);
   }
   
   /**
    * See {@link #ResourceValueHandler(boolean, HashMap, HashMap, boolean, String, String, HashMap, List, Set, String, Set, HashMap, boolean, boolean, HashMap, HashMap, Set, boolean)}
    */
   public ResourceValueHandler(String vocabNS, HashMap<String,Value> codebook) {
      this(false,codebook,null,false,vocabNS,null,null,null,null,null,null,null,null,false,false,null,null,null,false);
   }
   
   /**
    * @param valuesAreURISafe - if true, don't bother trying to make the string URI-friendly.
    * @param subjectSameAsLinks - <string,URI> to map value to a URI (this is conversion:links_via)
    * @param subjectSameAsCaseInsensitive - 
    * @param vocabNS - baseURI namespace where properties and classes should be defined.
    * @param type - the local name label of a rdfs:Class (spaces permitted). If non-null, uses TypedResourcePromotion. NOTE: This should never be a URI or template. use conv:subclass_of for that.
    * @param objectLabelTemplate - pattern to apply to produce a rdfs:label
    * @param codebook - mapping of csv cell values to a more meaningful value.
    * @param objectTemplates - set of templates that should be filled with this cell value - if none, applies the identity template.
    * @param humanRedirects - base URIs to map subjects into as foaf:primaryTopic (should avoid this - bad design).
    * 
    * @param externalTypesMap - URIs of classes to type subjects.
    * @param objectSameAsLinks - mapping of literal->URI to assert owl:sameAs of the objects.
    * @param objectSameAsCaseInsensitive - 
    * @param objectSameAsDirectReference - use external URI as object directly, instead of minting own object and same-as'ing to external URI.
    * @param additionalObjectDescriptions - Arbitrary attribute-values to assert on incoming subject.
    * @param triplesFromSearches - predicate-objectTemplates to assert on the subject based on searching the cell object value.
    */
   public ResourceValueHandler(boolean                                    valuesAreURISafe,
                               HashMap<String,Value>                      codebook,
                               HashMap<String,HashSet<URI>>               subjectSameAsLinks,
                               boolean                                    subjectSameAsCaseInsensitive,
                               String                                     vocabNS,
                               String                                     type, // NOTE: This should never be a URI or template. use conv:subclass_of for that.
                               HashMap<String,Set<URI>>                   externalTypesMap,
                               Collection<String>                         objectTemplates, 
                               Set<URI>                                   objectLabelProperties, 
                               String                                     objectLabelTemplate, 
                               Set<String>                                humanRedirects,
                               HashMap<String,HashSet<URI>>               objectSameAsLinks,
                               Repository                                 linksViaRep,
                               boolean                                    objectSameAsCaseInsensitive,
                               boolean                                    objectSameAsDirectReference,
                               HashMap<URI,Set<Value>>                    additionalObjectDescriptions,
                               HashMap<String, HashMap<Value,Set<Value>>> triplesFromSearches,
                               Set<URI>                                   inverses,
                               boolean                                    omitResourceLabels) {
      
      this.valuesAreURISafe = valuesAreURISafe;
      
      if( subjectSameAsLinks != null ) {
    	  this.subjectSameAsCaseInsensitive = subjectSameAsCaseInsensitive;
    	  if ( this.subjectSameAsCaseInsensitive ) {
            this.subjectSameAsLinks = desensitizeCase(subjectSameAsLinks);
         }
      }else {
         this.subjectSameAsLinks = subjectSameAsLinks;
      }
      
      this.vocabNS        = vocabNS;
      this.type           = "value";
      this.typeLN         = "value";
      if( type != null && type.length() > 0 ) {
         this.type   = type;
         this.typeLN = NameFactory.label2URI(type);
      }
      
      if( codebook != null ) this.codebook = codebook;
      
      ///////////////
      this.objectTemplates = new HashSet<String>(objectTemplates);
      if( this.objectTemplates.size() == 0 ) {
         this.objectTemplates.add(null); // null := Apply no object templates; use value as is. equivalent to "[.]"
      }
      // Pick one of the templates to be canonical.
      for( String template : objectTemplates ) {
         if( this.objectTemplate == null ) {
            this.objectTemplate = template;
         }else {
            System.err.println(ColumnEnhancementQuerylet.REPORT_INDENT + 
                               "WARNING: "+getClass().getSimpleName()+" "+"applying more than one object template. "+
                               "\""+this.objectTemplate+"\", will be used when bundling to it; "+
                               "\""+ template           +"\" will not.");
         }
      }
      if( objectLabelProperties != null && objectLabelProperties.size() > 0 ) {
      	this.objectLabelProperties = objectLabelProperties;
      }
      this.objectLabelTemplate = objectLabelTemplate;
      ///////////////
      
      
      if( humanRedirects   != null ) this.humanRedirects   = humanRedirects;
      if( externalTypesMap != null ) this.externalTypesMap = externalTypesMap;

      this.objectSameAsDirectReference = objectSameAsDirectReference;
      
      if( objectSameAsLinks != null ) {
         if( objectSameAsCaseInsensitive ) {
            this.objectSameAsCaseInsensitive = true;
            this.objectSameAsLinks = desensitizeCase(objectSameAsLinks);
         }else {
            this.objectSameAsLinks = objectSameAsLinks;
         }
      }
      
      
      if( additionalObjectDescriptions != null ) {
      	this.additionalObjectDescriptions = additionalObjectDescriptions;
      }
      if( triplesFromSearches          != null ) {
      	this.triplesFromSearches          = triplesFromSearches;
      }
      
      // TODO: EXPERIMENTAL
      objectSameAsTranscludeAttributes.add(vf.createURI("http://www.w3.org/2004/02/skos/core#altLabel"));
      objectSameAsTranscludeAttributes.add(vf.createURI("http://www.w3.org/2004/02/skos/core#prefLabel"));
      objectSameAsTranscludeAttributes.add(vf.createURI("http://xmlns.com/foaf/0.1/homepage"));
      
      if( inverses != null ) {
      	this.inverses = inverses;
      }
      
      this.omitResourceLabels = omitResourceLabels;
   }
   
   /**
    * 
    */
   @Override
   public URI getRange() {
      return RDFS.RESOURCE;
   }

   /**
    * @param subjectR - the subject that should be described with 'predicate' and 'value'.
    * @param predicate - the predicate that should be used to describe 'subject'.
    * @param predicateLocalName - the local name of 'predicate'. 
    *                             If 'predicate' is external, this is needed to predicate-scope promote the value.
    * @param value - the csv cell value that should become the object of <subjectR,predicate,O>.
    * @param conn - the repository to assert <subjectR,predicate,O>
    * @param objectURIbase - the baseURI namespace to prepend to the URIified 'value'.
    * @param templateFiller - takes a template and populates is with requested values from the current csv row.
    * @param ancilConn - the repository to assert anything other than <subjectR,predicate,O> (pretty serialization ordering)
    */
   @Override
   public void handleValue(Resource subjectR,                        // Subject
   								URI predicate, String predicateLocalName, // Predicate
   								String value,                             // Object
                           RepositoryConnection conn, 
                           String objectURIbase, CSVRecordTemplateFiller templateFiller, // context
                           RepositoryConnection ancilConn) {
      
      if( this.subjectSameAsLinks != null ) {
         
      }
      
      for( String template : this.objectTemplates ) {
         
      	logger.finest("RVH: value / template: " + value + " ----- " + template);
         // Value after applying Object Template
         String templatedValue = templateFiller.fillTemplate(template);
         
         // value == null || value.length() < 1 || 
         if( templatedValue == null || templatedValue.length() < 1 ) {
            //System.err.println("skipping <:"+pmap.bestLocalNameFor(subjectR.stringValue()) + " :" + predicateLocalName + " " + value + "> " +
            //                   " b/c templated value deficient: ."+templatedValue+".");
            this.failedOnValue(templatedValue, subjectR, predicate);
            return;
         }
         
         if( Conversion.NULL.equals(codebook.get(value))) {
            return;
         }
         
         // Resource Promotion (applies Range Template itself)
         Resource valueR = promote(predicateLocalName, objectURIbase, value, template, templateFiller);
         if( valueR == null ) {
            System.err.println("WARNING: " + this.getClass().getSimpleName() + " promoted value to null: "+
                               "<" + subjectR + ", " + predicateLocalName + ", " + value + "> "+
                               "@ " + templateFiller.fillTemplate("[r]") + "," + templateFiller.fillTemplate("[c]"));
         }
         
         if( valueR != null ) {
            logger.finer("value: \""+value + "\" templated: \""+ templatedValue +"\" ?o: \""+ valueR.stringValue()+"\"");
            try {
               if( subjectR  == null ) System.err.println("s "+subjectR);
               if( predicate == null ) System.err.println("   p "+predicate);
               if( valueR    == null ) System.err.println("       o "+valueR);
               
               // Resource Promotion

               conn.add(subjectR, predicate, valueR);                                               // ?row ?p ?internal
               for( URI inverse : inverses ) {
               	ancilConn.add(valueR, inverse, subjectR);
               	super.assertedPredicates.add(inverse);
               }
               /////////////////////////////////////////////////////////////////////////////////////
               if( this.objectSameAsDirectReference ) {
                  logger.finest("Asserting ?row ?col ?EXTERNAL - directly https://github.com/timrdf/csv2rdf4lod-automation/issues/234");
               	/*replaced with call to lodLinkDirect()
               	String squished = this.objectSameAsCaseInsensitive ? squishForIdentity(value) : value;
               	if( this.objectSameAsLinks.containsKey(squished) ) {
                     logger.finest("Asserting ?row ?col ?EXTERNAL - directly with 'value' "+squished);
                     for( URI externalResource : this.objectSameAsLinks.get(squished) ) {
                        conn.add(subjectR, predicate, externalResource);                            // ?row ?p ?EXTERNAL
                        for( URI inverse : inverses ) {
                        	ancilConn.add(externalResource, inverse, subjectR);
                        	super.assertedPredicates.add(inverse);
                        }
                        if( this.codebook.containsKey(value) ) {
                           ancilConn.add(externalResource, Conversion.symbol,         vf.createLiteral(value)); // Fills role of dc:identifier
                           ancilConn.add(externalResource, Conversion.interpretation, codebook.get(value));     // Fills role of dc:identifier
                        }
                     }
                  }else {
                  	logger.finest("did not lod-link value \""+squished+"\"");
                  }*/
               	lodLinkSubject(subjectR, predicate, value, conn, ancilConn);
               	
               	// DOING: consolidate this duplication! It is done twice here to the object and twice below with the object as subject.
               	
                  /* replaced with call to lodLinkDirect()
                  squished = this.objectSameAsCaseInsensitive ? squishForIdentity(templatedValue) : templatedValue;
                  if( this.objectSameAsLinks.containsKey(squished) ) {
                     logger.finest("Asserting ?row ?col ?EXTERNAL - directly with 'templatedValue' "+squished);
                     for( URI externalResource : this.objectSameAsLinks.get(squished) ) {
                        conn.add(subjectR, predicate, externalResource);                            // ?row ?p ?EXTERNAL
                        for( URI inverse : inverses ) {
                        	ancilConn.add(externalResource, inverse, subjectR);
                        	super.assertedPredicates.add(inverse);
                        }
                        if( this.codebook.containsKey(value) ) {
                           ancilConn.add(externalResource, Conversion.symbol,         vf.createLiteral(value)); // Fills role of dc:identifier
                           ancilConn.add(externalResource, Conversion.interpretation, codebook.get(value));     // Fills role of dc:identifier
                        }
                     }
                  }else {
                  	logger.finest("did not lod-link value \""+squished+"\"");
                  }*/
               	lodLinkSubject(subjectR, predicate, templatedValue, conn, ancilConn);
               	
                  if( this.codebook.containsKey(value) ) {
                     logger.finest("Asserting ?row ?col ?EXTERNAL - directly with 'codebooked value' "+value);
                  	lodLinkSubject(subjectR, predicate, this.codebook.get(value).stringValue(), conn,  ancilConn);
                  }
               }////////////////////////////////////////////////////////////////////////////////////
               
               
               
               
               // Decorate the URI object
               
            	// https://github.com/timrdf/csv2rdf4lod-automation/issues/353
               // https://github.com/timrdf/csv2rdf4lod-automation/issues/365
               String uriedSubValue = NameFactory.label2URI(value).toLowerCase();
               boolean valueRcontainsCellValue = template != null && template.indexOf("[.]") > 0  
               		                         || valueR != null && value != null && valueR.stringValue().toLowerCase().indexOf(uriedSubValue) > 0;
               // TODO: The string manipulations above really needs to call the same function that promote does.
               logger.finer("valueR: \""+ valueR.stringValue().toLowerCase()+ "\" contains uried value: \""+uriedSubValue + "\": "+valueRcontainsCellValue);
               
               
               if( !ResourceValueHandler.isURI(value) && valueRcontainsCellValue && !omitResourceLabels ) {
                  ancilConn.add(valueR,  DCTerms.identifier, vf.createLiteral(value)); // b/c the dataset creator meant to 
                                                                                       // refer to /something/, and this is 
                                                                                       // what they used. QED.
                  super.assertedPredicates.add(DCTerms.identifier);
                  //System.err.println("RVH: dct:id 1 "+value);
                  //logger.finest("RVH: dct:id 1 "+value);
               }
               // Typed Resource Promotion
               if( !this.typeLN.equals("value") ) { // TODO: if rdf:value is overridden (per Jim McC's request), this check will not trigger. 
                  if( typeR == null ) {
                     String templatedType = templateFiller.fillTemplate(type);
                     String typeURI       = templatedType.indexOf("://") > 0 ? templatedType : this.vocabNS+typeLN;
                     this.typeR           = vf.createURI(typeURI);
                  }
                  ancilConn.add(valueR, RDF.TYPE, typeR);
                  super.assertedPredicates.add(RDF.TYPE);
                  super.assertedClasses.add(typeR);
                  
                  // Add additional type if subclass enhancement present.
                  if( this.externalTypesMap.get(type) != null ) {
                     for( URI externalClassR : this.externalTypesMap.get(type) ) {
                        ancilConn.add(valueR, RDF.TYPE, externalClassR);
                        super.assertedPredicates.add(RDF.TYPE);
                        super.assertedClasses.add(externalClassR);
                     }
                  }
               }
               // Just type the thing...
               if( this.externalTypesMap.containsKey(null) ) {
               	// https://github.com/timrdf/csv2rdf4lod-automation/issues/326
                  for( URI externalClassR : this.externalTypesMap.get(null) ) {
                     ancilConn.add(valueR, RDF.TYPE, externalClassR);
                     super.assertedPredicates.add(RDF.TYPE);
                     super.assertedClasses.add(externalClassR);
                  }
               }
               
   
               // Codebook Resource Promotion
               if( this.codebook.containsKey(value) ) {
                  logger.finest("codebook promotion");
                  
                  // https://github.com/timrdf/csv2rdf4lod-automation/issues/353
                  valueRcontainsCellValue = valueR.stringValue().toLowerCase().indexOf(codebook.get(value).stringValue().toLowerCase()) > 0;
                  logger.finer("valueR: \""+ valueR.stringValue()+ "\" contains value: \""+codebook.get(value).stringValue().toLowerCase() + "\": "+valueRcontainsCellValue);
                  
                  ancilConn.add(valueR, Conversion.symbol,         vf.createLiteral(value)); // Fills role of dc:identifier
                  ancilConn.add(valueR, Conversion.interpretation, codebook.get(value));     // Fills role of dc:identifier
                  super.assertedPredicates.add(Conversion.symbol);
                  super.assertedPredicates.add(Conversion.interpretation);
                  if( !startsLikeURI(codebook.get(value).stringValue()) ) {
                  	if( !omitResourceLabels && valueRcontainsCellValue ) {
	                     ancilConn.add(valueR, RDFS.LABEL,             codebook.get(value));
	                     super.assertedPredicates.add(RDFS.LABEL);
                  	}else {
                  		logger.finest("not labeling codebooked "+valueR+" b/c does not start like URI");
                  	}
                     for( URI labelP : this.objectLabelProperties ) {
                     	ancilConn.add(valueR, labelP,              codebook.get(value));
                        super.assertedPredicates.add(labelP);
                     }
                  }
               }else if( templatedValue.indexOf("://") < 0 ) {
                  logger.finest("template didn't produce a URI-looking thing.");
                  if( templateFiller.doesExpand(value) ) {
                     logger.finest("template expanded");
                  	if( !omitResourceLabels && valueRcontainsCellValue ) {
                  		ancilConn.add(valueR, RDFS.LABEL,      vf.createLiteral(templatedValue)); 
                        super.assertedPredicates.add(RDFS.LABEL);
                  	}else {
                  		logger.finest("not labeling templated filled "+valueR+" b/c templated omitLabels or valueRcontainsCellValue");
                  	}
                  	if( valueRcontainsCellValue ) {
                  		ancilConn.add(valueR, DCTerms.identifier, vf.createLiteral(templatedValue));
                  		//System.err.println("RVH: dct:id 2 "+value);
                        super.assertedPredicates.add(DCTerms.identifier);
                     }

                     for( URI labelP : this.objectLabelProperties ) {
                     	ancilConn.add(valueR, labelP,          vf.createLiteral(templatedValue));
                        super.assertedPredicates.add(labelP);
                     }
                  }else {
                     logger.finest("template not expanded");
                     // May 2011, switched from templatedValue to just value:
                     //
                     // urn:org.iodp:exp:113;urn:org.iodp:exp:114;urn:org.iodp:exp:119;
                     // delimits_object ";"
                     // urn:org.iodp:exp:114

                  	if( !omitResourceLabels && valueRcontainsCellValue ) {
	                     ancilConn.add(valueR, RDFS.LABEL,        vf.createLiteral(value));
	                     super.assertedPredicates.add(RDFS.LABEL);
                  	}else {
                  		logger.finest("not labeling "+valueR+" b/c 'else' omitLabels or !valueContainsCellValue.");
                  	}
                  	if( valueRcontainsCellValue ) {
	                     ancilConn.add(valueR, DCTerms.identifier, vf.createLiteral(value));
	                     //System.err.println("RVH: dct:id 3 "+value);
	                     super.assertedPredicates.add(DCTerms.identifier);
                  	}
                     for( URI labelP : this.objectLabelProperties ) {
                     	ancilConn.add(valueR, labelP,          vf.createLiteral(value));
                        super.assertedPredicates.add(labelP);
                     }
                  }
                  //logger.finest("RVH: dct:id 2 "+value);
               }else {
                  logger.finest("OTHER: "+ value + " wasn't in codebook and templatedValue " + templatedValue + " had ://");
               }
               
               String templatedLabel = templateFiller.fillTemplate(this.objectLabelTemplate); // TODO: why was this asResource?
               //if( templatedLabel != null && !ResourceValueHandler.isURI(templatedLabel) && templateFiller.doesExpand(value) ) {
               //   conn2.add(valueR, RDFS.LABEL, vf.createLiteral(templatedLabel));
               //}
               logger.finest("Templated label: |" + this.objectLabelTemplate+ "|->|"+templatedLabel +"| equal:"+templatedLabel.equals(this.objectLabelTemplate));
               if( this.objectLabelTemplate != null && 
                   templatedLabel != null && !ResourceValueHandler.startsLikeURI(templatedLabel) &&
                  !templatedLabel.equals(this.objectLabelTemplate) ) {
               	
                  logger.finest("will S LABEL: |" + templatedLabel +"||"+ this.objectLabelTemplate+"|");
               	if( !omitResourceLabels && valueRcontainsCellValue ) {
	                  ancilConn.add(valueR, RDFS.LABEL,  vf.createLiteral(templatedLabel)); // this causes: "\\s*"\
	                  super.assertedPredicates.add(RDFS.LABEL);
               	}else {
               		logger.finest("not labeling "+valueR+" b/c #4 !omitResourceLabels="+!omitResourceLabels+" and valueRcontainsCellValue="+valueRcontainsCellValue);
               	}
                  for( URI labelP : this.objectLabelProperties ) {
                  	ancilConn.add(valueR, labelP,  vf.createLiteral(templatedLabel));
                     super.assertedPredicates.add(labelP);
                  }
               }
               
               // Link URI to human page.
               for( String baseURI : this.humanRedirects ) {
                  ancilConn.add(vf.createURI(baseURI+subjectR), FOAF.primaryTopic, subjectR);
                  super.assertedPredicates.add(FOAF.primaryTopic);
               }
               
               /////////////////////////////////////////////////////////////////////////////////////
               // Object SameAs Promotion (Indirect - using internal URI and making one link out)
               /*replaced with call to lodLink()         /////DEPRECATED\\\\\\
               if( this.objectSameAsLinks.containsKey(this.objectSameAsCaseInsensitive ? value.toLowerCase() : value) ) {
                  for( URI externalResource : this.objectSameAsLinks.get(this.objectSameAsCaseInsensitive ? squishForIdentity(value) : value) ) { // TODO: might sameAs to something too short - should add templated versions to lod-link file.
                     //System.err.println(in+in+"links_via: " + value + " " + externalResource + " of " + this.objectSameAsLinks.size()+"\r");
                     ancilConn.add(valueR, OWL.SAMEAS, externalResource);
                     String domain = NameFactory.uriDomain(externalResource.stringValue());
                     if( domain != null ) {
                     	ancilConn.add(externalResource, PROVO.wasAttributedTo, vf.createURI(domain));
                     }
                     super.assertedPredicates.add(OWL.SAMEAS);
                     // Link row/cell URI to dataset
                     // TODO: primary.add(subjectR, DCTerms.isReferencedBy, this.versionedDatasetR);
                     this.numSameAsAssertions ++;                                   // TODO: double counting if value == templated value.
                     if( this.codebook.containsKey(value) ) {
                        ancilConn.add(externalResource, Conversion.symbol,         vf.createLiteral(value)); // Fills role of dc:identifier
                        ancilConn.add(externalResource, Conversion.interpretation, codebook.get(value));     // Fills role of dc:identifier
                     }
                  }
               }                     ^^^^^DEPRECATED^^^^^^
               */
               lodLinkObject(valueR, value, ancilConn);
               
				   // DOING: consolidate this duplication! It is done twice here with the object as subject and twice above to the object.
               
               /*replaced with call to lodLink()     /////DEPRECATED\\\\\\
               if( this.objectSameAsLinks.containsKey(this.objectSameAsCaseInsensitive ? squishForIdentity(templatedValue) : templatedValue) ) {
                  for( URI externalResource : this.objectSameAsLinks.get(this.objectSameAsCaseInsensitive ? squishForIdentity(templatedValue) : templatedValue) ) {
                     ancilConn.add(valueR, OWL.SAMEAS, externalResource);
                     String domain = NameFactory.uriDomain(externalResource.stringValue());
                     if( domain != null ) {
                     	ancilConn.add(externalResource, PROVO.wasAttributedTo, vf.createURI(domain));
                     }
                     super.assertedPredicates.add(OWL.SAMEAS);
                     // Link row/cell URI to dataset
                     // TODO: primary.add(subjectR, DCTerms.isReferencedBy, this.versionedDatasetR);
                     this.numSameAsAssertions ++;
                     if( this.codebook.containsKey(value) ) {
                        ancilConn.add(externalResource, Conversion.symbol,         vf.createLiteral(value)); // Fills role of dc:identifier
                        ancilConn.add(externalResource, Conversion.interpretation, codebook.get(value));     // Fills role of dc:identifier
                     }
                  }
               }                                       ^^^^^DEPRECATED^^^^^^
               */
               lodLinkObject(valueR, templatedValue, ancilConn);
               
               if( this.codebook.containsKey(value) ) {
                  logger.finest("Asserting ?row ?col ?EXTERNAL - directly with 'codebooked value' "+value);
               	lodLinkObject(valueR,this.codebook.get(value).stringValue(),  ancilConn);
               }
               ////////////////////////////////////////////////////////////////////////////////////
               
               // Additional Descriptions
               for( URI additionalPredicate : additionalObjectDescriptions.keySet() ) {
                  for( Value additionalObject : additionalObjectDescriptions.get(additionalPredicate) ) {
                     //System.err.println("    describing o with "+additionalPredicate+" = "+additionalObject);
                     ancilConn.add(valueR, additionalPredicate, templateFiller.tryExpand(additionalObject));
                     super.assertedPredicates.add(additionalPredicate);
                     if( additionalPredicate.equals(RDF.TYPE) ) {
                     	super.assertedClasses.add((URI)templateFiller.tryExpand(additionalObject));
                     }
                  }
               }
               
               if( this.triplesFromSearches != null && this.triplesFromSearches.size() > 0 ) {
               	// NOTE: somewhat inconsistent:
               	//   EnhancedLiteralValueHandler passes: templated value
               	//   ResourceValueHandler        passes: cellValue. 
                  AssertedTerms terms = EnhancedLiteralValueHandler.assertObjectSearchDescriptions(
                  		 															  subjectR, value, 
                  		 														 	  triplesFromSearches, 
                  		 															  templateFiller, conn); // 2012 Jan changed from conn2);
                  super.assertedPredicates.addAll(terms.getPredicates());
                  super.assertedClasses.addAll(terms.getClasses());
                  // TODO: do we need to assert inverses for this?
               }
            } catch (RepositoryException e) {
               e.printStackTrace();
            }   
         }else { // There is no value
            this.failedOnValue(value+" -pattern-> "+templatedValue, subjectR, predicate);
         }
      }
   }
   
   
   //
   // Resource promotion
   //
   
   
   /**
    * Uses own object template.
    * 
    * @param propertyLN
    * @param baseURI
    * @param cellValue
    * @param templateFiller
    * @return
    */
   public URI promote(String propertyLN, String baseURI, String cellValue,
   						 CSVRecordTemplateFiller templateFiller) {
      return this.promote(propertyLN, baseURI, cellValue, this.objectTemplate, templateFiller);
   }
   
   /**
    * @param propertyLN     - local name of the property that will be pointing to the promoted Resource.
    * @param baseURI        - the URI namespace to prepend to the local name of the promoted Resource.
    * @param cellValue      - a string to turn into a local name for the promoted Resource.
    * @param template       - object template to apply.
    * @param templateFiller - fills templates with the requested values from the csv row that 'value' came from.
    * 
    * if conversion:delimits_object is used to parse a cell value into multiple values, the `cellValue` will be the 
    * substring of just one of the tokens (then this method is called for each token).
    * 
    * @return a promoted version of 'value'.
    */
   private URI promote(String propertyLN, String baseURI, String cellValue, String template,
   						  CSVRecordTemplateFiller templateFiller) {
      
      String uri = null;
      
      logger.finer(getClass().getSimpleName()+"#promote: \""+cellValue+"\"");
      
      String codebookedValue = this.codebook.containsKey(cellValue) 
      		                 ? this.codebook.get(cellValue).stringValue() : cellValue;
      // TODO: does codebook take affect through template? templateFiller has codebook (but can't handle delimited values from a single column value).
      logger.finer("cell value after codebook: \"" + codebookedValue+ "\""  +
                                      " a URI: "   + isURI(codebookedValue) + 
                                      " hackURI: " + (codebookedValue.indexOf("://") > 0));

      String templatedCodebookedValue = cellValue; String tApproach = "0";
      if( this.codebook.containsKey(cellValue) && // Added this 'contains' condition to handle range_template "[/]instance/external/md5([#7])" with #7 a legit [external] URI.
          (isURI(codebookedValue) || codebookedValue != null && codebookedValue.indexOf("://") > 0) ) { // TODO: why is apache commons validator returning false?
         logger.finest("tApproach 1 " + isURI(codebookedValue) + " " + (codebookedValue.indexOf("://") > 0) + " cellValue " + cellValue + " codebookedValue " + codebookedValue + " .... templatedCodebookValue " + templatedCodebookedValue);
         templatedCodebookedValue = codebookedValue; 
         // e.g., codebooking "U._S._GEOLOGICAL_SURVEY_(USGS)" -> http://dbpedia.org/resource/USGS
         tApproach = "1";
      }else if( this.codebook.containsKey(cellValue) ) { // TODO: why is codebook a condition on delimits_object?!
         // Let template filler use the entire cell value (since it was not object_delimited).
         templatedCodebookedValue = templateFiller.fillTemplate(template,            CSVRecordTemplateFiller.AS_RESOURCE);
         // breaks on Xian's "APPL,GOOG" with conv:range "[/]stock/[.]" (makes :APPL_GOOG)
         tApproach = "2";
      }else if( template != null ) {
         // Tell template filler to use the __delimited portion__ of the entire cell value (b/c it was delimited).
         // Template filler does NOT know about delimits_object (can can't/shouldn't)
         // If we don't provide the delimited portion to the template filler, it will use the entire cell value as
         // the URI for every parsed component of the full cell value.
         templatedCodebookedValue = templateFiller.fillTemplate(template, cellValue, CSVRecordTemplateFiller.AS_RESOURCE);
         // e.g. Xian's "APPL,GOOG" with delimiter "," and conv:range "[/]stock/[.]" needs to become
         //      :AAPL and :GOOG (not :APPL_GOOG)
         tApproach = "4";
         
         if( this.valuesAreURISafe ) {
            // Added condition Mar 2015 to suit "http://[.]" with value "123.1.1.2"
            templatedCodebookedValue = templateFiller.fillTemplate(template, cellValue, CSVRecordTemplateFiller.AS_LITERAL);
            tApproach = "4";
         }
      }
      // during Gino's bug  ? templateFiller.fillTemplate(this.rangeTemplate, CSVRecordTemplateFiller.AS_RESOURCE)
 
      logger.finer("templated: \"" + templatedCodebookedValue+"\" (used \""+template+"\" "+tApproach+"); templated fills: "+templateFiller.doesExpand(templatedCodebookedValue));
                              
      
      String valueLocalName = codebook.containsKey(cellValue) 
      		                ? NameFactory.label2URI(codebook.get(cellValue).stringValue()) 
                            : NameFactory.label2URI(templatedCodebookedValue);
      
      logger.finer("templatedCodebook codebook fills: "+templateFiller.doesExpand(  codebook.containsKey(templatedCodebookedValue) 
                                                                                  ? codebook.get(templatedCodebookedValue).stringValue() : null));
      		                
      		                
      String codebookedTemplatedCodebookValue =   codebook.containsKey(templatedCodebookedValue) 
      													   ? codebook.get(templatedCodebookedValue).stringValue()
      													   : null;
      String codebookedTemplatedCodebookValueFilled =   codebookedTemplatedCodebookValue != null 
      														      ? templateFiller.fillTemplate(codebookedTemplatedCodebookValue) 
      														      : null;
      														      
      logger.finest("TEMPLATE IT! " + templateFiller.fillTemplate(template,            CSVRecordTemplateFiller.AS_LITERAL)); //templateFiller.tryExpand(template));
	    
      String promotionApproach = "?";
      if( valueLocalName != null && valueLocalName.length() > 0 ) { // TODO: This could reasonable be an empty string [.]AMentor (not="")
         if( isURI(templatedCodebookedValue) ) {
            // Added this condition May 2014 to handle range_template "[/]instance/external/md5([#7])" with #7 a legit [external] URI.
            // This is the first time that we get a URI as input and want to use a different URI...
            // In this first occurrence of a use case, we want to mint a local prov:specializationOf the given external URI.
            uri = templatedCodebookedValue;
            // NOTE: This promotes this condition from FOURTH check to FIRST. How much did we break?
         }else if( isURI(cellValue) ) {
            /* Resource Cast Promotion (value is already a URI; just use it).*/
            uri = cellValue;                                                                    promotionApproach = "1";
         }else if( startsLikeNonHTTPURI(templatedCodebookedValue) ) {
            /* Resource Cast Promotion (templated value became a URI; just use it). */          promotionApproach = "2";
            uri = templateFiller.fillTemplate(template, codebookedValue,  CSVRecordTemplateFiller.AS_LITERAL);         
         }else if ( isURI(templatedCodebookedValue) && this.valuesAreURISafe ) {
            // https://github.com/timrdf/csv2rdf4lod-automation/issues/281
            uri = templateFiller.fillTemplate(template, cellValue, CSVRecordTemplateFiller.AS_LITERAL);
            if( uri.indexOf(" ") > -1 ) {
               // We were lied to!
               uri = uri.replaceAll(" ", "%20");
            }
            if( uri.indexOf("\n") > -1 ) {
               // We were lied to!
               uri = uri.replaceAll("\n", "%0");
            }
         }else if( isURI(templatedCodebookedValue) ) {
            /* Resource Cast Promotion (templated value became a URI; just use it). */          promotionApproach = "3";
            uri = templatedCodebookedValue;
            // HUH? Replaced by above: System.err.println("Alt 3: " + templateFiller.fillTemplate(template, valueLocalName, CSVRecordTemplateFiller.AS_RESOURCE));
         }else if( isURI(codebookedValue) || codebookedValue != null && codebookedValue.matches("^http.://") ) { // TODO: template vs codebook - not eloquent!
            /* Resource Cast Promotion (templated value became a URI; just use it). */          promotionApproach = "4";
            uri = codebookedValue;
         }else if( template != null && isURI(template) ) {        
            /* Template Resource External Promotion */                                          promotionApproach = "5";
            uri = templateFiller.fillTemplate(template, valueLocalName, CSVRecordTemplateFiller.AS_RESOURCE);
         }else if( !this.typeLN.equals("value") ) {
            /* Typed Resource Promotion */
            uri = baseURI + "typed/"  + typeLN.toLowerCase() + "/" + valueLocalName;            promotionApproach = "6";
         }else if (codebookedTemplatedCodebookValue       != null && 
         		    codebookedTemplatedCodebookValueFilled != null && 
         		    templateFiller.doesExpand(codebookedTemplatedCodebookValue) && 
         		    isURI(codebookedTemplatedCodebookValueFilled) ) {
         	/* */                                                                               promotionApproach = "9";
         	uri = codebookedTemplatedCodebookValueFilled;
         }else if ( true ) {
            /* Predicate-scoped Resource Promotion */
            uri = baseURI + "value-of/" + propertyLN + "/" + valueLocalName;                    promotionApproach = "7";
         }else {
            /* Global scope Resource Promotion NOTE: Never used. */
            uri = baseURI + valueLocalName;                                                     promotionApproach = "8";
         }
      }
      //logger.finer
      //System.err.println("promoted "+cellValue+" (--pattern-->"+templatedValue+") to "+uri+" (using "+baseURI+" and "+propertyLN+") - used "+promotionApproach);
      logger.finer("promoted "+cellValue+" (--pattern-->"+templatedCodebookedValue+") to "+uri+
                   " (using "+baseURI+" and "+propertyLN+") - used tApproach "+tApproach + " and proApproach " + promotionApproach);
      
      try {
         URI ret = uri != null && uri.length() > 0 ? vf.createURI(uri) : null;
         return ret;
      }catch (java.lang.IllegalArgumentException e) {
         return null;
      }
      //return uri != null && uri.length() > 0 ? vf.createURI(uri) : null;
   }
   
   //
   // LOD-linking
   //
   
   /**
    * Use 'value' to look up the URIs that should be owl:sameAs the resource promotion of 'value',
    * and relate 'subjectR' directly to these URIs for the triple derived from the cell value.
    * 
    * 'value' may be made case insensitive within this method.
    * 
    * @param subjectR  - The subject of the triple, which should be linked directly to an external resource.
    * @param predicate - The predicate of the triple to assert directly to an external resource.
    * @param value     - The literal value that should be used to link (similar to inverse functional property) (could be the cell value, the range-templated value, or the codebooked value).
    * @param conn      - Repository to describe 'subjectR'.
    * @param ancilConn - Repository to describe the external resource.
    * @throws RepositoryException
    */
   private void lodLinkSubject(Resource subjectR, URI predicate, String value,
   									 RepositoryConnection conn, RepositoryConnection ancilConn)
   									 throws RepositoryException {
   	
   	String squished = this.objectSameAsCaseInsensitive ? squishForIdentity(value) : value;
   	if( this.objectSameAsLinks.containsKey(squished) ) {
         for( URI externalResource : this.objectSameAsLinks.get(squished) ) {
            conn.add(subjectR, predicate, externalResource);                            // ?row ?p ?EXTERNAL
            for( URI inverse : inverses ) {
            	ancilConn.add(externalResource, inverse, subjectR);
            	super.assertedPredicates.add(inverse);
            }
            if( this.codebook.containsKey(value) ) {
               ancilConn.add(externalResource, Conversion.symbol,         vf.createLiteral(value)); // Fills role of dc:identifier
               ancilConn.add(externalResource, Conversion.interpretation, codebook.get(value));     // Fills role of dc:identifier
            }
         }
      }else {
      	logger.finest("did not lod-link (direct) value \""+squished+"\"");
      }
   }
   
   /**
    * Unlike {@link #lodLinkSubject(Resource, URI, String, RepositoryConnection, RepositoryConnection)},
    * a predicate does not need to be specified here since we are only processing the object and trying
    * to assert owl:sameAs between 'valueR' and some external resources.
    * 
    * @param valueR - The "internal" resource that was created during conversion that should be owl:sameAs some "external" resources according to 'value'.
    * @param value - The literal value that should be used to link (similar to inverse functional property) (could be the cell value, the range-templated value, or the codebooked value).
    * @param conn
    * @param ancilConn
    * @throws RepositoryException
    */
   private void lodLinkObject(Resource valueR, String value, RepositoryConnection ancilConn)
   						         throws RepositoryException {
   	
   	String squished = this.objectSameAsCaseInsensitive ? value.toLowerCase() : value;
   	if( this.objectSameAsLinks.containsKey(squished) ) {
         for( URI externalResource : this.objectSameAsLinks.get(squished) ) { // TODO: might sameAs to something too short - should add templated versions to lod-link file.
            //System.err.println(in+in+"links_via: " + value + " " + externalResource + " of " + this.objectSameAsLinks.size()+"\r");
            ancilConn.add(valueR, OWL.SAMEAS, externalResource);
            String domain = NameFactory.uriDomain(externalResource.stringValue());
            if( domain != null ) {
            	ancilConn.add(externalResource, PROVO.wasAttributedTo, vf.createURI(domain));
            }
            super.assertedPredicates.add(OWL.SAMEAS);
            // Link row/cell URI to dataset
            // TODO: primary.add(subjectR, DCTerms.isReferencedBy, this.versionedDatasetR);
            this.numSameAsAssertions ++;                                   // TODO: double counting if value == templated value.
            if( this.codebook.containsKey(value) ) {
               ancilConn.add(externalResource, Conversion.symbol,         vf.createLiteral(value)); // Fills role of dc:identifier
               ancilConn.add(externalResource, Conversion.interpretation, codebook.get(value));     // Fills role of dc:identifier
            }
         }
      }else {
      	logger.finest("did not lod-link value \""+squished+"\"");
      }
   }
   
   /**
    * 
    * @return number of objects that were promoted.
    */
   public int numSameAsAssertions() {
      return this.numSameAsAssertions;
   }
   
   
   //
   // Static string manipulations
   //
   
   
   /**
    * NOTE: this accepts URIs that contain '[]', so this should not be used outside of csv2rdf4lod.
    * 
    * @return
    */
   public static boolean isURI(String string) {
      // Before adding apache-commons validator: return string != null && string.indexOf("://") > 0 || isNonHTTPURI(string);
      // apache-commons validator does not work: return uriValidator.isValid(null) || isNonHTTPURI(string);
      return string != null            && 
            (string.indexOf("://") > 0 && 
             string.indexOf(" ")   < 0 &&
             string.indexOf("[")   < 0 || 
             isNonHTTPURI(string));
   }
   
   /**
    * 
    * @param string
    * @return
    */
   public static boolean isNonHTTPURI(String string) {
      return string.startsWith("mailto:") ||
    		    string.startsWith("tel:")    ||
    		    string.startsWith("doi:")    ||
    		    string.startsWith("urn:")    ||
    		    string.startsWith("tag:");
   }
   
   /**
    * 
    * @param string
    * @return
    */
   public static boolean startsLikeNonHTTPURI(String string) {
      return string.startsWith("mailto:") ||
    		    string.startsWith("tel:")    ||
    		    string.startsWith("doi:")    ||
    		    string.startsWith("urn:")    ||
    		    string.startsWith("tag:");
   }
   
   /**
    * 
    * @param string
    * @return
    */
   public static boolean startsLikeURI(String string) {
      return string.startsWith("http") || isNonHTTPURI(string);
   }
   
   /**
    * 
    * @param lodLinks
    * @return lowercase version of lodLinks for all keys.
    */
   public static HashMap<String,HashSet<URI>> desensitizeCase(HashMap<String,HashSet<URI>> lodLinks) {
      HashMap<String,HashSet<URI>> lowerCase = new HashMap<String,HashSet<URI>>();
      for( String identifier : lodLinks.keySet() ) {
         lowerCase.put(squishForIdentity(identifier), lodLinks.get(identifier));
         //System.err.println("squishing lodlink index: ." + identifier + ". ." + squishForIdentity(identifier)+".");
      }
      return lowerCase;
   }
   
   /**
    * Case insensitive, space insensitive.
    * 
    * @param value
    * @return
    */
   public static String squishForIdentity(String value) {
	   return NameFactory.trimChars(value.toLowerCase()," ");
   }
   
   /**
    * 
    * @param element
    * @return
    */
   private static List<String> listOf(String element) {
   	ArrayList<String> elements = new ArrayList<String>();
   	elements.add(element);
   	return elements;
   }
}