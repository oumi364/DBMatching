package jp.jobdirect.dbmatching.app;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;
import jp.jobdirect.dbmatching.model.Value;

public class DefaultWeakClassifier extends AbstractClassifier implements WeakClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 827367977273018133L;

	@Override
	public void train(DataSet dataSetToTrain) {
	}

	@Override
	public Match classify(Record record1, Record record2) {
		Value name1 = record1.getValue("NAME");
		Value name2 = record2.getValue("NAME");
		
		String nameString1 = ((DefaultValue)name1).getString();
		String nameString2 = ((DefaultValue)name2).getString();
		
		boolean matched = nameString1 != null && nameString2 != null && nameString1.equals(nameString2);
		
		return new DefaultMatch(record1, record2, matched);
	}
}
