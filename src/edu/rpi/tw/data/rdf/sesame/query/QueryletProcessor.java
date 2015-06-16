package edu.rpi.tw.data.rdf.sesame.query;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import edu.rpi.tw.data.rdf.sesame.query.impl.BindingHandler;
import edu.rpi.tw.data.rdf.sesame.query.impl.OnlyOneContextQuerylet;
import edu.rpi.tw.data.rdf.sesame.query.impl.PluralContextsQuerylet;
import edu.rpi.tw.data.rdf.sesame.vocabulary.OWLTime;
import edu.rpi.tw.data.rdf.sesame.vocabulary.PML2;
import edu.rpi.tw.data.rdf.sesame.vocabulary.TetherlessWorld;
import edu.rpi.tw.data.utils.SetUtilities;
import edu.rpi.tw.string.NameFactory;
import edu.rpi.tw.string.NameFactory.NameType;

/**
 * Execute queries against a Repository according to given Querylets.
 */
public class QueryletProcessor {

	private static Logger logger = Logger.getLogger(QueryletProcessor.class.getName());
	
	private static boolean errored = false;
	
   /**
    * Process queries from 'querylet' until !querylet.hasNext()
    * 
    * TODO: This should go away once processQuery(Repository,Querylet) 
    * handles IterableQuerylets correctly.
    * 
    * @deprecated use processQuery(repository, Querylet)
    * 
    * @param repository - the repository against which to query.
    * @param querylet - the querylet from which to get queries to execute.
    * 
    * @return the number of results from all queries executed.
    */
   public static int processQueries(Repository repository, IterableQuerylet handler) {
      int numResults = 0;

      errored = false;
      while( handler.hasNext() && !errored) {
         handler.advance();
         //System.err.println("===\n"+handler.getType() + ":\n\n");
         // caused an infinite loop:
         //    numResults += QueryletProcessor.processQuery(repository, handler);
         // skips past the processQuery(rep,handler) and calls what it would.
         numResults += processQuery(repository, handler.getQueryString(), handler.getQueryLanguage(), handler);
      }
      return numResults;
   }
   
   /**
    * Process query(ies) from 'querylet'.
    * If querylet is iterable, process until !querylet.hasNext().
    * If querylet is a plain old querylet, do the one query.
    * 
    * TODO: This method should handle instances of Querylet AND IterableQuerylet and loop if IterableQuerylet.
    * Current hack fix now is a differently-named method processQueries(Repository,IterableQuerylet).
    * 
    * @param repository - the repository against which to query.
    * @param querylet - the querylet from which to get queries to execute.
    * 
    * @return the number of results from all queries executed.
    */
   public static int processQuery(Repository repository, Querylet querylet) {
   	
      if( querylet instanceof IterableQuerylet ) {
      	
      	IterableQuerylet iQ = (IterableQuerylet) querylet;
         int numResults = 0;
         errored = false;
         while( iQ.hasNext() && !errored ) {
         	iQ.advance();
            //System.err.println("===\n"+handler.getType() + ":\n\n");
            // caused an infinite loop:
            //    numResults += QueryletProcessor.processQuery(repository, handler);
            // skips past the processQuery(rep,handler) and calls what it would.
            numResults += processQuery(repository, querylet.getQueryString(), querylet.getQueryLanguage(), querylet);
         }
         return numResults;
      }else {
         logger.finer("querylet.getQueryString() :\n"+ querylet.getQueryString());
      	return processQuery(repository, querylet.getQueryString(), querylet.getQueryLanguage(), querylet);
      }
   }

   /** The URI for the named graph that QueryletProcessor logs all queries. */
   public static Resource queryContext = ValueFactoryImpl.getInstance().createURI(TetherlessWorld.R + "QueryProcessor_query_log");
   /** A predicate used in the QueryProcessor logs. */
   public static URI      numResultsR  = ValueFactoryImpl.getInstance().createURI(TetherlessWorld.P + "num_results");
   
   /**
    * 
    * @param repository
    * @param queryString - a SPARQL query string.
    * @param handler
    * @return
    */
   public static int processQuery(Repository repository, String queryString, BindingHandler handler) {
      return processQuery(repository, queryString, QueryLanguage.SPARQL, handler);
   }
   
