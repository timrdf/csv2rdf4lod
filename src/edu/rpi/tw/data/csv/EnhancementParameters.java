package edu.rpi.tw.data.csv;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * Interface to provide CSVtoRDF the interpretation guidance it needs. 
 * Abstracts away the enhancement parameter encoding.
 */
public interface EnhancementParameters extends NamespaceCalculator {

   
   
   // Naming Parameters
   
   
   
   /**
    * 
    * @return
    */
   String getBaseURI();
   
   /**
    * 
    * @return
    */
   String getTokenDatasetSourceIdentifier();
   
   /**
    * 
    * @return
    */
   String getTokenDatasetIdentifier();
   
   /**
    * 
    * @return
    */
   String getTokenDatasetVersionIdentifier();
   
   /**
    * "raw" or "enhancement/1" (e.g.)
    * 
    * {@link #getStepConversionIdentifier()}
    * @return
    */
   String getTokenEnhancementIdentifier();
   
   /**
    * "raw" or "1" (e.g.)
    * @return
    */
   String getTokenConversionIdentifier(String prependE);
   
   /**
    * {@link #getTokenEnhancementIdentifier()}
    * @return
    */
   String getStepConversionIdentifier();
   
   /**
    * OPTIONAL
    * @return
    */
   String getTokenSubjectDiscriminator();
   
   /**
    * 
    * @return
    */
   Set<Namespace> getOutputNamespaces();
   
   /**
    * True if subjectDiscriminator is present and has length.
    * Multiple CSVs require the subjectDiscriminator to avoid naming the same rows of different files as the same URI.
    * 
    * @return
    */
   boolean isMultiPart();

   boolean isEnhanced();
   
   
   // Principle enhancements
   /**
    * @return the delimiter of the table - csv? tab delim? pipe?
    */
   char getDelimiter();
   
   /**
    * 
    * @return true if any of the cells has a "big" length (100,000 characters in javacsv's case).
    */
   boolean hasLargeValues();
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   String getColumnLabel(int columnIndex);

   /**
    * 
    * @param columnIndex
    * @return
    */
   Set<Value> getColumnComment(int columnIndex);
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   URI getRange(int columnIndex);
   
   
   
   // Structural Enhancements
   
   
   
   /**
    * 
    * @return
    */
   Integer getHeaderRow();
   
   /**
    * conversion:object "[#H+1]"
    * 
    * @return true if some template references the given row as a position relative to the header row.
    */
   //boolean isRowReferencedRelativeToHeader(long row);
   
   /**
    * @return
    */
   public Set<Long> getRowsReferencedRelativeToHeader();
   
   /**
    * 
    * @return
    */
   Integer getDataStartRow();   
   
   /**
    * 
    * @return
    */
   Integer getDataEndRow();
   
   /**
    * 
    * @return columns that MUST have a value for the row to be processed. If not, skip row.
    */
   Set<Integer> getOnlyIfColumns();
   
   /**
    * 
    * @return columns that "inherit" value from previous row if the current row's column value is empty.
    */
   Set<Integer> getRepeatPreviousIfEmptyColumns();
   
   /**
    * 
    * @return symbols that indicate the above value should be repeated.
    */
   Set<String> getRepeatPreviousSymbols();
   
   /**
    * 
    * @param rowNumber
    * @return
    */
   Set<Long> getExampleResourceRows();
   
   /**
    * 
    * @param rowNum
    * @return true iif this row should be processed as an example.
    */
   boolean isExampleResourceRow(long rowNum);
   
   /**
    * 
    * @param columnIndex
    * @return true iif this column should NOT be processed.
    */
   boolean isColumnOmitted(int columnIndex);

      
   
   // Pattern (parsing)
   
   
   
   /**
    * Global, applies to all columns.
    * NOTE: {@link #getInterpetAsNullStrings(int)} includes these values, so only one call needs to be made to that.
    * 
    * @return the set of strings that should be ignored in ALL columns (i.e., no triple asserted).
    */
   Set<String> getInterpetAsNullStrings();
   
   Set<String> getInterpetAsTrueStrings();

   Set<String> getInterpetAsFalseStrings();

   /**
    * NOTE: The InterpretAsNull strings for each column includes the strings that apply to all columns, too.
    * i.e., the set returned is the union of this column plus the global strings from {@link #getInterpetAsNullStrings()}
    * 
    * @param columnIndex - the column that these strings should be ignore.
    * @return the set of strings that should be ignored (i.e., no triple asserted).
    */
   Set<String> getInterpetAsNullStrings(int columnIndex); // TODO: did all of these make it? Should they be consolidated?

