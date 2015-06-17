package edu.rpi.tw.data.rdf.sesame.string;

import java.util.Comparator;

import org.openrdf.model.Namespace;

public class NamespaceComparator implements Comparator<Namespace> {
   
   public static Comparator<Namespace> byName() {
      return new Comparator<Namespace>() { public int compare(Namespace n1, Namespace n2){ return n1.getName().compareTo(n2.getName());}};
   }

   public static Comparator<Namespace> byPrefix() {
      return new Comparator<Namespace>() { public int compare(Namespace n1, Namespace n2){ return n1.getPrefix().compareTo(n2.getPrefix());}};
   }
   
   public int compare(Namespace n1, Namespace n2) { 
      return n1.getName().compareTo(n2.getName());
   }
}