   /**
    * 
    * @param repository
    * @param queryString
    * @param queryLang
    * @param handler
    * 
    * @return the number of results from all queries executed.
    */
   public static int processQuery(Repository repository, String queryString, 
   										 QueryLanguage queryLang, BindingHandler handler) {

      logger.info(queryString);
      RepositoryConnection conn = null;
      
      int  numResults = 0;
      long timeStarted = System.currentTimeMillis();                                                           // TIMING
      try {
         conn = repository.getConnection();

         TupleQuery       tupleQuery  = conn.prepareTupleQuery(queryLang, queryString);
         TupleQueryResult result      = tupleQuery.evaluate();
         long durationToEvaluateQuery = System.currentTimeMillis() - timeStarted;                              // TIMING
         
         try {
            while (result.hasNext()) {
               handler.handleBindingSet(result.next());
               numResults++;
            }
            handler.finish(numResults);

            // Log query string, start time, execution duration, and number of results into repository.
            boolean logQueryInRepository = false;
            if( logQueryInRepository ) {
               SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
               Date start = new Date(timeStarted);

               Resource queryName = NameFactory.getResource(TetherlessWorld.R, "Query", NameType.UUID);

               ValueFactory vf = ValueFactoryImpl.getInstance();
               conn.add(queryName, PML2.HAS_RAW_STRING,           vf.createLiteral(queryString),      queryContext);
               conn.add(queryName, PML2.HAS_LANGUAGE,             vf.createURI("http://www.w3.org/TR/rdf-sparql-query/"), queryContext);
               Resource beginning = vf.createBNode();
               conn.add(queryName, OWLTime.HAS_BEGINNING,        beginning,                          queryContext);
               conn.add(beginning, RDF.TYPE,                     OWLTime.INSTANT,                    queryContext);
               conn.add(beginning, OWLTime.IN_XSD_DATETIME,      vf.createLiteral(df.format(start)), queryContext);
               Resource duration  = vf.createBNode();
               conn.add(queryName, OWLTime.HAS_DURATION_DESCRIPTION, duration,                       queryContext);
               conn.add(duration,  RDF.TYPE,                     OWLTime.DURATION_DESCRIPTION,       queryContext);
               conn.add(duration,  OWLTime.SECONDS,              vf.createLiteral(durationToEvaluateQuery/1000.0), queryContext);
               conn.add(queryName, numResultsR,                  vf.createLiteral(numResults),       queryContext);
               conn.commit();
            }
            // System.err.println(queryName                + "\n"+
            //                    timeStarted              + " ms started. "+df.format(start)+"\n"+
            //                    durationToEvaluateQuery  + " ms to evaluate query.\n"+
            //                    numResults               + " results");
         } finally {
            result.close();
         }// TODO: are we handling exceptions from Querylets and printing all stack traces? getValue("novardef");
      } catch (OpenRDFException e) {
         //System.err.println(handler);
         System.err.println(queryString);
         e.printStackTrace();
         errored = true; // For use when we are processing iterable querylets.
      } finally {
         if( conn != null ) {
            try {
               conn.close();
            } catch (RepositoryException e) {
               e.printStackTrace();
            }
         }
      }
      //long timeFinished = System.currentTimeMillis();
      //logger.finest("                                        ");
      //logger.finest("("+numResults + " results in " + (timeFinished - timeStarted) + " ms.)");
      return numResults;
   }
   
   /**
    * This only handles SELECT queries.
    * 
    * 6 arguments (the one isn't a set).
    * 
    * @return {@link #composeQuery(String, String, Resource, String, String, String)} with no group by.
    */
   public static String composeQuery(String prefixes,
   											 String select,  Resource context, String graphPattern,
                                     String orderBy, String limitTo) {
   	return QueryletProcessor.composeQuery(prefixes, select, context, graphPattern, "", orderBy, limitTo);
   }

   /**
    * 
    * 6 arguments (one is a set)
    * 
    * @param prefixes
    * @param select
    * @param context
    * @param graphPattern
    * @param orderBy
    * @param limitTo
    * @return
    */
   public static String composeQuery(String prefixes,
                                     String select, Collection<Resource> contexts, String graphPattern,
                                     String orderBy, String limitTo) {
      return QueryletProcessor.composeQuery(prefixes, select, contexts, graphPattern, "", orderBy, limitTo);
   }

