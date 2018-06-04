package jp.jobdirect.dbmatching.app;

import java.util.Collection;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class ReviewCountClassifier extends AbstractClassifier implements WeakClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = 7486746343786395723L;
	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;

	private float _threshold = 0f;
	private float _normalizer = 0f;
	private final String DEVIATION_0 = "0";

	@Override
	public void train(DataSet dataSetToTrain) {
		Collection<Match> matches = dataSetToTrain.getMatches();

		Separation sep = new Separation();
		for(Match match : matches){
			Record[] records = match.getRecords();
			float d = this.distance(records[0].getStringValue("REV"), records[1].getStringValue("REV"));

			sep.addValue(d, match.isMatching());

			if(DEBUG){
				System.out.println("Train, " + this.getClass().getSimpleName() + ", " + match.isMatching() + ", " + d + ", " + records[0].getStringValue("REV") + ", " + records[1].getStringValue("REV"));
			}
		}
		this._threshold = sep.getThreshold();
		this._normalizer = sep.getNormalizer();
		if(DEBUG){
			System.out.println(this.getClass().getSimpleName() + ", norm=" + this._normalizer + ", threshold=" + this._threshold);
		}
	}

	@Override
	public Match classify(Record record1, Record record2) {
		String name1 = record1.getStringValue("REV");
		String name2 = record2.getStringValue("REV");

		float d = this.distance(name1, name2);
		float s = (d - this._threshold) / this._normalizer;
//		System.out.println("d=" + d + ", s=" + s);

		return new MatchWithLikelihood(record1, record2, this.getClass(), (d > 0) , s);
	}

	public float distance(String name1, String name2){

		if((!DEVIATION_0.equals(name1) && !DEVIATION_0.equals(name2) )) {

	        // 偏差値判定（両方50より上、または50以下でtrue）
	        if (50f < Float.parseFloat(name1) && 50f < Float.parseFloat(name2)) {
	            return 1f;
	        }
	    	if (50f >= Float.parseFloat(name1) && 50f >= Float.parseFloat(name2)) {
	            return 1f;
	    	}
		}
        return 0f;

	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		sb.append("{t=");
		sb.append(this._threshold);
		sb.append(", n=");
		sb.append(this._normalizer);
		sb.append("}");
		return sb.toString();
	}
}
