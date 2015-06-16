package edu.rpi.tw.data.csv.querylets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.PluralContextsQuerylet;

/**
 * 
 */
public class ParametersImportQuerylet extends PluralContextsQuerylet<Set<URI>> {

   public ParametersImportQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Collection<Resource> contexts) {
      this.addNamespace("conversion");
      
      String select       = "distinct ?importee";
      String graphPattern = "?s \n"+
                            "     conversion:includes ?importee . filter(isuri(?importee)) ";
      
      return this.composeQuery(select, contexts, graphPattern);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      Value importee = bindingSet.getValue("importee");
      uriSet.add((URI)importee);
      System.err.println(getClass().getSimpleName() + "(*) ." + importee.stringValue() + ".");
   }
   
   protected Set<URI> uriSet = new HashSet<URI>();
   
   @Override
   public Set<URI> get() {
      return uriSet;
   }
}