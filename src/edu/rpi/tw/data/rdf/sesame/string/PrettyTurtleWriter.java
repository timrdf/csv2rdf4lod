package edu.rpi.tw.data.rdf.sesame.string;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 * An attempt to aggregate triples by their subject.
 */
public class PrettyTurtleWriter implements RDFWriter {

   private HashMap<Resource,HashSet<Statement>> statements;
   private HashSet<Namespace> namespaces;
   //private Writer       writer = null;
   //private OutputStream out    = null;
   private TurtleWriter ttl;
   private int numStatementsGiven   = 0;
   private int numStatementsPrinted = 0;

   private boolean tryFoldingBNodes = true;

   /**
    * 
    */
   PrettyTurtleWriter() {
      this.statements = new HashMap<Resource,HashSet<Statement>>();
      this.namespaces = new HashSet<Namespace>();
   }
   
   /**
    * 
    * @param writer
    */
   public PrettyTurtleWriter(Writer writer) {
      this();
      //this.writer = new PrintWriter(new BufferedWriter(writer));
      this.ttl = new TurtleWriter(writer);
   }

   /**
    * 
    * @param out
    */
   public PrettyTurtleWriter(OutputStream out) {
      this();
      //this.out = out;
      this.ttl = new TurtleWriter(out);
   }

   public RDFFormat getRDFFormat() {
      return RDFFormat.TURTLE;
   }

   /**
    * 
    */
   public void startRDF() throws RDFHandlerException {
   }

   /**
    * 
    */
   public void handleNamespace(String prefix, String name) throws RDFHandlerException {
      ttl.handleNamespace(prefix, name);
      namespaces.add(new NamespaceImpl(prefix,name));
   }

   /**
    * 
    */
   public void handleComment(String comment) throws RDFHandlerException {
      ttl.handleComment(comment);
   }

   /**
    * 
    */
   public void handleStatement(Statement statement) throws RDFHandlerException {
      Resource subject = statement.getSubject();
      if( !statements.containsKey(subject)) {
         statements.put(subject, new HashSet<Statement>());
      }
      statements.get(subject).add(statement);
      numStatementsGiven++;
   }

   /**
    * 
    */
   public void endRDF() throws RDFHandlerException {
      ttl.startRDF();
      HashSet<Resource> bnodeObjects    = new HashSet<Resource>();
      HashSet<Resource> handledSubjects = new HashSet<Resource>();

      if( tryFoldingBNodes ) {
         // For each subject found.
         ArrayList<Resource> subjects = new ArrayList<Resource>(this.statements.keySet());
         Collections.sort(subjects,new ResourceComparator());
         for( Resource subject : subjects ) {

            // If the subject is not a blank node
            if( !(subject instanceof BNode) ) {

               // Sort the predicate/objects by the predicate
               ArrayList<Statement> statements = new ArrayList<Statement>(this.statements.get(subject));
               Collections.sort(statements, StatementComparator.byPredicate());
               for( Statement statement : statements ) {
                  ttl.handleStatement(statement); numStatementsPrinted++;

                  // Save up the statements with bnode objects.
                  Value object = statement.getObject();
                  if( object instanceof BNode && this.statements.containsKey(object) ) {
                     bnodeObjects.add((BNode)object);
                  }
               }
               //  Handle the saved statements from ^
               for( Resource bnodeSubject : bnodeObjects ) {
                  for( Statement statement : this.statements.get(bnodeSubject) ) {
                     ttl.handleStatement(statement); numStatementsPrinted++;
                  }
                  handledSubjects.add(bnodeSubject);
               }
            }
         }

         // Handle the statements with bnode subjects that were not handled ("nested") in first pass.
         for( Resource subject : subjects ) {
            if( subject instanceof BNode && !handledSubjects.contains(subject) ) {
               // Sort the predicate/objects by the predicate
               ArrayList<Statement> statements = new ArrayList<Statement>(this.statements.get(subject));
               Collections.sort(statements, StatementComparator.byPredicate());
               for( Statement statement : statements ) {
                  ttl.handleStatement(statement); numStatementsPrinted++;
               }
            }
         } 
      }else { // don't tryFoldingBNodes
      }
      if( this.numStatementsGiven != this.numStatementsPrinted ) {
         System.err.println("WARNING: PrettyTurtleWriter numStatementsGiven != numStatementsPrinted.");
      }

      ttl.endRDF();
   }

   // <-- Namespace printing utilities -->

   /**
    * @param names - a (potentially unsorted) collection of <abbreviation,name> pairings.
    * @return a pretty string listing 'names' (sorted by URI).
    */
   public static String prefixMappingAsString(Collection<Namespace> names) {
      ArrayList<Namespace> namesList = new ArrayList<Namespace>(names);
      return PrettyTurtleWriter.prefixMappingsAsString(namesList, true, false);
   }
   
