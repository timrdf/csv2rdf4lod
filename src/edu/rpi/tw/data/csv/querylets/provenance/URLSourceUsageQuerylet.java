package edu.rpi.tw.data.csv.querylets.provenance;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;


/**
 * 
 */
public class URLSourceUsageQuerylet extends OnlyOneContextQuerylet<String> {

   protected String url           = null;
   protected String urlModDate    = null;
   protected String usageDateTime = null;
      
   /**
    * 
    * @param context
    */
   public URLSourceUsageQuerylet(Resource context) {
      super(context);
   }

   /**
    * 
    */
   @Override
   public String getQueryString(Resource context) {
      this.addNamespace("xsd","pmlp");
      
      String select       = "?sourceUsage ?url ?urlModDate ?usageDateTime";
      String graphPattern = "?url " +
                            "   pmlp:hasModificationDateTime ?urlModDate; \n" +
                            ". " +
                            "?sourceUsage " +
                            "   a pmlp:SourceUsage;                   \n" +
                            "   pmlp:hasUsageDateTime ?usageDateTime; \n" +
                            "   pmlp:hasSource        ?url;           \n";
      String orderBy      = "";
      String limit        = "";
      
      return this.composeQuery(select, context, graphPattern, orderBy, limit);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      if( this.url == null ) {
         this.url           = bindingSet.getValue("url").stringValue();
         this.urlModDate    = bindingSet.getValue("urlModDate").stringValue();
         this.usageDateTime = bindingSet.getValue("usageDateTime").stringValue();
         System.err.println(getClass().getSimpleName() + "(*) ." + this.url + ". mod."+this.urlModDate+". use."+this.usageDateTime+ " from " + bindingSet.getValue("sourceUsage").stringValue());
      }else {
         System.err.println("WARNING: multiple:       . " + bindingSet.getValue("url").stringValue() + ". mod."+
               bindingSet.getValue("urlModDate").stringValue() + ". use." + bindingSet.getValue("usageDateTime").stringValue() + " from " + bindingSet.getValue("sourceUsage").stringValue());
      }
   }
   
   public String get() {
      return url;
   }
   
   public String getURLModificationDate() {
      return this.urlModDate;
   }
   
   public String getUsageDateTime() {
      return this.usageDateTime;
   }
}