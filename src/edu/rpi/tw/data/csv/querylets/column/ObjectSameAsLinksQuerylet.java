package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

/**
 * 
 */
public class ObjectSameAsLinksQuerylet extends SameAsLinksQuerylet {

   private static Logger logger = Logger.getLogger(ObjectSameAsLinksQuerylet.class.getName());
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public ObjectSameAsLinksQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.sameAsLinks = new HashMap<String,HashSet<URI>>();
      
      this.addNamespace("rdfs","xsd","conversion","dcterms",columnPrefix());
      
      String select       = "?linksVia ?predicate ?type ?keyP ?keyO";
      String graphPattern = "?col "+columnPO()+";                                       \n"+
                            "     conversion:range      ?range ;                        \n"+
                            "     conversion:links_via  ?linksVia .                     \n"+
                            "     filter(isIRI(?range))                                 \n"+
                            "  optional { ?col conversion:subject_of ?predicate         \n"+
                            "              filter(isIRI(?predicate))            }       \n"+
                            "  optional { ?col conversion:keys [                        \n"+
                            "                dcterms:hasPart [                          \n"+
                            "                   conversion:predicate ?keyP;             \n"+
                            "                   conversion:object    ?keyO;             \n"+
                            "                ];                                         \n"+
                            "             ] }                                           \n"+
                            "  optional { ?col a ?type }                                \n"+
                            "  optional { ?col2 a conversion:SubjectSameAsEnhancement   \n"+
                            "             filter(?col = ?col2 )                       } \n"+
                            "             filter(!bound(?col2))";
      String orderBy      = "";
      String limit        = "";
      
      //logger.finest(this.composeQuery(select, context, graphPattern, orderBy, limit));
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }
}