package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class SubjectAnnotationViaObjectSearchQuerylet 
                                                  extends    ColumnEnhancementQuerylet<HashMap<String,HashMap<Value,Set<Value>>>> {
   
   protected HashMap<String,                    // regex that searches cell value.
                     HashMap<Value,             // predicate of resulting triple.
                                   Set<Value>>> // set of object templates to apply captured groups to.
                                                triplesFromSearches = null;
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public SubjectAnnotationViaObjectSearchQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }
   
   @Override
   public String getQueryString(Resource context) {
      triplesFromSearches = new HashMap<String,HashMap<Value,Set<Value>>>();
      
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?regex ?predicate ?obj_template";
      String graphPattern = "  ?col"+columnPO()+                          ";\n"+
                            "        conversion:object_search [             \n"+
                            "           conversion:regex     ?regex;        \n"+
                            "           conversion:predicate ?predicate;    \n"+
                            "           conversion:object    ?obj_template; \n"+
                            "        ];                                     \n"+
                            "filter(isLiteral(?regex))\n"; // filter(isIRI(?predicate)) # Relaxing URI to template...
      String orderBy      = "?regex ?predicate ?obj_template";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, ""));
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String regex     = bindingSet.getValue("regex").stringValue();
      Value  predicate = bindingSet.getValue("predicate");
      Value  template  = bindingSet.getValue("obj_template"); // https://github.com/timrdf/csv2rdf4lod-automation/issues/349 .stringValue();
      
      System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") ." + regex + "." + " " + predicate + " " + template);
      
      if( !triplesFromSearches.containsKey(regex) ) {
         HashMap<Value,Set<Value>> pos = new HashMap<Value,Set<Value>>();
         HashSet<Value> templates = new HashSet<Value>();
         templates.add(template);
         pos.put(predicate, templates);
         triplesFromSearches.put(regex,pos);
      }else if( !triplesFromSearches.get(regex).containsKey(predicate) ) {
         Set<Value> templates = new HashSet<Value>();
         templates.add(template);
         triplesFromSearches.get(regex).put(predicate, templates);
      }else {
         triplesFromSearches.get(regex).get(predicate).add(template);
      }
   }

   @Override
   public HashMap<String, HashMap<Value,Set<Value>>> get() {
      return this.triplesFromSearches;
   }
   // ^^ Gets used at EnhancedLiteralValueHandler#assertObjectSearchDescriptions
}