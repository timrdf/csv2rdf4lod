package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * Acknowledges cell-based without conversion:object and uses the header label as the value.
 * e.g. hhs/chsi use case where each column is a statistical dimension that does not need to be renamed.
 * (before everything needed to be overridden)
 */
public class CellBasedQuerylet extends ColumnEnhancementQuerylet<Value> {

   protected boolean isCellBased;
   protected Value object;
   
   public CellBasedQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.isCellBased = false;
      this.object      = null;
      
      this.addNamespace("xsd", "ov", "conversion", "scovo", "qb");
      
      String select       = "distinct ?object"; // TODO: ?label was never selected.
      String graphPattern = "{            ?enhancement                              \n"+
                            "                         "+columnPO()+               ";\n"+
                            "                          a scovo:Item;                \n"+
                            "  optional { ?enhancement conversion:label  ?label  } .\n"+ // TODO: why was this NEVER access (even before adding OPTIONAL)?
                            "  optional { ?enhancement conversion:object ?object }  \n"+
                            " }                                                     \n"+
                            " union                                                 \n"+
                            " {           ?enhancement                              \n"+
                            "                         "+columnPO()+               ";\n"+
                            "                          a qb:Observation;            \n"+
                            "  optional { ?enhancement conversion:label  ?label  } .\n"+ // TODO: why was this NEVER access (even before adding OPTIONAL)?
                            "  optional { ?e2 "+columnPO()+"; conversion:object ?object }  \n"+
                            " }";
      // TODO: add optional { ?enhancement conversion:object_template ?template }
      String orderBy      = "";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, ""));
      return this.composeQuery(select, context, graphPattern, orderBy, "");
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      
      this.isCellBased = true;
      
      if( bindingSet.hasBinding("object") ) {
         this.object = bindingSet.getValue("object");
      }
      
      // Status reporting:
      
      //System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") ."+object + ".");
      String datatype = "";
      if( object instanceof Literal && ((Literal)object).getDatatype() != null ) {
         datatype="  "+((Literal)object).getDatatype().stringValue();
      }
      String o = object == null ? "null" : object.stringValue();
      String m = object == null ? " (will use header value to name 'up object')" : "";
      System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") ."+o + "."+datatype+m);
   }

   @Override
   public Value get() {
      return this.object;
   }
   
   /**
    * 
    * @return
    */
   public boolean isCellBased() {
      return this.isCellBased;
   }
}