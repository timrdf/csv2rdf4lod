package edu.rpi.tw.data.csv.querylets;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public class HumanRedirectQuerylet extends OnlyOneContextQuerylet<Set<Value>> {

   protected Set<Value> redirects;
   
   /**
    * 
    * @param context
    */
   public HumanRedirectQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.redirects = new HashSet<Value>();
      
      addNamespace("conversion");
      
      String select       = "distinct ?redirect";
      String graphPattern = "?ds conversion:conversion_process [     "+
                                   "conversion:enhance [             "+
                                      "conversion:human_redirect ?redirect;  "+
                                   "];"+
                            "] #filter(isIRI(?super))";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy);
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      Value redirect = bindingSet.getValue("redirect");
      this.redirects.add(redirect);
      
      System.err.println(getClass().getSimpleName() + "(*) ." + redirect + ".");
   }

   @Override
   public Set<Value> get() {
      return this.redirects;
   }
}