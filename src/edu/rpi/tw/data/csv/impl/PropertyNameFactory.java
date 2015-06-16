package edu.rpi.tw.data.csv.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import edu.rpi.tw.data.csv.CSVRecord;
import edu.rpi.tw.data.csv.EnhancementParameters;
import edu.rpi.tw.data.csv.TemplateFiller;
import edu.rpi.tw.string.NameFactory;

/**
 * Use the EnhancementParameters to determine the URIs for the properties created during conversion.
 * 
 * 1: "Disaster Number" -> "Disaster Number"   -> http://...vocab/enhancement/1/disaster_number
 * 2: "Disaster Number" -> "Disaster Number_2" -> http://...vocab/enhancement/1/disaster_number_2
 * 3: ""                -> "column_3"          -> http://...vocab/enhancement/1/column_3 
 * 4: ""                -> "column_4"          -> http://...vocab/enhancement/1/column_4
 * 
 * Usage overview:
 * 
 * (constructor)
 *       this.columnNameFactory = new PropertyNameFactory("column", this.predicateNS, eParams);
 *        
 * (visitHeader)
 *       URI    predicateR  = columnNameFactory.namePropertyFromHeader(csvHeader, columnIndex);
 *       String propertyLN  = columnNameFactory.getPropertyLocalName(columnIndex);
 *       String columnLabel = columnNameFactory.getLabel(columnIndex);
 *       
 *       if( RDFS.RESOURCE.equals(valueHandlers.get(columnIndex).getRange()) ) {
 *          String property_name = columnNameFactory.getPropertyLocalName(columnIndex);
 *       ...
 *       
 *       URI p = columnNameFactory.namePropertySDV(csvRecordFiller.tryExpand(predicate).stringValue());
 *       
 *       ...
 *       
 *       URI predicate        = columnNameFactory.getProperty(c,rowNum,this.csvRecordFiller);
 *       String predicateLN   = columnNameFactory.getPropertyLocalName(c);
 */
public class PropertyNameFactory {
   
   private static Logger logger = Logger.getLogger(PropertyNameFactory.class.getName());
   
   private static ValueFactory vf = ValueFactoryImpl.getInstance();
   
   protected String predicateNS   = "http://example.org/vocab/";
   protected String localNameBase = "thing";
   protected EnhancementParameters eParams;
   
   /**
    * 
    * @param localNameBase - e.g. 'column'
    */
   protected PropertyNameFactory(String localNameBase) {
      this(localNameBase,null);
   }
   
   /**
    * 
    * @param localNameBase - 
    * @param predicateNS - 
    */
   protected PropertyNameFactory(String localNameBase, String predicateNS) {
      this(localNameBase, predicateNS, null);
   }
 
   /**
    * 
    * @param localNameBase - Local name to number of header is empty. e.g. "column" -> http://example.org/vocab/column_1
    * @param predicateNS   - If null, placed into http://example.org/vocab/
    * @param eParams       - to access conversion:labels. If null, only uses header to name properties.
    */
   public PropertyNameFactory(String localNameBase, String predicateNS, EnhancementParameters eParams) {
      if( localNameBase != null ) this.localNameBase = localNameBase;
      if( predicateNS   != null ) this.predicateNS   = predicateNS;
      this.eParams = eParams;
   }
   
   
   
   
   //
   // Property-naming methods
   //
   
   // has never been needed:
   // protected HashMap<String,Integer> columnHeaderMap    = new HashMap<String,Integer>(); // csvHeader to csvCol
   protected HashMap<Integer,String> columnHeaderMapInv = new HashMap<Integer,String>(); // csvCol to csvHeader
   
