package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * 
 */
public class OpenVocab {
   
   public static final ValueFactory vf = ValueFactoryImpl.getInstance();

   public static final String     BASE_URI = "http://open.vocab.org/terms/";
   public static final String R = BASE_URI + "";
   public static final String P = BASE_URI + "";
   public static final String D = BASE_URI + "";

   public static final URI       Namespace  = vf.createURI(BASE_URI);
   public static final Namespace namespace = new NamespaceImpl("ov",BASE_URI);
   
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
  
   public static final URI DATASET = vf.createURI(R, "Dataset");
   
   public static final URI csvRow     = vf.createURI(R, "csvRow");
   public static final URI csvCol     = vf.createURI(R, "csvCol");
   public static final URI csvHeader  = vf.createURI(R, "csvHeader");
   public static final URI csvTopM    = vf.createURI(R, "csvTopMatter");
   public static final URI csvBottomM = vf.createURI(R, "csvBottomMatter");
   public static final URI subDis      = vf.createURI(R, "subjectDiscriminator");
}