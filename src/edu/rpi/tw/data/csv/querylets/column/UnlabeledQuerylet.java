package edu.rpi.tw.data.csv.querylets.column;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;

/**
 * 
 */
public class UnlabeledQuerylet extends    ColumnEnhancementQuerylet<Boolean> {
   
	private boolean unlabeled = false;

	// https://github.com/timrdf/csv2rdf4lod-automation/issues/336

	public UnlabeledQuerylet(Resource context, int csvColumnIndex) {
		super(context, csvColumnIndex);
	}

	@Override
	public String getQueryString(Resource context) {

	   unlabeled = false;
		
		this.addNamespace("xsd", "ov", "conversion");

		String select       = "?col";
		String graphPattern = "?col "+columnPO()+"; a conversion:Unlabeled .";
		String orderBy      = "";

		return this.composeQuery(select, context, graphPattern, orderBy, "");
	}

	@Override
	public void handleBindingSet(BindingSet bindingSet) {
		unlabeled = true;
		System.err.println(getClass().getSimpleName() +"("+ this.csvColumnIndex +") is unlabeled.");
	}

	@Override
	public Boolean get() {
		return unlabeled;
	}
}