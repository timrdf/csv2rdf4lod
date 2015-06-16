package edu.rpi.tw.data.rdf.sesame.vocabulary;

import java.util.Collection;
import java.util.HashSet;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class FRBR extends DefaultVocabulary {
   
   public static String BASE_URI = "http://purl.org/vocab/frbr/core#";
   public static String PREFIX = "frbrcore";
   
   protected static ValueFactory vf = ValueFactoryImpl.getInstance();
   public static final Namespace namespace = new NamespaceImpl("frbrcore","http://purl.org/vocab/frbr/core#");
   
   public static Collection<Resource> getTerms() {
      ValueFactory vf = ValueFactoryImpl.getInstance();
      Collection<Resource> terms = new HashSet<Resource>();
      terms.add(vf.createURI(FRBR.BASE_URI+"Manifestation"));

      return terms;
   }

   @Override
   public String getBaseURI() {
      return FRBR.BASE_URI;
   }

   @Override
   public String getPrefix() {
      return "frbrcore";
   }
   
   public static final URI Work           = vf.createURI(BASE_URI, "Work");          //   ||
   public static final URI realization    = vf.createURI(BASE_URI, "realization");   //   \/
   
  
   public static final URI realizationOf  = vf.createURI(BASE_URI, "realizationOf"); //   /\
   public static final URI Expression     = vf.createURI(BASE_URI, "Expression");    //   ||
   public static final URI embodiment     = vf.createURI(BASE_URI, "embodiment");    //   \/

 
   public static final URI embodimentOf   = vf.createURI(BASE_URI, "embodimentOf");  //   /\
   public static final URI Manifestation  = vf.createURI(BASE_URI, "Manifestation"); //   ||
   public static final URI exemplar       = vf.createURI(BASE_URI, "exemplar");      //   \/
   

   public static final URI exemplarOf     = vf.createURI(BASE_URI, "exemplarOf");    //   /\
   public static final URI Item           = vf.createURI(BASE_URI, "Item");          //   ||
}