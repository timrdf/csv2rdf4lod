package edu.rpi.tw.data.csv.impl;

import java.util.HashMap;

import edu.rpi.tw.data.csv.TemplateFiller;
import edu.rpi.tw.string.NameFactory;

/**
 * Expands the column-contextual variables in the given template.
 * Does NOT do the context-free (use EnhancementParameters for that).
 * Does NOT do the row/cell contexts.
 * 
 * See also CSVRecordTemplateFiller, DefaultEnhancementParameters, PropertyNameFactory
 *
 */
public abstract class TemplateFillerColumnContext implements TemplateFiller {

	protected HashMap<String,String> context = new HashMap<String,String>();
	
	/**
	 * 
	 * @param template
	 * @param index     - template variable [c]
	 * @param header    - template variable [H]
	 * @param label     - template variable [L]
	 * @param localName - template variable [@]
	 * @return
	 */
	public static String fillTemplateWithColumnContext(String template, 
													   int index, String header, String label, String localName) {
        String filled = template.replaceAll("\\[c\\]",                      ""+index);      // [c] 
        filled          = filled.replaceAll("\\[H\\]",   NameFactory.label2URI(header));    // [H] 
        filled          = filled.replaceAll("\\[L\\]",   NameFactory.label2URI(label));     // [L] 
        filled          = filled.replaceAll("\\[@\\]",   NameFactory.label2URI(localName)); // [@] 
        
        // setContext and fillTemplate were created after this method.
        
		return filled;
	}
	
	/**
	 * 
	 * @param identifier e.g. 'L' for property label, 'H' for column header, 
	 *                        '@' for property local name
	 * @param value
	 */
	public void setContext(String identifier, String value) {
		if ( identifier != null && identifier.length() > 0 && value != null ) {
			this.context.put(identifier, value);
		}
	}
	
	/**
	 * Provides the column-contextual variables.
	 */
	@Override
	public String fillTemplate(String template) {
		//String filled = template;
		//if ( template != null && template.length() > 0 ) {
		//	for( String identifier : this.context.keySet() ) {
				//System.err.println("replacing " + identifier);
				//System.err.println(" with " + this.context.get(identifier));
				//filled = filled.replaceAll("\\["+identifier+"\\]", this.context.get(identifier));
			//}
		//}
		return template;
	}
}