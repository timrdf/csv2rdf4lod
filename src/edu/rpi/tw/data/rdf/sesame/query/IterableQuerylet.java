package edu.rpi.tw.data.rdf.sesame.query;


/**
 * An IterableQuerylet is a Querylet that provides a series of queries.
 * 
 * {@link edu.rpi.tw.data.rdf.sesame.query.QueryletProcessor}
 */
public interface IterableQuerylet<T> extends Querylet<T> {
   
   /**
    * 
    * @return
    */
   public boolean hasNext();
   
   /**
    * 
    */
   public void advance();
   
   /**
    * This might be deprecated.
    * @return
    */
   public String getType();

   // public String getQueryString();
   // public QueryLanguage getQueryLanguage();
   // public void handleBindingSet(BindingSet bindingSet);  *
   // public void finish(int numResults);
}