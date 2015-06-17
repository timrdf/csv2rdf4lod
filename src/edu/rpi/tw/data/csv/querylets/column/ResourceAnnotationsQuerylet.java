package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * Obtain predicate-object pairs to annotate resource objects.
 * Handles annotations on subjects AND objects
 * (since, subjects of row-based have an implied 0 column).
 * 
 * edu.rpi.tw.data.csv.querylets.AdditionalDescriptionsQuerylet 
 * queries for annotations to apply to all objects from cells (i.e. column-independent annotations).
 */
public class ResourceAnnotationsQuerylet extends    ColumnEnhancementQuerylet<HashMap<Value,Set<Value>>> {
   
   protected HashMap<Value,Set<Value>> augmentations;
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public ResourceAnnotationsQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   /**
    * 
    */
   @Override
   public String getQueryString(Resource context) {
      this.augmentations = new HashMap<Value,Set<Value>>();
      this.addNamespace("xsd","ov","conversion");
      
      // Note:
      //    see edu.rpi.tw.data.csv.querylets.AdditionalDescriptionsQuerylet for column-independent.
      //
      
      String select       = "distinct ?enhancement ?p ?o ";
      
      String rowOrCell    = super.csvColumnIndex == 0 ? "a conversion:SubjectAnnotation" : columnPO();
      String filter       = super.csvColumnIndex == 0 ? " optional { ?enhancement ov:csvCol ?col } filter(?col = 0)" : "";
      // This filter was added for https://github.com/timrdf/csv2rdf4lod-automation/issues/337 on Jun 20, 2013
      
      String graphPattern = "?enhancement "+rowOrCell+";              \n"+
                            "               conversion:predicate ?p ; \n"+
                            "               conversion:object    ?o . \n"+
                            filter;
      String orderBy      = "";
      String limit        = "";
      
      // TODO: consider if this should just "fall out" of a cell-based conversion.
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, limit));
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   /**
    * 
    */
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      Value predicate = bindingSet.getValue("p");
      Value object    = bindingSet.getValue("o");
      System.err.println(getClass().getSimpleName() + 
                        "("+csvColumnIndex+") ."+ predicate.stringValue() + ". "+ "."+ object.stringValue() + ".");
      if( !this.augmentations.containsKey(predicate) ) {
         this.augmentations.put(predicate, new HashSet<Value>());
      }
      this.augmentations.get(predicate).add(object);
   }

   /**
    * 
    */
   @Override
   public HashMap<Value,Set<Value>> get() {
      return augmentations;
   }
}