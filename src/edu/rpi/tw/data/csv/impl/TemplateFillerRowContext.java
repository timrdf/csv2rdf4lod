package edu.rpi.tw.data.csv.impl;

import org.openrdf.model.Value;

import edu.rpi.tw.data.csv.TemplateFiller;

/**
 * 
 */
public abstract class TemplateFillerRowContext implements TemplateFiller {
	
	/**
	 * 
	 * @param template
	 * @return true if 'template' contains variables that require a row as context (e.g. [#1])
	 */
	public static boolean isRowContextual(Value template) {
		return isRowContextual(template.stringValue());
	}
	
	/**
	 * 
	 * @param template
	 * @return true if 'template' contains variables that require a row as context (e.g. [#1])
	 */
	public static boolean isRowContextual(String template) {
		//System.err.println("#isRowContextual " + template + " " + template.matches(".*\\[#[0-9]*\\].*"));
		return template.matches(".*\\[#[0-9]*\\].*") || template.matches(".*\\[\\.*\\].*");
	}
}