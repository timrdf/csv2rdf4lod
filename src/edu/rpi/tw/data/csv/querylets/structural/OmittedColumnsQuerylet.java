package edu.rpi.tw.data.csv.querylets.structural;

import java.util.HashSet;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultIntegerSetQuerylet;

/**
 * 
 */
public class OmittedColumnsQuerylet extends DefaultIntegerSetQuerylet {

   public OmittedColumnsQuerylet(Resource context) {
      super(context);
   }
 
   @Override
   public String getQueryString(Resource context) {
      super.integerSet = new HashSet<Integer>();
      
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "?index";
      String graphPattern = "?s a conversion:Omitted ; ov:csvCol ?index ";
      String orderBy      = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      int omittedColumnIndex = Integer.parseInt(bindingSet.getValue("index").stringValue());
      super.integerSet.add(omittedColumnIndex);
      System.err.println(getClass().getSimpleName() +"("+ omittedColumnIndex +")");
   }
}