package edu.rpi.tw.data.csv.querylets;

import java.util.HashMap;
import java.util.HashSet;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * Get the attribute-values of the dataset in the enhancements to assert in the conversion output.
 */
public class LayerDatasetDescriptionsQuerylet extends    OnlyOneContextQuerylet<HashMap<Value, HashSet<Value>>> { 
   
   protected HashMap<Value,HashSet<Value>> augmentations;
   
   public LayerDatasetDescriptionsQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.augmentations = new HashMap<Value,HashSet<Value>>();
      this.addNamespace("conversion");
      
      String select       = "distinct ?p ?o ";
      String graphPattern = "?s conversion:conversion_process []; ?p ?o . "+
                            "filter(?p != conversion:base_uri) "+
                            "filter(?p != conversion:source_identifier) "+  // TODO: removing these could simply asserting them manually later.
                            "filter(?p != conversion:dataset_identifier) "+
                            "filter(?p != conversion:dataset_version) "+
                            "filter(?p != conversion:version_identifier) "+
                            "filter(?p != conversion:conversion_process)";
      String orderBy      = "?p";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      Value predicate = bindingSet.getValue("p");
      Value object    = bindingSet.getValue("o");
      System.err.println(getClass().getSimpleName() + "(D) ." + predicate + ". ." + object + ".");
      if( !this.augmentations.containsKey(predicate) ) {
         this.augmentations.put(predicate, new HashSet<Value>());
      }
      this.augmentations.get(predicate).add(object);
   }

   @Override
   public HashMap<Value, HashSet<Value>> get() {
      return this.augmentations;
   }
}