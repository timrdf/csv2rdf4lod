package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * https://docs.google.com/spreadsheets/d/1mI9FU83JjYcSkKV43J-v3t3cOaQHlKLhOuQ03VkUdCE/edit#gid=0
 */
public class PML3 extends DefaultVocabulary {

   public static final ValueFactory vf = ValueFactoryImpl.getInstance();
	
   public static String BASE_URI = "http://provenanceweb.org/ns/pml#";
   public static String PREFIX   = "pml";
   
   @Override
   public String getBaseURI() {
      return BASE_URI;
   }

   @Override
   public String getPrefix() {
      return PREFIX;
   }
   
   public static final Namespace namespace            = new NamespaceImpl(PREFIX, BASE_URI);

   public static final URI       TranslationActivity  = vf.createURI(BASE_URI, "TranslationActivity");
   public static final URI       Query                = vf.createURI(BASE_URI, "Query");

   public static final URI       hasAnswer            = vf.createURI(BASE_URI, "hasAnswer");

   /**
    * @deprecated use {@link #wasGeneratedWithPlan}
    */
   public static final URI       generatedWithPlan    = vf.createURI(BASE_URI, "wasGeneratedWithPlan");
   public static final URI       wasGeneratedWithPlan = vf.createURI(BASE_URI, "wasGeneratedWithPlan");

   public static final URI       VariableMapping      = vf.createURI(BASE_URI, "VariableMapping");
   public static final URI       Variable             = vf.createURI(BASE_URI, "Variable");
}