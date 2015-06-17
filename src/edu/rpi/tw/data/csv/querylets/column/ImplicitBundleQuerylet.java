package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashMap;
import java.util.HashSet;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class ImplicitBundleQuerylet extends    ColumnEnhancementQuerylet<String> {

   protected String                      propertyLabel;
   protected String                      type;
   protected boolean                     anonymous;
   protected HashMap<URI,HashSet<Value>> annotations;
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public ImplicitBundleQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   /**
    * 
    */
   @Override
   public String getQueryString(Resource context) {
      this.propertyLabel = null;
      this.type          = null;
      this.anonymous     = false;
      this.annotations   = new HashMap<URI,HashSet<Value>>();
      
      this.addNamespace("xsd", columnPrefix(), "conversion");
      
      String select       = "?propLabel ?type ?p ?o ?anonymous";
      String graphPattern = "?col "+columnPO()+";                                                       \n"+
                            "       conversion:bundled_by ?bundle .                                     \n"+
                            "                             ?bundle conversion:property_name ?propLabel . \n"+
                            "                  optional { ?bundle conversion:type_name     ?type }    . \n"+
                            "                  optional { ?bundle a ?anonymous . filter(?anonymous = conversion:Anonymous) }    . \n"+
                            "  optional { ?bundle ?p ?o .                                               \n"+
                            "    filter(!regex(str(?p),'^http://purl.org/twc/vocab/conversion/.*'))     \n"+
                            "    filter(!regex(str(?o),'^http://purl.org/twc/vocab/conversion/.*')) }   \n";
      // NOTE: see ImplicitBundledNameTemplateQuerylet to name the implicit bundle resource.
      
      String orderBy      = "";
      //String limit        = "1";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy));
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.propertyLabel = bindingSet.getValue("propLabel").stringValue();
      if( bindingSet.hasBinding("type") ) {
         this.type = bindingSet.getValue("type").stringValue();
      }
      this.anonymous = bindingSet.hasBinding("anonymous");
      if( bindingSet.hasBinding("p") && bindingSet.hasBinding("p") ) {
         URI   p = (URI) bindingSet.getValue("p");
         Value o =       bindingSet.getValue("o");
         if( !this.annotations.containsKey(p) ) {
            this.annotations.put(p, new HashSet<Value>());
         }
         this.annotations.get(p).add(o);
         System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") annotation " + p.stringValue() + " " + o.stringValue() + ".");
      }
   }
   
   public void finish(int numResults) {
      if (  this.propertyLabel != null || this.type != null ) {
         System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") p " + this.propertyLabel + " type " + this.type + ".");
      }
   }

   public String get() {
      return this.propertyLabel;
   }

   public String getType() {
      return this.type;
   }
   
   public boolean isAnonymous() {
   	return this.anonymous;
   }
   
   public HashMap<URI,HashSet<Value>> getAnnotations() {
      return this.annotations;
   }
}