   Set<String> getInterpetAsTrueStrings(int columnIndex);

   Set<String> getInterpetAsFalseStrings(int columnIndex);
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   HashMap<String, URI> getDatePatterns(int columnIndex);
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   HashMap<String,URI> getDateTimePatterns(int columnIndex);

   /**
    * Used in conjunction with {@link #getDateTimePatterns(int)}.
    * @param columnIndex
    * @return number of minutes displaced from GMT.
    */
   int getDateTimeTimeZone(int columnIndex);

   
   
   // Templates (subject/object naming)
   
   
   
   /**
    * The column whose value is a URI and should be used as is, i.e. cast. 
    * (not Resource promotion into namespace using local name).
    * @deprecated - use {@link #getDomainTemplate()}
    * @return
    */
   int getURIKeyColumnIndex();
   
   /**
    * The column 
    * @deprecated - use {@link #getDomainTemplate()}
    * @return
    */
   int getPrimaryKeyColumnIndex();

   /**
    * The column from which the template was asserted. Needed to fulfill "[.]" template patterns.
    * @return
    */
   Integer getDomainTemplateColumn();
   
   /**
    * 
    * @return
    */
   String getDomainTemplate();
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   HashMap<String, Value> getCodebook(int columnIndex);
   
   /**
    * 
    * @param columnIndex
    * @return Prioritized list of templates to apply. First is most important (or, just canonical).
    */
   List<String> getObjectTemplates(int columnIndex);
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   Set<Double> getMultipliers(int columnIndex);
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   // replaced by getObjectTemplates: String getObjectTemplate(int columnIndex);
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   String getObjectLabelTemplate(int columnIndex);
   
   
   
   // Types (subject/object)
   
   /**
    * Aggregates {@link #getSubjectTypeLocalName()}, {@link #getImplicitBundleTypeLocalNames()}, and 
    * {@link #getRangeLocalNames()}
    * 
    * @return all local names for all types (subject, implicit bundles, objects).
    */
   Set<String> getTypeLocalNames();
   
   /**
    * Local name of rdfs:Class that subject (row/col) of triple should be typed.
    * 
    * In enhancement parameters, asserted as:
    *   conversion:domain_name "Person";
    * 
    * @return
    */
   String getSubjectTypeLocalName();
   
   /**
    * Return the set of all local names of types within the local dataset vocab namespace.
    * This does not return local names for types with templates containing [/] or external URIs.
    * 
    *  In enhancement parameters, asserted as:
    *    conversion:range_name "Person";
    *  
    * @return
    */
   Set<String> getRangeLocalNames();
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   String getObjectTypeLocalName(int columnIndex);   // TODO: consolidate/reconcile these.
   
   /**
    * @deprecated {@link #getRangeLocalNames()} gives multiple.
    * @param columnIndex
    * @return
    */
   URI getObjectType(int columnIndex);
   
   /**
    * 
    */
   Collection<String> getImplicitBundleTypeLocalNames();
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   String getImplicitBundleTypeLocalName(int columnIndex);
   
   
   
   // External vocabulary
   
   
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   Set<URI> getSuperProperties(int columnIndex);

   /**
    * Returns URIs and Values that are templates with 
    * global variables            (e.g. [/sdv] [v] [e]) or 
    * column-contextual variables (e.g. [@] [H] [c] [L]).
    * 
    * @param columnIndex
    * @return the URI of the property that 'columnIndex' represents.
    */
   Value getEquivalentPropertyChainHead(int columnIndex);
   

   /**
    * 
    * @param columnIndex
    * @return
    */
	Value getEquivalentPropertyInChain(int columnIndex);

	
   /**
    *
    * @param typeLabel
    * @return
    */
   Set<URI> getExternalSuperClassesOfLocalClass(String typeLabel);

   /**
    * Value can be a pattern or a URI.
    * 
    * @return
    */
   HashMap<String, Set<URI>> getExternalSuperClasses();


   
   // Bundle (implicit/explicit)
   
  
  
   /**
    * Implict bundle.
    * 
    * Label of the property inserted to point to the intermediary resource that will bundle this column index.
    * 
    * The property/value of this column's value should be describing the implicit resource instead of the row URI.
    * 
    * @param columnIndex
    * @return the local name of the property from the row URI to the implicit resource.
    */
   String getImplicitBundlePropertyName(int columnIndex);
   
   /**
    * 
    * @param columnIndex
    * @return
    */
	Boolean isImplicitBundleAnonymous(int columnIndex);
	
