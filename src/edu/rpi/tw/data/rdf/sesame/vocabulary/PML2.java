package edu.rpi.tw.data.rdf.sesame.vocabulary;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.string.NameFactory;
import edu.rpi.tw.string.NameFactory.NameType;

/**
 * 
 */
public class PML2 {
   public static final ValueFactory vf = ValueFactoryImpl.getInstance();

   public static final String P_NAMESPACE  = "http://inference-web.org/2.0/pml-provenance.owl#";
   public static final String J_NAMESPACE  = "http://inference-web.org/2.0/pml-justification.owl#";
   public static final String T_NAMESPACE  = "http://inference-web.org/2.0/pml-trust.owl#";

   // Classes 

   /**  */
   public final static URI INFORMATION      = vf.createURI(P_NAMESPACE+"Information");
   public final static URI INFERENCE_ENGINE = vf.createURI(P_NAMESPACE+"InferenceEngine");
   
   public final static URI NODE_SET         = vf.createURI(J_NAMESPACE+"NodeSet");
   public final static URI INFERENCE_STEP   = vf.createURI(J_NAMESPACE+"InferenceStep");
   
   public static final URI       WebService            = vf.createURI(P_NAMESPACE, "WebService");
   
   // Properties

   /**  */
   public final static URI HAS_RAW_STRING = vf.createURI(P_NAMESPACE+"hasRawString");
   public final static URI HAS_LANGUAGE   = vf.createURI(P_NAMESPACE+"hasLanguage");
   public final static URI HAS_FORMAT     = vf.createURI(P_NAMESPACE+"hasFormat");
   
   public final static URI HAS_CONCLUSION       = vf.createURI(J_NAMESPACE+"hasConclusion");
   public final static URI IS_CONSEQUENT_OF     = vf.createURI(J_NAMESPACE+"isConsequentOf");
   public final static URI HAS_INFERENCE_ENGINE = vf.createURI(J_NAMESPACE+"hasInferenceEngine");
   public final static URI HAS_ANTECEDENT_LIST  = vf.createURI(J_NAMESPACE+"hasAntecedentList");

   //    static {
   //        ValueFactory factory = ValueFactoryImpl.getInstance();
   //
   //        // Classes 
   //        
   //        INFORMATION              = factory.createURI(PML.namespaceP, "Information");
   //        INFERENCE_ENGINE         = factory.createURI(PML.namespaceP, "InferenceEngine");
   //
   //        
   //        // Properties
   //        
   //        HAS_RAW_STRING            = factory.createURI(PML.namespaceP, "hasRawString");
   //        HAS_LANGUAGE              = factory.createURI(PML.namespaceP, "hasLanguage");
   //    }

   public static final Namespace namespaceP = new NamespaceImpl("pmlp",P_NAMESPACE);
   public static final Namespace namespaceJ = new NamespaceImpl("pmlj",J_NAMESPACE);
   public static final Namespace namespaceT = new NamespaceImpl("pmlt",T_NAMESPACE);
   

   /**
    * @param primary           - connection to assert the list.
    * @param provenanceBase - namespace to put new nodesets, link nodes.
    * @param listR          - the head of the list. Will begin by asserting rdf:first and rdf:rest to this.
    * @param antecedents    - The antecedents to put into the list.
    * 
    * @return NodeSet resources that were used to wrap each Information antecedent resource.
    */
   public static Map<Resource,Resource> addAntecedentList(RepositoryConnection conn, String provenanceBase, 
                                                          Resource listR, Resource ... antecedents) {
  
      HashMap<Resource,Resource> antecedentNodeSets = new HashMap<Resource,Resource>();
      
      boolean useBnodeLists = true; // Rapper doesn't like URI rdf:Lists. Jena is cool with it, though.
      
      Resource nextToLastListR = null;
      Resource lastListR       = listR;
      Resource nextListR       = null;

      for( Resource antecedent : antecedents ) {
         Resource nodeSet = NameFactory.getResource(provenanceBase, "nodeset", NameType.INCREMENTING);
         antecedentNodeSets.put(antecedent, nodeSet);
         
         nextListR        = useBnodeLists ? vf.createBNode() 
                                          : NameFactory.getResource(provenanceBase, "list", NameType.INCREMENTING);
         try {
            conn.add(lastListR, RDF.TYPE,           RDF.LIST);
            conn.add(lastListR, RDF.FIRST,          nodeSet);
            conn.add(nodeSet,   RDF.TYPE,           PML2.NODE_SET);
            conn.add(nodeSet,   PML2.HAS_CONCLUSION, antecedent);
            conn.add(lastListR, RDF.REST,           nextListR);
            nextToLastListR = lastListR;                            // Storing to trim off last list.
            lastListR       = nextListR;
         } catch (RepositoryException e) {
            e.printStackTrace();
         }
      }
      
      try {
         conn.remove(nextToLastListR, RDF.REST, nextListR);
         conn.add(nextToLastListR,    RDF.REST, RDF.NIL);
         conn.commit();
      } catch (RepositoryException e) {
         e.printStackTrace();
      }
      return antecedentNodeSets;
   }
}