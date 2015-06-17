package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * 
 */
public class TetherlessWorld {
   public static final ValueFactory vf = ValueFactoryImpl.getInstance();
   
   // HTTP vs HTTPS: Wikimedia's semwiki RDFFeed reports http
   public static final String     BASE_URI = "http://tw.rpi.edu/wiki/Special:URIResolver/";
   public static final String R = BASE_URI + "";
   public static final String P = BASE_URI + "Property-3A";
   public static final String D = BASE_URI + "Datatype-3A";

   public static final URI       Namespace  = vf.createURI(BASE_URI);
   public static final Namespace namespace = new NamespaceImpl("tw",BASE_URI);
   
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
  
   public static final URI acknowledged    = vf.createURI(P, "acknowledged");
   public static final URI acknowledges    = vf.createURI(P, "acknowledges");
   public static final URI abbreviatedWith = vf.createURI(P, "abbreviatedWith");
   public static final URI prefers         = vf.createURI(P, "prefers");
}