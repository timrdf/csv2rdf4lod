package edu.rpi.tw.data.csv.valuehandlers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.csv.impl.CSVRecordTemplateFiller;
import edu.rpi.tw.string.pmm.DefaultPrefixMappings;
import edu.rpi.tw.string.pmm.PrefixMappings;

/**
 * 
 */
public class DateTimeValueHandler extends DefaultValueHandler {
   
   private static Logger logger = Logger.getLogger(DateTimeValueHandler.class.getName());
   
   private static ValueFactory vf = ValueFactoryImpl.getInstance();

   public static PrefixMappings  pmap = new DefaultPrefixMappings();
   
   protected HashMap<String,URI> patterns; // <datepattern,range>
   protected int                 timezone; // in minutes

   protected DatatypeFactory   dtfactory;
   protected GregorianCalendar gcal = new GregorianCalendar();
   
   public static SimpleDateFormat outputDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
   
   /**
    * 
    * @param pattern
    * @param timezone - number of minutes from GMT (EST would be -300)
    */
   public DateTimeValueHandler(HashMap<String,URI> patterns, int timezone) {
      super();
      this.patterns = patterns;
      if( this.patterns.size() == 0 ) {
         this.patterns.put("yyyy-MM-dd'T'HH:mm:ssZ", vf.createURI(XMLSchema.DATETIME.stringValue()));
      }
      this.timezone = timezone;
      try {
         this.dtfactory = DatatypeFactory.newInstance();
      } catch (DatatypeConfigurationException e) {
         e.printStackTrace();
      }
   }

   @Override
   public URI getRange() {
      return XMLSchema.DATETIME;
   }

   @Override
   public void handleValue(Resource subjectR, URI predicate, String predicateLocalName, String value,
                           RepositoryConnection conn, String resourceURIbase, CSVRecordTemplateFiller rec, 
                           RepositoryConnection conn2) {

      //System.err.println("in here");
      //http://java.sun.com/j2se/1.5.0/docs/api/java/util/Formatter.html#dt
      //strftime
      //http://java.sun.com/j2se/1.4.2/docs/api/java/text/DateFormat.html#parse%28java.lang.String%29
      
      // Chad help.
      //String sd = "12/23/09 9:08";
      //SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy HH:mm");
      //SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy h:mm a");

      logger.fine(this.patterns.size() + " patterns to try timezone @"+timezone);
      boolean success = false;
      for( String pattern : this.patterns.keySet() ) {
         logger.fine("trying "+pattern);

         DateTimeFormatter jt_fmt = DateTimeFormat.forPattern(pattern); // Chad is magical
         
         if( !success ) {
            try {
               //pre-Joda time DateFormat inputDF = new SimpleDateFormat(pattern);
               //inputDF.setLenient(true);
               //Date date = inputDF.parse(value);

               // Chad magical
               DateTime jt_date = jt_fmt.parseDateTime(value);   // Chad is magical
               Date date = jt_date.toDate();                     // Chad is magical (back to normal Java Date)
               success = true;
               // http://joda-time.sourceforge.net/api-release/index.html
               // http://download.oracle.com/javase/6/docs/api/java/text/DateFormat.html
               
               
               /*if( false ) {
                  // Just pass through the literal (SUBOPTIMAL).
                  conn.add(subjectR, predicate, vf.createLiteral(value));
               }else if ( false ) {
                  // Pass through the literal in the dateTime string format (NOT TYPED).
                  conn.add(subjectR, predicate, vf.createLiteral(outputDF.format(date)));
               }else {*/
                  // Pass through the typed dateTime format (IDEAL).
                  
               
               
               // Find out whether the pattern was for DATE or DATE TIME.
               URI range = this.patterns.get(pattern);
               
               if( range.stringValue().equals(XMLSchema.DATE.stringValue()) ) {
                  //
                  // Treat as just a DATE
                  //
                  String formatted = DateValueHandler.outputDF.format(date);
                  conn.add(subjectR, predicate, vf.createLiteral(formatted,range));
               }else {
                  //
                  // Treat as a DATE TIME.
                  //
                  StringBuffer formatted = new StringBuffer(DateTimeValueHandler.outputDF.format(date));
                  formatted.insert(formatted.length()-2, ":");
                  conn.add(subjectR, predicate, vf.createLiteral(formatted.toString(),range));
                  
                  /*this.gcal.setTime(date);
                  XMLGregorianCalendar xDate = dtfactory.newXMLGregorianCalendar(
                                                    gcal.get(Calendar.YEAR),         gcal.get(Calendar.MONTH), 
                                                    gcal.get(Calendar.DAY_OF_MONTH), gcal.get(Calendar.HOUR_OF_DAY), 
                                                    gcal.get(Calendar.MINUTE),       gcal.get(Calendar.SECOND), 
                                                    gcal.get(Calendar.MILLISECOND),  this.timezone);
                  primary.add(subjectR, predicate, vf.createLiteral(xDate));*/
                     
                     
                  //}
               }
            }/* pre-joda parse catch (ParseException e) {
               logger.finest("gonna stumble");
               this.stumbledOnValue(value, pattern, subjectR, predicate);
               e.printStackTrace();
            }*/ catch ( IllegalArgumentException e ) {
               // http://joda-time.sourceforge.net/api-release/org/joda/time/format/DateTimeFormatter.html
               // throws IllegalArgumentException
               this.stumbledOnValue(value, pattern, subjectR, predicate);
               System.err.println("datetime XMLG: \""+value+"\" !~ \""+pattern+"\" Z"+timezone+" @ "+
                                   pmap.bestLabelFor(subjectR.stringValue())+" "+predicate.getLocalName());
               e.printStackTrace();
            }catch( UnsupportedOperationException e ) {
               // http://joda-time.sourceforge.net/api-release/org/joda/time/format/DateTimeFormatter.html
               // throws IllegalArgumentException
               this.stumbledOnValue(value, pattern, subjectR, predicate);
            }catch (RepositoryException e) {
               e.printStackTrace();
            }
         }
      }
      
      if(!success) {
         this.failedOnValue(value, this.patterns.size(), subjectR, predicate);
         try {
            conn.add(subjectR, predicate, vf.createLiteral(value));
         } catch (RepositoryException e1) {
            e1.printStackTrace();
         }
      }
   }
   
