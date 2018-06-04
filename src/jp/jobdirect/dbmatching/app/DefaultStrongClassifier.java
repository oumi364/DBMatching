package jp.jobdirect.dbmatching.app;

import java.util.ArrayList;
import java.util.List;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.StrongClassifier;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class DefaultStrongClassifier extends AbstractClassifier implements StrongClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = -7276911771256222787L;
	private List<WeakClassifier> _weakClassifiers = new ArrayList<WeakClassifier>();

	public void addWeakClassifier(WeakClassifier weakClassifier){
		this._weakClassifiers.add(weakClassifier);
	}

	@Override
	public void train(DataSet dataSetToTrain) {
	}

	@Override
	public Match classify(Record record1, Record record2) {
		int m = 0, c = 0;
		for(WeakClassifier weakClassifier : this._weakClassifiers){
			c++;
			if(weakClassifier.classify(record1, record2).isMatching()){
				m++;
			}
		}
		return new DefaultMatch(record1, record2, (m > c / 2));
	}

}
