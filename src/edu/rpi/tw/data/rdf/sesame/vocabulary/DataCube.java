package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;

/**
 * 
 */
public class DataCube extends DefaultVocabulary {

   public static String BASE_URI = "http://purl.org/linked-data/cube#";
   public static String PREFIX   = "qb";

   @Override
   public String getBaseURI() {
      return BASE_URI;
   }

   @Override
   public String getPrefix() {
      return PREFIX;
   }
   
   public static final Namespace namespace = new NamespaceImpl(PREFIX,BASE_URI);
   
   public static final URI Observation  = vf.createURI(BASE_URI, "Observation");
}