   /**
    * 
    * @return Sesame-friendly date object for ValueFactory.createLiteral
    */
   public static XMLGregorianCalendar getNowXMLGregorianCalendar() {
      return getXMLGregorianCalendar(System.currentTimeMillis());
   }
   
   /**
    * 
    * @return Sesame-friendly date object for ValueFactory.createLiteral
    */
   public static Literal getNow() {
      return vf.createLiteral(getXMLGregorianCalendar(System.currentTimeMillis()));
   }
   
   /**
    * 
    * @return Sesame-friendly date object for ValueFactory.createLiteral
    */
   public static XMLGregorianCalendar getXMLGregorianCalendar(long milliseconds) {
      GregorianCalendar gcal = new GregorianCalendar();
      gcal.setTimeInMillis(milliseconds);
      return getXMLGregorianCalendar(gcal);
   }
   
   /**
    * 
    * @return Sesame-friendly date object for ValueFactory.createLiteral
    */
   public static XMLGregorianCalendar getXMLGregorianCalendar(GregorianCalendar gcal) {
      DatatypeFactory dtfactory;
      XMLGregorianCalendar xgcal = null;
      try {
         dtfactory = DatatypeFactory.newInstance();
         //System.err.println("getNowXMLGregorianCalendar: month " + gcal.get(Calendar.MONTH));
         xgcal = dtfactory.newXMLGregorianCalendar(                           //    XMLGregorianCalendar: 1 == January
                     gcal.get(Calendar.YEAR),         gcal.get(Calendar.MONTH)+1, // gcal.get(Calendar.MONTH) 0 == January
                     gcal.get(Calendar.DAY_OF_MONTH), gcal.get(Calendar.HOUR_OF_DAY), 
                     gcal.get(Calendar.MINUTE),       gcal.get(Calendar.SECOND), 
                     gcal.get(Calendar.MILLISECOND),  0);
      } catch (DatatypeConfigurationException e1) {
         e1.printStackTrace();
      }
      return xgcal;
   }
}