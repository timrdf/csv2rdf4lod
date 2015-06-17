package edu.rpi.tw.data.csv.querylets.column;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import edu.rpi.tw.data.csv.Conversion;
import edu.rpi.tw.data.rdf.sesame.query.QueryletProcessor;
import edu.rpi.tw.data.rdf.sesame.querylets.pipes.stops.SGO;
import edu.rpi.tw.data.rdf.sesame.vocabulary.DCTerms;
import edu.rpi.tw.data.rdf.utils.pipes.starts.Cat;

/**
 * Subclasses: SubjectSameAsLinksQuerylet and ObjectSameAsLinksQuerylet.
 */
public abstract class SameAsLinksQuerylet extends    ColumnEnhancementQuerylet<HashMap<String,HashSet<URI>>> {

   private static Logger logger = Logger.getLogger(SameAsLinksQuerylet.class.getName());
   
   protected HashSet<String>              linkGraphs      = new HashSet<String>();
   protected HashSet<URI>                 predicates      = new HashSet<URI>();
   protected boolean                      caseInsensitive = false;
   protected boolean                      direct          = false;
   protected boolean                      include         = false;
   protected HashMap<Value,Set<Value>>    keys            = new HashMap<Value, Set<Value>>();
   
   protected Repository                   linkGraphsRep   = null;
   protected HashMap<String,HashSet<URI>> sameAsLinks     = null;

   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public SameAsLinksQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
      this.linkGraphsRep = new SailRepository(new MemoryStore());
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      
      this.linkGraphs.add(bindingSet.getValue("linksVia").stringValue());

      if( bindingSet.hasBinding("predicate") ) {
         System.err.println(super.REPORT_INDENT+getClass().getSimpleName()+"(" + this.csvColumnIndex +"): " +
               bindingSet.getValue("linksVia").stringValue() + " " +
               bindingSet.getValue("predicate").stringValue());
         this.predicates.add(ValueFactoryImpl.getInstance().createURI(bindingSet.getValue("predicate").stringValue()));
      }else {
         System.err.println(super.REPORT_INDENT+getClass().getSimpleName()+"(" + this.csvColumnIndex +"): " +
               bindingSet.getValue("linksVia").stringValue());
      }
      
      if( bindingSet.hasBinding("type") ) {
         if ( bindingSet.getValue("type").equals(Conversion.DirectSameAsEnhancement) ) {
            direct = true;
            System.err.println(super.REPORT_INDENT+getClass().getSimpleName()+"(" + this.csvColumnIndex +"): direct");
         }
         if ( bindingSet.getValue("type").equals(Conversion.CaseInsensitiveLODLink) ) {
            caseInsensitive = true;
            System.err.println(super.REPORT_INDENT+getClass().getSimpleName()+"(" + this.csvColumnIndex +"): insensitive");
         }
         if ( bindingSet.getValue("type").equals(Conversion.IncludesLODLinks) ) {
            include = true;
            System.err.println(super.REPORT_INDENT+getClass().getSimpleName()+"(" + this.csvColumnIndex +"): include");
         }
      }
      
