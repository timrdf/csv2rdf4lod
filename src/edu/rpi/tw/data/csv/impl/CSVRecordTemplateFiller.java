package edu.rpi.tw.data.csv.impl;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import edu.rpi.tw.data.csv.CSVRecord;
import edu.rpi.tw.data.csv.Conversion;
import edu.rpi.tw.data.csv.EnhancementParameters;
import edu.rpi.tw.data.csv.valuehandlers.ResourceValueHandler;
import edu.rpi.tw.string.IDManager;
import edu.rpi.tw.string.NameFactory;

/**
 * Fill the cell-dependent variables in a template:
 * 
 * 
 * @see https://github.com/timrdf/csv2rdf4lod-automation/wiki/Using-template-variables-to-construct-new-values
 */
public class CSVRecordTemplateFiller extends TemplateFillerColumnContext {

   private static Logger logger = Logger.getLogger(CSVRecordTemplateFiller.class.getName());
   
   protected static ValueFactory vf = ValueFactoryImpl.getInstance();
   
   protected static boolean FILL_EMPTY_VALUE = true;

   protected CSVRecord             record;         // Stores them zero-based.
   protected int                   columnIndex;
   protected PropertyNameFactory   propNameFactory;
   protected EnhancementParameters eParams;
   
   // Cache for speed
   protected String uriOfVersionedDataset = "";
   protected String namespaceOfDataset    = "";
   protected String namespaceOfSource     = "";
   protected String baseURI               = "";

   protected long    rowNumber;
   protected int     tempColumnIndex;
   protected boolean useTempColumnIndex = false;
   
   // row , column, value
   protected HashMap<Long, HashMap<Long,String>> cellsReferencedRelativeToHeader = 
		 new HashMap<Long, HashMap<Long,String>>();
   
   public static final boolean AS_RESOURCE = true;
   public static final boolean AS_LITERAL  = false;
   
   protected IDManager idm = new IDManager();
   
   /**
    * String template, String currentValue, boolean asResource, String filled
    * 
    * Added to address https://github.com/timrdf/csv2rdf4lod-automation/issues/380
    * 
    * fillTemplate(String,String,boolean) is called a dozen times for each value,
    * so just memoize it. http://en.wikipedia.org/wiki/Memoization
    */
   private HashMap<String,
                   HashMap<String, 
                           HashMap<Boolean,
                                   String>>> memoized;
   /**
    * @param propNameFactory
    */
   public CSVRecordTemplateFiller(PropertyNameFactory propNameFactory) {
      this(propNameFactory,null);
   }
   
   /**
    * 
    * @param propNameFactory
    * @param eParams
    */
   public CSVRecordTemplateFiller(PropertyNameFactory propNameFactory, 
		                            EnhancementParameters eParams ) {
      super();
      this.propNameFactory = propNameFactory;
      if( eParams != null ) {
         this.eParams = eParams;
         this.uriOfVersionedDataset = eParams.getURIOfVersionedDataset()+"/";
         this.namespaceOfDataset    = eParams.getNamespaceOfAbstractDataset();
         this.namespaceOfSource     = eParams.getNamespaceOfSource();
         this.baseURI               = eParams.getBaseURI()+"/";
      }

      memoized = new HashMap<String, HashMap<String, HashMap<Boolean, String>>>();
   }
   
   /**
    * 
    * @param record
    */
   public void setCSVRecord(CSVRecord record, long rowNumber) {
      this.record    = record;
      this.rowNumber = rowNumber;
   }
   
   /**
    * 
    * @param columnIndex - one-based.
    */
   public void setColumnIndex(int columnIndex) {
      this.columnIndex = columnIndex;
   }
   
   /**
    * 
    * @param columnIndex
    */
   public void pushColumnIndex(int columnIndex) {
      this.tempColumnIndex = columnIndex;
      this.useTempColumnIndex = true;
   }
   
   /**
    * 
    * @param record
    * @param columnIndex
    */
   public void set(CSVRecord record, int columnIndex) {
      setCSVRecord(record,0);
      setColumnIndex(columnIndex);
   }
   
   @Override
   public void setCellReferencedRelativeToHeader(long row, long column, String cellValue) {
      if( !this.cellsReferencedRelativeToHeader.containsKey(row) ) {
         this.cellsReferencedRelativeToHeader.put(row, new HashMap<Long,String>());
      }
      this.cellsReferencedRelativeToHeader.get(row).put(column,cellValue);
   }
   
