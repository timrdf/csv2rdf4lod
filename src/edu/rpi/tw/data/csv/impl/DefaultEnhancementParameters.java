package edu.rpi.tw.data.csv.impl;

import info.aduna.iteration.Iterations;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.CSVtoRDF;
import edu.rpi.tw.data.csv.Conversion;
import edu.rpi.tw.data.csv.EnhancementParameters;
import edu.rpi.tw.data.csv.NamespaceCalculator;
import edu.rpi.tw.data.csv.TemplateFiller;
import edu.rpi.tw.data.csv.Useful;
import edu.rpi.tw.data.csv.querylets.AdditionalDescriptionsQuerylet;
import edu.rpi.tw.data.csv.querylets.AuthorsQuerylet;
import edu.rpi.tw.data.csv.querylets.ColumnIndexesQuerylet;
import edu.rpi.tw.data.csv.querylets.ColumnRangeQuerylet;
import edu.rpi.tw.data.csv.querylets.DomainTemplateQuerylet;
import edu.rpi.tw.data.csv.querylets.ExampleResourcesQuerylet;
import edu.rpi.tw.data.csv.querylets.ExcludeIsReferencedByQuerylet;
import edu.rpi.tw.data.csv.querylets.ExcludeRowNumQuerylet;
import edu.rpi.tw.data.csv.querylets.ExcludeVoIDInDatasetQuerylet;
import edu.rpi.tw.data.csv.querylets.HumanRedirectQuerylet;
import edu.rpi.tw.data.csv.querylets.InterpretedAsNullQuerylet;
import edu.rpi.tw.data.csv.querylets.LayerDatasetDescriptionsQuerylet;
import edu.rpi.tw.data.csv.querylets.PrefixMappingsQuerylet;
import edu.rpi.tw.data.csv.querylets.PrimaryKeyColumnQuerylet;
import edu.rpi.tw.data.csv.querylets.StatementsQuerylet;
import edu.rpi.tw.data.csv.querylets.SubClassOfQuerylet;
import edu.rpi.tw.data.csv.querylets.SubjectTypeQuerylet;
import edu.rpi.tw.data.csv.querylets.CoIN.BaseURIQuerylet;
import edu.rpi.tw.data.csv.querylets.CoIN.DatasetComponentIdentifierQuerylet;
import edu.rpi.tw.data.csv.querylets.CoIN.DatasetIdentifierQuerylet;
import edu.rpi.tw.data.csv.querylets.CoIN.EnhancementIdentifierQuerylet;
import edu.rpi.tw.data.csv.querylets.CoIN.IdentifierNamesI18NQuerylet;
import edu.rpi.tw.data.csv.querylets.CoIN.SourceIdentifierQuerylet;
import edu.rpi.tw.data.csv.querylets.CoIN.VersionIdentifierQuerylet;
import edu.rpi.tw.data.csv.querylets.cellbased.CellBasedSubjectOutPredicateQuerylet;
import edu.rpi.tw.data.csv.querylets.column.CellBasedQuerylet;
import edu.rpi.tw.data.csv.querylets.column.CodebookQuerylet;
import edu.rpi.tw.data.csv.querylets.column.ColumnCommentQuerylet;
import edu.rpi.tw.data.csv.querylets.column.ColumnLabelQuerylet;
import edu.rpi.tw.data.csv.querylets.column.DatePatternQuerylet;
import edu.rpi.tw.data.csv.querylets.column.DateTimePatternQuerylet;
import edu.rpi.tw.data.csv.querylets.column.DelimitsObjectQuerylet;
import edu.rpi.tw.data.csv.querylets.column.ExistingBundleQuerylet;
import edu.rpi.tw.data.csv.querylets.column.ImplicitBundleQuerylet;
import edu.rpi.tw.data.csv.querylets.column.ImplicitBundledNameTemplateQuerylet;
import edu.rpi.tw.data.csv.querylets.column.InterpretedAsFalseQuerylet;
import edu.rpi.tw.data.csv.querylets.column.InterpretedAsTrueQuerylet;
import edu.rpi.tw.data.csv.querylets.column.InterpretedWithRegexQuerylet;
import edu.rpi.tw.data.csv.querylets.column.InverseOfQuerylet;
import edu.rpi.tw.data.csv.querylets.column.MultiplierQuerylet;
import edu.rpi.tw.data.csv.querylets.column.ObjectLabelPropertyQuerylet;
import edu.rpi.tw.data.csv.querylets.column.ObjectLabelTemplateQuerylet;
import edu.rpi.tw.data.csv.querylets.column.ObjectSameAsLinksQuerylet;
import edu.rpi.tw.data.csv.querylets.column.ObjectTemplateQuerylet;
import edu.rpi.tw.data.csv.querylets.column.RangeQuerylet;
import edu.rpi.tw.data.csv.querylets.column.ResourceAnnotationsQuerylet;
import edu.rpi.tw.data.csv.querylets.column.SubjectAnnotationViaObjectSearchQuerylet;
import edu.rpi.tw.data.csv.querylets.column.SubjectSameAsLinksQuerylet;
import edu.rpi.tw.data.csv.querylets.column.SubpropertyOfQuerylet;
import edu.rpi.tw.data.csv.querylets.column.TypedResourcePromotionQuerylet;
import edu.rpi.tw.data.csv.querylets.column.URISafeQuerylet;
import edu.rpi.tw.data.csv.querylets.column.UnlabeledQuerylet;
import edu.rpi.tw.data.csv.querylets.column.chainhead.DelimitsObjectQueryletChainHead;
import edu.rpi.tw.data.csv.querylets.column.chainhead.EnhancementChainQuerylet;
import edu.rpi.tw.data.csv.querylets.column.chainhead.EquivalentPropertyQueryletChainHead;
import edu.rpi.tw.data.csv.querylets.column.chainhead.InterpretedWithRegexQueryletChainHead;
import edu.rpi.tw.data.csv.querylets.column.inchain.EquivalentPropertyQueryletInChain;
import edu.rpi.tw.data.csv.querylets.provenance.FRBRStackQuerylet;
import edu.rpi.tw.data.csv.querylets.provenance.URLSourceUsageQuerylet;
import edu.rpi.tw.data.csv.querylets.row.RowRangeQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.CellDelimiterQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.CharsetQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.DataEndRowQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.DataStartRowQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.HeaderRowQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.LargeValueQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.MultipleHeadersReferencesQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.OmittedColumnsQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.OnlyIfColumnsQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.RepeatPreviousIfEmptyColumnsQuerylet;
import edu.rpi.tw.data.csv.querylets.structural.RepeatPreviousSymbolQuerylet;
import edu.rpi.tw.data.csv.valuehandlers.ResourceValueHandler;
import edu.rpi.tw.data.rdf.sesame.query.QueryletProcessor;
import edu.rpi.tw.data.rdf.sesame.vocabulary.OpenVocab;
import edu.rpi.tw.string.NameFactory;

/**
 * 
 */
