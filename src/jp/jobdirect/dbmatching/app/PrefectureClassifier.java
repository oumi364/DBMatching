package jp.jobdirect.dbmatching.app;

import java.util.Collection;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class PrefectureClassifier extends AbstractClassifier implements WeakClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = 7486746343786395723L;
	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;

	@Override
	public void train(DataSet dataSetToTrain) {
		Collection<Match> matches = dataSetToTrain.getMatches();
		for(Match match : matches){
			Record[] records = match.getRecords();
			if(DEBUG){
				String p1 = records[0].getStringValue("PREF");
				String p2 = records[1].getStringValue("PREF");

				System.out.println("Train, " + this.getClass().getSimpleName() + ", " + match.isMatching() + ", " + (p1.equals(p2)) + ", " + p1 + ", " + p2);
			}
		}
	}

	@Override
	public Match classify(Record record1, Record record2) {
		String name1 = record1.getStringValue("PREF");
		String name2 = record2.getStringValue("PREF");

		boolean equals = name1.equals(name2);

//		if(DEBUG){
//			System.out.println(this.getClass().getSimpleName() + ", equals=" + equals + ", name1=" + name1 + ", name2=" + name2);
//		}

		return new MatchWithLikelihood(record1, record2, this.getClass(), equals, equals ? 1f : 0f);
	}
}
