package edu.rpi.tw.data.csv.querylets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.PluralContextsQuerylet;


/**
 * 
 */
public class AuthorsQuerylet extends PluralContextsQuerylet<Set<URI>> {

   public AuthorsQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Collection<Resource> contexts) {
      this.addNamespace("conversion");

      String select       = "distinct ?author";
      String graphPattern = "?s conversion:conversion_process [ "+
                                  "conversion:author ?author; "+ // TODO: generalize this to give any attr/val https://github.com/timrdf/csv2rdf4lod-automation/issues/96
                              " ] filter(isIRI(?author))";
      
      return composeQuery(select, contexts, graphPattern);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      uriSet.add((URI)bindingSet.getValue("author"));
   }
   
   protected Set<URI> uriSet = new HashSet<URI>();
   
   @Override
   public Set<URI> get() {
      return uriSet;
   }
}