   /**
    * Create predicate from 'columnIndex', using 'headerLabel' as a best guess for how to name it.
    * 
    * If the enhancement parameters provides a conversion:label, use that value instead
    * of 'headerLabel' to name the predicate.
    * 
    * (what happens if conversion:equivalent_property is defined?)
    * 
    * If the predicates should be (according to the enhancement parameters) distinctly named 
    * depending on the column they come from, impose _2, _3, etc. for repeated labels. For example,
    * if col 2 is "State" and col 4 is "State", name as "State_1" and "State_2" to keep distinct.
    * (This distinction occurs depending on being row-based or cell-based).
    * 
    * @param headerLabel - 
    * @param columnIndex - 
    * 
    * @return 
    */
   public URI namePropertyFromHeader(String headerLabel, int columnIndex) {

	   //
	   // Called only by CSVtoRDF.visitHeader 2012-Sep-02
	   //
	   
      String csvHeader = CSVRecord.stripQuotes(headerLabel);
      columnHeaderMapInv.put(columnIndex, headerLabel);
      //System.err.println("HEADER FOR REAL?: " + columnIndex + " " + headerLabel);
      String enhanceLabel = eParams.getColumnLabel(columnIndex);
   	boolean enhancedLabelThere = enhanceLabel != null && enhanceLabel.length() > 0;
      
      String columnLabel = enhancedLabelThere ? enhanceLabel : csvHeader;
      
      if( columnLabel.matches("^\\d.*") ) {
         System.err.println("Note: tweaking property name to conform to RDF/XML's whims: "+columnLabel+
                            " -> "+rdfxml_property_naming_hack+columnLabel);
         columnLabel = rdfxml_property_naming_hack + columnLabel; // :-( rdf/xml hack.
      }

      /*
       * If multiple cell-based subjects cite the same property, they should be the same.
       * If multiple properties are given the same conversion:label, they should be the same.
       * Distinctness is ensured in the naive case where headers are used.
       */
      boolean columnsDistinct = !(eParams.isCellBased(columnIndex) || enhancedLabelThere);
      
      columnLabel = createUniqueColumnLabel(columnLabel, columnIndex-1, columnsDistinct);
      predicateLabels.put(columnIndex, columnLabel); // TODO: move into createUniq...
      
      return nameProperty(columnLabel, columnIndex);
   }
   
   protected HashMap<Integer,String> predicateLabels   = new HashMap<Integer,String>();          // csvCol to "P Labels"
   
   // Manages naming properties based on missing and duplicate headers.
   protected HashMap<String,Integer> columnTitleMap    = new HashMap<String,Integer>();           // csvHeader-probablyColumnLabel to csvCol
   protected HashMap<Integer,String> columnTitleMapInv = new HashMap<Integer,String>();           // csvCol to csvHeader-probablyColumnLabel
   
   /**
    * 
    * @param columnLabel
    * @param columnIndex     - zero-based
    * @param columnsDistinct - if true, include "_x" for duplicate-named columns; if false, allow columns to overlap.
    * @return
    */
   public String createUniqueColumnLabel(String columnLabel, int columnIndex, boolean columnsDistinct) {
	   
	   //
	   // Only directly called by CSVtoRDF.visitHeader and this.namePropertyFromHeader 2012-Sep-02
	   //
	   
      String uniqueLabel = columnLabel.replace("(","").replace(")","").trim();
      if( columnLabel == null || columnLabel.length() == 0 ) {
         uniqueLabel = localNameBase+"_"+Integer.toString(columnIndex+1);
      }
      if( !columnTitleMap.containsKey(uniqueLabel) ) {
         columnTitleMap.put(uniqueLabel, 1);
      }else {
         int counter = columnTitleMap.get(uniqueLabel) + 1;
         columnTitleMap.put(uniqueLabel, counter);
         if( columnsDistinct ) {
            uniqueLabel = uniqueLabel+"_"+counter;
         }
      }
      this.columnTitleMapInv.put(columnIndex+1, uniqueLabel);
      //System.err.println(getClass().getSimpleName()+": "+(columnIndex+1)+" \""+columnLabel+"\" -> \""+uniqueLabel+"\"");
      return uniqueLabel;
   }
   
