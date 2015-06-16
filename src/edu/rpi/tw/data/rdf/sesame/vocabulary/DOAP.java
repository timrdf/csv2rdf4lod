package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class DOAP {
   public static final ValueFactory vf = ValueFactoryImpl.getInstance();

   public static final String     BASE_URI = "http://usefulinc.com/ns/doap#";
   public static final String R = BASE_URI + "";
   public static final String P = BASE_URI + "";
   public static final String D = BASE_URI + "";

   public static final Namespace namespace = new NamespaceImpl("doap",BASE_URI);
   
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
   public static final URI name        = vf.createURI(P, "name"); 
   public static final URI description = vf.createURI(P, "description"); 
   public static final URI shortdesc   = vf.createURI(P, "shortdesc"); 
   public static final URI license     = vf.createURI(P, "license"); 
   public static final URI created     = vf.createURI(P, "created"); 
   
   // doap:Project
   public static final URI Project              = vf.createURI(R, "Project");   
   public static final URI implementsP          = vf.createURI(P, "implements");
   public static final URI developer            = vf.createURI(P, "developer");
   public static final URI maintainer           = vf.createURI(P, "maintainer");
   public static final URI documenter           = vf.createURI(P, "documenter");
   public static final URI tester               = vf.createURI(P, "tester");
   public static final URI helper               = vf.createURI(P, "helper");
   public static final URI homepage             = vf.createURI(P, "homepage");
   public static final URI old_homepage         = vf.createURI(P, "old-homepage");
   public static final URI language             = vf.createURI(P, "language");
   public static final URI audience             = vf.createURI(P, "audience");
   public static final URI category             = vf.createURI(P, "category");
   public static final URI download_page        = vf.createURI(P, "download_page");
   public static final URI programming_language = vf.createURI(P, "programming-language");

   public static final URI Specification        = vf.createURI(R, "Specification");   
   
   // doap:Version
   public static final URI Version      = vf.createURI(R, "Version"); 
   public static final URI os           = vf.createURI(P, "os");   
   public static final URI revision     = vf.createURI(P, "revision");   
   public static final URI file_release = vf.createURI(P, "file-release");   
   public static final URI platform     = vf.createURI(P, "platform");   
   
   
   public static final URI Repository    = vf.createURI(R, "Repository"); 
   public static final URI GitRepository = vf.createURI(R, "GitRepository"); 
   public static final URI location      = vf.createURI(P, "location");  
   public static final URI browse        = vf.createURI(P, "browse");  
}