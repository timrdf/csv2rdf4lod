package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

public class URISafeQuerylet extends ColumnEnhancementQuerylet<Boolean> {

   protected boolean safe;
   
   public URISafeQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.safe = false;
      this.addNamespace("xsd", "ov", "conversion");

      String select       = "?enhancement";
      String graphPattern = "?enhancement "+columnPO()+" ;\n"+
                            "     a conversion:URISafe;";
      String orderBy      = "";

      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, "1"));
      return this.composeQuery(select, context, graphPattern, orderBy, "1");
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.safe = true;
      System.err.println(getClass().getSimpleName()+"("+this.csvColumnIndex+") URISafe");
   }

   @Override
   public Boolean get() {
      return this.safe;
   }
}