package edu.rpi.tw.data.rdf.sesame.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * 
 */
public class VoID {
   public static final ValueFactory vf = ValueFactoryImpl.getInstance();

   public static final String     PREFIX   = "void";
   public static final String     BASE_URI = "http://rdfs.org/ns/void#";
   public static final String R = BASE_URI + "";
   public static final String P = BASE_URI + "";
   public static final String D = BASE_URI + "";

   public static final URI       Namespace  = vf.createURI(BASE_URI);
   public static final Namespace namespace = new NamespaceImpl("void",BASE_URI);
   
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
  
   public static final URI DATASET            = vf.createURI(R, "Dataset");
   public static final URI Linkset            = vf.createURI(R, "Linkset");
   public static final URI DatasetDescription = vf.createURI(R, "DatasetDescription");

   public static final URI inDataset       = vf.createURI(P, "inDataset");
   public static final URI exampleResource = vf.createURI(P, "exampleResource");
   public static final URI dataDump        = vf.createURI(P, "dataDump");
   public static final URI subset          = vf.createURI(P, "subset");  
   public static final URI statItem        = vf.createURI(P, "statItem");  
   public static final URI vocabulary      = vf.createURI(P, "vocabulary");
   public static final URI triples         = vf.createURI(P, "triples"); 
   public static final URI target          = vf.createURI(P, "target"); 
   
   public static final URI numberOfTriples = vf.createURI(P, "numberOfTriples");
   
   public static final URI propertyPartition = vf.createURI(P, "propertyPartition");
   public static final URI property = vf.createURI(P, "property");
   
   
   /**
    * Number of triples required to assert the number of triples of a dataset.
    */
   public static final long NUM_TRIPLES_TO_ASSERT_NUM_TRIPLES = 4;
   
   /**
    * 
    * @param primary
    * @param dataset
    * @param dimension
    * @param value
    */
   public static void addStatItem(RepositoryConnection conn, URI dataset, URI dimension, Value value) {
      
      Resource statItem = vf.createBNode();
      
      try {
         conn.add(dataset,  VoID.statItem,   statItem);
         conn.add(statItem, SCOVO.dimension, dimension);
         conn.add(statItem, RDF.VALUE,       value);
      } catch (RepositoryException e) {
         e.printStackTrace();
      }
   }
   
   /**
    * per e.g. at http://www.w3.org/TR/2009/WD-sparql11-service-description-20091022/ 
    * 
    * @param primary
    * @param dataset
    * @param numTriples
    */
   public static void addStatItemNumTriples(RepositoryConnection conn, URI dataset, long numTriples) {
      addStatItem(conn, dataset, VoID.numberOfTriples, vf.createLiteral(""+numTriples,XMLSchema.INTEGER));
      // 3 triples are added to assert the number of triples. ---------------------^
   }
}