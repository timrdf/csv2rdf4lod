package edu.rpi.tw.data.csv.querylets.structural;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public class LargeValueQuerylet extends OnlyOneContextQuerylet<Boolean> {

   protected boolean large = false;
   
   /**
    * 
    * @param context
    */
   public LargeValueQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.large = false;
      
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?object";
      String graphPattern = "?enhancement                  \n"+
                            "     ov:csvCol ?col;          \n"+
                            "     a conversion:LargeValue .\n";
      // TODO: add optional { ?enhancement conversion:object_template ?template }
      String orderBy      = "";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, "1"));
      return this.composeQuery(select, context, graphPattern, orderBy, "1");
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      System.err.println(getClass().getSimpleName()+"(*) .true.");
      this.large = true;
   }

   @Override
   public Boolean get() {
      return this.large;
   }
}