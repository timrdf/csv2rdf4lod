package edu.rpi.tw.data.rdf.sesame.query.impl;

import java.util.Collection;

import org.openrdf.model.Resource;

import edu.rpi.tw.data.utils.SetUtilities;
import edu.rpi.tw.string.pmm.PrefixMappings;

/**
 * This exists to span the gap between old-school one context query creation and the newer approach
 * to query across a union of contexts.
 * 
 * @param <T>
 */
public abstract class OnlyOneContextQuerylet<T> extends DefaultQuerylet<T> {

   public OnlyOneContextQuerylet() {
      this(null);
   }
   
   public OnlyOneContextQuerylet(Resource context) {
      super(context);
   }
   
   public OnlyOneContextQuerylet(Resource context, PrefixMappings prefixMappings) {
      super(context, prefixMappings);
   }

   /**
    * This method reflects the original assumptions of the 
    * Querylet.getQueryString(Resource context) implementation.
    * 
    * Old Querylets that extended DefaultQuerylet now extend this class.
    */
   @Override
   public final String getQueryString(Collection<Resource> contexts) {
      if( null != contexts && contexts.size() > 1 ) {
         System.err.println(getClass().getName() + " SQUASHING "+contexts.size() + " contexts (as an OnlyOneContextQuerylet)");
      }
      return getQueryString(SetUtilities.theOne(contexts));
   }
}