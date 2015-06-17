package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * See:
 * https://github.com/timrdf/DataFAQs/wiki/BTE-Between-The-Edges
 * https://github.com/timrdf/vsr/wiki/Characterizing-a-list-of-RDF-node-URIs#bte-vocabulary
 * https://github.com/timrdf/DataFAQs/blob/master/ontology/between-the-edges.owl
 * 
 */
public class BTE extends DefaultVocabulary {

   public static final ValueFactory vf = ValueFactoryImpl.getInstance();
	
   public static String BASE_URI = "http://purl.org/twc/vocab/between-the-edges/";
   public static String PREFIX   = "bte";
   
   @Override
   public String getBaseURI() {
      return BASE_URI;
   }

   @Override
   public String getPrefix() {
      return PREFIX;
   }
   
   public static final Namespace namespace = new NamespaceImpl(PREFIX,BASE_URI);
   
   public static final URI RDFNode          = vf.createURI(BASE_URI, "RDFNode");
   public static final URI Node             = vf.createURI(BASE_URI, "Node");
   public static final URI depth            = vf.createURI(BASE_URI, "depth");
   public static final URI fragment         = vf.createURI(BASE_URI, "fragment");
   public static final URI length           = vf.createURI(BASE_URI, "length");
   public static final URI path             = vf.createURI(BASE_URI, "path");
   public static final URI scheme           = vf.createURI(BASE_URI, "scheme");
   public static final URI netloc           = vf.createURI(BASE_URI, "netloc");
   public static final URI root             = vf.createURI(BASE_URI, "root");
   public static final URI pld              = vf.createURI(BASE_URI, "pld");
   public static final URI PayLevelDomain   = vf.createURI(BASE_URI, "PayLevelDomain");
   public static final URI PrefixTree       = vf.createURI(BASE_URI, "PrefixTree");
}