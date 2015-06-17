package edu.rpi.tw.string;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This is used by 2graffle and COULD be used by xmi for uuid. 
 * TODO: find A superclass of ColorManager and UUIDManager and UUIDManager
 * 
 * was jv...2-rdf/src/.../.../jv.../rdf/string
 */
public class IDManager {
   
   enum IDType {UUID, INT64};
   
   private long    nextID = 0;
   private boolean hitMax = false;
   
   private HashMap<String,Long> assignments = null;
   
   private static Logger theLogger = Logger.getLogger(IDManager.class.getName());
   
// public IDManager() {
//    new IDManager(IDType.INT64);
// }

   /**
    * 
    */
   public IDManager() {
      //System.err.println("# created new ID manager.");
      this.assignments = new HashMap<String,Long>();
   }
   
   /**
    * If the provided label has not been assigned an identifier, 
    * assign an identifier that has not been assigned.
    * 
    * Requesting the identifier of a label repeatedly returns the same identifier.
    * 
    * @param label - A client-side string that uniquely identifies some thing.
    * @return an alternate identifier in the IDType (e.g. UUID, INT64) format that this object 
    *         was configured to return.
    */
   public String getIdentifier(String label) {
      String id = "";
      //IDManager.theLogger.finer("  IDM.getIdentifier(" + label + ") --> ");
      //System.err.print("  ("+this.assignments.size()+") " +this.toString()+" getIdentifier(" + label + ") --> ");
      if( this.assignments.containsKey(label) ) {
         // Return the already-assigned identifier
         id = this.assignments.get(label).toString();
         //IDManager.theLogger.finer("  " + label + " already assigned id " + id);
         //System.err.println("already assigned id " + id);
      }else {
         // Assign the next identifier
         this.assignments.put(label,this.getNextID());
         id = Long.toString(this.nextID);
         //IDManager.theLogger.finer(label + " gets id " + id);
         //System.err.println("gets id " + id);
      }
      return id;
   }
   
   /**
    * 
    * @param label
    * @return
    */
   public boolean hasIdentified(String label) {
      //System.err.print("  ("+this.assignments.size()+") " +this.toString()+" hasIdentified(" + label + ") --> "+this.assignments.containsKey(label));
      return this.assignments.containsKey(label);
   }
   
   /**
    * 
    * @return
    */
   private long getNextID()
   {
      // Counter starts at 1, grows to MAX_VALUE, resets to MIN_VALUE and stops at 0   
      // 1,2,3, ... 2^64, -2^64, -2^64 - 1, -2^64 - 2, ..., -1, 0, 0, 0, 0, ...
      if (!this.hitMax && this.nextID < Long.MAX_VALUE || this.hitMax && this.nextID < 0) 
      {
         this.nextID++;
      }
      else if (!this.hitMax && this.nextID == Long.MAX_VALUE) {
         this.hitMax = true;
         this.nextID = Long.MIN_VALUE;
         IDManager.theLogger.finer("hitMax");
      }
      // Neither of the above conditions is true when identifiers are exhausted
      return this.nextID;
   }
   
   /**
    * 
    * @return
    */
   public boolean hasMoreDistinctAssignments()
   {
      // Counter starts at 1, grows to MAX_VALUE, resets to MIN_VALUE and stops at 0
      return !(this.hitMax && this.nextID == 0);
   }
   
   /**
    * 
    * @param labels
    * @return
    */
   /*public String toString( Set<URI> labels ) {
//    String retVal = "{";
//    for( Object o : labels ) {
//       retVal = retVal + getIdentifier(o.toString()) + ", ";
//    }
//    return retVal+"nil}";
      
      String retVal ="[";
      Iterator<URI> iter = labels.iterator();
      while( iter.hasNext() ) {
         String val = getIdentifier(iter.next().toString());
         retVal = iter.hasNext() ? retVal + val + "," 
                             : retVal + val;
      }
      return retVal + "]";
   }*/
   
   /**
    * 
    * @param args
    */
   public static void main(String args[])
   {
      IDManager idm = new IDManager();
      for (long label = 0; label < Long.MAX_VALUE; label++) {
         System.out.println(Long.toString(label) + " " + idm.getIdentifier(Long.toString(label)));
         //idm.getIdentifier(Long.toString(label));
         System.out.println(Long.toString(label) + " " + idm.getIdentifier(Long.toString(label)));
      }
   }
}