   /**
    * This only handles SELECT queries.
    * 
    * 5 arguments (context NOT a set).
    * 
    * @return {@link #composeQuery(String, String, Resource, String, String, String)} with no limit.
    */
   public static String composeQuery(String prefixes,
                                     String select, Resource context, String graphPattern,
                                     String orderBy) { // omits 'limitTo'
      return QueryletProcessor.composeQuery(prefixes, select, SetUtilities.setOf(context), graphPattern, "", orderBy, "");
   }
   
   /**
    * This only handles SELECT queries.
    * 
    * 5 arguments (contexts as a set).
    * 
    * @return {@link #composeQuery(String, String, Resource, String, String, String)} with no limit.
    */
   public static String composeQuery(String prefixes,
                                     String select,
                                     Collection<Resource> contexts, String graphPattern,
                                     String orderBy) { // omits 'limitTo'
      return QueryletProcessor.composeQuery(prefixes, select, contexts, graphPattern, "", orderBy, "");
   }
   
   /**
    * 
    * @param prefixes
    * @param select
    * @param context
    * @param graphPattern
    * @param groupBy
    * @param orderBy
    * @param limitTo
    * @return
    */
   public static String composeQuery(String prefixes,
                                     String select,  Resource context, String graphPattern,
                                     String groupBy, String   orderBy, String limitTo) {
      return QueryletProcessor.composeQuery(prefixes, select, SetUtilities.setOf(context), graphPattern, "", orderBy, limitTo);
   }
   
   /**
    * This only handles SELECT queries.
    * 
    * 7 arguments.
    * 
    * @param prefixes     - prefixes of a SPARQL query string.
    * @param select       - select of a SPARQL query string.
    * @param context      - context/named graph of a SPARQL query string.
    * @param graphPattern - where of a SPARQL query string.
    * @param groupBy - 
    * @param orderBy      - order by of a SPARQL query string.
    * @param limit        - limit of a SPARQL query string.
    * 
    * @return A concatenation of all arguments with a few tidbits of syntax logic.
    */
   public static String composeQuery(String prefixes,
                                     String select,  Collection<Resource> contexts, String graphPattern,
                                     String groupBy, String   orderBy, String limitTo) {
      
      // This code was commented out to generalize this method to handle ANY type of SPARQL
      // (ask, construct, describe, select). Dec 2014
      
//      String _prefixes = prefixes == null || prefixes.length() <= 0 ? ""  : prefixes;
      String _select   =   select == null || select.length()   <= 0 ? "*" : select;
//      String graph     =  context == null ? "" : context.stringValue();
//      String order     = (orderBy == null || orderBy.length()  <= 0) ? "" : "\norder by " + orderBy;
//      String group     = (groupBy == null || groupBy.length()  <= 0) ? "" : "\ngroup by " + groupBy;
//      String limit     = (limitTo == null || limitTo.length()  <= 0) ? "" : "\nlimit "    + limitTo;
      
//      return context == null ? _prefixes + "\nselect " + _select + "\nwhere {\n " +
//                                                                             graphPattern + "\n"+
//                                                                         "} "  + group + order + limit
//                             : _prefixes + "\nselect " + _select + "\nwhere {\n  "+
//                                                                            "graph <"+graph+"> { \n    " + 
//                                                                               graphPattern.replace("\n","\n    ")+"\n  "+
//                                                                            "}\n"+
//                                                                          "}" + group + order + limit;
      return composeAnyQuery(prefixes, "select "+_select, contexts, graphPattern, groupBy, orderBy, limitTo);
   }
   
   /**
    * {@link #composeAnyQuery(String, String, Resource, String, String, String, String)} with null groupBy and null limitTo.
    * 
    * 5 arguments (one is NOT a set).
    * 
    * @param prefixes
    * @param queryTypeOperator
    * @param context
    * @param graphPattern
    * @param orderBy
    * @return
    */
   public static String composeAnyQuery(String prefixes,
                                        String queryTypeOperator,
                                        Resource context, String graphPattern,
                                        //String groupBy, 
                                        String orderBy
                                        //String limitTo
                                        ) {
      return composeAnyQuery(prefixes, queryTypeOperator, SetUtilities.setOf(context), graphPattern, null, orderBy, null);
   }
   
