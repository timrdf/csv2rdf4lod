package edu.rpi.tw.data.csv.querylets.column;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

/**
 * TODO: conversion:bundled_by "[#3]" should work just like conversion:bundled_by [ ov:csvCol 3 ];
 */
public class ObjectTemplateQuerylet extends ColumnEnhancementQuerylet<List<String>> {

   protected List<String> templates;
   protected String       template;
   
// TODO: reconcile with CrutchTemplateQuerylet
   /**
    * 
    * @param context
    * @param csvColumnIndex
    */
   public ObjectTemplateQuerylet(Resource context, int csvColumnIndex) {
      super(context, csvColumnIndex);
   }

   @Override
   public String getQueryString(Resource context) {
      this.templates = new ArrayList<String>();
      this.template  = null;
      
      this.addNamespace("xsd", "ov", "conversion");
      
      String select       = "distinct ?template";
      String graphPattern = "?col "+columnPO()+                       ";\n"+
                            "     conversion:range_template ?template . \n";
      String orderBy      = "?template";
      
      //System.err.println(this.composeQuery(select, context, graphPattern, orderBy, "1"));
      return this.composeQuery(select, context, graphPattern, orderBy);
   }

   @Override
   public void handleBindingSet(BindingSet bindingSet) {
      this.template = bindingSet.getValue("template").stringValue();
      this.templates.add(template);
      System.err.println(getClass().getSimpleName() + "(" + this.csvColumnIndex+") ." + template + ".");
   }

//   @Override
//   public List<String> getSet() {
//      return this.templates;
//   }
//
//   @Override
//   public String getStringResult() {
//      return this.template;
//   }

   @Override
   public List<String> get() {
      return this.templates;
   }
}