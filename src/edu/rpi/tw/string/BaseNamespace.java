package edu.rpi.tw.string;

import edu.rpi.tw.data.rdf.sesame.vocabulary.TetherlessWorld;

/**
 * 
 */
public class BaseNamespace {
   private static String BASE_NAME      = System.getProperty("edu.rpi.tw.data.rdf.string.BaseNamespace",TetherlessWorld.R);
   private static String RESOURCE_ADDON = System.getProperty("edu.rpi.tw.data.rdf.string.BaseNamespace.resource_addon","");
   private static String PROPERTY_ADDON = System.getProperty("edu.rpi.tw.data.rdf.string.BaseNamespace.property_addon","Property-3A");
   private static String DATATYPE_ADDON = System.getProperty("edu.rpi.tw.data.rdf.string.BaseNamespace.datatype_addon","Datatype-3A");
   
   public static String forResource() {
      return BASE_NAME + RESOURCE_ADDON;
   }

   public static String forProperty() {
      return BASE_NAME + PROPERTY_ADDON;
   }
   
   public static String forDatatype() {
      return BASE_NAME + DATATYPE_ADDON;
   }
   
   public static void main(String[] args) {
      String propertyName = args[0];
      System.out.println(propertyName + " = " + System.getProperty(propertyName));
   }
}