   /**
    * 
    * 5 arguments (one is a set).
    * 
    * @param prefixes
    * @param queryTypeOperator
    * @param contexts
    * @param graphPattern
    * @param orderBy
    * @return
    */
   public static String composeAnyQuery(String prefixes,
                                        String queryTypeOperator,
                                        Collection<Resource> contexts, String graphPattern,
                                        // String groupBy,
                                        String orderBy
                                        // String limitTo
                                        ) {
      return composeAnyQuery(prefixes, queryTypeOperator, contexts, graphPattern, null, orderBy, null);
   }
   
   /**
    * This handles ASK, DESCRIBE, SELECT, and CONSTRUCT queries,
    * and requires a bit more detail in the 'queryTypeOperator' parameter.
    * 'queryTypeOperator' is passed through RAW without any checks or decorations.
    * 
    * This is a more raw version of
    * {@link #composeQuery(String, String, Resource, String, String, String, String)}, 
    * which assumes a SELECT.
    * In that method, 'queryTypeOperator' is assumed to be some value after an implied "select", 
    * and the literal 'select ' is filled in in addition to the argument 'select'.
    * 
    * 7 arguments.
    * 
    * @param prefixes          - prefixes of a SPARQL query string.
    * @param queryTypeOperator - select, ask, construct, etc. Must include the keyword
    * @param context           - context/named graph of a SPARQL query string.
    * @param graphPattern      - where of a SPARQL query string.
    * @param groupBy - 
    * @param orderBy           - order by of a SPARQL query string.
    * @param limit             - limit of a SPARQL query string.
    * 
    * @return A concatenation of all arguments with a few tidbits of syntax logic.
    */
   public static String composeAnyQuery(String prefixes,
                                        String queryTypeOperator, Resource context, String graphPattern,
                                        String groupBy, String orderBy, String limitTo) {
      return composeAnyQuery(prefixes,
                             queryTypeOperator, SetUtilities.setOf(context), graphPattern, 
                             groupBy, orderBy, limitTo);
   }
   
   /**
    * This handles ASK, DESCRIBE, SELECT, and CONSTRUCT queries,
    * and requires a bit more detail in the 'queryTypeOperator' parameter.
    * 'queryTypeOperator' is passed through RAW without any checks or decorations.
    * 
    * This is a more raw version of
    * {@link #composeQuery(String, String, Resource, String, String, String, String)}, 
    * which assumes a SELECT.
    * In that method, 'queryTypeOperator' is assumed to be some value after an implied "select", 
    * and the literal 'select ' is filled in in addition to the argument 'select'.
    * 
    * 7 arguments.
    * 
    * 
    * - - - - - - - - - - - - - - - - context = null - - - - - - - - - - - - 
    * @prefix eg: <http://example.org/vocab/>
    * select distinct *
    * where {
    *   ?s ?p ?o
    * } 
    * group by ?o
    * order by ?s
    * limit 10
    * 
    * 
    * - - - - - - - - - - - - - - - context contains null - - - -  - - - - - 
    * @prefix eg: <http://example.org/vocab/>
    * select distinct *
    * where {
    *   graph ?g { 
    *     ?s ?p ?o
    *   }
    * }
    * group by ?o
    * order by ?s
    * limit 10
    * 
    * 
    * - - - - - - - - - - - - -context(g1) - - - - - - - - - - - - - - - - - - 
    * @prefix eg: <http://example.org/vocab/>
    * select distinct *
    * where {
    *   graph <http://example.org/g1> { 
    *     ?s ?p ?o
    *   }
    * }
    * group by ?o
    * order by ?s
    * limit 10
    * 
    * 
    * - - - - - - - - - - - -context(g1, g2) - - - - - - - - - - - - - - - - - 
    * @prefix eg: <http://example.org/vocab/>
    * select distinct *
    * where {
    *   values ?g {
    *     <http://example.org/g2>
    *     <http://example.org/g1>
    *   }
    *   graph ?g { 
    *     ?s ?p ?o
    *   }
    * }
    * group by ?o
    * order by ?s
    * limit 10
    *
    * @param prefixes          - prefixes of a SPARQL query string.
    * @param queryTypeOperator - select, ask, construct, etc. Must include the keyword
    * @param contexts          - context(s)/named graph(s) of a SPARQL query string (graph union).
    *                            A null set has no GRAPH keyword; a set of null has GRAPH ?g.
    * @param graphPattern      - where of a SPARQL query string.
    * @param groupBy - 
    * @param orderBy           - order by of a SPARQL query string.
    * @param limit             - limit of a SPARQL query string.
    * 
    * @return A concatenation of all arguments with a few tidbits of syntax logic.
    */
   public static String composeAnyQuerySlicing(String prefixes,
                                        String queryTypeOperator,
                                        Collection<Resource> contexts, String graphPattern,
                                        String groupBy, String orderBy, String limitTo) {
      
      String _prefixes = prefixes == null || prefixes.length() <= 0 ? "" : prefixes;

      //String graph     =  contexts == null ? "" : "<" + contexts.stringValue() +">";
      String graph =  SetUtilities.justOne(contexts) &&
                      SetUtilities.theOne(contexts) != null
                                            ? "<" + SetUtilities.theOne(contexts).stringValue() +">" 
                                            : "?g";
      StringBuffer graphs = new StringBuffer();
      if( null != contexts && contexts.size() > 1 ) {
         graphs.append("  values ?g {\n");
         String or = "    ";
         for( Resource context : contexts ) {
            if( null != context ) {
               graphs.append(or + "<" + context.stringValue() + ">");
               or = "\n    ";
            }
         }
         if( graphs.length() > 0 ) {
            graphs.append("\n  }\n");
         }
      }
      
      String order = (orderBy == null || orderBy.length()  <= 0) ? "" : "\norder by " + orderBy;
      String group = (groupBy == null || groupBy.length()  <= 0) ? "" : "\ngroup by " + groupBy;
      String limit = (limitTo == null || limitTo.length()  <= 0) ? "" : "\nlimit "    + limitTo;
      
      // 'queryTypeOperator' includes the keyword 'ask', 'construct', 'describe', or 'select'
      // PLUS whatever it wants to construct, describe, or select.
      return contexts == null ? _prefixes + "\n" + 
                               queryTypeOperator + "\n"+
                              "where {\n" +
                              "  " + graphPattern + "\n"+
                              "} " + group + order + limit
                               
                              : _prefixes + "\n" + 
                               queryTypeOperator + "\n"+
                              "where {\n"+
                                 graphs + 
                              "  graph "+ graph +" { \n" + 
                              "    " + graphPattern.replace("\n","\n    ")+"\n"+
                              "  }\n"+
                              "}" + group + order + limit;
   }
   
