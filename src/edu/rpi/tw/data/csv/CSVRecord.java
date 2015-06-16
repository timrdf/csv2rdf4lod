package edu.rpi.tw.data.csv;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class CSVRecord implements CSVRecordWriteable, CSVRecordReadable {

   List<String>         columnLabels;
   Map<Integer, String> values;
   
   /**
    * Shallow copies the list of header keys since it's likely hundreds of
    * records will be sharing the same keys.
    * 
    * @param colHeaders
    *           The list of column headers from the CSV file.
    */
   public CSVRecord(List<String> colHeaders) {
      columnLabels = colHeaders;
      values       = new HashMap<Integer, String>(colHeaders.size());
   }

   /**
    * @param columnIndex - zero-based index of the column
    * @param value       - 
    */
   @Override
   public void addItem(int columnIndex, String value) {
      values.put(columnIndex, value);
   }

   /**
    * 
    * @param columnLabel
    * @return
    */
   public String getItem(String columnLabel) {
      String item = null;
      // TODO: map label to index and stop searching.
      for(int columnIndex = 0; columnIndex < this.columnLabels.size(); columnIndex++ ) {
         if( this.columnLabels.get(columnIndex).equals(columnLabel) ) {
            item = this.values.get(columnIndex);
         }
      }
      return item;
   }
   
   /**
    * 
    * @param columnIndex
    * @return
    */
   public String getValue(Integer columnIndex) {
      return values.get(columnIndex);
   }

   /**
    * 
    * @param columnIndex
    * @return
    */
   public String getQuotelessCommadValue(Integer columnIndex) {
      return unescapeCommas(stripQuotes(getValue(columnIndex)));
   }
   
   /**
    * 
    */
   public void clearValues() {
      this.values.clear();
   }
   
   /**
    * 
    * @return
    */
   public List<String> getKeys() {
      return Collections.unmodifiableList(this.columnLabels);
   }

   /**
    * 
    * @return
    */
   public Map<String, String> getAttributeValues() {
      HashMap<String, String> attrVals = new HashMap<String,String>();
      for( Integer columnIndex : this.values.keySet() ) {
         attrVals.put(this.columnLabels.get(columnIndex), this.values.get(columnIndex));
      }
      return Collections.unmodifiableMap(attrVals);
   }

   /**
    * 
    * @return
    */
   public int getItemCount() {
      return this.values.size();
   }

   /**
    * 
    */
//   public int hashCode() {
//      return values.hashCode() * 31 + headerKeys.hashCode();
//   }

   /**
    * 
    */
   public boolean equals(Object o) {
      if (o.getClass() == this.getClass()) {
         CSVRecord rec = (CSVRecord)o;
         return this.columnLabels.equals(rec.columnLabels)
                && this.values.equals(rec.values);
      } else {
         return false;
      }
   }

   /**
    * 
    * @param colHdrs
    * @return
    */
   public String toString(List<String> colHdrs) {
      String str = new String();
      for (int i = 0; i < colHdrs.size(); i++) {
         String key = colHdrs.get(i);
         str = str.concat(this.values.get(key));

         if (i < (colHdrs.size() - 1)) {
            str = str.concat(",");
         }
      }
      return str;
   }

   /**
    * 
    */
   public String toString() {
      String str = new String();
      for (int i = 0; i < this.columnLabels.size(); i++) {
         String key = this.columnLabels.get(i);
         str = str.concat(this.values.get(key));

         if (i < (this.columnLabels.size() - 1)) {
            str = str.concat(",");
         }
      }
      return str;
   }
   
   /**
    * @param value - a String to strip leading and trailing double quotes.
    * @return 'value' with no leading double quotes and not trailing double quotes (if they exist).
    */
   public static String stripQuotes(String value) {
      String newVal = value;
      //System.out.println("Stripping ."+value+".");
      if(value != null && newVal.length() > 0 && newVal.charAt(0) == '"') {
         //System.out.println("first");
         newVal = newVal.substring(1,newVal.length());
         if(newVal.length() > 1 && newVal.charAt(newVal.length()-1) == '"') {
            //System.out.println("second");
            newVal = newVal.substring(0,newVal.length()-1);
         }
      }
      if(value != null && newVal.length() > 0 && newVal.charAt(0) == '\'') {
         //System.out.println("first");
         newVal = newVal.substring(1,newVal.length());
         if(newVal.length() > 1 && newVal.charAt(newVal.length()-1) == '\'') {
            //System.out.println("second");
            newVal = newVal.substring(0,newVal.length()-1);
         }
      }
      return newVal;
   }

   /**
    * @param value
    * @return a copy of 'value' replacing escaped commas with commas.
    */
   public static String unescapeCommas(String value) {
      return value != null ? value.replace("%2C", ",") : value;
   }
   
   /**
    * 
    * @param value
    * @return
    */
   public static String clean(String value) {
      return unescapeCommas(stripQuotes(value));
   }
}