   //
   //
   //
   //
   
   /**
    * A less advanced form of the ValueHandlers that does not use the full context, 
    * available for when object searching.
    * 
    * See EnhancedLiteralValueHandler#assertObjectSearchDescriptions
    * 
    * @param template - 
    * @param tempalteV - a value from which the template was created (contains the target datatype).
    */
   @Override
   public Value tryExpand(String template, Value templateV) {

   	Value  retVal  = null;
   	String objectS = this.fillTemplate(template);
   	
   	//logger.finest(" tryExpand " + (null != templateV) + " a Literal: " + (templateV instanceof Literal));

   	if( ResourceValueHandler.isURI(objectS) ) {
   		retVal = vf.createURI(objectS) ;
   	}else if(ResourceValueHandler.isURI(this.fillTemplate(template, true))) {
   		retVal = vf.createURI(this.fillTemplate(template, true));
   	} else if( ValueHandlerFactory.getValueHandler(template) != null ) {
   		// xsd:decimal(3.14)
   		// xsd:integer(4)
   		// xsd:date(
   		// and other similar simple datatypes.
   		retVal = ValueHandlerFactory.getValueHandler(template).handleValue(objectS);
   	}else if( null != templateV && templateV instanceof Literal && null != ((Literal)templateV).getDatatype() ) {
   		//System.err.println("TYPING expand to "+ ((Literal)templateV).getDatatype());
   		retVal = vf.createLiteral(objectS, ((Literal)templateV).getDatatype());
   	}else {
   		retVal = vf.createLiteral(objectS);
   	}
   	return retVal;
   }

   @Override
   public Value tryExpand(Value template) {
      return tryExpand(template.stringValue());
   }

   @Override
   public Value tryExpand(String template) {
      return tryExpand(template, null);
   }
   
   @Override
   public boolean doesExpand(String template) {
      return template != null && template.length() != fillTemplate(template).length(); 
      // TODO: some situations may incorrectly return false if 
      // value length is == variable name length.
   }
   
   
   //
   //
   //
   //
   
   /**
    * Fill a template as a literal, i.e., don't "URI-ify" the current value.
    * 
    * TODO: this should expand cell-independent variables, too 
    * (by calling eParams' fillTemplate in addition to own).
    * 
    * @param template - the template containing variables to expand.
    */
   @Override
   public String fillTemplate(String template) {
   	
   	return fillTemplate(template, CSVRecordTemplateFiller.AS_LITERAL);
   	// Superclass fills column-contextual variables
   	// Slows things down, though...
   	//return fillTemplate(super.fillTemplate(template), CSVRecordTemplateFiller.AS_LITERAL);
   }
   
   /**
    * Fill a template, as either a literal or a resource (as per asResource param).
    * 
    * potentially old comment: label2URI the entire filled-template result. 
    * Does not handle templates that result in full URIs.
    * 
    * @param template - the template containing variables to expand.
    * @param asResource - if true, "URI-ify" the current value so that the result is a valid URI.
    */
   @Override
   public String fillTemplate(String template, boolean asResource) {
      int index = this.useTempColumnIndex ? this.tempColumnIndex : this.columnIndex;
      //return asResource ? ResourceValueHandler.label2URI(filled) : filled; 
      //return fillTemplate(template, getColumnValue(index,asResource,FILL_EMPTY_VALUE), asResource); // Value of current column
      //                                                              ^ this doesn't make sense when filling literals.
      return fillTemplate(template, getColumnValue(index,asResource,asResource), asResource); // Value of current column
   }
   
