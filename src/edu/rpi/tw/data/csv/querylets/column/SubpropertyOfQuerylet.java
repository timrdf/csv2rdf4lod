package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.csv.TemplateFiller;

/**
 * 
 */
public class SubpropertyOfQuerylet extends    ColumnEnhancementQuerylet<Set<Value>> {

   protected Set<Value> superProperties;
   
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public SubpropertyOfQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.superProperties = new HashSet<Value>();
      
      this.addNamespace("rdfs","xsd", columnPrefix(), "conversion");
      
      String select       = "?superProperty";
      String graphPattern = "?col "+columnPO()+";                          \n"+
                            "     conversion:subproperty_of ?superProperty . ";
      String orderBy      = "";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      Value superProperty = bindingSet.getValue("superProperty");
      System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") ." + 
                         superProperty.stringValue() + ".");
      this.superProperties.add(superProperty);
   }
   
   /**
    * @return URIs and Literals (templates) asserted in the parameters.
    */
   @Override
   public Set<Value> get() {
      return superProperties;
   }
   
   /**
    * Fill the Literals using 'templateFiller' to name all super properties.
    * 
    * @param templateFiller
    * @return
    */
   public Set<URI> getSet(TemplateFiller templateFiller) {
      Set<URI> subProperties = new HashSet<URI>();
      for( Value value : get() ) {
         if( value instanceof URI ) {
            subProperties.add((URI) value);
         }else {
            subProperties.add(vf.createURI(templateFiller.fillTemplate(value.stringValue())));
         }
      }
      return subProperties;
   }
}