package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class OWLTime {
   
   /** http://www.w3.org/2006/time# */
   public static final String NAMESPACE = "http://www.w3.org/2006/time#";

   // Classes 
   
   /** http://www.w3.org/2006/time# */
   public final static URI INSTANT;
   
   /** http://www.w3.org/2006/time# */
   public final static URI DURATION_DESCRIPTION;

   /** http://www.w3.org/2006/time# */
   public final static URI DATE_TIME_INTERVAL;
   
   // Properties
   
   /** http://www.w3.org/2006/time#hasBeginning */
   public final static URI HAS_BEGINNING;

   /** http://www.w3.org/2006/time# */
   public final static URI HAS_DURATION_DESCRIPTION;
   
   /** http://www.w3.org/2006/time# */
   public final static URI HAS_END;

   /** http://www.w3.org/2006/time# */
   public final static URI SECONDS;
   /** http://www.w3.org/2006/time# */
   public final static URI MINUTES;
   /** http://www.w3.org/2006/time# */
   public final static URI HOURS;
   /** http://www.w3.org/2006/time# */
   public final static URI DAYS;
   /** http://www.w3.org/2006/time# */
   public final static URI WEEKS;
   /** http://www.w3.org/2006/time# */
   public final static URI MONTHS;
   /** http://www.w3.org/2006/time# */
   public final static URI YEARS;

   /** http://www.w3.org/2006/time# */
   public final static URI IN_XSD_DATETIME;
   
   static {
      ValueFactory factory = ValueFactoryImpl.getInstance();

      // Classes 
      
      INSTANT              = factory.createURI(OWLTime.NAMESPACE, "Instant");
      DURATION_DESCRIPTION = factory.createURI(OWLTime.NAMESPACE, "DurationDescription");
      DATE_TIME_INTERVAL   = factory.createURI(OWLTime.NAMESPACE, "DateTimeInterval");
      
      // Properties
      
      HAS_BEGINNING            = factory.createURI(OWLTime.NAMESPACE, "hasBeginning");
      HAS_DURATION_DESCRIPTION = factory.createURI(OWLTime.NAMESPACE, "hasDurationDescription");
      HAS_END                  = factory.createURI(OWLTime.NAMESPACE, "hasEnd");
      SECONDS                  = factory.createURI(OWLTime.NAMESPACE, "seconds");
      MINUTES                  = factory.createURI(OWLTime.NAMESPACE, "minutes");
      HOURS                    = factory.createURI(OWLTime.NAMESPACE, "hours");
      DAYS                     = factory.createURI(OWLTime.NAMESPACE, "days");
      WEEKS                    = factory.createURI(OWLTime.NAMESPACE, "weeks");
      MONTHS                   = factory.createURI(OWLTime.NAMESPACE, "months");
      YEARS                    = factory.createURI(OWLTime.NAMESPACE, "years");

      IN_XSD_DATETIME          = factory.createURI(OWLTime.NAMESPACE, "inXSDDateTime");
   }
}