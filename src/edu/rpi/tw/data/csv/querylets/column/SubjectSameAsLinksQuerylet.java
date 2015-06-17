package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashMap;
import java.util.HashSet;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

/**
 * 
 */
public class SubjectSameAsLinksQuerylet extends SameAsLinksQuerylet {

   public SubjectSameAsLinksQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.sameAsLinks = new HashMap<String,HashSet<URI>>();
      
      this.addNamespace("rdfs","xsd","conversion",columnPrefix());
      
      String select       = "?linksVia ?predicate ?type";
      String graphPattern = "?col "+columnPO()+";                        \n"+
                            "     a conversion:SubjectSameAsEnhancement; \n"+
                            "     conversion:links_via  ?linksVia;       \n"+
                            "     conversion:subject_of ?predicate .     \n"+
                            "             filter(isIRI(?predicate))      \n"+
                            "  optional { ?col a ?type }";
      String orderBy      = "";
      String limit        = "";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, limit));
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }
}