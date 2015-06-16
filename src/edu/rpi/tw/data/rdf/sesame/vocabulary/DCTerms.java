package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class DCTerms {
   public static final ValueFactory vf = ValueFactoryImpl.getInstance();

   public static final String     BASE_URI = "http://purl.org/dc/terms/";
   public static final String R = BASE_URI + "";
   public static final String P = BASE_URI + "";
   public static final String D = BASE_URI + "";

   public static final URI       Namespace = vf.createURI(BASE_URI);
   public static final Namespace namespace = new NamespaceImpl("dcterms",BASE_URI);
   
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
 
   public static final URI isPartOf       = vf.createURI(P, "isPartOf");
   public static final URI hasPart        = vf.createURI(P, "hasPart");
   public static final URI isReferencedBy = vf.createURI(P, "isReferencedBy");
   public static final URI references     = vf.createURI(P, "references");
   public static final URI identifier     = vf.createURI(P, "identifier");
   public static final URI created        = vf.createURI(P, "created");
   public static final URI modified       = vf.createURI(P, "modified");
   public static final URI version        = vf.createURI(P, "version");
   public static final URI source         = vf.createURI(P, "source");
   public static final URI author         = vf.createURI(P, "author");
   public static final URI contributor    = vf.createURI(P, "contributor");
   public static final URI description    = vf.createURI(P, "description");
   public static final URI title          = vf.createURI(P, "title");
   public static final URI format         = vf.createURI(P, "format");
   public static final URI date           = vf.createURI(P, "date");
   public static final URI subject        = vf.createURI(P, "subject");
}