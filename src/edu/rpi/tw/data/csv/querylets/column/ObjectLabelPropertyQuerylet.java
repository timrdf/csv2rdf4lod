package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class ObjectLabelPropertyQuerylet extends    ColumnEnhancementQuerylet<Set<URI>> {
	
	private HashSet<URI> labelProperties = null;

	public ObjectLabelPropertyQuerylet(Resource context, int csvColumnIndex) {
	   super(context, csvColumnIndex);
   }

	@Override
   public String getQueryString(Resource context) {
		this.labelProperties = new HashSet<URI>();
		
      this.addNamespace("xsd", columnPrefix(), "conversion");
      
      String select       = "?property";
      String graphPattern = "?col "+columnPO()+";\n"+
                            "     conversion:object_label_property ?property .\n"+
                            "filter isURI(?property)";
      String orderBy      = "";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

	@Override
   public void handleBindingSet(BindingSet bindingSet) {
      Value property = bindingSet.getValue("property");
      System.err.println(REPORT_INDENT + getClass().getSimpleName() +"("+ this.csvColumnIndex+") ."+ 
                         property.stringValue() + ".");
      this.labelProperties.add((URI)property);
   }

	@Override
   public Set<URI> get() {
	   return this.labelProperties;
   }
}