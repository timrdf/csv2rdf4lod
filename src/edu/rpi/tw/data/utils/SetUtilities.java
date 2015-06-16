package edu.rpi.tw.data.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SetUtilities {
   
   /**
    * 
    * @param <T>
    * @param subject
    * @return a new set with the given resource as the only element.
    */
   public static <T> Set<T> setOf(T element) {
      HashSet<T> set = new HashSet<T>();
      set.add(element);
      return set;
   }
   
   /**
    * 
    * @param <T>
    * @param set
    * @return true iif 'set' is not null and size > 0.
    */
   public static <T> boolean useful(Collection<T> set) {
      return set != null && set.size() != 0;
   }
   
   /**
    * 
    * @param set
    * @return
    */
   public static <T> boolean justOne(Collection<T> set) {
      return set != null && set.size() == 1;
   }
   
   /**
    * 
    * @param <T>
    * @param set
    * @return the one and only element of 'set', null otherwise.
    */
   public static <T> T theOne(Collection<T> set) {
      T theOne = null;
      if( null != set && set.size() == 1 ) {
         for( T resource : set ) {
            theOne = resource;
         }
      }
      return theOne;
   }
}