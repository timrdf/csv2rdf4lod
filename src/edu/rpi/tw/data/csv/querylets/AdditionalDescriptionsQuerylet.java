package edu.rpi.tw.data.csv.querylets;

import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * Obtain predicate-object pairs to annotate all resource objects created from all columns' cells.
 * 
 * edu.rpi.tw.data.csv.querylets.column.ResourceAnnotationsQuerylet 
 * queries for column-specific annotations.
 */
public class AdditionalDescriptionsQuerylet extends OnlyOneContextQuerylet<HashMap<String, String>> {

   // TODO: make String,Value (to handle URIs)
   protected HashMap<String, String> augmentations;
   
   public AdditionalDescriptionsQuerylet(Resource context) {
      super(context);
      this.augmentations = new HashMap<String,String>();
   }

   @Override
   public String getQueryString(Resource context) { // Is this annotating the subject or _object_?
      this.addNamespace("ov","conversion");
      
      //
      // NOTE: see edu.rpi.tw.data.csv.querylets.column.ResourceAnnotationsQuerylet for column-specific.
      //
      
      String select       = "distinct ?p ?o ";
      String graphPattern = "?s conversion:conversion_process [     \n"+
                            "      conversion:enhance ?enhancement  \n"+
                            "] .                                    \n"+
                            "?enhancement conversion:predicate ?p;  \n"+
                            "             conversion:object    ?o . \n"+
     "optional { ?enhancement a                        ?type       } filter(?type != conversion:SubjectAnnotation) \n"+
     "optional { ?enhancement ov:csvCol                ?col        } filter(!bound(?col))                          \n"+
     "optional { ?enhancement conversion:property_name ?local_name } filter(!bound(?local_name))";
      
      String orderBy      = "";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String predicate = bindingSet.getValue("p").stringValue();
      String object    = bindingSet.getValue("o").stringValue();
      System.err.println(getClass().getSimpleName() + "(*) ." + predicate + ". ." + object + ".");
      this.augmentations.put(predicate, object);
   }

   @Override
   public HashMap<String, String> get() {
      return augmentations;
   }
}