   /**
    * Self-catches thrown exception if this is called for the same 'columnLabel'.
    * Use {@link #getPropertySDV(int)} to get the property with conversion:label applied, conversion:equivalent-property ignored. 
    * 
    * @param columnLabel - label to use to name the property. EnhancementParameters (label / equiv-prop) are NOT used to override 'columnLabel'.
    * @param columnIndex - the column for which this property is being made.
    * 
    * @return A predicate for 'columnIndex' based DIRECTLY on the columnLabel (modulo spaces/digits).
    */
   protected URI nameProperty(String columnLabel, int columnIndex) {
	   
	   //
	   // Called only by namePropertyFromHeader 2012-Sep-02
	   //
	   
      if( this.predicates.containsKey(columnIndex) ) {
         try {
            throw new NullPointerException("naming property from columnIndex that already has property defined. overwriting.");
         }catch(Exception e) {
            e.printStackTrace();
         }
      }
      String propertyLocalName = getLocalNameFromLabel(columnLabel);
      String propertyName = columnLabel.indexOf("://") > 0  
                          ? columnLabel // TODO: commas and parens in titles makes errors in RDF/XML
                          : predicateNS + propertyLocalName;
      
      URI predicateR = vf.createURI(propertyName);
      setProperty(         columnIndex, predicateR       );
      setPropertyLocalName(columnIndex, propertyLocalName);
      return predicateR;
   }
   
   /**
    * Does not affect state, just gives the URI.
    * 
    * @param label - string to use for the local name of the property to create. Does not use EnhancementParameters (label, equivalent property) to override this value.
    * @return the URI of the predicate, contextualized by SDV (equivalent property does not have effect on this result).
    */
   public URI namePropertySDV(String label) {
	   
	   //
	   // Called only by CSVtoRDF.visit 2012-Sep-02
	   //
	   
      return vf.createURI(predicateNS + getLocalNameFromLabel(label));
   }
   
   /**
    * Duplication handled.
    * conversion:label applied (and avoids duplication handling)
    * cell-based logic applied.
    * 
    * conversion:equivalent_property ignored.
    * 
    * @param columnIndex
    * @return The URI that would be used if conversion:equivalent_property did not apply.
    */
   public URI getPropertySDV(int columnIndex) {
   	return namePropertySDV(getPropertyLocalName(columnIndex));
   }
   
   protected HashMap<Integer,String> equivalentPropertyRowContextual = new HashMap<Integer,String>();
   protected Hashtable<Integer,URI>  predicates                      = new Hashtable<Integer,URI>(); // csvCol to predicate URI.
   
   /**
    * Return 'predicateR' whenever {@link #getProperty(int)} is called with 'columnIndex' UNLESS 
    * EnhancementParameters specifies a conversion:equivalent_property (if so, use that instead).
    * 
    * @param columnIndex - one based column from which the predicate was created.
    * @param predicateR  - the URI of the predicate.
    */
   protected void setProperty(int columnIndex, URI predicateR) {
      
	   //
	   // Called only by nameProperty(String, int) 2012-Sep-02
	   //
	   
      Value equivProperty = eParams.getEquivalentPropertyChainHead(columnIndex);
      if ( equivProperty == null ) {
    	  predicates.put(columnIndex, predicateR);
      }else if( equivProperty instanceof URI ) {
    	  predicates.put(columnIndex, (URI) equivProperty); 
      }else if( equivProperty != null ) {
          logger.finest("PropertyNameFactory#setProperty / equiv property: "+
                        equivProperty.stringValue()+" header: "+columnTitleMapInv.get(columnIndex));
          
          // Context-free template filling ([/sdv] [v] [e])
          String p = eParams.fillTemplate(equivProperty.stringValue());
          //System.err.println(p);
          
          // Column-context template filling ([@] [H] [c] [L])
          p = TemplateFillerColumnContext.fillTemplateWithColumnContext(p, columnIndex,
        		  							                   columnHeaderMapInv.get(columnIndex),
        		  							                   columnTitleMapInv.get(columnIndex),
        		  							                   getPropertyLocalName(columnIndex));
          //System.err.println(p);

          if( TemplateFillerRowContext.isRowContextual(p) ) {
            // Cache up the row-contextual template for evaluation at each row.
        	  	this.equivalentPropertyRowContextual.put(columnIndex, p);
          }else {
          // This indicates that there is not template to fill at each row.
        	  	this.equivalentPropertyRowContextual.put(columnIndex, null);    	  
          }
          
    	  predicates.put(columnIndex, vf.createURI(p));
      }
   }
   