   /**
    * This handles ASK, DESCRIBE, SELECT, and CONSTRUCT queries,
    * and requires a bit more detail in the 'queryTypeOperator' parameter.
    * 'queryTypeOperator' is passed through RAW without any checks or decorations.
    * 
    * This is a more raw version of
    * {@link #composeQuery(String, String, Resource, String, String, String, String)}, 
    * which assumes a SELECT.
    * In that method, 'queryTypeOperator' is assumed to be some value after an implied "select", 
    * and the literal 'select ' is filled in in addition to the argument 'select'.
    * 
    * 7 arguments.
    * 
    * Historical note: this was modified from {@link #composeAnyQuerySlicing(String, String, Collection, String, String, String, String)},
    * which did not UNION the graph names that we wanted to combine to query across.
    * We changed VALUES ?G {} to: FROM ?G
    * 
    * @param prefixes          - prefixes of a SPARQL query string.
    * @param queryTypeOperator - select, ask, construct, etc. Must include the keyword
    * @param contexts          - context(s)/named graph(s) of a SPARQL query string (graph union).
    *                            A null set has no GRAPH keyword; a set of null has GRAPH ?g.
    * @param graphPattern      - where of a SPARQL query string.
    * @param groupBy - 
    * @param orderBy           - order by of a SPARQL query string.
    * @param limit             - limit of a SPARQL query string.
    * 
    * @return A concatenation of all arguments with a few tidbits of syntax logic.
    */
   public static String composeAnyQuery(String prefixes,
                                        String queryTypeOperator,
                                        Collection<Resource> contexts, String graphPattern,
                                        String groupBy, String orderBy, String limitTo) {
      
      String _prefixes = prefixes == null || prefixes.length() <= 0 ? "" : prefixes;

      //String graph     =  contexts == null ? "" : "<" + contexts.stringValue() +">";
      String graph =  SetUtilities.justOne(contexts) &&
                      SetUtilities.theOne(contexts) != null
                                            ? "<" + SetUtilities.theOne(contexts).stringValue() +">" 
                                            : "?g";
      StringBuffer froms = new StringBuffer();
      if( null != contexts ) {
         for( Resource context : contexts ) {
            if( null != context ) {
               froms.append("from <" + context.stringValue() + ">\n");
            }
         }
      }
      
      String order = (orderBy == null || orderBy.length()  <= 0) ? "" : "\norder by " + orderBy;
      String group = (groupBy == null || groupBy.length()  <= 0) ? "" : "\ngroup by " + groupBy;
      String limit = (limitTo == null || limitTo.length()  <= 0) ? "" : "\nlimit "    + limitTo;
      
      // 'queryTypeOperator' includes the keyword 'ask', 'construct', 'describe', or 'select'
      // PLUS whatever it wants to construct, describe, or select.
      return 
//            contexts == null ? _prefixes + "\n" + 
//                               queryTypeOperator + "\n"+
//                              "where {\n" +
//                              "  " + graphPattern + "\n"+
//                              "} " + group + order + limit
                               
                               _prefixes + "\n" + 
                               queryTypeOperator + "\n"+
                               froms + 
                              "where {\n"+
                              "  " + graphPattern.replace("\n","\n  ")+"\n"+
                              "}" + group + order + limit;
   }
   
