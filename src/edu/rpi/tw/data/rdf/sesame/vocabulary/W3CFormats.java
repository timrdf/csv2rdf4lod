package edu.rpi.tw.data.rdf.sesame.vocabulary;

import java.util.Collection;
import java.util.HashSet;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * 
 */
public class W3CFormats extends DefaultVocabulary {
   
   public static String BASE_URI = "http://www.w3.org/ns/formats/";
   public static String PREFIX   = "formats";

   @Override
   public String getBaseURI() {
      return BASE_URI;
   }

   @Override
   public String getPrefix() {
      return PREFIX;
   }
   
   public static final Namespace namespace = new NamespaceImpl(PREFIX,BASE_URI);

   protected static ValueFactory vf = ValueFactoryImpl.getInstance();
   
   public static final URI media_type       = vf.createURI(BASE_URI+"media_type");
   
   public static final URI                  Turtle           = vf.createURI(BASE_URI + "Turtle");
   public static final URI                  RDF_XML          = vf.createURI(BASE_URI + "RDF_XML");
   public static final URI                  N_TRIPLES        = vf.createURI(BASE_URI + "N-Triples");
   public static final URI                  N_QUADS          = vf.createURI(BASE_URI + "N-Quads");
   public static final URI                  preferred_suffix = vf.createURI(BASE_URI + "preferred_suffix");
   
   public static final Collection<Resource> terms = new HashSet<Resource>();

   public static Collection<Resource> getTerms() {
      ValueFactory vf = ValueFactoryImpl.getInstance();
      if( terms.size() == 0 ) {
         terms.add(Turtle);
         terms.add(RDF_XML);
         terms.add(N_TRIPLES);
      }
      return terms;
   }
}