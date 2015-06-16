package edu.rpi.tw.data.rdf.sesame.query;

import java.util.Collection;

import org.openrdf.model.Resource;
import org.openrdf.query.QueryLanguage;

import edu.rpi.tw.data.rdf.sesame.query.impl.BindingHandler;


/**
 * A Querylet is a BindingHandler that also provides the query that should be processed.
 * 
 * 
 * 
 * {@link edu.rpi.tw.data.rdf.sesame.query.QueryletProcessor}
 */
public interface Querylet<T> extends BindingHandler {
   
   /**
    * @return the query string to execute against an entire repository (and not a specific named graph).
    */
   public String getQueryString();
   
   /**
    * @param context
    * @return the query string to execute against the named graph 'context'
    */
   public String getQueryString(Resource context);
   
   /**
    * 
    * @param contexts
    * @return
    */
   public String getQueryString(Collection<Resource> contexts);
   
   /**
    * @return the language for the query string returned by {@link #getQueryString()}.
    */
   public QueryLanguage getQueryLanguage();
   
   // ----- Inherited from BindingHandler -----
   //public void handleBindingSet(BindingSet bindingSet); // from BindingHandler
   //public void finish(int numResults);                  // from BindingHandler
   
   /**
    * 
    * @return
    */
   public T get();
}