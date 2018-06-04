package jp.jobdirect.dbmatching.app;

import java.util.Collection;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class GeographicCoordinateDistanceClassifier extends AbstractClassifier implements WeakClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = -8896742710822148343L;

	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;

	private float _threshold = 0f;
	private float _normalizer = 0f;

	@Override
	public void train(DataSet dataSetToTrain) {
		Collection<Match> matches = dataSetToTrain.getMatches();

		Separation sep = new Separation();
		for(Match match : matches){
			Record[] records = match.getRecords();
			float latitude1 = records[0].getFloatValue("LATITUDE");
			float latitude2 = records[1].getFloatValue("LATITUDE");
			float longitude1 = records[0].getFloatValue("LONGITUDE");
			float longitude2 = records[1].getFloatValue("LONGITUDE");

			float d = (float)Math.hypot(latitude1 - latitude2, longitude1 - longitude2);

			// 緯度経度が付与されていない(0が入っている)データは除外する。
			//if(!(latitude1 == 0f || latitude2 == 0f ||longitude1 == 0f ||longitude2 == 0f)) {
			if(!(latitude1 == 0f || latitude2 == 0f ||longitude1 == 0f ||longitude2 == 0f)) {
				sep.addValue(d, match.isMatching());
			}
//			else{
//				sep.addValue(0f, match.isMatching());
//			}

			if(DEBUG){
				System.out.println("Train, " + this.getClass().getSimpleName() + ", " + match.isMatching() + ", " + d + ", [" + latitude1 + ", " + longitude1 + "], [" + latitude2 + ", " + longitude2 + "]");
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
//		if("津山城跡（鶴山公園）".equals(record1.getStringValue("NAME")) && "弥高山公園".equals(record2.getStringValue("NAME"))) {
//			System.out.println("name1:" + record1.getStringValue("NAME") + " name2:" + record2.getStringValue("NAME"));
//		}

		float latitude1 = record1.getFloatValue("LATITUDE");
		float latitude2 = record2.getFloatValue("LATITUDE");
		float longitude1 = record1.getFloatValue("LONGITUDE");
		float longitude2 = record2.getFloatValue("LONGITUDE");

		float d = (float)Math.hypot(latitude1 - latitude2, longitude1 - longitude2);
		float s = (d - this._threshold) / this._normalizer;

		return new MatchWithLikelihood(record1, record2, this.getClass(), (s > 0), s);
	}
}