   /**
    * Fill a template, as either a literal or resource 
    * (as per asResource param) - using currentValue as "[.]".
    * 
    * "[#2]-[#3]";        # only one pattern is required;
    * "[@city]-[@state]"; # these are equivalent.
    * "[.]-[#3]";         # only one pattern is required;
    * 
    * {@link DefaultEnhancementParameters#fillTemplate(String)} fills the cell-independent variables.
    * 
    * https://github.com/timrdf/csv2rdf4lod-automation/wiki/Using-template-variables-to-construct-new-values
    * 
    * @param template - the template to fill.
    * @param currentValue - the value to use for the "[.]" variable.
    * @param asResource - if true, "URI-ify" the local value, o/w use as is.
    * @return the template string with the values of the variables it contained.
    */
   public String fillTemplate(String template, String currentValue, boolean asResource) {
      
      //System.out.println("fillTemplate("+template+","+currentValue+","+asResource+")");
      
      this.useTempColumnIndex = false;
      
      // TODO: this method is not incorporating the symbol/interpretations on a column's enhancement when getting its value.
      
      String filled;

      if( asResource ) {
         currentValue = NameFactory.label2URI(currentValue);
      }
      
      //System.err.println("HERE TEMPLATE: "+template + " as resource " + asResource);
      if( template == null || "[.]".equals(template) ) {
         filled = currentValue;
      }else {
         
         filled = new String(template);
         
         //
         // TODO: consider TemplateFillerColumnContext here. Does this overlap?
         //
         
         
         try {
            currentValue = currentValue == null ? "" : currentValue;
            
            //
            // Handle template functions before handling their operands
            //
            
            if( template.indexOf("increment(") >= 0 ) { // Short circuit to avoid heavy-duty processing.
               // e.g. "increment([#1])"
               //System.err.println("increment()");
               Pattern pattern = Pattern.compile("increment\\(\\[#([0-9]*)\\]\\)");
               Matcher matcher = pattern.matcher(filled);
               while( matcher.find() ) {
                  //System.err.println("increment(): " + matcher.group(1));
                  int    csvCol   = Integer.parseInt(matcher.group(1));
                  String valAtCol = getColumnValue(csvCol, asResource, FILL_EMPTY_VALUE);
                  //System.err.println("increment(): \"" + filled + "\" has " + csvCol + " gets \"" + idm.getIdentifier(valAtCol) + "\", ");
                  filled = filled.replaceAll("increment\\(\\[#"+csvCol+"\\]\\)", idm.getIdentifier(valAtCol));
                  //System.err.println("becomes \""+filled+"\"");
               }
            }
            
            if( template.indexOf("domain(") >= 0 ) { // Short circuit to avoid heavy-duty processing.
               // e.g. domain([#4])
               //System.err.println("domain()");
               Pattern pattern = Pattern.compile("domain\\(\\[#([0-9]*)\\]\\)");
               Matcher matcher = pattern.matcher(filled);
               while( matcher.find() ) {
                  //System.err.println("domain(): " + matcher.group(1));
                  int    csvCol   = Integer.parseInt(matcher.group(1));
                  String valAtCol = getColumnValue(csvCol, asResource, FILL_EMPTY_VALUE);
                  //System.err.println("domain(): \"" + filled + "\" has " + csvCol + " gets \"" + NameFactory.uriDomain(valAtCol) + "\", ");
                  filled = filled.replaceAll("domain\\(\\[#"+csvCol+"\\]\\)", NameFactory.uriDomain(valAtCol));
                  //System.err.println("becomes \""+filled+"\"");
               }
            }
            
            if( template.indexOf("md5(") >= 0 ) { // Short circuit to avoid heavy-duty processing.
               // e.g. "[/]id/url/md5/md5([#1])/access"
               //System.err.println("md5()");
               Pattern pattern = Pattern.compile("md5\\(\\[#([0-9]*)\\]\\)");
               Matcher matcher = pattern.matcher(filled);
               while( matcher.find() ) {
                  //System.err.println("md5(): " + matcher.group(1));
                  int    csvCol   = Integer.parseInt(matcher.group(1));
                  //String valAtCol = getColumnValue(csvCol, asResource, FILL_EMPTY_VALUE);
                  // ^^ had problem with "http://hcil2.cs.umd.edu/new" vs.
                  //                     "http_hcil2_cs_umd_edu_newv"
                  String valAtCol = getColumnValue(csvCol, AS_LITERAL, FILL_EMPTY_VALUE);
                  //System.err.println("md5(): \"" + filled + "\" needs md5 of col " + csvCol + " ("+valAtCol+") becomes \"" + NameFactory.getMD5(valAtCol) + "\", ");
                  filled = filled.replaceAll("md5\\(\\[#"+csvCol+"\\]\\)", NameFactory.getMD5(valAtCol));
                  //System.err.println("becomes \""+filled+"\"");
               }
            }
            
            
            
            //
            // Handle [.] [^.^] [_._] [!] [+] variables
            //
            
            // Note that we are asking the original template if it contains the variable to expand,
            // but then expanding it within the growing expansion. This is to save some computation.
            if( template.indexOf("[.]") >= 0 ) {
               filled = filled.replaceAll("\\[\\.\\]",       currentValue);                                                   // [.] 
            }
            if( template.indexOf("[^.^]") >= 0 ) {
               filled = filled.replaceAll("\\[\\^\\.\\^\\]", currentValue.toUpperCase());                                     // [^.^]
            }
            if( template.indexOf("[_._]") >= 0 ) {
               filled = filled.replaceAll("\\[_\\._\\]",     currentValue.toLowerCase());                                     // [_._]
            }
            if( template.indexOf("[^.-]") >= 0 ) {
               filled = filled.replaceAll("\\[\\^\\.-\\]",   NameFactory.capitalizeFirst(currentValue));                      // [^.-]
            }
            if( template.indexOf("[^._]") >= 0 ) {
               filled = filled.replaceAll("\\[\\^\\._\\]",   NameFactory.titleCase(currentValue));                            // [^._]
            }
            if( template.indexOf("[>.<]") >= 0 ) {
               filled = filled.replaceAll("\\[\\>\\.<\\]",   NameFactory.label2URI(NameFactory.trimChars(currentValue,"'"))); // [>.<]
               // TODO trimming the single quote is a hack. -----------------------------------------------------------/\
            }

            if( filled.indexOf("[!]") >= 0 ) {
               String unfilledValue = getColumnValue(this.columnIndex, !asResource, !FILL_EMPTY_VALUE);
               if( unfilledValue == null || unfilledValue.length() == 0) {
                  logger.finest("template \""+ filled + "\" contains [!] and cell value is empty." );      
                  return null;
               }
            }
            if( template.indexOf("[!]") >= 0 ) {
               filled = filled.replaceAll("\\[!\\]",         currentValue);                                                   // [!] 
            }
            if( template.indexOf("[+]") >= 0 ) {
               filled = filled.replaceAll("\\[\\+\\]",       getColumnValue(this.columnIndex, asResource, FILL_EMPTY_VALUE)); // [+] 
            }
            
            //
            // Fill the [/] [/s] [/sd] [/sdv] and [e] variables.
            //
            if( eParams != null ) {                   // TODO: reconcile with call to this at very end of this method.
               filled = eParams.fillTemplate(filled); // [/] [/s] [/sd] [/sdv] and [e].
            }
            
            //
            // Handle [r] and [c] variables
            //
            if( template.indexOf("[c]") >= 0 ) {
               filled = filled.replaceAll("\\[c\\]", ""+this.columnIndex);
            }
            if( template.indexOf("[r]") >= 0 ) {
               filled = filled.replaceAll("\\[r\\]", ""+this.rowNumber);
            }
            //System.err.println("Filled [c] and [r] with "+this.columnIndex+" and "+this.rowNumber+": "+filled);
            
            //
            // Handle all [#H-1] [#H+1] [#H+2] ... variables
            //
            if( template.indexOf("#H") >= 0 ) { // Short circuit to avoid heavy-duty processing.
               //Pattern pattern = Pattern.compile("\\[#H([-+])([0-9]*)\\]");   //    <---------------------------------\
               Pattern pattern = Pattern.compile("#H([-+])([0-9]*)"); // TODO: why doesn't adding \\[ match it (see line|)?
               Matcher matcher = pattern.matcher(filled);
               while( matcher.find() ) {
                  String sign   = "-".equals(matcher.group(1)) ? "-" : "";
                  String digits = matcher.group(2);
                  long row = Long.parseLong(sign+digits);
   
                  //System.err.println("sign:   " + sign);
                  //System.err.println("digits: " + digits);
                  //System.err.println(this.cellsReferencedRelativeToHeader + " " + this.cellsReferencedRelativeToHeader.containsKey(row) + " @ " + this.columnIndex);
                  //System.err.println("value: " + this.cellsReferencedRelativeToHeader.get(row).get(this.columnIndex));
                  if( this.cellsReferencedRelativeToHeader.containsKey(row) &&
                      this.cellsReferencedRelativeToHeader.get(row).containsKey((long)this.columnIndex) ) {
                     filled = filled.replaceAll("\\[#H."+digits+"\\]", this.cellsReferencedRelativeToHeader.get(row).get((long)this.columnIndex));
                  }else {
                     filled = filled.replaceAll("\\[#H."+digits+"\\]", "TODO");
                  }
               }
            }
            
            //
            // Handle all [#1] [#2] [#3] .... variables
            //
            // NEW:
            if( template.indexOf("[#") >= 0 ) { // Short circuit to avoid heavy-duty processing.
               Pattern pattern = Pattern.compile("\\[#([0-9]*)\\]");
               Matcher matcher = pattern.matcher(filled);
               while( matcher.find() ) {
               	//System.err.println("#N var: " + matcher.group(1));
                  int    csvCol   = Integer.parseInt(matcher.group(1));
                  String valAtCol = getColumnValue(csvCol, asResource, FILL_EMPTY_VALUE);
                  //logger.finest("[#N]: \"" + filled + "\" has " + csvCol + " gets \"" + valAtCol + "\", ");
                  if( ("[#"+csvCol+"]").equals(template) ) {
                     // Special case for "Just give me the value!"
                     filled = valAtCol;
                  }else {
                     filled = filled.replaceAll("\\[#"+csvCol+"\\]", valAtCol);
                  }
                  //logger.finest("becomes \""+filled+"\"");
               }
               /*OLD (kept around b/c it has been used for so long; replaced by pattern/matcher above):
               int pos  = filled.indexOf("[#");
               int posE = filled.indexOf("]");
               while( pos >= 0 ) {
                  String beforeParam = filled.substring(0,pos);
                  int    csvCol      = Integer.parseInt(filled.substring(pos+2,posE));
                  String afterParam  = filled.substring(posE+1);
   
                  filled = beforeParam + getColumnValue(csvCol, asResource) + afterParam;
                  //System.err.println("Filled [#"+csvCol+"]: "+filled);
                  
                  pos  = filled.indexOf("[#");
                  posE = filled.indexOf("]");
               }*/
               
               //
               // Handle all [#1/] [#2/] [#3/] .... variables
               // If [#1] is empty, omit entire value.
               // If [#2] is empty, omit entire value.
               // If [#3] is empty, omit entire value.
               // https://github.com/timrdf/csv2rdf4lod-automation/issues/158
               //
               pattern = Pattern.compile("\\[#([0-9]*)\\/\\]");
               matcher = pattern.matcher(filled);
               while( matcher.find() ) {
                  int    csvCol   = Integer.parseInt(matcher.group(1));
                  String valAtCol = getColumnValue(csvCol, asResource, !FILL_EMPTY_VALUE);
                  String slash = valAtCol != null && valAtCol.length() > 0 ? "/" : "";
                  logger.finest("[#N/]: \"" + filled + "\" has " + csvCol + " gets \"" + valAtCol + "\", ");
                  filled = filled.replaceAll("\\[#"+csvCol+"\\/\\]", valAtCol+slash);
                  logger.finest("becomes \""+filled+"\"");
               }
            } // end if "[#"
            

            
            //
            // Handle all [^#1^] [_#1_] [^#2^] [_#2_] ... variables
            //
            Pattern pattern = Pattern.compile("\\[(.)#([0-9]*)(.)\\]");
            Matcher matcher = pattern.matcher(filled);
            while( matcher.find() ) {
               String caseOperatorStart =                  matcher.group(1);
               int    csvCol            = Integer.parseInt(matcher.group(2));
               String caseOperatorEnd   =                  matcher.group(3);
               
               String value             = getColumnValue(csvCol, !asResource, FILL_EMPTY_VALUE);
               
               if(      "^".equals(caseOperatorStart) && "^".equals(caseOperatorEnd) ) {                                 // [^#N^]
                  filled = filled.replaceAll("\\[\\^#" + matcher.group(2) + "*\\^\\]", value.toUpperCase());
               }else if("_".equals(caseOperatorStart) && "_".equals(caseOperatorEnd)) {                                  // [_#N_]
                  filled = filled.replaceAll("\\[_#"   + matcher.group(2) +    "_\\]", value.toLowerCase());                  
               }else if(">".equals(caseOperatorStart) && "<".equals(caseOperatorEnd)) {                                  // [>#N<]
                   filled = filled.replaceAll("\\[>#"  + matcher.group(2) +    "<\\]", NameFactory.trimChars(value,"'"));
               }else {
                  System.err.println("unknown case operators: " + caseOperatorStart + " " + caseOperatorEnd);
               }
            }
            
            //
            // Handle [@] and all [@col_1] [@property_name] [@foo] [@bar] .... variables
            //
            if( template.indexOf("[@") >= 0 ) { // Short circuit to avoid heavy-duty processing.
               filled = filled.replaceAll("\\[\\@\\]", this.propNameFactory.getPropertyLocalName(this.columnIndex));
               int pos  = filled.indexOf("[@"); // TODO: replace by pattern/matcher
               int posE = filled.indexOf("]");
               while( pos >= 0 ) {
                  String beforeParam   = filled.substring(0,pos);
                  String property_name = filled.substring(pos+2,posE);
                  String afterParam    = filled.substring(posE+1);
   
                  int csvCol = propNameFactory.getColumnIndexOfPropertyLocalName(property_name);
                  //logger.finest("(@"+property_name + " -> #" + csvCol+")");
                  filled = beforeParam + getColumnValue(csvCol, asResource, FILL_EMPTY_VALUE) + afterParam;
                  //logger.finest("Filled [@"+property_name+"]: "+filled);
   
                  pos  = filled.indexOf("[@");
                  posE = filled.indexOf("]");
                  
                  // TODO: conversion:label "thisProp"; conversion:domain_template "[@thisProp]" breaks. ("[.]" works...)
                  // Why can't an enhancement cite itself by it's own property_name?
               }
            }
         }catch( IllegalArgumentException e ) {
            System.err.println("template: "+template + " currentValue: " + currentValue);
            e.printStackTrace();
            filled = currentValue;
         }catch( IndexOutOfBoundsException e ) {
            System.err.println("template: "+template + " currentValue: " + currentValue);
            e.printStackTrace();
            filled = currentValue;
         }catch( Exception e ) {
            System.err.println("template: "+template + " currentValue: " + currentValue);
            e.printStackTrace();
            filled = currentValue;
         }
         //System.err.println("filled template: "+template+" to "+filled);
      }
      
      // NOTE: added eParams.fillTemplate; shouldn't cause problems, but does it?
      //logger.finest("fillTemplate("+template + ", "+ currentValue + ", " + asResource + ") -> " + filled);
      return asResource ? eParams.fillTemplate(NameFactory.uriString2URI(filled)) : eParams.fillTemplate(filled);
   }
   