   /**
    * Implicit bundle.
    * 
    * Identifier for the bundle. All columns in the same bundle will get the same identifier.
    * 
    * @param columnIndex
    * @return
    */
   String getImplicitBundleIdentifier(int columnIndex);
   
   /**
    * Implicit bundle.
    * 
    * @param columnIndex
    */
   String getImplicitBundleNameTemplate(int columnIndex);
   
   /**
    * Explicit bundle column
    * 
    * The property/value of this column's value should be describing the promoted resource of other column instead of
    * the row URI.
    * 
    * @param columnIndex
    * @return the column whose value should be the subject of the given columnIndex. 
    *         If none specified, return 0 (the "row"'s URI - the default behavior).
    */
   Set<Integer> getBundledByColumns(int columnIndex);
   
   /**
    * OLDER version to be replaced by {@link #getBundledByColumns(int)}
    * @param columnIndex
    * @return
    * @deprecated
    */
   Set<Integer> getBundledByColumn(int columnIndex);

   /**
    * 
    * @param columnIndex
    * @return
    */
   HashMap<URI,HashSet<Value>> getImplicitBundleAnnotations(int columnIndex);


   
   // Provenance

   
   
   /**
    * URL of original document that led to this csv being converted. Could be a non-csv file.
    * 
    * @return
    */
   String getSourceUsageURL();

   /**
    * DateTime that the original document was retrieved. e.g., system time of pcurl.sh.
    * 
    * @return
    */
   String getSourceUsageDateTime();

   /**
    * DateTime that HTTP server reports for the modification of the original document.
    * @return
    */
   String getSourceUsageURLModificiationTime();

   /**
    * Authors of this parameters file.
    * @return
    */
   Set<URI> getAuthors();


   
   // Cell-based methods
   
   
   
   /**
    * 
    * @return true if any column should be interpreted as cell-based (instead of row-based).
    */
   boolean isCellBased();

   /**
    * 
    * @param columnIndex
    * @return true if the value at columnIndex should be interpreted as a cell-based subject.
    */
   boolean isCellBased(int columnIndex);

   /**
    * 
    * @return the set of columnIndexes that should be interpreted as cell-based subjects.
    */
   Set<Integer> getCellBasedColumns();

   /**
    * 
    * @return
    */
   Integer getFirstCellBasedColumn();
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   Value getCellBasedUpPredicateLN(int columnIndex);
   
   /**
    * 
    * @return the object of the triple that should be interpreted for a cell-based interpretation of columnIndex.
    */
   Value getCellBasedValue(int columnIndex);

   /**
    * 
    * @param columnIndex
    * @return
    */
   HashMap<Value, Set<Value>> getCellBasedUpPredicateValueSecondaries(int columnIndex);

   /**
    * 
    * @param columnIndex
    * @return the local name of the predicate from a cell-based resource to the value within the cell (i.e., "out" of the page).
    */
   Value getCellBasedOutPredicateLN(int columnIndex);

   /**
    * 
    * @param columnIndex
    * @return the URI of a predicate to override the default rdf:value.
    */
   Value getCellBasedOutPredicate(int columnIndex);
   
   
   
   // Linking methods
   
   
   
   /**
    * For a given value that occurs in the source data, 
    * provide the set of URIs that the promoted value will be owl:sameAs.
    * 
    * This returns a view of the entire links-via graph that is loaded by {@link #getLODLinksRepositories()}.
    * 
    * @param columnName
    * @return a mapping from literal values to URIs that imply identity.
    */
   HashMap<String,HashSet<URI>> getObjectSameAsLinks(int columnName);
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   HashMap<String,HashSet<URI>> getSubjectSameAsLinks(int columnIndex);


   /**
    * https://github.com/timrdf/csv2rdf4lod-automation/issues/234
    * @return
    */
   boolean referenceLODLinkedURIsDirectly(int columnIndex);
   
   
   // Annotations
   
   
   
   /**
    * Pointers to pages whose primary topic is the subject created during the conversion.
    * @return
    */
   Set<String> getSubjectHumanRedirects();
   
   /**
    * Collection of property/objects that should hang off of every subject row created.
    * @return
    */
   HashMap<String, String> getAdditionalDescriptions();
   
   /**
    * Get the predicate-object pairs to annotate the subject.
    * Predicates are URIs (for predicates with strings or templates, see {@link #getContextualAdditionalDescriptions(int)}
    * 
    * TODO: just as awkward
    * 
    * @return
    */
   HashMap<URI, Set<Value>> getConstantAdditionalDescriptions(int columnIndex);

   /**
    * Like {@link #getConstantAdditionalDescriptions(int)}, but returns the predicate-object pairs with string
    * or template predicates.
    * 
    * @param columnIndex
    * @return
    */
   HashMap<String, Set<Value>> getContextualAdditionalDescriptions(int columnIndex);
   
