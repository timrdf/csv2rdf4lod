package edu.rpi.tw.data.rdf.sesame.query.impl;

import java.util.Collection;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

import edu.rpi.tw.string.pmm.PrefixMappings;

/**
 * A FocusedQuerylet is a DefaltQuerylet that has a single focus Resource 
 * that is a parameter to the query.
 */
public abstract class FocusedQuerylet<T> extends PluralContextsQuerylet<T> {

   protected Resource focus;
   
   /**
    * 'focus' can be accessed from getQueryString(Resource) methods in extensions to this class.
    * 
    * @param context
    * @param focus
    */
   public FocusedQuerylet(Resource context, Resource focus) {
      super(context);
      this.focus = focus;
   }
   
   /**
    * 
    * @param contexts
    * @param focus
    */
   public FocusedQuerylet(Collection<Resource> contexts, Resource focus) {
      super(contexts);
      this.focus = focus;
   }
   
   /**
    * 
    * @param context
    */
   public FocusedQuerylet(Resource context) {
   	super(context);
   }
   
   /**
    * 
    * @param context
    * @param focus
    * @param pmap
    */
   public FocusedQuerylet(Resource context, Resource focus, PrefixMappings prefixMappings ) {
      super(context, prefixMappings);
      this.focus = focus;
   }
   
   /**
    * 
    * @return
    */
   public String getFocusRef() {
      return "<"+this.focus+">";
   }
   
   /**
    * 
    * @param context
    * @param focus
    * @return
    */
   //public String getQueryString(Resource context, Resource focus);
   
   //public FocusedQuerylet set(Resource context);
   
   /**
    * 
    * @param context
    * @param focus
    */
   public FocusedQuerylet<T> set(Resource context, Resource focus) {
      return new FocusedQuerylet<T>(context, focus, this.pmap) {
         @Override
         public String getQueryString(Collection<Resource> contexts) {
            return FocusedQuerylet.this.getQueryString(contexts);
         }
         @Override
         public void handleBindingSet(BindingSet bindingSet) {
            FocusedQuerylet.this.handleBindingSet(bindingSet);
         }
         @Override
         public T get() {
            return FocusedQuerylet.this.get();
         }
      };
   }
}