   public static void main(String[] args) {
      
      String prefixes          = "@prefix eg: <http://example.org/vocab/>";
      String queryTypeOperator = "select distinct *";

      String graphPattern      = "?s ?p ?o";
      String groupBy           = "?o";
      String orderBy           = "?s";
      String limitTo           = "10";
      
      System.out.println("- - - - - - - - - - - - - - - - context = null - - - - - - - - - - - - ");
      Collection<Resource> contexts = null;
      System.out.println(QueryletProcessor.composeAnyQuery(prefixes, queryTypeOperator, contexts, graphPattern, groupBy, orderBy, limitTo));

      System.out.println("- - - - - - - - - - - - - - - context contains null - - - -  - - - - - ");
      contexts = SetUtilities.setOf((Resource) null);
      System.out.println(QueryletProcessor.composeAnyQuery(prefixes, queryTypeOperator, contexts, graphPattern, groupBy, orderBy, limitTo));
      
      System.out.println("- - - - - - - - - - - - -context(g1) - - - - - - - - - - - - - - - - - - ");
      contexts = SetUtilities.setOf((Resource) ValueFactoryImpl.getInstance().createURI("http://example.org/g1"));
      System.out.println(QueryletProcessor.composeAnyQuery(prefixes, queryTypeOperator, contexts, graphPattern, groupBy, orderBy, limitTo));
      
      System.out.println("- - - - - - - - - - - -context(g1, g2) - - - - - - - - - - - - - - - - - ");
      contexts.add((Resource) ValueFactoryImpl.getInstance().createURI("http://example.org/g2"));
      System.out.println(QueryletProcessor.composeAnyQuery(prefixes, queryTypeOperator, contexts, graphPattern, groupBy, orderBy, limitTo));
   }
   
   /**
    * 
    * @param select 'select' querylet - WITH NO LIMIT/OFFSET AND NO ORDER BY
    * @param contextR
    * @return
    */
   public static Querylet<Boolean> getAsk(final Querylet<?> select, Resource contextR) {
      return new OnlyOneContextQuerylet<Boolean>(contextR) {
         @Override
         public String getQueryString(Resource context) {
            //System.out.println(select.getQueryString(context) + "\nlimit 1");
            return select.getQueryString(context) + "\nlimit 1"; // TODO: strip off any group/ LIMIT/OFFSET, ORDER BY
         }
         @Override
         public void handleBindingSet(BindingSet row) {
            this.ask = true;
         }
         @Override
         public Boolean get() {
            return this.ask;
         }
      };
   }
   
   /**
    * 
    * @param select 'select' querylet - WITH NO LIMIT/OFFSET AND NO ORDER BY
    * @param contextR
    * @return
    */
   public static Querylet<Boolean> getAsk(final Querylet<?> select, Collection<Resource> contexts) {
      return new PluralContextsQuerylet<Boolean>(contexts) {
         @Override
         public String getQueryString(Collection<Resource> contexts) {
            return select.getQueryString(contexts) + "\nlimit 1"; // TODO: strip off any Group/LIMIT/OFFSET, ORDER BY
         }
         @Override
         public void handleBindingSet(BindingSet row) {
            this.ask = true;
         }
         @Override
         public Boolean get() {
            return this.ask;
         }
      };
   }
}