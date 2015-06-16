package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class FOAF {
   public static final ValueFactory vf = ValueFactoryImpl.getInstance();

   public static final String     BASE_URI = "http://xmlns.com/foaf/0.1/";
   public static final String R = BASE_URI + "";
   public static final String P = BASE_URI + "";
   public static final String D = BASE_URI + "";

   public static final URI       Namespace  = vf.createURI(BASE_URI);
   public static final Namespace namespace = new NamespaceImpl("foaf",BASE_URI);
   
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
  
   public static final URI Person           = vf.createURI(R, "Person");
   public static final URI Agent            = vf.createURI(R, "Agent");
   public static final URI Image            = vf.createURI(R, "Image");

   public static final URI primaryTopic     = vf.createURI(P, "primaryTopic");
   public static final URI isPrimaryTopicOf = vf.createURI(P, "isPrimaryTopicOf");
   public static final URI homepage         = vf.createURI(P, "homepage");
   public static final URI topic            = vf.createURI(P, "topic");
   public static final URI topic_interest   = vf.createURI(P, "topic_interest");
   public static final URI interest         = vf.createURI(P, "interest");
   public static final URI depiction        = vf.createURI(P, "depiction");
   public static final URI depicts          = vf.createURI(P, "depicts");
   public static final URI img              = vf.createURI(P, "img");
   public static final URI name             = vf.createURI(P, "name");
   public static final URI knows            = vf.createURI(P, "knows");
}