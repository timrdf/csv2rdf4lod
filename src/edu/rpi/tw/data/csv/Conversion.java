package edu.rpi.tw.data.csv;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import edu.rpi.tw.string.WikimediaURIMapper;

/**
 * 
 */
public class Conversion {
   public static final ValueFactory vf = ValueFactoryImpl.getInstance();

   //public static final String     BASE_URI = "http://data-gov.tw.rpi.edu/vocab/conversion/";
   public static final String     BASE_URI = "http://purl.org/twc/vocab/conversion/";
   public static final String R = BASE_URI + "";
   public static final String P = BASE_URI + "";
   public static final String D = BASE_URI + "";

   public static final URI       Namespace  = vf.createURI(R, WikimediaURIMapper.map("Namespace"));
   public static final Namespace namespace = new NamespaceImpl("conversion",BASE_URI);
  
   protected static Set<URI> terms = null;
   
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
  
   public static final URI DATASET                  = vf.createURI(R, "Dataset");
   public static final URI AbstractDataset          = vf.createURI(R, "AbstractDataset");
   // renamed to AbstractDataset: public static final URI UnVersionedDataset = vf.createURI(R, "UnVersionedDataset");
   public static final URI VersionedDataset         = vf.createURI(R, "VersionedDataset");
   public static final URI LayerDataset             = vf.createURI(R, "LayerDataset");
   public static final URI SameAsDataset            = vf.createURI(R, "SameAsDataset");
   public static final URI MetaDataset              = vf.createURI(R, "MetaDataset");
   public static final URI DatasetSample            = vf.createURI(R, "DatasetSample");
   public static final URI EnhancedDataset          = vf.createURI(R, "EnhancedDataset");
   public static final URI RawDataset               = vf.createURI(R, "RawDataset");
   public static final URI DirectSameAsEnhancement  = vf.createURI(R, "DirectSameAsEnhancement");
   public static final URI CaseInsensitiveLODLink   = vf.createURI(R, "CaseInsensitiveLODLink");
   public static final URI IncludesLODLinks         = vf.createURI(R, "IncludesLODLinks");
   
   public static final URI num_triples            = vf.createURI(P, "num_triples");
   public static final URI uses_predicate         = vf.createURI(P, "uses_predicate");
   public static final URI uses_class             = vf.createURI(P, "uses_class");
   public static final URI has_version            = vf.createURI(P, "has_version");
   
   public static final URI base_uri               = vf.createURI(P, "base_uri");
   public static final URI source_identifier      = vf.createURI(P, "source_identifier");
   public static final URI dataset_identifier     = vf.createURI(P, "dataset_identifier");
   public static final URI subject_discriminator  = vf.createURI(P, "subject_discriminator");
   public static final URI dataset_version        = vf.createURI(P, "dataset_version"); // TODO: deprecate
   public static final URI version_identifier     = vf.createURI(P, "version_identifier"); // TODO: use instead of dataset_version
   
   public static final URI symbol                 = vf.createURI(P,"symbol");
   public static final URI interpretation         = vf.createURI(P,"interpretation");
   
   public static final URI NULL                   = vf.createURI(R,"null");
   public static final String NULL_String         = vf.createURI(R,"null").stringValue();
   
   public static final URI enhances               = vf.createURI(P,"enhances");
   public static final URI conversion_identifier  = vf.createURI(P,"conversion_identifier");
   public static final URI enhancement_layer      = vf.createURI(P,"enhancement_layer");
   
   public static final URI triples_per_minute     = vf.createURI(P,"triples_per_minute");
   
   public static final URI topic                  = vf.createURI(P,"topic");
   public static final URI name                   = vf.createURI(P,"name");   
   public static final URI value                   = vf.createURI(P,"value");
   
   //public static final URI from_dataset           = vf.createURI(P,"from_dataset"); // what is this?
   
   public static Set<URI> getTerms() {
      if(terms == null) {
         terms = new HashSet<URI>();
         terms.add(Conversion.DATASET);
         terms.add(Conversion.AbstractDataset);
         terms.add(Conversion.VersionedDataset);
         terms.add(Conversion.LayerDataset);
         terms.add(Conversion.SameAsDataset);
         terms.add(Conversion.MetaDataset);
         terms.add(Conversion.DatasetSample);
         
         terms.add(Conversion.num_triples);
         terms.add(Conversion.uses_predicate);
         terms.add(Conversion.uses_class);
         terms.add(Conversion.has_version);
         
         terms.add(Conversion.base_uri);
         terms.add(Conversion.source_identifier);
         terms.add(Conversion.dataset_identifier);
         terms.add(Conversion.subject_discriminator);
         terms.add(Conversion.version_identifier);
         
         terms.add(Conversion.symbol);
         terms.add(Conversion.interpretation);
         
         terms.add(Conversion.NULL);
         
         terms.add(Conversion.enhances);
         terms.add(Conversion.conversion_identifier);
         terms.add(Conversion.enhancement_layer);
         
         //terms.add(Conversion.from_dataset);
      }
      return terms;
   }
}