public class DefaultEnhancementParameters implements EnhancementParameters, NamespaceCalculator, 
                                                     TemplateFiller, Useful {
   
   private static Logger logger = Logger.getLogger(DefaultEnhancementParameters.class.getName());
   
   private static ValueFactory vf = ValueFactoryImpl.getInstance();
   
   // These are the fundamental naming parameters.
   protected String  baseURI           = "http://localhost";
   protected String  sourceIdentifier  = "SSS";
   protected String  datasetIdentifier = "DDD";
   protected String  versionIdentifier = "VVV";
   protected String  subjectDiscriminator;
   protected boolean hasIdentifiersSpecified;
   
   protected Repository    paramsRepository;
   
   // DEPRECATE:
   public int           primaryKeyCol;
   public int           uriKeyCol;
   public String        datasetNS;
   //public String        datasetIDTag;
   public String        enhancementIdentifier;
   public String        classURI;
   public String        subjectNS;
   public boolean       uuidSubject;
   public String        predicateNS;
   public boolean       uuidPredicate;
   public String        objectNS;
   public boolean       uuidObject;
   public String        resourceOrLiteralBitString;
   // end DEPRECATE.
   
   
   private Set<Integer> onlyIfColumns         = null;
   private Set<Integer> repeatPreviousColumns = null;
   private Set<String>  repeatPreviousSymbols = null;
   private Set<Integer> omittedColumns        = null;
   private Set<Long>    exampleResourceRows   = null;
   
   protected HashMap<String,String>                       queried;
   protected HashMap<Integer,Set<Integer>>                bundledByColumn;
   protected HashMap<Integer,String>                      bundlePropertyName;
   protected HashMap<Integer,String>                      bundleType;
   protected HashMap<Integer,Boolean>                     bundleAnonymous;
   protected HashMap<Integer,HashMap<URI,HashSet<Value>>> implicitBundleAnnotations = new HashMap<Integer,HashMap<URI,HashSet<Value>>>();

   protected String                        domainTemplate       = null;
   protected Integer                       domainTemplateColumn = null;
   protected HashMap<Integer, String>      implicitBundleNameTemplateByCol;
   
   protected HashMap<Integer,List<String>>           objectTemplatesByCol;
   protected HashMap<Integer,Set<URI>>               objectLabelPropertiesByCol;
   //replaced by objectTemplatesByCol: protected HashMap<Integer,String>      objectTemplateByCol; // TODO: plural replaces singular (crutch subsumes promotion template).
   protected HashMap<Integer,String>                 objectLabelPatternByCol;
   protected HashMap<Integer,Set<URI>>               subPropertyByCol;
   protected HashMap<Integer,
                     HashMap<String, 
                               HashMap<Value,
                                       Set<Value>>>> subjectAnnotationsViaObjectSearchesByCol;
   protected HashMap<Integer,Set<URI>>               inversesByCol;
   protected HashMap<Integer,HashMap<String,String>> interpWithRegexByColChainHeads;
   protected HashSet<Integer>                        interpWithRegexColsChainHead = new HashSet<Integer>();
   protected HashMap<Integer,HashMap<String,String>> interpWithRegexByColInChain;
   protected HashSet<Integer>                        interpWithRegexColsInChain = new HashSet<Integer>();
   
   protected HashMap<Integer,String>           objectDelimiterByColChainHead;
   protected HashMap<Integer,String>           objectDelimiterByColInChain;
   
   protected String                        subjectRowType;
   protected HashMap<String,Set<URI>>      superClassesOfLocalClass = new HashMap<String,Set<URI>>();
   protected Set<String>                   classLabels;     // Aggregates subject, implicit bundle, and object types.
   
   protected HashMap<Integer,Value>        rangeNameByCol;
   protected HashMap<Integer,String>       objectDelimiterCol;
   
   protected Set<String>                   humanRedirects;
   
   private   Integer                       headerRow = 1;
   protected Set<Long>                     rowsReferencedRelativeToHeader = new HashSet<Long>();
   
   protected Set<String>                   interpretedAsNullStringsGlobal = null;
   protected HashMap<Integer,Set<String>>  interpretedAsNullStrings       = new HashMap<Integer,Set<String>>();
   
   protected HashMap<Integer,Value>  cellBasedValue;
   protected HashMap<Integer,String> cellBasedPropertyLN;
   protected HashMap<Integer,Value>  cellBasedOutPredicates;
   
   protected Integer firstCellBasedColumn;
   protected boolean isDirectToLODCloud       = false; // https://github.com/timrdf/csv2rdf4lod-automation/issues/234
   protected boolean isLODLinkCaseInsensitive = false; // https://github.com/timrdf/csv2rdf4lod-automation/issues/241
   protected boolean includesLODLinksGraph    = false; // https://github.com/timrdf/csv2rdf4lod-automation/issues/274
   protected HashMap<Integer,Repository> lodLinksGraphs = null;
   
   protected HashMap<Integer,HashMap<String, Value>> codebookByCol = new HashMap<Integer,HashMap<String, Value>>();
   
   protected HashMap<String,String> i18n;
   
   protected HashMap<Integer,Boolean> omitResourceLabelsByCol;
   
   // For URI/Namespace construction. Includes / if there.
   private String datasetSourceT;
   private String datasetIdentifierT;
   private String datasetVersionT;
   private String subjectDiscriminatorT;
   private String conversionIdentifierT;

   protected HashMap<Integer,HashMap<URI,Set<Value>>>    additionalColumnDescriptions           = new HashMap<Integer,HashMap<URI,Set<Value>>>();
   protected HashMap<Integer,HashMap<String,Set<Value>>> additionalColumnContextualDescriptions = new HashMap<Integer,HashMap<String,Set<Value>>>();
   
   // TODO: untraditional: storing the querylet instead of the values.
   protected URLSourceUsageQuerylet urlSourceUsageQuerylet = new URLSourceUsageQuerylet(null);
   
   protected String D = "."; // .raw.sample should become -raw-sample
   
   protected static HashSet<URI> EMPTY_HashSetOfURI = new HashSet<URI>();
   protected static Set<Integer> EMPTY_SetOfInteger = new HashSet<Integer>();
   
   // true if any enhancement given is "useful" in any way worth producing an enhancement layer.
   protected boolean useful = false;
   
   protected boolean excludeRowNumbers = false;
   protected boolean excludeDCRef      = false;
   protected boolean excludeVoIDRef    = false;
   
   /**
    * @deprecated
    * @param primaryKeyCol
    * @param uriKeyCol
    * @param datasetNS
    * @param datasetIDTag
    * @param enhancementIdentifier
    * @param classURI
    * @param subjectNS
    * @param uuidSubject
    * @param predicateNS
    * @param uuidPredicate
    * @param objectNS
    * @param uuidObject
    * @param resourceOrLiteralBitString
    */
   public DefaultEnhancementParameters(int primaryKeyCol, int uriKeyCol,
                                      String datasetNS, String datasetIDTag,
                                      String enrichmentTag,
                                      String classURI, 
                                      String subjectNS,   boolean uuidSubject,
                                      String predicateNS, boolean uuidPredicate,
                                      String objectNS,    boolean uuidObject,
                                      String resourceOrLiteralBitString) {
      
      this.primaryKeyCol              = primaryKeyCol;
      this.uriKeyCol                  = uriKeyCol;
      this.datasetNS                  = datasetNS;
      this.enhancementIdentifier      = enrichmentTag;
      this.classURI                   = classURI;
      this.subjectNS                  = subjectNS;
      this.uuidSubject                = uuidSubject;
      this.predicateNS                = predicateNS;
      this.uuidPredicate              = uuidPredicate;
      this.objectNS                   = objectNS;
      this.uuidObject                 = uuidObject;
      this.resourceOrLiteralBitString = resourceOrLiteralBitString;

      this.queried = new HashMap<String,String>();
      this.bundlePropertyName = new HashMap<Integer,String>();
   }

   /**
    * 
    * @param paramsRepository
    */
   public DefaultEnhancementParameters(Repository paramsRepository) {
      this(paramsRepository, null);
   }
     
   /**
    * 
    * @param paramsRepository - Repository containing the RDF-encoded enhancement parameters.
    * @param overrideBaseURI - OVERRIDE conversion:base_uri in parameters with 'baseURI' if it is non-null.
    */
   public DefaultEnhancementParameters(Repository paramsRepository, String overrideBaseURI) {
   	
      this.paramsRepository = paramsRepository;
      
      this.queried = new HashMap<String,String>();
      
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      //                                   Dataset URI naming parameters                                              //
      //                                                                                                              //
      //System.err.println("eParams: got baseURI = "+baseURI);                                                        //
      if( overrideBaseURI != null && overrideBaseURI.indexOf("://") > 0 ) {                                           //
         // Override parameters' conversion:base_uri with value given.                                                //
         this.baseURI = overrideBaseURI;                                                                              //
         //System.err.println("eParams: overriding baseURI");                                                         //
      }else {                                                                                                         //
         // Use conversion:base_uri in parameters.                                                                    //
         BaseURIQuerylet baseURIQ = new BaseURIQuerylet(null);                                                        //
         QueryletProcessor.processQuery(paramsRepository, baseURIQ);                                                  //
         this.baseURI = baseURIQ.get();                                                                               //
         //System.err.println("eParams: conversion:base_uri = "+this.baseURI);                                        //
         if( this.baseURI == null || this.baseURI.length() == 0 ) {                                                   //
            // Default if nothing there.                                                                              //
            this.baseURI = "http://localhost";                                                                        //
            //System.err.println("eParams: defaulting baseURI");                                                      //
         }                                                                                                            //
      }                                                                                                               //
      //                                                                                                              // 
      SourceIdentifierQuerylet sourceIDQ = new SourceIdentifierQuerylet(null);                                        //
      QueryletProcessor.processQuery(paramsRepository, sourceIDQ);                                                    //
      if( sourceIDQ.get() != null  )  this.sourceIdentifier = sourceIDQ.get();                                        //
      //                                                                                                              // 
      DatasetIdentifierQuerylet datasetIDQ = new DatasetIdentifierQuerylet(null);                                     //
      QueryletProcessor.processQuery(paramsRepository, datasetIDQ);                                                   //
      if( datasetIDQ.get() != null )  this.datasetIdentifier = datasetIDQ.get();                                      //
      //                                                                                                              // 
      VersionIdentifierQuerylet versionIDQ = new VersionIdentifierQuerylet(null);                                     //
      QueryletProcessor.processQuery(paramsRepository, versionIDQ);                                                   //
      if( versionIDQ.get() != null )  this.versionIdentifier = versionIDQ.get();                                      //
      //                                                                                                              //
      this.hasIdentifiersSpecified = sourceIDQ.get() != null && datasetIDQ.get() != null && versionIDQ.get() != null; //
      //                                                                                                              // 
      DatasetComponentIdentifierQuerylet subjectDiscrimQ = new DatasetComponentIdentifierQuerylet(null);              //
      QueryletProcessor.processQuery(paramsRepository, subjectDiscrimQ);                                              //
      this.subjectDiscriminator = subjectDiscrimQ.get();                                                              //
      //                                                                                                              // 
      EnhancementIdentifierQuerylet eTagQ = new EnhancementIdentifierQuerylet(null);                                  //
      QueryletProcessor.processQuery(paramsRepository, eTagQ);                                                        //
      this.enhancementIdentifier = eTagQ.get();                                                                       //
      //                                                                                                              //     
      IdentifierNamesI18NQuerylet i18nQ = new IdentifierNamesI18NQuerylet(null);                                      //
      QueryletProcessor.processQuery(paramsRepository, i18nQ);                                                        //
      this.i18n = i18nQ.get();                                                                                        //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      
      
      // Materialize conversion:{from,to}Col ranges into individual ov:csvCol statements.
      ColumnRangeQuerylet colRangesQ = new ColumnRangeQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, colRangesQ);
      RowRangeQuerylet rowRangesQ = new RowRangeQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, rowRangesQ);
      try {
      	RepositoryConnection conn = paramsRepository.getConnection();
      	for( Resource e : colRangesQ.get().keySet() ) {
      		HashMap<Integer,Integer> ranges = colRangesQ.get().get(e);
      		for( int from : ranges.keySet() ) {
      			int to = ranges.get(from);
      			//System.out.println("materializing from " + from + " to " + to + " on "+ e.stringValue());   
      			for( int c = from; c <= to; c++ ) {
         			//System.out.println("    materializing " + c);   
         			conn.add(e, OpenVocab.csvCol, vf.createLiteral(""+c,XMLSchema.INTEGER));
      			}
      		}
      		conn.commit();
      		//conn.export(Constants.handlerForFileExtension("ttl", System.out));
      	}
      	
      	for( Resource e : rowRangesQ.get().keySet() ) {
      		HashMap<Integer,Integer> ranges = rowRangesQ.get().get(e);
      		for( int from : ranges.keySet() ) {
      			int to = ranges.get(from);
      			//System.out.println("materializing rows from " + from + " to " + to + " on "+ e.stringValue());   
      			for( int c = from; c <= to; c++ ) {
         			//System.out.println("    materializing row " + c);   
         			conn.add(e, OpenVocab.csvRow, vf.createLiteral(""+c,XMLSchema.INTEGER));
      			}
      		}
      		conn.commit();
      		//conn.export(Constants.handlerForFileExtension("ttl", System.out));
      	}
      	conn.close();
      }catch (RepositoryException e) {
      	e.printStackTrace();
      }

      MultipleHeadersReferencesQuerylet multHeadRefQ = new MultipleHeadersReferencesQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, multHeadRefQ);
      for( long displacement : multHeadRefQ.get() ) {
         //rowsReferencedRelativeToHeader.add(getHeaderRow() + displacement);
         rowsReferencedRelativeToHeader.add(displacement);
         this.useful = true;
      }
      
      SubjectTypeQuerylet subjectTypeQ = new SubjectTypeQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, subjectTypeQ);
      this.subjectRowType = subjectTypeQ.get();
      this.useful = this.useful || this.subjectRowType != null;
      
      SubClassOfQuerylet subClassQ = new SubClassOfQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, subClassQ);
      for( String classLabel : subClassQ.get().keySet() ) {
         this.superClassesOfLocalClass.put(classLabel, new HashSet<URI>());
         for( Value superClass : subClassQ.get().get(classLabel) ) {
         	this.useful = true;
            if( superClass instanceof URI ) {
               this.superClassesOfLocalClass.get(classLabel).add((URI) superClass);
            }else {
               // edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller also calls fillTemplate.
               String template = fillTemplate(superClass.stringValue());
               if( template.indexOf(':') < 0 ) {
                  // Local namespace
                  template = this.getNamespaceOfVocab() + NameFactory.label2URI(template);
               }
               //System.err.println("handling superclass pattern: "+superClass.stringValue()+" -> "+pattern);
               this.superClassesOfLocalClass.get(classLabel).add(ValueFactoryImpl.getInstance().createURI(template));
            }
         }
      }
         
      
      /////////////////////////////////////////////////////////////////////////////////////////////////////
      //                                   Structural parameters                                         //
      //                                                                                                 //
      OnlyIfColumnsQuerylet onlyIfQ = new OnlyIfColumnsQuerylet(null);                                   //
      QueryletProcessor.processQuery(paramsRepository, onlyIfQ);                                         //
      this.onlyIfColumns = onlyIfQ.get();                                                                //
      //this.useful = this.useful || this.onlyIfColumns.size() > 0;                                      //
                                                                                                         //                            
      RepeatPreviousIfEmptyColumnsQuerylet repeatPrevQ = new RepeatPreviousIfEmptyColumnsQuerylet(null); //
      QueryletProcessor.processQuery(paramsRepository, repeatPrevQ);                                     //
      this.repeatPreviousColumns = repeatPrevQ.get();                                                    //
      //this.useful = this.useful || this.repeatPreviousColumns.size() > 0;                              //
                                                                                                         //
      RepeatPreviousSymbolQuerylet repeatPrevSymbolQ = new RepeatPreviousSymbolQuerylet(null);           //
      QueryletProcessor.processQuery(paramsRepository, repeatPrevSymbolQ);                               //
      this.repeatPreviousSymbols = repeatPrevSymbolQ.get();                                              //
      //this.useful = this.useful || this.repeatPreviousSymbols.size() > 0;                              //
                                                                                                         //
      OmittedColumnsQuerylet omittedColumnsQ = new OmittedColumnsQuerylet(null);                         //
      QueryletProcessor.processQuery(paramsRepository, omittedColumnsQ);                                 //
      this.omittedColumns = omittedColumnsQ.get();                                                       //
      //this.useful = this.useful || this.omittedColumns.size() > 0;                                     //
      /////////////////////////////////////////////////////////////////////////////////////////////////////

      this.bundlePropertyName                       = new HashMap<Integer,String>();
      this.bundleType                               = new HashMap<Integer,String>();
      this.bundleAnonymous                          = new HashMap<Integer,Boolean>();
      this.bundledByColumn                          = new HashMap<Integer,Set<Integer>>();
      this.objectTemplatesByCol                     = new HashMap<Integer,List<String>>();
      this.objectLabelPropertiesByCol               = new HashMap<Integer,Set<URI>>();
      this.objectLabelPatternByCol                  = new HashMap<Integer,String>();
      this.objectDelimiterByColChainHead            = new HashMap<Integer,String>();
      this.objectDelimiterByColInChain              = new HashMap<Integer,String>();
      this.subPropertyByCol                         = new HashMap<Integer,Set<URI>>();
      this.rangeNameByCol                           = new HashMap<Integer,Value>();
      this.cellBasedValue                           = new HashMap<Integer,Value>();
      this.cellBasedPropertyLN                      = new HashMap<Integer,String>();
      this.implicitBundleNameTemplateByCol          = new HashMap<Integer,String>();
      this.humanRedirects                           = new HashSet<String>();
      this.exampleResourceRows                      = new HashSet<Long>();
      this.objectDelimiterCol                       = new HashMap<Integer,String>();
      this.subjectAnnotationsViaObjectSearchesByCol = new HashMap<Integer,HashMap<String, HashMap<Value,Set<Value>>>>();
      this.inversesByCol                            = new HashMap<Integer,Set<URI>>();
   	this.interpWithRegexByColChainHeads           = new HashMap<Integer,HashMap<String,String>>();
   	this.interpWithRegexByColInChain              = new HashMap<Integer,HashMap<String,String>>();
   	this.lodLinksGraphs                           = new HashMap<Integer,Repository>();
      this.omitResourceLabelsByCol                  = new HashMap<Integer,Boolean>();
      
      // Run through all columns and store any results required for lookup at each csv value.
      ColumnIndexesQuerylet columnsQ = new ColumnIndexesQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, columnsQ);
      
      // Grab descriptions for the row.
      this.additionalColumnDescriptions.put(          0, new HashMap<URI,    Set<Value>>());
      this.additionalColumnContextualDescriptions.put(0, new HashMap<String, Set<Value>>());
      ResourceAnnotationsQuerylet addColDescriptionsQ = new ResourceAnnotationsQuerylet(null,0);
      QueryletProcessor.processQuery(paramsRepository, addColDescriptionsQ);
      for( Value predicate : addColDescriptionsQ.get().keySet() ) {
      	this.useful = true;
         if( predicate instanceof URI ) {
            additionalColumnDescriptions.get(0).put((URI)predicate, addColDescriptionsQ.get().get(predicate));
         }else {
            System.err.println("WARNING: row's additional description predicate is not a URI; not applying a template: "+predicate.stringValue());
            additionalColumnContextualDescriptions.get(0).put(predicate.stringValue(), addColDescriptionsQ.get().get(predicate));
         }
      }// TODO: why not doing this for other columns? what is CSVtoRDF#visitHeader doing that we can't do here?
      
      //System.err.println(columnsQ.getSet().size()+" columns described in conversion parameters.");
      // TODO: Make sure these return OK values if no columns are described in params.
      for( int c : columnsQ.get() ) {
         
      	// Only at the head of the chain
      	InterpretedWithRegexQueryletChainHead interRegexQ = new InterpretedWithRegexQueryletChainHead(null,c);
         QueryletProcessor.processQuery(paramsRepository, interRegexQ);
         this.interpWithRegexByColChainHeads.put(c, interRegexQ.get());
         if( interRegexQ.get().size() > 0 ) {
         	interpWithRegexColsChainHead.add(c);
         }
         
         // Only *within* the chain (non-head)
      	InterpretedWithRegexQuerylet interRegexInChainQ = new InterpretedWithRegexQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, interRegexInChainQ);
         this.interpWithRegexByColInChain.put(c, interRegexInChainQ.get());
         if( interRegexInChainQ.get().size() > 0 ) {
         	interpWithRegexColsInChain.add(c);
         }

         ImplicitBundleQuerylet implicitBundleQ = new ImplicitBundleQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, implicitBundleQ);
         this.bundlePropertyName.put(c,        implicitBundleQ.get());
         this.bundleType.put(c,                implicitBundleQ.getType());
         this.bundleAnonymous.put(c,           implicitBundleQ.isAnonymous());
         this.implicitBundleAnnotations.put(c, implicitBundleQ.getAnnotations());
         
         ImplicitBundledNameTemplateQuerylet bundleDomainQ = new ImplicitBundledNameTemplateQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, bundleDomainQ);
         this.implicitBundleNameTemplateByCol.put(c, bundleDomainQ.get());
         
         ExistingBundleQuerylet existingBundleQ = new ExistingBundleQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, existingBundleQ);
         this.bundledByColumn.put(c, existingBundleQ.get());
         
         //CrutchTemplateQuerylet crutchQ = new CrutchTemplateQuerylet(null,c); // Superceded by RangePromotionTemplate
         //QueryletProcessor.processQuery(paramsRepository, crutchQ);
         //this.rangeTemplatesByCol.put(c, crutchQ.get()); // TODO: reconcile crutch and range promotion.
         ObjectTemplateQuerylet templateQ = new ObjectTemplateQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, templateQ);
         this.objectTemplatesByCol.put(c, templateQ.get());
         //this.useful = this.useful || templateQ.get().size() > 0;
         
         ObjectLabelPropertyQuerylet objectLabelPropertyQ = new ObjectLabelPropertyQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, objectLabelPropertyQ);
         this.objectLabelPropertiesByCol.put(c,objectLabelPropertyQ.get());
         
         SubpropertyOfQuerylet subPropQ = new SubpropertyOfQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, subPropQ);
         this.subPropertyByCol.put(c, subPropQ.getSet(this));
         //this.useful = this.useful || subPropQ.getSet(this).size() > 0;
         
         TypedResourcePromotionQuerylet querylet = new TypedResourcePromotionQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, querylet);
         this.rangeNameByCol.put(c, querylet.get());
         //this.useful = this.useful || querylet.get() != null;
         
         ObjectLabelTemplateQuerylet objectLabelQ = new ObjectLabelTemplateQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, objectLabelQ);
         this.objectLabelPatternByCol.put(c, objectLabelQ.get());
         //this.useful = this.useful || objectLabelQ.getStringResult() != null;
         
         CellBasedQuerylet cellBasedQ = new CellBasedQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, cellBasedQ);
         if( cellBasedQ.isCellBased() ) this.cellBasedValue.put(c, cellBasedQ.get());
         
         DelimitsObjectQueryletChainHead objectDelimiterChainHeadQ = new DelimitsObjectQueryletChainHead(null,c);
         QueryletProcessor.processQuery(paramsRepository, objectDelimiterChainHeadQ);
         this.objectDelimiterByColChainHead.put(c, objectDelimiterChainHeadQ.get());
         //this.useful = this.useful || objectDelimiterQ.get() != null;
         
         DelimitsObjectQuerylet objectDelimiterInChainQ = new DelimitsObjectQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, objectDelimiterInChainQ);
         this.objectDelimiterByColInChain.put(c, objectDelimiterInChainQ.get());
         
         CodebookQuerylet codebookQ = new CodebookQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, codebookQ);
         HashMap<String,Value> codebook = codebookQ.get();
         if( codebook == null || codebook.isEmpty() ) {
            this.codebookByCol.put(c, new HashMap<String,Value>());
         }else {
         	this.useful = true;
            this.codebookByCol.put(c, codebook);
         }
         
         SubjectAnnotationViaObjectSearchQuerylet objectSearchQ = new SubjectAnnotationViaObjectSearchQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, objectSearchQ);
         subjectAnnotationsViaObjectSearchesByCol.put(c, objectSearchQ.get());
         this.useful = this.useful || objectSearchQ.get().size() > 0;
         
         InverseOfQuerylet inversesQ = new InverseOfQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, inversesQ);
         this.inversesByCol.put(c, inversesQ.get());
         
         UnlabeledQuerylet unlabeledQ = new UnlabeledQuerylet(null,c);
         QueryletProcessor.processQuery(paramsRepository, unlabeledQ);
         this.omitResourceLabelsByCol.put(c, unlabeledQ.get());
      }
      
      this.firstCellBasedColumn = Integer.MAX_VALUE;
      for( int c : this.cellBasedValue.keySet() ) {
         if( c < this.firstCellBasedColumn ) {
            this.firstCellBasedColumn = c;
         }
      }
      
      // Find out what predicate to use from the cell to the value of the cell (in cell based interpretation)
      cellBasedOutPredicates = new HashMap<Integer,Value>();
      CellBasedSubjectOutPredicateQuerylet cellBasedOutPredQ = new CellBasedSubjectOutPredicateQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, cellBasedOutPredQ);
      for( int c : this.cellBasedValue.keySet() ) {
         Value outPredicate = cellBasedOutPredQ.get();
         if( outPredicate != null ) {
         	this.useful = true;
            if( outPredicate instanceof URI ) {
               cellBasedOutPredicates.put(c, cellBasedOutPredQ.get());
            }else if( outPredicate instanceof Literal ) {
               System.err.println("WARNING: overriding predicate ("+ outPredicate +") is not a URI; predicate construction not supported.");
            }
         }
      }// TODO: store per-column predicate overrides (one applies to all for now).
      
      InterpretedAsNullQuerylet nullQ = new InterpretedAsNullQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, nullQ);
      this.interpretedAsNullStringsGlobal =  nullQ.get();
      this.useful = this.useful || nullQ.get().size() > 0;
      
      DomainTemplateQuerylet domainTemplateQ = new DomainTemplateQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, domainTemplateQ);      
      this.domainTemplate       = domainTemplateQ.get();
      this.domainTemplateColumn = domainTemplateQ.getColumn();
      this.useful = this.useful || domainTemplateQ.get() != null;
      
      HumanRedirectQuerylet humanRedirectQ = new HumanRedirectQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, humanRedirectQ);
      for( Value template : humanRedirectQ.get() ) {
         this.humanRedirects.add(fillTemplate(template.stringValue()));
      }
      
      ExampleResourcesQuerylet egsQ = new ExampleResourcesQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, egsQ);
      this.exampleResourceRows = egsQ.get();
      
      ExcludeRowNumQuerylet xRowNumQ = new ExcludeRowNumQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, xRowNumQ);
      this.excludeRowNumbers = xRowNumQ.get();
      
      ExcludeIsReferencedByQuerylet xRefQ = new ExcludeIsReferencedByQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, xRefQ);
      this.excludeDCRef = xRefQ.get();
      
      ExcludeVoIDInDatasetQuerylet xVoIDRef = new ExcludeVoIDInDatasetQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, xVoIDRef);
      this.excludeVoIDRef = xVoIDRef.get();
      
      this.urlSourceUsageQuerylet = new URLSourceUsageQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, this.urlSourceUsageQuerylet);
      
      FRBRStackQuerylet q = new FRBRStackQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, q);
      
      this.datasetSourceT        = NameFactory.slashIfThere("source",  this.getTokenDatasetSourceIdentifier()); 
      this.datasetIdentifierT    = NameFactory.slashIfThere("dataset", this.getTokenDatasetIdentifier());
      this.datasetVersionT       = NameFactory.slashIfThere("version", this.getTokenDatasetVersionIdentifier());
      this.subjectDiscriminatorT = NameFactory.slashIfThere(           this.getTokenSubjectDiscriminator());
      this.conversionIdentifierT = NameFactory.slashIfThere(           this.getStepConversionIdentifier());
   }

   @Override
   public String getBaseURI() {
      return this.baseURI;
   }
   
   
   // Namespace token methods
   
   
   @Override
   public String getTokenDatasetSourceIdentifier() {
      return this.sourceIdentifier;
   }
   
   @Override
   public String getTokenDatasetIdentifier() {
      return this.datasetIdentifier;
   }

   @Override
   public String getTokenDatasetVersionIdentifier() {
      return this.versionIdentifier;
   }
   
   @Override
   public String getTokenSubjectDiscriminator() {
      return this.subjectDiscriminator;
   }
   
   @Override
   public boolean isMultiPart() {
      boolean multi = getTokenSubjectDiscriminator() != null && getTokenSubjectDiscriminator().length() > 0;
      return multi;
   }
   
   @Override
   public boolean isEnhanced() {
      return getTokenEnhancementIdentifier() != null && getTokenEnhancementIdentifier().length() > 0;
   }
   
   /**
    * "" if raw, "1", "2", etc. if enhanced. 
    * @return the actual value of conversion:enhancement_identifier from the parameters.
    */
   @Override
   public String getTokenEnhancementIdentifier() {
      return this.enhancementIdentifier;
   }
   
   /**
    * 
    * @return path step of URI derived from {@link #getTokenEnhancementIdentifier()}: e.g., 'raw' or 'enhancement/x'
    */
   @Override
   public String getStepConversionIdentifier() {
      String conversionIdentifier = "raw";
      if( this.enhancementIdentifier != null && this.enhancementIdentifier.length() > 0 ) {
         conversionIdentifier = "enhancement/"+this.enhancementIdentifier;
      }
      return conversionIdentifier;
   }
   
   /**
    * @param prependE - string to prepend to the enhancement identifier (e.g., "e").
    * @return fills in "raw" if {@link #getTokenEnhancementIdentifier()} is empty: e.g., 'raw' or 'x'
    */
   @Override
   public String getTokenConversionIdentifier(String prependE) {
      String conversionIdentifier = "raw";
      if( this.enhancementIdentifier != null && this.enhancementIdentifier.length() > 0 ) {
         conversionIdentifier = prependE + this.enhancementIdentifier;
      }
      return conversionIdentifier;
   }

   
   /**
    * Fill the cell-independent template variables.
    * 
    * {@link CSVRecordTemplateFiller#fillTemplate(String)} fills the cell-dependent variables ("[.]", etc.).
    */
   @Override
   public String fillTemplate(String template) {
      
      /*boolean faster       =  true;
      boolean triedAndTrue = !faster;
      
      if( triedAndTrue && !faster ) {
      
         String triedAndTrueS = new String(template);
         
         // 
         // The following was the implementation for years until we hit some speed issues.
         // See https://github.com/timrdf/csv2rdf4lod-automation/issues/380 
         //
         triedAndTrueS = triedAndTrueS.replaceAll("\\[/\\]",    getBaseURI()+"/");
         triedAndTrueS = triedAndTrueS.replaceAll("\\[/s\\]",   getNamespaceOfSource());
         triedAndTrueS = triedAndTrueS.replaceAll("\\[/sd\\]",  getNamespaceOfAbstractDataset());
         triedAndTrueS = triedAndTrueS.replaceAll("\\[/sdv\\]", getURIOfVersionedDataset()+"/");
         
         triedAndTrueS = triedAndTrueS.replaceAll("\\[s\\]",    getTokenDatasetSourceIdentifier());
         triedAndTrueS = triedAndTrueS.replaceAll("\\[d\\]",    getTokenDatasetIdentifier());
         triedAndTrueS = triedAndTrueS.replaceAll("\\[v\\]",    getTokenDatasetVersionIdentifier());
         triedAndTrueS = triedAndTrueS.replaceAll("\\[D\\]",    getTokenSubjectDiscriminator());
         triedAndTrueS = triedAndTrueS.replaceAll("\\[e\\]",    getTokenEnhancementIdentifier());
         
         triedAndTrueS = triedAndTrueS.replaceAll("\\[/sD\\]",  getNamespaceOfAbstractSubdataset());
         triedAndTrueS = triedAndTrueS.replaceAll("\\[/sDv\\]", getURIOfVersionedDiscriminatedDatasetLayer()+"/");
         
         // https://github.com/timrdf/csv2rdf4lod-automation/issues/347
         triedAndTrueS = triedAndTrueS.replaceAll("\\[uuid\\]", UUID.randomUUID().toString());
         return triedAndTrueS;

      }else {*/
         
      String fasterS = new String(template);
      /*
       * Search the original template (which will stay smaller than the expanded template),
       * but grow the expanded result if the original template had the variable.
       */

      if( template.indexOf("[/]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[/\\]",    getBaseURI()+"/");
      }
      if( template.indexOf("[/s]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[/s\\]",   getNamespaceOfSource());
      }
      if( template.indexOf("[/sd]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[/sd\\]",  getNamespaceOfAbstractDataset());
      }
      if( template.indexOf("[/sdv]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[/sdv\\]", getURIOfVersionedDataset()+"/");
      } // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      if( template.indexOf("[s]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[s\\]",    getTokenDatasetSourceIdentifier());
      }
      if( template.indexOf("[d]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[d\\]",    getTokenDatasetIdentifier());
      }
      if( template.indexOf("[v]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[v\\]",    getTokenDatasetVersionIdentifier());
      }
      if( template.indexOf("[D]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[D\\]",    getTokenSubjectDiscriminator());
      }
      if( template.indexOf("[e]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[e\\]",    getTokenEnhancementIdentifier());
      } // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      if( template.indexOf("[/sD]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[/sD\\]",  getNamespaceOfAbstractSubdataset());
      }
      if( template.indexOf("[/sDv]") >= 0 ) {
         fasterS = fasterS.replaceAll("\\[/sDv\\]", getURIOfVersionedDiscriminatedDatasetLayer()+"/");
      } // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
      if( template.indexOf("[uuid]") >= 0 ) {
         // https://github.com/timrdf/csv2rdf4lod-automation/issues/347
         fasterS = fasterS.replaceAll("\\[uuid\\]", UUID.randomUUID().toString());
      }
      
      /*if( !triedAndTrueS.equals(fasterS) ) {
         System.err.println("DEP tried and true: " + triedAndTrueS);
         System.err.println("         vs faster: " + fasterS);
         System.err.println(" - - [/]   @ " + template.indexOf("\\[/\\]")   + " in " + template);
         System.err.println(" - - [/sd] @ " + template.indexOf("\\[/sd\\]") + " in " + template);
      }*/
      return fasterS;
   }
   
   @Override
   public String fillTemplate(String template, boolean asResource) {
      // All of sdvd should already be URI friendly.
      return fillTemplate(template);
   }
   
   @Override
   public String fillTemplate(String template, String currentValue, boolean asResource) {
      // All of sdvd should already be URI friendly.
      return fillTemplate(template);
   }
   
   @Override
   public boolean doesExpand(String template) {
      return template.length() != fillTemplate(template).length(); 
      // TODO: some situations may incorrectly return false if value length is == variable name length.
   }
   
   
   // Namespace calculations
   
   
   /**
    * 
    */
   @Override
   public String getNamespaceOfVocab() {
      String datasetIdentifierT = NameFactory.slashIfThere("dataset", getTokenDatasetIdentifier());
      return getNamespaceOfSource() + datasetIdentifierT + "vocab/";
   }

   @Override
   public String getNamespaceOfRawProperty() {
    /*String datasetSourceT        = NameFactory.slashIfThere("source",  getTokenDatasetSourceIdentifier());*/
      String datasetIdentifierT    = NameFactory.slashIfThere("dataset", getTokenDatasetIdentifier());
      return getNamespaceOfSource() + datasetIdentifierT + "vocab/raw/"; 
   }
   
   @Override
   public String getNamespaceOfEnhancementProperty() {
    /*String datasetSourceT        = NameFactory.slashIfThere("source",  getTokenDatasetSourceIdentifier()); */
      String datasetIdentifierT    = NameFactory.slashIfThere("dataset", getTokenDatasetIdentifier());
      return getNamespaceOfSource() + datasetIdentifierT + "vocab/enhancement/" + this.enhancementIdentifier + "/"; 
   }
   
   @Override
   public String getURIOfDatasetType() {
      // "T" for token
    /*String datasetSourceT        = NameFactory.slashIfThere("source",  this.getTokenDatasetSourceIdentifier()); 
      String datasetIdentifierT    = NameFactory.slashIfThere("dataset", this.getTokenDatasetIdentifier());
      String datasetVersionT       = NameFactory.slashIfThere("version", this.getTokenDatasetVersionIdentifier());
      String subjectDiscriminatorT = NameFactory.slashIfThere(           this.getTokenSubjectDiscriminator());
      String conversionIdentifierT = NameFactory.slashIfThere(           this.getStepConversionIdentifier());*/
      return getNamespaceOfSource() + "vocab/Dataset";
   }
   
   
   private String namespaceOfAbstractDataset = null; // For SPEED ONLY; and only for use by this method.
   @Override
   public String getNamespaceOfAbstractDataset() {
      if( namespaceOfAbstractDataset == null ) {
         // "T" for token
         /*String datasetSourceT        = NameFactory.slashIfThere("source",  this.getTokenDatasetSourceIdentifier());*/
         String datasetIdentifierT    = NameFactory.slashIfThere("dataset", this.getTokenDatasetIdentifier());
         /*String datasetVersionT       = NameFactory.slashIfThere("version", this.getTokenDatasetVersionIdentifier());
      String subjectDiscriminatorT = NameFactory.slashIfThere(           this.getTokenSubjectDiscriminator());
      String conversionIdentifierT = NameFactory.slashIfThere(           this.getStepConversionIdentifier());*/
         namespaceOfAbstractDataset = getNamespaceOfSource() + datasetIdentifierT;
      }
      return namespaceOfAbstractDataset;
   }
   
   
   private String namespaceOfAbstractSubdataset = null; // For SPEED ONLY; and only for use by this method.
   @Override
   public String getNamespaceOfAbstractSubdataset() {
      if( namespaceOfAbstractSubdataset == null ) {
         // "T" for token
         /*String datasetSourceT        = NameFactory.slashIfThere("source",  this.getTokenDatasetSourceIdentifier()); 
           String datasetIdentifierT    = NameFactory.slashIfThere("dataset", this.getTokenDatasetIdentifier());
           String datasetVersionT       = NameFactory.slashIfThere("version", this.getTokenDatasetVersionIdentifier());*/
           String subjectDiscriminatorT = NameFactory.slashIfThere(           this.getTokenSubjectDiscriminator());
         /*String conversionIdentifierT = NameFactory.slashIfThere(           this.getStepConversionIdentifier());*/
           namespaceOfAbstractSubdataset = getNamespaceOfAbstractDataset() + subjectDiscriminatorT;
      }
      return namespaceOfAbstractSubdataset;
   }
   
   
   /**
    * /dataset_page/
    * 
    * @return
    */
   protected String getPagespaceOfDataset() {
      return getNamespaceOfDataset(i18n("_page"));
   }
   
   /**
    * 
    * @param modifier - e.g. "_page" so that /dataset/ becomes /dataset_page/
    * @return
    */
   private String namespaceOfDataset = null; // For SPEED ONLY; and only for use by this method.
   protected String getNamespaceOfDataset(String modifier) {
      if( namespaceOfDataset == null ) {
         // "T" for token
         String datasetIdentifierT = NameFactory.slashIfThere("dataset"+modifier, getTokenDatasetIdentifier());
         namespaceOfDataset = getNamespaceOfSource() + datasetIdentifierT;
      }
      return namespaceOfDataset;
   }
   
   
   private String namespaceOfSource = null; // For SPEED ONLY; and only for use by this method.
   @Override
   public String getNamespaceOfSource() {
      if( namespaceOfSource == null ) {
         String datasetSourceT        = NameFactory.slashIfThere("source",  this.getTokenDatasetSourceIdentifier()); 
         /*String datasetIdentifierT    = NameFactory.slashIfThere("dataset", this.getTokenDatasetIdentifier());
           String datasetVersionT       = NameFactory.slashIfThere("version", this.getTokenDatasetVersionIdentifier());
           String subjectDiscriminatorT = NameFactory.slashIfThere(           this.getTokenSubjectDiscriminator());
           String conversionIdentifierT = NameFactory.slashIfThere(           this.getStepConversionIdentifier());*/
         namespaceOfSource = getBaseURI() + "/" + datasetSourceT;
      }
      return namespaceOfSource;
   }
   
   
   
   @Override
   public String getNamespaceOfVersionedProvenance() {
      return getBaseURI() + "/" + datasetSourceT + "provenance/" + this.getTokenDatasetIdentifier() + "/" + subjectDiscriminatorT 
                           + datasetVersionT + "conversion/" + getStepConversionIdentifier() + "/";
   }
   
   /**
    * 
    * @return
    */
   private String getNamespaceOfVersionedDatasetLayer() {
      return getURIOfVersionedDataset()+"/conversion/";
   }
   
   
   // URI calculations
   
   

   @Override
   public String getURIOfDatasetSource() {
      return getBaseURI() + "/" + i18n("source") + "/" + this.getTokenDatasetSourceIdentifier();
   }
   

	@Override
   public String getURIOfSiteLevelVoID() {
	   return this.getBaseURI() + "/void.ttl";
   }

	@Override
   public String getURIOfSiteLevelDataset() {
		return this.getBaseURI() + "/void";
   }
	
	
	private String URIOfAbstractDataset = null; // For SPEED ONLY; and only for use by this method.
	@Override
	public String getURIOfAbstractDataset() {
	   if( URIOfAbstractDataset == null ) {
	      // "T" for token
	      String datasetSourceT        = NameFactory.slashIfThere("source",  this.getTokenDatasetSourceIdentifier()); 
	    /*String datasetIdentifierT    = NameFactory.slashIfThere("dataset", this.getTokenDatasetIdentifier());
         String datasetVersionT       = NameFactory.slashIfThere("version", this.getTokenDatasetVersionIdentifier());
         String subjectDiscriminatorT = NameFactory.slashIfThere(           this.getTokenSubjectDiscriminator());
         String conversionIdentifierT = NameFactory.slashIfThere(           this.getStepConversionIdentifier());*/
	      URIOfAbstractDataset = this.getBaseURI() + "/" + datasetSourceT + "dataset/" + this.getTokenDatasetIdentifier();
	   }
	   return URIOfAbstractDataset;
	}
   
	
   private String URIOfVersionedDataset = null; // For SPEED ONLY; and only for use by this method.
   @Override
   public String getURIOfVersionedDataset() {
      if( URIOfVersionedDataset == null ) {
         URIOfVersionedDataset = getURIOfAbstractDataset() + "/version/" + getTokenDatasetVersionIdentifier();
      }
      return URIOfVersionedDataset;
   }
   
   
   private String URIOfLayerDataset = null; // For SPEED ONLY; and only for use by this method.
   @Override
   public String getURIOfLayerDataset() {
      if( URIOfLayerDataset == null ) {
         URIOfLayerDataset = getNamespaceOfVersionedDatasetLayer() + getStepConversionIdentifier();
      }
      return URIOfLayerDataset;
   }
   
   
   private String URIOfVersionedDiscriminatedDatasetLayer = null; // For SPEED ONLY; and only for use by this method.
   @Override
   public String getURIOfVersionedDiscriminatedDatasetLayer() {
      if( URIOfVersionedDiscriminatedDatasetLayer == null ) {
         String subjectDiscriminatorT = NameFactory.slashIfThere(this.getTokenSubjectDiscriminator());
         URIOfVersionedDiscriminatedDatasetLayer = getURIOfAbstractDataset() + "/" + subjectDiscriminatorT + 
               "version/"    + getTokenDatasetVersionIdentifier() + "/" +
               "conversion/" + getStepConversionIdentifier();
      }
      return URIOfVersionedDiscriminatedDatasetLayer;
   }

   
   
   @Override
   public String getURIOfLayerDatasetSample() {
      return getURIOfLayerDataset() + "/subset/sample";
   }
   
   @Override
   public String getURIOfDatasetSameAsSubset() {
      return getNamespaceOfAbstractDataset() + "subset/sameas";
   }
   
   @Override
   public String getURIOfDatasetMetaData() {
      return getNamespaceOfAbstractDataset() + "subset/meta";
   }
   

   
   @Override
   public String getURIOfDiscriminator() {
      //return this.getBaseURI() + "/" + datasetSourceT + datasetIdentifierT + "discriminator/" + getTokenSubjectDiscriminator();
      // TL: just using the name of the datasubset; one less uri floating around and accomplishes same thing.
      return this.getBaseURI() + "/" + datasetSourceT + datasetIdentifierT + getTokenSubjectDiscriminator();
   }
   
   
   // Dump file calculations
   
   
   /**
    * e.g. http://logd.tw.rpi.edu/source/data-gov/file/1008/version/2010-Jul-21/conversion/data-gov-1008-2010-Jul-21
    */
   @Override
   public String getURIOfDumpFileVersioned() {
      // "T" for token
      String datasetSourceT        = NameFactory.slashIfThere("source",  this.getTokenDatasetSourceIdentifier()); 
      String datasetIdentifierT    = NameFactory.slashIfThere("file",    this.getTokenDatasetIdentifier());
      String datasetVersionT       = NameFactory.slashIfThere("version", this.getTokenDatasetVersionIdentifier());
      /*String subjectDiscriminatorT = NameFactory.slashIfThere(           this.getTokenSubjectDiscriminator());
      String conversionIdentifierT = NameFactory.slashIfThere(           this.getStepConversionIdentifier());*/
      return this.getBaseURI() + "/" + datasetSourceT + datasetIdentifierT + datasetVersionT + "conversion/" +
             getTokenDatasetSourceIdentifier() + "-" + getTokenDatasetIdentifier() + "-" + getTokenDatasetVersionIdentifier();
   }
   
   /**
    * Third layer down in the void:subset tree: Dataset, Versioned dataset, [Enhancement] layer.
    * 
    * e.g. http://logd.tw.rpi.edu/source/data-gov/file/1008/version/2010-Jul-21/conversion/data-gov-1008-2010-Jul-21.raw
    * e.g. http://logd.tw.rpi.edu/source/data-gov/file/1008/version/2010-Jul-21/conversion/data-gov-1008-2010-Jul-21.e1
    */
   @Override
   public String getURIOfDumpFileVersionedLayer() {
      // "T" for token
      String datasetSourceT        = NameFactory.slashIfThere("source",     this.getTokenDatasetSourceIdentifier()); 
      String datasetIdentifierT    = NameFactory.slashIfThere("file",       this.getTokenDatasetIdentifier());
      String datasetVersionT       = NameFactory.slashIfThere("version",    this.getTokenDatasetVersionIdentifier());
      /*String subjectDiscriminatorT = NameFactory.slashIfThere(              this.getTokenSubjectDiscriminator());
      String conversionIdentifierT = NameFactory.slashIfThere("conversion", this.getStepConversionIdentifier());*/
      String layer = getTokenEnhancementIdentifier() == null ? "raw" : "e"+getTokenEnhancementIdentifier();
      
      return this.getBaseURI() + "/" + 
             datasetSourceT + datasetIdentifierT + datasetVersionT + "conversion/" +
             getTokenDatasetSourceIdentifier() +"-"+ getTokenDatasetIdentifier() +"-"+ getTokenDatasetVersionIdentifier()+"."+layer;
   }

   
   @Override
   public String getURIOfDumpFileSubsetSameAs() {
      // "T" for token
      String datasetSourceT        = NameFactory.slashIfThere("source",     this.getTokenDatasetSourceIdentifier()); 
      String datasetIdentifierT    = NameFactory.slashIfThere("file",       this.getTokenDatasetIdentifier());
      String datasetVersionT       = NameFactory.slashIfThere("version",    this.getTokenDatasetVersionIdentifier());
      /*String subjectDiscriminatorT = NameFactory.slashIfThere(              this.getTokenSubjectDiscriminator());
      String conversionIdentifierT = NameFactory.slashIfThere("conversion", this.getStepConversionIdentifier());*/
      return this.getBaseURI() + "/" + 
         datasetSourceT + datasetIdentifierT + datasetVersionT + "conversion/" +
         getTokenDatasetSourceIdentifier() +"-"+ getTokenDatasetIdentifier() +"-"+ getTokenDatasetVersionIdentifier()+".sameas";
   }
   
   @Override
   public String getURIOfDumpFileSubsetMeta() {
      // "T" for token
      return getNamespaceOfDumpFiles()+".void";
   }
   
   @Override
   public String getURIOfDumpFileDatasetSample() {
      String layer = getTokenEnhancementIdentifier() == null ? "raw" : "e"+getTokenEnhancementIdentifier();
      return getNamespaceOfDumpFiles()+"."+layer+".sample";
   }
   
   
   /**
    * 
    * @return
    */
   protected String getNamespaceOfDumpFiles() {
      String datasetFileIdentifierT = NameFactory.slashIfThere("file", this.getTokenDatasetIdentifier());
      return this.getBaseURI() + "/" + 
         datasetSourceT + datasetFileIdentifierT + datasetVersionT + "conversion/" +
         getTokenDatasetSourceIdentifier() +"-"+ getTokenDatasetIdentifier() +"-"+ getTokenDatasetVersionIdentifier();
   }
   
   // Filespace calculations
   
   @Override
   public String getFilespaceOfVersionedProvenance() {
      // http://logd.tw.rpi.edu/source/data-gov/provenance_file/1033/fm_alternative_name_file/version/2010-Aug-30/source/FM_ALTERNATIVE_NAME_FILE.CSV
      // should not have the subjectDiscriminator in it:
      // http://logd.tw.rpi.edu/source/data-gov/provenance_file/1033/version/2010-Aug-30/source/FM_ALTERNATIVE_NAME_FILE.CSV
      return getBaseURI() + "/" + 
            datasetSourceT + "file" +  // Changed 2012-Sep-06 from "provenance_file/" + 
            getTokenDatasetIdentifier() + "/" + 
            //subjectDiscriminatorT +
            datasetVersionT;
   }
   
   
   
   // Pagespace calculations
   
   
   
   @Override
   public String getURIOfDatasetSourcePage() {
      return getBaseURI() + "/" +         // TODO: replace with getPagespaceOfDataset()
     i18n("source")+i18n("_page")+"/" + 
      getTokenDatasetSourceIdentifier();
   }
   
   @Override
   public String getURIOfDatasetPage() {
      return getBaseURI() + "/" + 
         datasetSourceT +          // TODO: replace with getPagespaceOfDataset()
        i18n("dataset")+i18n("_page")+"/" + 
         getTokenDatasetIdentifier();
   }
   
   @Override
   public String getURIOfDatasetSameAsSubsetPage() {
      return getPagespaceOfDataset() + "subset/sameas";
   }
   
   @Override
   public String getURIOfDatasetMetaDataPage() {
      return getPagespaceOfDataset() + "subset/meta";
   }
   
   @Override
   public String getURIOfVersionedDatasetPage() {
      return getBaseURI() + "/" + 
         datasetSourceT +          // TODO: replace with getPagespaceOfDataset()
         i18n("dataset")+i18n("_page")+"/" +
         getTokenDatasetIdentifier() + "/" + 
         i18n("version")+"/"+getTokenDatasetVersionIdentifier();
   }
   
   @Override
   public String getURIOfVersionedDatasetLayerPage() {
      return getPagespaceOfDataset() + 
      "version/"+getTokenDatasetVersionIdentifier() + "/conversion/" + getStepConversionIdentifier();
   }
   
   @Override
   public String getURIOfLayerDatasetSamplePage() {
      return getURIOfVersionedDatasetLayerPage() + "/subset/sample";
   }
   
   
   
   // Column query methods
   
   
   
   /**
    * 
    * @return
    */
   @Override
   public int getPrimaryKeyColumnIndex() {
      PrimaryKeyColumnQuerylet querylet = new PrimaryKeyColumnQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get(); // TODO: cache these results
   }

   @Override
   public int getURIKeyColumnIndex() {
      return this.uriKeyCol;
   }

   @Override
   public URI getRange(int columnIndex) {
      RangeQuerylet querylet = new RangeQuerylet(null,columnIndex);
      //System.err.println(querylet.getQueryString());
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }

   @Override
   public String getObjectDelimiter(int column) {
      return objectDelimiterByColChainHead.get(column);
   }
   
   @Override
   public String getObjectDelimiterInChain(int column) {
      return objectDelimiterByColInChain.get(column);
   }
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   @Override
   public Set<Double> getMultipliers(int columnIndex) {
      MultiplierQuerylet multiplierQ = new MultiplierQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, multiplierQ);
      return multiplierQ.get();
   }
   


   @Override
   public Integer getHeaderRow() {
      if( !this.queried.containsKey("headerRow") ) {
         HeaderRowQuerylet querylet = new HeaderRowQuerylet(null);
         QueryletProcessor.processQuery(paramsRepository, querylet);
         this.headerRow = querylet.get();
         this.queried.put("headerRow", ""+this.headerRow);
      }
      return this.headerRow == null ? 1 : this.headerRow;
   }