   /**
    * 
    * @param names
    * @param wrap uris in '<>', include 'prefix' keyword for SPARQL.
    * @return
    */
   public static String prefixMappingAsString(Collection<Namespace> names, boolean wrap) {
      ArrayList<Namespace> namesList = new ArrayList<Namespace>(names);
      return PrettyTurtleWriter.prefixMappingsAsString(namesList, true, wrap);
   }

   /**
    * @param names - a list of <abbreviation,name> pairings.
    * @return a pretty string listing 'names' (with original order preserved).
    */
   public static String prefixMappingsAsString(List<Namespace> names) {
      return PrettyTurtleWriter.prefixMappingsAsString(names, false);
   }

   /**
    * 
    * @param names
    * @param sort
    * @return
    */
   public static String prefixMappingsAsString(List<Namespace> names, boolean sort) {
      return prefixMappingsAsString(names, sort, false);
   }
   
   /**
    * 
    * @param names
    * @param sort
    * @param wrap - wrap uris in '<>', include 'prefix' keyword for SPARQL.
    * @return
    */
   public static String prefixMappingsAsString(List<Namespace> names, boolean sort, boolean wrap) {
      
      String keyword = wrap ? "prefix " : "";
      String left  = wrap ? "<" : "";
      String right = wrap ? ">" : "";
      
      String string = "";
      List<Namespace> sortedNames = names;
      if( sort ) {
         Collections.sort(sortedNames,NamespaceComparator.byName());
      }
      int longestPrefixLength = longestPrefixLength(sortedNames);
      for( Namespace namespace : sortedNames ) {
         string = string + keyword + namespace.getPrefix() + ":";
         for( int i = 0; i < longestPrefixLength - namespace.getPrefix().length(); i++ ) {
            string = string + " ";
         }
         string = string + " " + left + namespace.getName() + right + "\n";
      }
      return string;
   }
   
   public static int longestPrefixLength( Collection<Namespace> namespaces ) {
      int longestLength = 0;
      for( Namespace ns : namespaces ) {
         if( ns.getPrefix().length() > longestLength ) {
            longestLength = ns.getPrefix().length();
         }
      }
      return longestLength;
   }
   
   /**
    * 
    * @param primary
    * @param out
    */
   public static void printNamespaces(RepositoryConnection conn, PrintStream out) {
      RepositoryResult<Namespace> namespaces;
      try {
         namespaces = conn.getNamespaces();
         out.print("("+namespaces.asList().size()+") :");
         for( Namespace namespace : namespaces.asList() ) {
            out.print(namespace.getPrefix()+" ");
         }out.println();   
      } catch (RepositoryException e) {
         e.printStackTrace();
      }finally {
         try {
            conn.close();
         } catch (RepositoryException e) {
            e.printStackTrace();
         }
      }
   }
   
   /**
    * 
    * @param n
    * @param out
    */
   public static void printNSpaces(int n, PrintStream out) {
      out.print(nSpaces(n));
   }

   public static String nSpaces(int n) {
      StringBuffer spaces = new StringBuffer();
      for( int i = 0; i <  n; i++ ) {
         spaces.append(" ");
      }
      return spaces.toString();
   }

   /**
    * 
    * @param namespace
    * @param longestPrefixLength
    * @return
    */
   public static String printNamespace(Namespace namespace) {
      return printNamespace(namespace, namespace.getPrefix().length());
   }
   
   /**
    * 
    * @param namespace
    * @param longestPrefixLength
    * @return
    */
   public static String printNamespace(Namespace namespace, int longestPrefixLength) {
      StringBuffer retVal = new StringBuffer();
      retVal.append(namespace.getPrefix());
      retVal.append(nSpaces(longestPrefixLength-namespace.getPrefix().length()));
      retVal.append(" : " + namespace.getName());
      return retVal.toString();
   }
   
   /**
    * 
    * @param value
    * @return
    */
   public static String serializeValue(Value value) {
      String flat = "";
      if( value instanceof URI ) {
        flat =  "<"+value.stringValue()+">";
      }else if( value instanceof Literal ) {
         Literal valueL = (Literal) value;
         flat = "\""+value.stringValue()+"\"";
         if( valueL.getDatatype() != null ) {
            flat = flat + "^^<" + valueL.getDatatype().stringValue() + ">";
         }
      }else if( value instanceof BNode ) {
         flat = "[]";
      }
      return flat;
   }

   @Override
   public Collection<RioSetting<?>> getSupportedSettings() {
      return null;
   }

   @Override
   public WriterConfig getWriterConfig() {
      return null;
   }

   @Override
   public void setWriterConfig(WriterConfig arg0) {
   }
}