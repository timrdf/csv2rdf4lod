package edu.rpi.tw.data.csv;

import java.util.Set;

/**
 * 
 */
public interface NullInterpreter {
   
   /**
    * 
    * @param interpretAsNulls
    */
   public void setInterpretAsNulls(Set<String> interpretAsNulls);
   
   /**
    * 
    * @param value - value of cell that may become object of a triple.
    * @return true iff 'value' should not produce a triple.
    */
   public boolean interpretsAsNull(String value);
}