   //
   //
   //
   //
   
   
   /**
    * Convenience method to URI-ify values while retrieving them from the CSVRecord.
    * 
    * @param columnIndex    - 1-based.
    * @param asResource     -  
    * @param fillEmptyValue - return 'rRcCreference' if the value is empty.
    * @return
    */
   private String getColumnValue(int columnIndex, boolean asResource, boolean fillEmptyValue) {
      
      //      try{
      //         throw new NullPointerException("blah");
      //      }catch(Exception e) {
      //         e.printStackTrace();
      //      }
      
      // Pull from input cell.
      String cellValue = record.getQuotelessCommadValue(columnIndex-1);
      //logger.finest("getColumnValue "+columnIndex+": \""+cellValue+"\"");
      
      // Pull from codebook.
      if( this.eParams.getCodebook(columnIndex).containsKey(cellValue) ) {
         cellValue = this.eParams.getCodebook(columnIndex).get(cellValue).stringValue(); // TODO: what if this is a URI?
         logger.finest("getColumnValue "+columnIndex+" had codebook interpretation: \""+cellValue+"\"");
         // The value of the column should not produce a triple, but the value is being used
         // to construct a value for another column's template. So just return empty.
         if( Conversion.NULL_String.equals(cellValue) ) {
            cellValue = "";
         }
      }
      if( fillEmptyValue && (cellValue == null || cellValue.length() == 0) ) {
         cellValue = "r"+rowNumber+"c"+columnIndex+"reference";
         //logger.finest("getColumnValue "+columnIndex+" filling empty value with: \""+cellValue+"\"");
      //}else {
      //   return null; // When would we want to return null?
      //}
      }
      return asResource ? NameFactory.label2URI(cellValue) : cellValue;
   }
}