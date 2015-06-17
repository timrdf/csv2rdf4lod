package edu.rpi.tw.data.csv.querylets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;


/**
 * Obtain mappings of local class name to external class names.
 * 
 * "Survey respondent" -> <http://xmlns.com/foaf/0.1/Person>
 */
public class SubClassOfQuerylet extends    OnlyOneContextQuerylet<HashMap<String,Set<Value>>> {

   protected HashMap<String,Set<Value>> superClassesOfLocalClass;
   
   /**
    * 
    * @param context
    */
   public SubClassOfQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.superClassesOfLocalClass = new HashMap<String,Set<Value>>();
      
      addNamespace("conversion");
      
      String select       = "distinct ?sub ?super";
      String graphPattern = "?ds conversion:conversion_process [     "+
                                   "conversion:enhance [             "+
                                      "conversion:class_name  ?sub;  "+
                                      "conversion:subclass_of ?super;"+
                                   "];"+
                            "] #filter(isIRI(?super))";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy);
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String subClassLocal = bindingSet.getValue("sub").stringValue();
      Value  superClassURI = bindingSet.getValue("super");
      
      System.err.println(getClass().getSimpleName() + "(*) ." + subClassLocal + ". -> ." + superClassURI + ".");
      
      if( !this.superClassesOfLocalClass.containsKey(subClassLocal)) {
         this.superClassesOfLocalClass.put(subClassLocal, new HashSet<Value>());
      }
      this.superClassesOfLocalClass.get(subClassLocal).add(superClassURI);
   }

   /**
    * Value can be a pattern or a URI.
    */
   @Override
   public HashMap<String, Set<Value>> get() {
      return superClassesOfLocalClass;
   }

//	@Override
//   public boolean useful() {
//	   return false;
//   }
}