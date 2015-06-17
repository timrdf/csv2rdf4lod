package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class ExistingBundleQuerylet extends ColumnEnhancementQuerylet<Set<Integer>> {

   protected Set<Integer> bundlerColumnIndexes;
   
   /**
    * 
    * @param context
    * @param csvObjectColumnIndex
    */
   public ExistingBundleQuerylet(Resource context, int csvObjectColumnIndex) {
      super(context, csvObjectColumnIndex);
   }

   /**
    * 
    */
   @Override
   public String getQueryString(Resource context) {
      this.bundlerColumnIndexes = new HashSet<Integer>();
      this.addNamespace("xsd", columnPrefix(), "conversion");
      
      String select       = "?bundlingColumnIndex";
      String graphPattern = "?col "+columnPO()+";                                            \n"+
                            "       conversion:bundled_by [ ov:csvCol ?bundlingColumnIndex ] \n";
      String orderBy      = "";
      String limit        = "";
      // TODO: query by property_name as well.
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, limit));
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.bundlerColumnIndexes.add(Integer.parseInt(bindingSet.getValue("bundlingColumnIndex").stringValue()));
      System.err.println("ExistingBundleQuerylet: "+this.csvColumnIndex+" bundled by "+this.bundlerColumnIndexes);
   }

   @Override
   public Set<Integer> get() {
      return this.bundlerColumnIndexes;
   }

   /**
    * 
    * @return
    */
   /*public int getBundlerColumnIndex() {
      return this.bundlerColumnIndex;
   }*/
}