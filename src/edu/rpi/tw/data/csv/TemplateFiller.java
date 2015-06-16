package edu.rpi.tw.data.csv;

import org.openrdf.model.Value;

/**
 * See TemplateFillerColumnContext, PropertyNameFactory, DefaultEnhancementParameters,
 * CSVRecordTemplateFiller
 */
public interface TemplateFiller {
   
   /**
    * 
    * @param row
    * @param column
    * @param cellValue
    */
   public void setCellReferencedRelativeToHeader(long row, long column, String cellValue);
   
   /**
    * Defaults to literal, not Resource.
    * 
    * @param template
    * @return
    */
   public String fillTemplate(String template);
   
   /**
    * 
    * @param template
    * @param asResource - if true, URI-ify the value for [.].
    * @return
    */
   public String fillTemplate(String template, boolean asResource);
   
   /**
    * Fill 'template', using 'currentValue' for [.].
    * 
    * @param template
    * @param currentValue
    * @param asResource
    * @return
    */
   public String fillTemplate(String template, String currentValue, boolean asResource);
   
   /**
    * 
    * @param template
    * @return true if 'template' contains a variable.
    */
   public boolean doesExpand(String template);
   
   /**
    * 
    * @param template
    * @return
    */
   public Value tryExpand(String template);

   /**
    * 
    * @param template
    * @param tempalteV - a value from which the template was created (contains the target datatype).
    * @return
    */
   public Value tryExpand(String template, Value templateV);
   
   /**
    * 
    * @param template
    * @return
    */
   public Value tryExpand(Value template);
}