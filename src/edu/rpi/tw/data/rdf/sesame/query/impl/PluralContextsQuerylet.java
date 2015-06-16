package edu.rpi.tw.data.rdf.sesame.query.impl;

import java.util.Collection;

import org.openrdf.model.Resource;

import edu.rpi.tw.data.utils.SetUtilities;
import edu.rpi.tw.string.pmm.PrefixMappings;

/**
 * As opposed to the backward-compatible OnlyOneContextQuerylet.
 * 
 * @author AFRL Knowledge-Based Visualization Team
 * @param <T>
 */
public abstract class PluralContextsQuerylet<T> extends DefaultQuerylet<T> {

   public PluralContextsQuerylet() {
      super((Resource) null);
   }
   
   public PluralContextsQuerylet(Resource context) {
      super(context);
   }
   
   public PluralContextsQuerylet(Collection<Resource> contexts) {
      super(contexts);
   }
   
   public PluralContextsQuerylet(Resource context, PrefixMappings prefixMappings) {
      super(context, prefixMappings);
   }
   
   @Override
   public final String getQueryString(Resource context) {
      return getQueryString(SetUtilities.setOf(context));
   }
}