   /**
    * Arbitrary descriptions of the versioned dataset.
    * @return
    */
   HashMap<Value, HashSet<Value>> getLayerDatasetDescriptions();

   /**
    * 
    * @param column
    * @return regular expression used to delimit a string into multiple objects.
    */
   String getObjectDelimiter(int column);
   
   /**
    * 
    * @param column
    * @return
    */
	String getObjectDelimiterInChain(int column);

   /**
    * Assert all triples in the enhancement Repository into the given RepositoryConnection.
    * 
    * @param metaConn
    */
   void assertEnhancementsRepository(RepositoryConnection metaConn);
   
   /**
    * 
    * @param columnIndex
    * @return The regular expressions to search, and the predicate/object templates to assert for each match.
    */
   HashMap<String, HashMap<Value,Set<Value>>> getSubjectAnnotationsViaObjectSearches(int columnIndex);

   /**
    * 
    * @return true if conversion:source_identifer, conversion:dataset_identifer, or conversion:version_identifer are NOT specified.
    */
   boolean hasIdentifiersSpecified();

   /**
    * 
    * @param columnIndex
    * @return true if the values should be compared independent of case sensitivity.
    */
   boolean lodLinkCaseInsensitive(int columnIndex);

   /**
    * 
    * @param columnIndex
    * @return true if the LODLinks graph(s) for this column should be included in the output converted dataset.
    */
   boolean includeLODLinksGraph(int columnIndex);

   /**
    * Used for https://github.com/timrdf/csv2rdf4lod-automation/wiki/conversion%3AIncludesLODLinks
    * 
    * @return all Repositories containing the links-via graphs.
    */
   Collection<Repository> getLODLinksRepositories();
   
   /**
    * Used for https://github.com/timrdf/csv2rdf4lod-automation/wiki/conversion%3Akeys
    * 
    * @param columnIndex
    * @return a Repository containing the union of all links via graphs for column 'columnIndex'
    */
   Repository getLODLinksRepository(int columnIndex);

   /**
    * Charset.forName("UTF-8")
    * 
    * @return
    */
   Charset getCharset();

   /**
    * Assumes false if not specified by enhancement parameters. If false, slashes become underscores.
    * 
    * @param columnIndex
    * @return true if the column is known to contain values safe for URL construction (e.g. 5/5d/EnvironmentalProtectionAgency.png)
    */
   boolean isColumnURISafe(int columnIndex);

   /**
    * 
    * @param columnIndex
    * @return the properties to assert to promoted resources (in addition to rdfs:label and dcterms:identifier).
    */
	Set<URI> getObjectLabelProperties(int columnIndex);

	/**
	 * Return the owl:inverses of the given column.
	 * 
	 * @param columnIndex
	 * @return
	 */
	Set<URI> getInverses(int columnIndex);

	//
	// Regex
	//
	
	/**
	 * 
	 * @return the set of column indexes that have regex applied to their interpretations before processing.
	 */
	Set<Integer> getInterpretWithRegexColumnsChainHead();

	/**
	 * 
	 * @param columnIndex
	 * @return the regex/replacement pairs that should be applied to values in the given column.
	 */
	HashMap<String, String> getInterpretWithRegexesChainHead(int columnIndex);

	/**
	 * 
	 * @return
	 */
	Set<Integer> getInterpretWithRegexColumnsInChain();

	/**
	 * 
	 * @param col
	 * @return
	 */
	HashMap<String, String> getInterpretWithRegexesInChain(int col);
	
	
	
	
	/**
	 * 
	 * @param col
	 * @return true if any URIs created from this column's values should NOT be annotated with rdfs:label.
	 */
	boolean isColumnUnlabeled(int col);

	/**
	 * 
	 * @return
	 */
	HashMap<Value,HashMap<Value,Set<Value>>> getTemplatedStatements();

	/**
	 * 
	 * @param col
	 * @return true if the first-level enhancement chains to a second enhancement.
	 */
	boolean isChained(int col);

	
	/**
	 * 
	 * @return
	 */
   Set<Resource> getRowTopics();

   /**
    * 
    * @param topic
    */
   HashMap<URI,Set<Value>> getTopicDescriptions(Resource topic);
   
   
   /**
    * Omit ov:csvCol?
    * 
    * @return
    */
   boolean excludeRowNumbers();
   
   /**
    * 
    * @return
    */
   boolean excludeDCTermsReference();
   
   /**
    * 
    * @return
    */
   boolean excludeVoIDReference();
}