//   
//   @Override
//   public boolean isRowReferencedRelativeToHeader(long row) {
//      System.err.println(row + " in " +this.headerDisplacementReferences.size() +" ROWS: " + 
//                         rowsReferencedRelativeToHeader + " ? : "+ (row - this.getHeaderRow())) ;
//      return rowsReferencedRelativeToHeader.contains(row - this.getHeaderRow());
//   }
   
   @Override
   public Set<Long> getRowsReferencedRelativeToHeader() {
      return this.rowsReferencedRelativeToHeader;
   }
   
   @Override
   public Integer getDataStartRow() {
      
      int startRowD = getHeaderRow() + 1; // Default
      
      DataStartRowQuerylet querylet = new DataStartRowQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      
      int startRow = startRowD;
      if( querylet.get() != null ) {
         startRow = querylet.get(); // Specified in Enhancements.;
         if( startRow < startRowD ) {
            System.err.println("WARNING: DataStartRow ("+startRow+") <= HeaderRow.("+(startRowD-1)+")");
         }
      }
      return startRow;
   }
   
   @Override
   public Integer getDataEndRow() {
      DataEndRowQuerylet querylet = new DataEndRowQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }
   
   @Override
   public Set<Integer> getOnlyIfColumns() {
      return this.onlyIfColumns;
   }

   @Override
   public Set<String> getInterpetAsNullStrings() {
      return this.interpretedAsNullStringsGlobal;
   }
   
   @Override
   public Set<String> getInterpetAsNullStrings(int columnIndex) {
      Set<String> interpretAsNull = this.interpretedAsNullStrings.get(columnIndex);
      if( interpretAsNull == null ) {
         // Full path given b/c there is an InterpretedAsNullQuerylet in edu.rpi.tw.data.csv.querylets (not column).
         edu.rpi.tw.data.csv.querylets.column.InterpretedAsNullQuerylet querylet = 
                                   new edu.rpi.tw.data.csv.querylets.column.InterpretedAsNullQuerylet(null,columnIndex);
         QueryletProcessor.processQuery(paramsRepository, querylet);
         interpretAsNull = querylet.get();
         interpretAsNull.addAll(getInterpetAsNullStrings()); // NOTE: adding global strings here, too.
         this.interpretedAsNullStrings.put(columnIndex, interpretAsNull);
      }
      return interpretAsNull;
   }
   
   @Override
   public Set<String> getInterpetAsTrueStrings() {
      return null;
   }
   
   @Override
   public Set<String> getInterpetAsFalseStrings() {
      return null;
   }

   @Override
   public Set<String> getInterpetAsTrueStrings(int columnIndex) {
      InterpretedAsTrueQuerylet querylet = new InterpretedAsTrueQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }
   
   @Override
   public Set<String> getInterpetAsFalseStrings(int columnIndex) {
      InterpretedAsFalseQuerylet querylet = new InterpretedAsFalseQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }
   
   @Override
   public Set<Integer> getRepeatPreviousIfEmptyColumns() {
      return this.repeatPreviousColumns;
   }
   
   @Override
   public Set<String> getRepeatPreviousSymbols() {
      return this.repeatPreviousSymbols;
   }
   
   @Override
   public String getColumnLabel(int columnIndex) {
      ColumnLabelQuerylet querylet = new ColumnLabelQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }

   @Override
   public Set<Value> getColumnComment(int columnIndex) {
      ColumnCommentQuerylet querylet = new ColumnCommentQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }
   
   @Override
   public String getSubjectTypeLocalName() {
      return this.subjectRowType;
   }
   
   @Override
   public HashMap<String,URI> getDateTimePatterns(int columnIndex) {
      DateTimePatternQuerylet querylet = new DateTimePatternQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }

   @Override
   public HashMap<String,URI> getDatePatterns(int columnIndex) {
      DatePatternQuerylet querylet = new DatePatternQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }
   
   @Override
   public int getDateTimeTimeZone(int columnIndex) {
      DateTimePatternQuerylet querylet = new DateTimePatternQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.getTimezone();
   }

   
   @Override
   public boolean isColumnURISafe(int columnIndex) {
      URISafeQuerylet querylet = new URISafeQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }
   
   // Types
   
   
   
   @Override
   public Set<String> getTypeLocalNames() {
      if( this.classLabels == null ) {
         this.classLabels = new HashSet<String>();
         this.classLabels.add(    getSubjectTypeLocalName()         );
         this.classLabels.addAll( getImplicitBundleTypeLocalNames() );
         this.classLabels.addAll( getRangeLocalNames()         );
      }
      return this.classLabels;
   }
   
   @Override
   public URI getObjectType(int columnIndex) {
      URI   type  = null;
      Value typeV = this.rangeNameByCol.get(columnIndex);
      if( typeV instanceof URI ) {
         type = (URI) typeV;
      }else if( typeV != null ) {
         String typeS = fillTemplate(typeV.stringValue());
         if( typeS.indexOf("://") < 0 ) {
            typeS = fillTemplate("[/sd]vocab/"+typeV.stringValue());
         }
         type = vf.createURI(typeS);
      }
      if( type != null) System.err.println("TYPE: "+columnIndex+" "+type.stringValue());
      return type;
   }
   
   @Override
   public String getObjectTypeLocalName(int columnIndex) {
      String type = null;
      if( this.rangeNameByCol.get(columnIndex) != null ) {
         type = this.rangeNameByCol.get(columnIndex).stringValue();
      }
      return type;
   }

   /**
    * The property from the row URI to the implicit bundle.
    */
   @Override
   public String getImplicitBundlePropertyName(int columnIndex) {
      //System.err.println(columnIndex+": "+this.bundlePropertyName+" = "+this.bundlePropertyName.get(columnIndex));
      return this.bundlePropertyName.get(columnIndex);
   }
   
   /**
    * RespondentP
    * 
    * Used to construct the URI of the implicit bundle.
    * 
    * version/2001-Jan-01/RespondentP/respondent_2
    */
   @Override
   public String getImplicitBundleIdentifier(int columnIndex) {
      return this.getImplicitBundlePropertyName(columnIndex);
   }
   
   @Override
   public Set<Integer> getBundledByColumns(int columnIndex) {
      //System.err.println(this.bundledByColumn.size() + " " + this.bundledByColumn.get(columnIndex) + " " + columnIndex);
      return this.bundledByColumn.containsKey(columnIndex) ? this.bundledByColumn.get(columnIndex) : EMPTY_SetOfInteger;
   }
   
   @Override
   public Set<Integer> getBundledByColumn(int columnIndex) {
      int col = 0; // TODO: HACK
      for( int sCol : this.bundledByColumn.get(columnIndex) ) {
         if( col > 0 ) {
            System.err.println("WARNING: bundling into explict subject at column " + col + " is being omitted; superceded by subject at "+ sCol);
         }
         col = sCol;
      }
      //System.err.println(this.bundledByColumn.size() + " " + this.bundledByColumn.get(columnIndex) + " " + columnIndex);
      return getBundledByColumns(columnIndex);
   }
   
   @Override
   public HashMap<URI, HashSet<Value>> getImplicitBundleAnnotations(int columnIndex) {
      return this.implicitBundleAnnotations.get(columnIndex);
   }

   
   /*@Override
   public String getObjectTemplate(int columnIndex) {
      return this.objectTemplateByCol.get(columnIndex);
   }*/
   
   /**
    * 
    */
   @Override
   public List<String> getObjectTemplates(int columnIndex) {
      if( this.objectTemplatesByCol.get(columnIndex) == null ) {
         this.objectTemplatesByCol.put(columnIndex, new ArrayList<String>());
      }
      return this.objectTemplatesByCol.get(columnIndex);
   }
   
	@Override
   public Set<URI> getObjectLabelProperties(int columnIndex) {
	   return this.objectLabelPropertiesByCol.get(columnIndex);
   }
   
   @Override
   public String getObjectLabelTemplate(int columnIndex) {
      return this.objectLabelPatternByCol.get(columnIndex);
   }
   
   @Override
   public Set<String> getRangeLocalNames() {
      HashSet<String> promotionTypes = new HashSet<String>();
      
      ColumnIndexesQuerylet querylet = new ColumnIndexesQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      for( int columnIndex : querylet.get() ) {
         Value givenV = rangeNameByCol.get(columnIndex);
         if( givenV == null ) continue;
         
         String given     = givenV.stringValue();
         String templated = fillTemplate(given);
         if( givenV instanceof Literal && given.equals(templated) ) {
            // Literal with no template variables.
            promotionTypes.add(given);            
            //System.err.println("LOCAL TYPE NAME: "+given);
         }
      }
      return promotionTypes;
   }
   
   /**
    * No need to cache; only requested once per conversion.
    */
   @Override
   public HashMap<String, HashSet<URI>> getObjectSameAsLinks(int columnIndex) {
      ObjectSameAsLinksQuerylet querylet = new ObjectSameAsLinksQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      this.isDirectToLODCloud       = querylet.isDirect();
      this.isLODLinkCaseInsensitive = querylet.isCaseInsensitive();
      this.includesLODLinksGraph    = this.includesLODLinksGraph || querylet.doesInclude(); // true if any column says to do it.
      this.lodLinksGraphs.put(columnIndex, querylet.getLODLinksGraph());
      
      return querylet.get();
   }
   
   /**
    *
    */
   @Override
   public HashMap<String, HashSet<URI>> getSubjectSameAsLinks(int columnIndex) {
      SubjectSameAsLinksQuerylet querylet = new SubjectSameAsLinksQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      this.isLODLinkCaseInsensitive = querylet.isCaseInsensitive();
      this.lodLinksGraphs.put(columnIndex, querylet.getLODLinksGraph());
      return querylet.get();
   }
   
   @Override
   public Collection<Repository> getLODLinksRepositories() {
      return this.lodLinksGraphs.values();
   }
   
   @Override
   public Repository getLODLinksRepository(int columnIndex) {
   	return this.lodLinksGraphs.get(columnIndex);
   }
   
   @Override
   public boolean referenceLODLinkedURIsDirectly(int columnIndex) {
      return this.isDirectToLODCloud;
   }
   
   @Override
   public boolean lodLinkCaseInsensitive(int columnIndex) {
      return this.isLODLinkCaseInsensitive;
   }
   
   @Override
   public boolean includeLODLinksGraph(int columnIndex) {
      return this.includesLODLinksGraph;
   }

   
   // Provenance methods.
   
   
   @Override
   public String getSourceUsageURL() {
      return this.urlSourceUsageQuerylet.get();
   }

   @Override
   public String getSourceUsageDateTime() {
      return this.urlSourceUsageQuerylet.getUsageDateTime();
   }

   @Override
   public String getSourceUsageURLModificiationTime() {
      return this.urlSourceUsageQuerylet.getURLModificationDate();
   }


   @Override
   public Set<URI> getAuthors() {
      AuthorsQuerylet querylet = new AuthorsQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }

   @Override
   public HashMap<String, Value> getCodebook(int columnIndex) {
//      CodebookQuerylet querylet = new CodebookQuerylet(null,columnIndex);
//      QueryletProcessor.processQuery(paramsRepository, querylet);
//      return querylet.getHashMap();
      return this.codebookByCol.containsKey(columnIndex) ? this.codebookByCol.get(columnIndex) 
                                                         : new HashMap<String,Value>(); // TODO: CodebookQuerylet doesn't look like it's returning null, but ValueFactoryHandler is getting nulls.
   }

   @Override
   public Set<URI> getSuperProperties(int columnIndex) {
      return this.subPropertyByCol.get(columnIndex) != null ? this.subPropertyByCol.get(columnIndex) : EMPTY_HashSetOfURI;
   }

   @Override
   public Value getEquivalentPropertyChainHead(int columnIndex) {
	   // This is only called once when the header is processed by CSVtoHeader.visitHeader.
      EquivalentPropertyQueryletChainHead querylet = new EquivalentPropertyQueryletChainHead(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get(); // Just passing the value through, let them handle the template.
   }
   
   @Override
   public Value getEquivalentPropertyInChain(int columnIndex) {
	   // This is only called once when the header is processed by CSVtoHeader.visitHeader.
      EquivalentPropertyQueryletInChain querylet = new EquivalentPropertyQueryletInChain(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get(); // Just passing the value through, let them handle the template.
   }
   
   @Override
   public Set<Namespace> getOutputNamespaces() {
      PrefixMappingsQuerylet querylet = new PrefixMappingsQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      HashSet<Namespace> namespaces = new HashSet<Namespace>(querylet.get());
      RepositoryConnection conn = null; 
      try {
         conn = this.paramsRepository.getConnection();
         for( Namespace ns : conn.getNamespaces().asList() ) {
            namespaces.add(ns);
         }
      } catch (RepositoryException e) {
         e.printStackTrace();
      } finally {
         if( conn != null ) {
            try {
               conn.close();
            } catch (RepositoryException e) {
               e.printStackTrace();
            }
         }
      }
      return namespaces;
   }

   @Override
   public Collection<String> getImplicitBundleTypeLocalNames() {
      return this.bundleType.values();
   }
   
   @Override
   public Boolean isImplicitBundleAnonymous(int columnIndex) {
      return this.bundleAnonymous.get(columnIndex);
   }

   @Override
   public String getImplicitBundleTypeLocalName(int columnIndex) {
      return this.bundleType.get(columnIndex);
   }

   @Override
   public Set<URI> getExternalSuperClassesOfLocalClass(String typeLabel) {
      return this.superClassesOfLocalClass.get(typeLabel) != null ? this.superClassesOfLocalClass.get(typeLabel) 
                                                                  : new HashSet<URI>();
   }

   @Override
   public HashMap<String, Set<URI>> getExternalSuperClasses() {
      return this.superClassesOfLocalClass;
   }

   
   // Cell-based methods.
   
   
   @Override
   public boolean isCellBased() {
      return this.cellBasedValue.size() > 0;
   }
   
   @Override
   public boolean isCellBased(int columnIndex) {
      return this.cellBasedValue.containsKey(columnIndex);
   }

   @Override
   public Set<Integer> getCellBasedColumns() {
      return this.cellBasedValue.keySet();
   }
   
   @Override
   public Integer getFirstCellBasedColumn() {
      return this.firstCellBasedColumn;
   }

   @Override
   public Value getCellBasedValue(int columnIndex) {
      return this.cellBasedValue.get(columnIndex);
   }
   
   @Override
   public Value getCellBasedUpPredicateLN(int columnIndex) {
      // TODO: Is this right? same as above...
      return this.cellBasedValue.get(columnIndex);
   }

   @Override
   public HashMap<Value,Set<Value>> getCellBasedUpPredicateValueSecondaries(int columnIndex) {
      ResourceAnnotationsQuerylet querylet = new ResourceAnnotationsQuerylet(null,columnIndex);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }

   @Override
   public Value getCellBasedOutPredicateLN(int columnIndex) {
      return this.cellBasedValue.get(columnIndex);
   }
   
   @Override
   public Value getCellBasedOutPredicate(int columnIndex) {
      return this.cellBasedOutPredicates.get(columnIndex);
   }
   
   // Key template
   
   
   @Override
   public String getDomainTemplate() {
      return this.domainTemplate;
   }
   
   @Override
   public Integer getDomainTemplateColumn() {
      return this.domainTemplateColumn;
   }

   @Override
   public String getImplicitBundleNameTemplate(int columnIndex) {
      return this.implicitBundleNameTemplateByCol.get(columnIndex);
   }
   
   // Human redirects
   
   /**
    * 
    */
   @Override
   public Set<String> getSubjectHumanRedirects() {
      return this.humanRedirects;
   }
   
   @Override
   public HashMap<Value,HashSet<Value>> getLayerDatasetDescriptions() {
      LayerDatasetDescriptionsQuerylet querylet = new LayerDatasetDescriptionsQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }
   
   @Override
   public HashMap<String,String> getAdditionalDescriptions() {
      // TODO: need to cache?
      AdditionalDescriptionsQuerylet querylet = new AdditionalDescriptionsQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
      // TODO: This seems redundant with ResourceAnnotationsQuerylet, which is used in the constructor.
      // but, this query is more elaborate (developed?)...
   }
   
   /**
    * Given to ResourceValueHandler to add PO on any promoted resources 
    * 
    * @see CSVtoRDF#visitHeader
    * 
    * @param columnIndex - 
    */
   @Override
   public HashMap<URI, Set<Value>> getConstantAdditionalDescriptions(int columnIndex) {
      // NOTE: CSVtoRDF#visitHeader calls eParams.getAdditionalDescriptions(columnIndex).put(predicateR, object);
      
      
      // WHAT? This is being populated by CSVtoRDF#visitHeader() ?
      // There better be a good reason for this, b/c I know better than this.
      // Did some calculation need to happen using the header values?
      // This looks like a factory pattern. - TL
      //
      // The reason: visitHeaders does logic on the predicateR and does some ontology assertions. - 2011 Apr 1 TL
      if( !additionalColumnDescriptions.containsKey(columnIndex)) {
         additionalColumnDescriptions.put(columnIndex, new HashMap<URI,Set<Value>>());
      }
      // index 0 is populated in DefaultEnhancementParameters's constructor.
      return additionalColumnDescriptions.get(columnIndex);
   }
   
   @Override
   public HashMap<String, Set<Value>> getContextualAdditionalDescriptions(int columnIndex) {
      return this.additionalColumnContextualDescriptions.get(0);
   }

   @Override
   public boolean isColumnOmitted(int columnIndex) {
      return this.omittedColumns.contains(columnIndex);
   }

   @Override
   public Set<Long> getExampleResourceRows() {
      return this.exampleResourceRows;
   }
   
   @Override
   public boolean isExampleResourceRow(long rowNumber) {
      return this.exampleResourceRows.contains(rowNumber);
   }

   @Override
   public void printNamespaces(PrintStream stream) {
      /*
       * Sorry for lacking Reflection :-(
       * 
       * grep "String get" ../src/edu/rpi/tw/data/csv/NamespaceCalculator.java | sed 's/;//g' | awk '{printf("stream.println(\"%s:\" + %s);\n",$2,$2)}'
       * grep "String get" ../src/edu/rpi/tw/data/csv/NamespaceCalculator.java | sed 's/;//g' | awk '{printf("stream.println(\"%s :\" + %s);\n",$2,$2)}' | awk -f alignColumn.awk
       * grep "String get" ../src/edu/rpi/tw/data/csv/NamespaceCalculator.java | sed 's/;//g' | sort | awk 'BEGIN{print "//ream.println(\"getURIOfVersionedDiscriminatedDat-----ayer()"}{printf("stream.println(\"%s \" + %s);\n",$2,$2)}' | awk -f alignColumn.awk
       */
      stream.println("getFilespaceOfVersionedProvenance()          " + getFilespaceOfVersionedProvenance());
      stream.println("getNamespaceOfAbstractDataset()              " + getNamespaceOfAbstractDataset());
      stream.println("getNamespaceOfEnhancementProperty()          " + getNamespaceOfEnhancementProperty());
      stream.println("getNamespaceOfRawProperty()                  " + getNamespaceOfRawProperty());
      stream.println("getNamespaceOfSource()                       " + getNamespaceOfSource());
      stream.println("getNamespaceOfVersionedProvenance()          " + getNamespaceOfVersionedProvenance());
      stream.println("getNamespaceOfVocab()                        " + getNamespaceOfVocab());
      stream.println("getURIOfDataset()                            " + getURIOfAbstractDataset());
      stream.println("getURIOfDatasetMetaData()                    " + getURIOfDatasetMetaData());
      stream.println("getURIOfDatasetMetaDataPage()                " + getURIOfDatasetMetaDataPage());
      stream.println("getURIOfDatasetPage()                        " + getURIOfDatasetPage());
      stream.println("getURIOfDatasetSameAsSubset()                " + getURIOfDatasetSameAsSubset());
      stream.println("getURIOfDatasetSameAsSubsetPage()            " + getURIOfDatasetSameAsSubsetPage());
      stream.println("getURIOfDatasetSource()                      " + getURIOfDatasetSource());
      stream.println("getURIOfDatasetSourcePage()                  " + getURIOfDatasetSourcePage());
      stream.println("getURIOfDatasetType()                        " + getURIOfDatasetType());
      stream.println("getURIOfDiscriminator()                      " + getURIOfDiscriminator());
      stream.println("getURIOfDumpFileDatasetSample()              " + getURIOfDumpFileDatasetSample());
      stream.println("getURIOfDumpFileSubsetMeta()                 " + getURIOfDumpFileSubsetMeta());
      stream.println("getURIOfDumpFileSubsetSameAs()               " + getURIOfDumpFileSubsetSameAs());
      stream.println("getURIOfDumpFileVersioned()                  " + getURIOfDumpFileVersioned());
      stream.println("getURIOfDumpFileVersionedLayer()             " + getURIOfDumpFileVersionedLayer());
      stream.println("getURIOfLayerDatasetSample()                 " + getURIOfLayerDatasetSample());
      stream.println("getURIOfLayerDatasetSamplePage()             " + getURIOfLayerDatasetSamplePage());
      stream.println("getURIOfVersionedDataset()                   " + getURIOfVersionedDataset());
      stream.println("getURIOfVersionedDatasetLayer()              " + getURIOfLayerDataset());
      stream.println("getURIOfVersionedDatasetLayerPage()          " + getURIOfVersionedDatasetLayerPage());
      stream.println("getURIOfVersionedDatasetPage()               " + getURIOfVersionedDatasetPage());
      stream.println("getURIOfVersionedDiscriminatedDatasetLayer() " + getURIOfVersionedDiscriminatedDatasetLayer());
   }


	@Override
   public Value tryExpand(String template, Value templateV) {
	   return tryExpand(template, null);
   }
	
   @Override
   public Value tryExpand(String template) {
      
      String valueS = template;
      
      if( doesExpand(template) ) {
         // Value contained a template variable that needs to expand (could be to URI or still a local).
         valueS = fillTemplate(template);
      }
      
      return ResourceValueHandler.isURI(valueS) ? vf.createURI(valueS) : vf.createLiteral(valueS);
   }
   
   @Override
   public Value tryExpand(Value template) {
      return tryExpand(template.stringValue());
   }

   @Override
   public void assertEnhancementsRepository(RepositoryConnection metaConn) {
      RepositoryConnection myConn;
      try {
         myConn = paramsRepository.getConnection();
         //System.err.println("PARAMS SIZE: " + myConn.size());
         Resource nullR = (Resource) null;
         for( Statement triple : myConn.getStatements(null, null, nullR, false).asList() ) {
            //System.err.println(triple.toString());
            metaConn.add(triple.getSubject(), triple.getPredicate(), triple.getObject());
         }
         metaConn.commit();
      } catch (RepositoryException e) {
         e.printStackTrace();
      }
   }

   @Override
   public HashMap<String, HashMap<Value,Set<Value>>> getSubjectAnnotationsViaObjectSearches(int columnIndex) {
      return subjectAnnotationsViaObjectSearchesByCol.get(columnIndex) != null 
                                                            ? subjectAnnotationsViaObjectSearchesByCol.get(columnIndex) 
                                                            : new HashMap<String, HashMap<Value,Set<Value>>>() ;
   }

   @Override
   public Set<URI> getInverses(int columnIndex) {
   	return this.inversesByCol.containsKey(columnIndex) ? 
   			 this.inversesByCol.get(columnIndex)         : new HashSet<URI>();
   }
   
   @Override
   public char getDelimiter() {
      CellDelimiterQuerylet querylet = new CellDelimiterQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      this.useful = this.useful || ",".equals(querylet.getDelimiter());
      return querylet.getDelimiter();
   }

   @Override
   public void setCellReferencedRelativeToHeader(long row, long column, String value) {
      // TODO: don't think we need to implement this.
   }


   // i18n 
   
   /**
    * @param englishToken - e.g. 'source', 'dataset', 'version', '_page', 'typed', 'value-of'
    * @return i18nToken - e.g. 'sourcia', 'datasetia', 'versionia', '_pagina', 'typo', 'valia'
    */
   protected String i18n(String englishToken) {
      return i18n.containsKey(englishToken) ? i18n.get(englishToken) : englishToken;
   }

   @Override
   public boolean hasLargeValues() {
      LargeValueQuerylet querylet = new LargeValueQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }

   @Override
   public Charset getCharset() {
      CharsetQuerylet querylet = new CharsetQuerylet(null);
      QueryletProcessor.processQuery(paramsRepository, querylet);
      return querylet.get();
   }
   
   @Override
   public boolean hasIdentifiersSpecified() {
      return this.hasIdentifiersSpecified;
   }

	@Override
	public boolean useful() {
		if( useful ) {
			return useful;
		}else {
	      this.useful = this.useful || !",".equals(getDelimiter());
	      
	      ColumnIndexesQuerylet columnsQ = new ColumnIndexesQuerylet(null);
	      QueryletProcessor.processQuery(paramsRepository, columnsQ);
	      
	      for( int c : columnsQ.get() ) {
	      	
	      	// conversion:range rdfs:Resource
	      	//System.err.println(this.getRange(c).stringValue());
	      	this.useful = this.useful || !getRange(c).stringValue().equals(RDFS.LITERAL.stringValue());

	      	// convesion:label "Something"
	      	this.useful = this.useful || getColumnLabel(c) != null;
	      	
	      	// conversion:interpret [ conversion:symbol ?x; conversion:interpretation conversion:null ]
	      	this.useful = this.useful || getInterpetAsNullStrings(c).size() > 0;
	      	
	      	// conversion:equivalent_property :x
	      	this.useful = this.useful || getEquivalentPropertyChainHead(c) != null;
	      }
		}
		return useful;
	}

	@Override
   public Set<Integer> getInterpretWithRegexColumnsChainHead() {
	   return this.interpWithRegexColsChainHead;
   }

	@Override
   public HashMap<String, String> getInterpretWithRegexesChainHead(int columnIndex) {
	   return this.interpWithRegexByColChainHeads.get(columnIndex);
   }
	
	@Override
   public Set<Integer> getInterpretWithRegexColumnsInChain() {
	   return this.interpWithRegexColsInChain;
   }

	@Override
   public HashMap<String, String> getInterpretWithRegexesInChain(int columnIndex) {
	   return this.interpWithRegexByColInChain.get(columnIndex);
   }
	

	@Override
   public boolean isColumnUnlabeled(int col) {
	   return this.omitResourceLabelsByCol.get(col);
   }

	@Override
   public HashMap<Value, HashMap<Value, Set<Value>>> getTemplatedStatements() {
		StatementsQuerylet querylet = new StatementsQuerylet(null);
		QueryletProcessor.processQuery(this.paramsRepository, querylet);
	   return querylet.get();
   }

	@Override
   public boolean isChained(int col) {
		EnhancementChainQuerylet querylet = new EnhancementChainQuerylet(null, col);
		QueryletProcessor.processQuery(paramsRepository, querylet);
	   return querylet.get();
   }

   @Override
   public Set<Resource> getRowTopics() {
      
      Set<Resource> topics = new HashSet<Resource>();
      
      RepositoryConnection conn = null;
      try {
         conn = this.paramsRepository.getConnection();
         for( Statement statement : Iterations.asList(conn.getStatements(null, Conversion.topic, null, false)) ) {
            if( statement.getObject() instanceof Resource ) {
               topics.add((Resource) statement.getObject());
            }
         }
      } catch (RepositoryException e) {
         e.printStackTrace();
      } finally {
         if( conn != null ) {
            try {
               conn.close();
            } catch (RepositoryException e) {
               e.printStackTrace();
            }
         }
      }

      return topics;
   }

   @Override
   public HashMap<URI,Set<Value>> getTopicDescriptions(Resource topic) {
      
      HashMap<URI,Set<Value>> descriptions = new HashMap<URI,Set<Value>>();
      
      RepositoryConnection conn = null;
      try {
         conn = this.paramsRepository.getConnection();
         for( Statement statement : Iterations.asList(conn.getStatements(topic, null, null, false)) ) {
            if( ! descriptions.containsKey(statement.getPredicate()) ) {
               descriptions.put(statement.getPredicate(), new HashSet<Value>());
            }
            descriptions.get(statement.getPredicate()).add(statement.getObject());
         }
      } catch (RepositoryException e) {
         e.printStackTrace();
      } finally {
         if( conn != null ) {
            try {
               conn.close();
            } catch (RepositoryException e) {
               e.printStackTrace();
            }
         }
      }
      return descriptions;
   }

   @Override
   public boolean excludeRowNumbers() {
      return this.excludeRowNumbers;
   }

   @Override
   public boolean excludeDCTermsReference() {
      return this.excludeDCRef;
   }

   @Override
   public boolean excludeVoIDReference() {
      return this.excludeVoIDRef;
   }
}