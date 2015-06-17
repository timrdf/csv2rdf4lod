package edu.rpi.tw.data.csv.valuehandlers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;

/**
 * handle "12/31/2010"
 */
public class DateValueHandler extends DefaultValueHandler {

   public static SimpleDateFormat outputDF = new SimpleDateFormat("yyyy-MM-dd");
   
   protected Set<String> patterns = new HashSet<String>();
   
   protected DateFormat           inputDF;
   protected DatatypeFactory      dtfactory;
   protected GregorianCalendar    gcal = new GregorianCalendar();

   protected HashMap<URI,Integer> errorPredicates   = new HashMap<URI,Integer>();
   protected HashMap<URI,Integer> stumblePredicates = new HashMap<URI,Integer>();
   
   private static ValueFactory vf = ValueFactoryImpl.getInstance();
   
   /**
    * 
    * @param pattern
    */
   public DateValueHandler(Set<String> patterns) {
   	this();
   	
      this.patterns = patterns != null && patterns.size() > 0 ? patterns : this.patterns;
      
      System.err.println("patterns size: " + this.patterns.size());
   }
   
   /**
    * Populate with only this pattern.
    * @param pattern
    */
   public DateValueHandler(String pattern) {
   	this();
      this.patterns = new HashSet<String>();
      this.patterns.add(pattern);
   }
   
   /**
    * Auto-populate templates to attempt.
    */
   public DateValueHandler() {
   	super();
      this.patterns = new HashSet<String>();
      this.patterns.add("yyyy-MM-dd");
      this.patterns.add("M/d/yy");
      this.patterns.add("M-d-yy");
   }
   
   @Override
   public URI getRange() {
      return vf.createURI("http://www.w3.org/2001/XMLSchema#date");
   }

   @Override
   /**
    * This was added for https://github.com/timrdf/csv2rdf4lod-automation/issues/279
    * Done only in DecimalValueHandler for now.
    */
   public Value handleValue(String cellValue) {
      if( cellValue.startsWith("xsd:date(") ) {
          cellValue = cellValue.substring(9,cellValue.length()-1);
      }
      return vf.createLiteral(cellValue,this.getRange());
   }

   /**
    * @param subjectR
    * @param predicate
    * @param predicateLocalName
    * @param value
    * @param primary
    * @param resourceURIbase
    * @param conn2
    */
   @Override
   public void handleValue(Resource subjectR, URI predicate, String predicateLocalName, String value, 
                           RepositoryConnection conn, String resourceURIbase, CSVRecordTemplateFiller rec, RepositoryConnection conn2) {

      if( interpretsAsNull(value) ) return; // TODO: is this consistent with the way other handlers use codebook?
      // TODO: reconcile IntegerMultiplierValueHandler's codebook handling with interpretsAsNull(value).
      
      boolean success = false;
      for( String pattern : this.patterns ) {
         this.inputDF = new SimpleDateFormat(pattern);
         if( !success ) {
            Date date;
            try {
               date = inputDF.parse(value);
               this.gcal.setTime(date);
               conn.add(subjectR, predicate, vf.createLiteral(outputDF.format(date),this.getRange()));
               success = true;
            } catch (ParseException e) {
               this.stumbledOnValue(value, pattern, subjectR, predicate,"date");
               //e.printStackTrace();
            } catch (RepositoryException e) {
               e.printStackTrace();
            }
         }
      }
      if( !success  ) {
         this.failedOnValue(value, this.patterns.size(), subjectR, predicate,"DATE");
         try {
            conn.add(subjectR, predicate, vf.createLiteral(value));
         } catch (RepositoryException e1) {
            e1.printStackTrace();
         }
      }




      if( false ) {
      
      
      try {
         String[] tokens = null;
         if( value.split("/").length == 3 ) {
            tokens = value.split("/");
         }
         if( value.split("/").length == 2 ) {
            String month = value.split("/")[0].length()  < 4 ? value.split("/")[0] : value.split("/")[1];
            String year  = value.split("/")[0].length() == 4 ? value.split("/")[0] : value.split("/")[1];
            tokens = new String[] {month, "1", year};
         }
         if( value.split("-").length == 3 ) {
            tokens = value.split("-");
         }
         if( value.length() == 4 ) {
            tokens = new String[] {"1", "1", value};
         }
         // 12-31-2010 and 12/31/2010 and 2010
         if( tokens != null && tokens.length == 3 ) {
            //primary.add(subjectR, predicate, vf.createLiteral(value+" length "+tokens.length));
            int month = Integer.parseInt(tokens[0]);
            int day   = Integer.parseInt(tokens[1]);
            int year  = Integer.parseInt(tokens[2]);
            if( month > 12 ) {
               System.err.println(value+" month is > 12 ("+subjectR.stringValue()+" "+predicate.stringValue());
               conn.add(subjectR, predicate, vf.createLiteral(value,this.getRange()));
            }else {
               try {
                  //System.err.println("trying date from: "+value);
                  DatatypeFactory dtfactory = DatatypeFactory.newInstance();
                  XMLGregorianCalendar valueD = dtfactory.newXMLGregorianCalendar(
                        year,         month, 
                        day, 0, // Calendar.HOUR_OF_DAY 
                        0, 0,   // Calendar.MINUTE      Calendar.SECOND), 
                        0, 0);  // Calendar.MILLISECOND, -5);
                  if( subjectR == null || predicate == null ) {
                     System.err.println("ACK: "+(predicate == null));
                  }
                  if (valueD!= null) {
                     conn.add(subjectR, predicate, vf.createLiteral(valueD)); //vf.createURI("http://purl.org/dc/elements/1.1/date"), 
                  }
               } catch (DatatypeConfigurationException e) {
                  System.err.println("value: "+value);
                  e.printStackTrace();
                  //primary.add(subjectR, predicate, vf.createLiteral(value));
               } catch (IllegalArgumentException e) {
                  System.err.println("value: "+value);
                  e.printStackTrace();
                  //primary.add(subjectR, predicate, vf.createLiteral(value));
               } catch (NullPointerException e) {
                  this.gotBadValue(value, "Date-npe", predicate);
                  //System.err.println("value: "+value);
                  //e.printStackTrace();
               }
            }
         }else {
            this.gotBadValue(value, "Date", predicate);
            //System.err.println("\""+value+"\" is not friendly to Date ("+subjectR.stringValue()+" "+predicate.stringValue());
            //+" - adding as literal.");
            //primary.add(subjectR, predicate, vf.createLiteral(value));
         }
      } catch (RepositoryException e) {
         System.err.println("value: "+value);
         e.printStackTrace();
      } catch (NumberFormatException e) {
         System.err.println("value: "+value);
         e.printStackTrace();
      }
   }
   } // ???
}