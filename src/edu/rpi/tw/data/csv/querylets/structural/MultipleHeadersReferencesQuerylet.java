package edu.rpi.tw.data.csv.querylets.structural;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public class MultipleHeadersReferencesQuerylet extends OnlyOneContextQuerylet<Set<Long>> {

   protected Set<Long> displacements = null;
   
   public MultipleHeadersReferencesQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {
      this.displacements = new HashSet<Long>();
      
      this.addNamespace("ov","conversion");
      
      String select       = "distinct ?o";
      String graphPattern = "?s \n"+
                            "     conversion:enhance [ ?p ?o ] . filter(isLiteral(?o)) \n"+
                            "      filter(?p != conversion:label)   \n"+
                            "      filter(?p != conversion:comment) \n"+
                            "      filter(regex(?o,\"\\\\[#H\")) \n";
      String orderBy      = "";
      String limit        = "";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, limit));
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      String oS = bindingSet.getValue("o").stringValue();
      
      Pattern pattern = Pattern.compile("#H([-+])([0-9]*)"); // TODO: why doesn't adding \\[ match it?
      Matcher matcher = pattern.matcher(oS);
      while( matcher.find() ) {
         String sign   = "-".equals(matcher.group(1)) ? "-" : "";
         String digits = matcher.group(2);
         long displacement = Long.parseLong(sign+digits);
         this.displacements.add(displacement);
      }
      
      
//      long displacement = Long.parseLong(oS.replace("[#H", "").replace("]", "").replace("+",""));
//      System.err.println(getClass().getSimpleName() + "(*) ." + oS + ". => " + displacement);
      //this.displacements.add();
   }

   @Override
   public Set<Long> get() {
      return this.displacements;
   }
}