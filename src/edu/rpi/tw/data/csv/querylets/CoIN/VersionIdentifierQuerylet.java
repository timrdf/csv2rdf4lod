package edu.rpi.tw.data.csv.querylets.CoIN;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.returning.impl.DefaultStringQuerylet;


/**
 * 
 */
public class VersionIdentifierQuerylet extends DefaultStringQuerylet {
   
   public VersionIdentifierQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd", "ov", "conversion");
      super.stringResult = null;
      
      String select       = "?versionIdentifier";
      String graphPattern = "?s \n"+
                            "     conversion:dataset_version ?versionIdentifier ."; // TODO: acknowledge conversion:version_identifier, too.
      String orderBy      = "";
      
      
      
      select       = "*";
      graphPattern = "{?s conversion:dataset_version ?old } UNION {?s conversion:version_identifier ?new }";
      orderBy      = "";

      return this.composeQuery(select, context, graphPattern, orderBy);
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      if( bindingSet.hasBinding("new") ) {
         super.stringResult = bindingSet.getValue("new").stringValue();
         System.err.println(getClass().getSimpleName() + "(*) ." + super.stringResult + ".");
      }else if( super.stringResult == null && bindingSet.hasBinding("old")) {
         super.stringResult = bindingSet.getValue("old").stringValue();
         System.err.println(getClass().getSimpleName() + "(*) ." + super.stringResult + ".");
      }else {
         System.err.println(getClass().getSimpleName() + "(*) .???.");
      }
   }
}