   // Manages the columnIndex and URI-ready local name of a property.
   protected HashMap<String,Integer> prop2csvCol = new HashMap<String,Integer>(); // property_name to csvCol
   protected HashMap<Integer,String> csvCol2prop = new HashMap<Integer,String>(); // csvCol to property_name
   
   /**
    * 
    * @param columnIndex       - one based
    * @param propertyLocalName - the URI-ready local name of a property.
    */
   protected void setPropertyLocalName(int columnIndex, String propertyLocalName) {
      prop2csvCol.put(propertyLocalName, columnIndex);            
      csvCol2prop.put(columnIndex, propertyLocalName);    
   }
   
   
   
   
   
   //
   // Accessor methods
   //
   
   
   
   
   
   /**
    * 
    * @return
    */
   public Collection<URI> getProperties() {
	   
	   //
	   // Called only by CSVtoRDF.{toRDF, visit, visitRelativeToHeader} 2012-Sep-02
	   //
	   
      return predicates.values();
   }
   
   /**
    * This has been superceded by {@link #getProperty(int, long, TemplateFiller)} to handle
    * row-contextual variables.
    * 
    * Calling this is fine, as long as you don't need row-contextual variables.
    * 
    * @param columnIndex
    * @return
    */
   public URI getProperty(int columnIndex) {
	   
	   //
	   // Called only by CSVtoRDF.visit and this. 2012-Sep-02
	   //
	  
      return predicates.get(columnIndex);
   }
   
   /**
    * 
    * @param columnIndex - 
    * @param rowIndex    - 
    * @return null if the property for this cell is not row-contextual. Otherwise, return the 
    *         URI of the predicate created by applying the template with the given filler.
    */
   public URI getProperty(int columnIndex, long rowIndex, TemplateFiller templateFiller) {
	   
	   //
	   // Called only by CSVtoRDF.visit 2012-Sep-02
	   //
	   
	   if( this.equivalentPropertyRowContextual.get(columnIndex) == null ) {
		   // The predicate was a URI or a Value template that did not have row-contextual vars.
		   return predicates.get(columnIndex);
	   }else {
		   // The predicate was a Value template with row-contextual variables.
		   String template =  this.equivalentPropertyRowContextual.get(columnIndex);
		   String p = templateFiller.fillTemplate(template, CSVRecordTemplateFiller.AS_RESOURCE);
		   logger.finest("#getProperty needed row-context: "+p);
		   return vf.createURI(p);
	   }
   }
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   public String getPropertyLocalName(int columnIndex) {
	   
	   //
	   // Called only by CSVtoRDF.{visit, visitHeader} and this. 2012-Sep-02
	   //
	   
      return this.csvCol2prop.get(columnIndex);
   }
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   public String getLabel(int columnIndex) {
	   
	   //
	   // Called only by CSVtoRDF.visitHeader 2012-Sep-02
	   //
	   
      return this.predicateLabels.get(columnIndex);
   }
   
   /**
    *
    * 
    * @param colNum - one-based
    * @return
    */
   public String getUniqueColumnLabel(int colNum) {
	   
	   //
	   // Called only by CSVtoRDF.visit 2012-Sep-02
	   //
	   
      return this.columnTitleMapInv.get(colNum);
   }

   /**
    * 
    * @param property_name
    * @return
    */
   public Integer getColumnIndexOfPropertyLocalName(String property_name) {
	   
	   //
	   // Called only by CSVRecordTemplateFiller.fillTemplate(String, String, boolean) 2012-Sep-02
	   //
	   
      return prop2csvCol.get(property_name);
   }

