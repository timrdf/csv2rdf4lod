package edu.rpi.tw.data.csv;

/**
 * 
 */
public interface RowHandler {
   
   /**
    * 
    * @param columnIndex
    * @param headerLabel
    */
   public void visitHeader(long rowIndex, int columnIndex, String headerLabel);
   
   /**
    * 
    * @param record
    * @param rowNum
    */
   public void visitRelativeToHeader(CSVRecord record, long rowNum);
   
   /**
    * 
    * @param topMatter
    */
   public void visitTopMatter(String topMatter);
   
   /**
    * 
    * @param record
    * @param rowNum
    */
   public void visit(CSVRecord record, long rowNum);
   
   /**
    * 
    * @param bottomMatter
    */
   public void visitBottomMatter(String bottomMatter);
}