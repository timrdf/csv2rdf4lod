package edu.rpi.tw.data.rdf.sesame.string;

import java.util.Comparator;

import org.openrdf.model.Resource;

/**
 * 
 */
public class ResourceComparator implements Comparator<Resource> {

   public int compare(Resource r1, Resource r2) { 
      return r1.stringValue().compareTo(r2.stringValue());
   }

   public static Comparator<Resource> byName() {
      return new Comparator<Resource>() { public int compare(Resource r1, Resource r2) { 
         return r1.stringValue().compareTo(r2.stringValue());}};
   }

   public static Comparator<Resource> byNamespacePopularity() throws Exception {
      if( true ) {
         throw new Exception("not implemented");
      }
      return new Comparator<Resource>() { 
      	public int compare(Resource r1, Resource r2) { 
      		return r1.stringValue().compareTo(r2.stringValue());
      	}
      };
   }
}