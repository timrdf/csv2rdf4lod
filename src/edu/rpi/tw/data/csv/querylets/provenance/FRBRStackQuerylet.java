package edu.rpi.tw.data.csv.querylets.provenance;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public class FRBRStackQuerylet extends    OnlyOneContextQuerylet<Integer> {
   
	private int i = 0;
   
   public FRBRStackQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      
      this.addNamespace("xsd","frbrcore","dcterms");
      
      String select       = "distinct ?item ?manifestation ?expression ?work ?recentdate";
      String graphPattern = 
                            "?item a frbrcore:Item;                     \n"+
                            "      dcterms:date        ?recentdate;     \n"+
                            "      frbrcore:exemplarOf ?manifestation . \n"+
                            "?manifestation a frbrcore:Manifestation;   \n"+
                            "      frbrcore:embodimentOf ?expression .  \n"+
                            "?expression a frbrcore:Expression;         \n"+
                            "      frbrcore:realizationOf ?work .       \n"+
                            "?work a frbrcore:Work .                    \n"+
                            "optional { ?item dcterms:date ?otherdate . filter(?otherdate < ?mindate) }\n"+
                            "filter ( !bound(?otherdate) )";

      String orderBy      = "?recentdate";
      String limit        = "";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, limit));
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      i++;
      //System.err.println(i + " " + bindingSet.getBinding("recentdate") + " " + bindingSet.getBinding("recentdate").getValue());
   }

	@Override
   public Integer get() {
	   return i;
   }
}