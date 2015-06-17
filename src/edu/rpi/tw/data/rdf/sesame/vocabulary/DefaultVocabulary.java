package edu.rpi.tw.data.rdf.sesame.vocabulary;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * 
 */
public abstract class DefaultVocabulary implements Vocabulary {

   protected static ValueFactory vf = ValueFactoryImpl.getInstance();

   protected static String     BASE_URI = "http://open.vocab.org/terms/";
   protected static String R = BASE_URI + "";
   protected static String P = BASE_URI + "";
   protected static String D = BASE_URI + "";
   
   protected static String PREFIX = "ov";

   public static URI       namespace = null;
   public static Namespace Namespace = null;
   
   protected Set<URI> terms = new HashSet<URI>(); 
   
   protected DefaultVocabulary() {
   }
   
   /**
    * 
    * @param prefix
    * @param ns
    */
   protected DefaultVocabulary(String prefix, URI ns) {
      this();
      PREFIX    = prefix;
      BASE_URI  = namespace.stringValue();
      namespace = vf.createURI(BASE_URI);
      Namespace = new NamespaceImpl("void",BASE_URI);
   }
   
   public static String name(String localName) {
       return nameResource(localName);
   }
   public static String nameResource(String localName) {
       return R + localName;
   }
   public static String nameProperty(String localName) {
       return P + localName;
   }
   public static String nameDatatype(String localName) {
       return D + localName;
   }
   

   public static URI nameR(String localName) {
      return vf.createURI(nameResource(localName));
   }
   public static URI nameResourceR(String localName) {
      return vf.createURI(R + localName);
   }
   public static URI namePropertyR(String localName) {
      return vf.createURI(P + localName);
   }
   public static URI nameDatatypeR(String localName) {
      return vf.createURI(D + localName);
   } 
}