   /**
    * 
    * @param bundlePropertyLN - the URI-ready local name of a property.
    * @return
    */
   public boolean isPropertyLocalNameDefined(String propertyLocalName) {
	   
	   //
	   // Called only by CSVtoRDF.visitHeader 2012-Sep-02
	   //
	   
      return prop2csvCol.containsKey(propertyLocalName);
   }
   

   
   
   
   
   
   /**
    * 
    */
   public String toString() {
      StringBuffer retVal = new StringBuffer();
      
      retVal.append(predicateNS + " " + localNameBase + "\n");
      
      retVal.append("\n---------- columnHeaderMapInv\n");
      for( Integer column : columnHeaderMapInv.keySet()) {   //
         retVal.append("csvCol: " + column + " -> csvHeader: " + columnHeaderMapInv.get(column) + "\n");
      }
      
      
      
      retVal.append("\n---------- columnTitleMap and columnTitleMapInv\n");
      for( String csvHeader : columnTitleMap.keySet()) {   // csvHeader-probablyColumnLabel to csvCol
         retVal.append("csvHeader: " + csvHeader + " -> csvCol: " + columnTitleMap.get(csvHeader) + "\n");
      }
      retVal.append("\n");
      for( int csvCol : columnTitleMapInv.keySet() ) {     // csvCol to csvHeader-probablyColumnLabel
         retVal.append("csvCol: " + csvCol + " -> csvHeader: " + columnTitleMapInv.get(csvCol) + "\n");         
      }
      
      
      
      retVal.append("\n---------- equivalentPropertyRowContextual\n");
      for( Integer column : equivalentPropertyRowContextual.keySet()) {   //
         retVal.append("csvCol: " + column + " -> property: " + equivalentPropertyRowContextual.get(column) + "\n");
      }
      retVal.append("\n");
      
      
      retVal.append("\n---------- predicates\n");
      for( Integer column : predicates.keySet()) {   //
         retVal.append("csvCol: " + column + " -> property: " + predicates.get(column) + "\n");
      }
      retVal.append("\n");
      
      
      retVal.append("\n---------- prop2csvCol and csvCol2prop\n");
      for( String property_name : prop2csvCol.keySet() ) { // property_name to csvCol
         retVal.append("property_name: " + property_name + " -> csvCol: " + prop2csvCol.get(property_name) + "\n");         
      }
      retVal.append("\n");
      for( int csvCol : csvCol2prop.keySet() ) {           // csvCol to property_name
         retVal.append("csvCol: " + csvCol + " -> property_name: " + csvCol2prop.get(csvCol) + "\n");
      }
      
      
      
      retVal.append("\n---------- predicates and predicateLabels\n");
      for( int csvCol : predicates.keySet() ) {            // csvCol to predicate URI.
         retVal.append("csvCol: " + csvCol + " -> predicate: " + predicates.get(csvCol) + "\n");         
      }
     
      
      retVal.append("\n");
      for( int csvCol : predicateLabels.keySet() ) {       // csvCol to "P Labels"
         retVal.append("csvCol: " + csvCol + " -> predicate label: " + predicateLabels.get(csvCol) + "\n");         
      }
      
      return retVal.toString();
   }
   
   
   
   
   
   //
   // Static methods
   //
   
   private static final String rdfxml_property_naming_hack = "p_";
   
   /**
    * Accounts for spaces and initial digits.
    * 
    * @param uniqueColumnLabel - the local name to use to name the property.
    * @return a URI-friendly local name for the given string. label and equiv-property do NOT have an effect.
    */
   public static String getLocalNameFromLabel(String uniqueColumnLabel) {
      String trimmed = NameFactory.trimChars(uniqueColumnLabel.replaceAll("\\W","_"),"_").toLowerCase().trim();
      return trimmed.matches("^\\d.*") ? rdfxml_property_naming_hack + trimmed : trimmed;
   }
}