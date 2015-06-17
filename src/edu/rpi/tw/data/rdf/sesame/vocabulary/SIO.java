package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class SIO extends DefaultVocabulary {

   public static final ValueFactory vf = ValueFactoryImpl.getInstance();
	
   public static String BASE_URI = "http://semanticscience.org/resource/";
   public static String PREFIX   = "sio";
   
   @Override
   public String getBaseURI() {
      return BASE_URI;
   }

   @Override
   public String getPrefix() {
      return PREFIX;
   }
   
   public static final Namespace namespace = new NamespaceImpl(PREFIX,BASE_URI);
   
   
   public static final URI count               = vf.createURI(BASE_URI, "count");
   public static final URI hasMember           = vf.createURI(BASE_URI, "has-member");
   
   public static final URI hasAttribute        = vf.createURI(BASE_URI, "has-attribute");
   public static final URI Attribute           = vf.createURI(BASE_URI, "Attribute");
   public static final URI refersTo            = vf.createURI(BASE_URI, "refers-to");
}