package edu.rpi.tw.data.rdf.sesame.string;

import java.util.Comparator;

import org.openrdf.model.Statement;

public class StatementComparator implements Comparator<Statement> { 

   public int compare(Statement s1, Statement s2) { 
      return s1.getPredicate().stringValue().compareTo(s2.getPredicate().stringValue());
   }

   public static Comparator<Statement> bySPO() {
      return new Comparator<Statement>() { 
         public int compare(Statement s1, Statement s2) { 
            int compVal = s1.getSubject().stringValue().compareTo(s2.getSubject().stringValue());
            if( compVal == 0 ) {
               compVal = s1.getPredicate().stringValue().compareTo(s2.getPredicate().stringValue());
               if( compVal == 0 ) {
                  compVal = s1.getObject().stringValue().compareTo(s2.getObject().stringValue());
               }
            }
            return compVal;
         }
      };
   }

   public static Comparator<Statement> byPredicate() {
      return new Comparator<Statement>() { public int compare(Statement s1, Statement s2) { 
         return s1.getPredicate().stringValue().compareTo(s2.getPredicate().stringValue());}};
   }
}