      if( bindingSet.hasBinding("keyP") ) {
      	// https://github.com/timrdf/csv2rdf4lod-automation/issues/355
      	Value predicate = bindingSet.getValue("keyP");
      	Value object =    bindingSet.getValue("keyO");

      	System.err.println(super.REPORT_INDENT+getClass().getSimpleName()+"(" + this.csvColumnIndex +"): keyP \""+predicate.stringValue()+"\"");
      	System.err.println(super.REPORT_INDENT+getClass().getSimpleName()+"(" + this.csvColumnIndex +"): keyO \""+object.stringValue()+"\"");

      	if( !this.keys.containsKey(predicate) ) {
      		this.keys.put(predicate, new HashSet<Value>());
      	}
      	this.keys.get(predicate).add(object);
      }
   }

   /**
    * Union the RDF sources and query for Ss Os of all predicates.
    */
   @Override
   public void finish(int numResults) {
      
      if( this.predicates.size() == 0 ) {
         this.predicates.add(DCTerms.identifier); // Provide a default.
         this.predicates.add(DCTerms.title);      // Provide a default.
      }
      
      try {
         linkGraphsRep.initialize();
      } catch (RepositoryException e) {
         e.printStackTrace();
      }
      
      for( String linksVia : this.linkGraphs ) {
         System.err.print(super.REPORT_INDENT+"collecting SOs from "+linksVia);
         Cat.load(linksVia, linkGraphsRep);
         System.err.println(" col " + this.csvColumnIndex + " (size: "+Cat.size(linkGraphsRep)+")");
      }

      RepositoryConnection conn = null;
      try {
	      conn = linkGraphsRep.getConnection();
	      
	      for( URI predicate : this.predicates ) {
	         //System.err.println("collecting SOs of "+predicate.stringValue());
	         SGO querylet = new SGO(null,predicate);
	         QueryletProcessor.processQuery(linkGraphsRep, querylet);
	         for( Resource subject : querylet.get().keySet() ) {
	            // e.g., http://dbpedia.org/page/Vermont -> "50", "VT"

	         	// TODO: https://github.com/timrdf/csv2rdf4lod-automation/issues/355
	         	boolean exhibited = true;
	         	for( Value requiredPredicate : keys.keySet() ) {
	         		// e.g. rdf:type
	         		for( Value requiredObject : keys.get(requiredPredicate) ) {
	         			// e.g. http://dbpedia.org/ontology/Company
	         			if( requiredPredicate instanceof URI ) {
	         				exhibited &= conn.hasStatement(subject, (URI)requiredPredicate, requiredObject, false, Cat.DEFAULT_CONTEXT);
	         				if( exhibited ) {
	         					logger.fine(subject.stringValue() + " should and does have " + 
	         										    pmap.bestQNameFor(requiredPredicate.stringValue() + " " + requiredObject.stringValue()));
	         				}else {
	         					logger.fine(subject.stringValue() + " should BUT DOES NOT have " + 
										    pmap.bestQNameFor(requiredPredicate.stringValue() + " " + requiredObject.stringValue()));
	         				}
	         			}else {
	         				System.err.println("WARNING: only processing URI key predicate.");
	         			}
	         		}
	         	}

	         	if( exhibited ) {
		            for( Value object : querylet.get().get(subject) ) {
		               String string = object.stringValue();
		               if( !this.sameAsLinks.containsKey(string) ) {
		                  this.sameAsLinks.put(string, new HashSet<URI>());
		               }
		               this.sameAsLinks.get(string).add((URI)subject);
		               // e.g. "VT" -> http://dbpedia.org/page/Vermont
		               // e.g. "50" -> http://dbpedia.org/page/Vermont
		               logger.fine(" col " + this.csvColumnIndex + " value "+string+" implies same as " +subject+")");
		            }
	         	}
	         }
	      }
      } catch (RepositoryException e) {
	      e.printStackTrace();
      }
      
      if( conn != null ) {
      	try {
	         conn.close();
         } catch (RepositoryException e) {
	         e.printStackTrace();
         }
      }
   }
   
   /**
    * 
    * @return mappings from string values to URIs that imply identity according to inverse functionality.
    */   
   @Override
   public HashMap<String, HashSet<URI>> get() {
      /*for( String string : this.sameAsLinks.keySet() ) {
         System.err.println("ObjectSameAsLinksQuerylet: "+string+" => sameAs "+this.sameAsLinks.get(string));
      }*/
      return this.sameAsLinks;
   }
   
   /**
    * https://github.com/timrdf/csv2rdf4lod-automation/issues/234
    * 
    * @return true if external LOD bubble URIs should be asserted directly as objects of triples, 
    * instead of asserting a local URI as the only object and filling in an owl:sameAs.
    */
   public boolean isDirect() {
      return this.direct;
   }
   
   /**
    * https://github.com/timrdf/csv2rdf4lod-automation/issues/241
    * 
    * @return true if the values in the input should be sameAs linked if they match regardless of case.
    */
   public boolean isCaseInsensitive() {
      return this.caseInsensitive;
   }
   
   /**
    * https://github.com/timrdf/csv2rdf4lod-automation/issues/274
    * 
    * @return true if the LODLink graph should be passed through to the converted output.
    */
   public boolean doesInclude() {
      return this.include;
   }
   
   /**
    * https://github.com/timrdf/csv2rdf4lod-automation/issues/355
    * 
    * @return the set of predicate-objects that the target resource should have to link to it.
    */
   public HashMap<Value,Set<Value>> getKeys() {
   	return this.keys;
   }
   
   /**
    * https://github.com/timrdf/csv2rdf4lod-automation/issues/355
    * 
    * @return a Repository of the RDF graphs mentioned by the enhancements.
    */
   public Repository getLODLinksGraph() {
      return this.linkGraphsRep;
   }
}