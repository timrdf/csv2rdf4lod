package edu.rpi.tw.data.csv.querylets;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;

/**
 * 
 */
public class PrefixMappingsQuerylet extends    OnlyOneContextQuerylet<Set<Namespace>> {

   private static Logger logger = Logger.getLogger(PrefixMappingsQuerylet.class.getName());
   
   protected Set<Namespace> namespaces;
   
   /**
    * 
    * @param context
    */
   public PrefixMappingsQuerylet(Resource context) {
      super(context);
   }

   @Override
   public String getQueryString(Resource context) {

      this.namespaces = new HashSet<Namespace>();
      
      this.addNamespace("rdf", "xsd", "ov", "conversion", "vann");
      
      String select       = "distinct ?prefix ?namespace";
      // NOTE: generalized for PrefixMappings to use outside of conversion: use.
      String graphPattern = //"?ds conversion:conversion_process [              \n"+
                            //"    conversion:interpret [                       \n"+
                            "    ?pair   vann:preferredNamespacePrefix ?prefix;    \n"+
                            "       vann:preferredNamespaceUri    ?namespace  \n";
                            //"    ];                                           \n";
                            //"] .";
      String orderBy      = "";

      return this.composeQuery(select, context, graphPattern, orderBy);
   }
   
   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      
      String prefix    = bindingSet.getValue("prefix").stringValue();
      String namespace = bindingSet.getValue("namespace").stringValue();
      logger.finest(getClass().getSimpleName()+"(*) ." + prefix+"."+" ."+namespace+".");
      
      this.namespaces.add(new NamespaceImpl(prefix, namespace));
   }

   @Override
   public Set<Namespace> get() {
      return this.namespaces;
   }
}