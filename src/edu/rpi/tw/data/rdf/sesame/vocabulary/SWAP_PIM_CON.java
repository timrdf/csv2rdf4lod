package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class SWAP_PIM_CON {
   public static final ValueFactory vf = ValueFactoryImpl.getInstance();

   public static final String     BASE_URI = "http://www.w3.org/2000/10/swap/pim/contact#";
   public static final String R = BASE_URI + "";
   public static final String P = BASE_URI + "";
   public static final String D = BASE_URI + "";

   public static final Namespace namespace = new NamespaceImpl("con",BASE_URI);
   
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
   
   //
   public static final URI preferredURI = vf.createURI(P, "preferredURI");
}
