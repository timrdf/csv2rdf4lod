package edu.rpi.tw.data.csv.querylets;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

public class ExcludeVoIDInDatasetQuerylet extends OnlyOneContextQuerylet<Boolean> {
   protected boolean exclude = false;
   

   public ExcludeVoIDInDatasetQuerylet(Resource context) {
      super(context);
   }
  

   @Override
   public String getQueryString(Resource context) {
      exclude = false;
      
      addNamespace("conversion","void");
      
      String select       = "distinct ?e";
      String graphPattern = "[] conversion:conversion_process [ conversion:enhance ?e ] . \n"+
                            "?e void:inDataset conversion:null .\n";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      exclude = true;
      System.err.println(getClass().getSimpleName()+"(*) ." + exclude+".");
   }
   
   @Override
   public Boolean get() {
      return exclude;
   }
}
