package jp.jobdirect.dbmatching.app;

import java.util.Collection;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class PlansDistanceClassifier extends AbstractClassifier implements WeakClassifier {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4340721074643171520L;

	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;
	
	private float _threshold = 0f;
	private float _normalizer = 0f;

	private static boolean USE_PRICES = true;
	
	public PlansDistanceClassifier(){
		System.out.println(new StringBuffer()
				.append(this.getClass().getSimpleName()).append(": ")
				.append("usePrices=").append(USE_PRICES).append(": ")
				);
	}
	
	@Override
	public void train(DataSet dataSetToTrain) {
		Collection<Match> matches = dataSetToTrain.getMatches();
		
		Separation sep = new Separation();
		for(Match match : matches){
			Record[] records = match.getRecords();
			
			float np1 = records[0].getFloatValue("NPLANS");
			float mp1 = records[0].getFloatValue("MINPRICE");
			float Mp1 = records[0].getFloatValue("MAXPRICE");
			float ap1 = records[0].getFloatValue("AVGPRICE");
			float np2 = records[1].getFloatValue("NPLANS");
			float mp2 = records[1].getFloatValue("MINPRICE");
			float Mp2 = records[1].getFloatValue("MAXPRICE");
			float ap2 = records[1].getFloatValue("AVGPRICE");
			float d = this.distance(np1, mp1, Mp1, ap1, np2, mp2, Mp2, ap2);
			sep.addValue(d, match.isMatching());
			
			if(DEBUG){
				System.out.println("Train, " + this.getClass().getSimpleName() + ", " + match.isMatching() + ", " + d + ", " + records[0].getStringValue("ADDRESS") + ", " + records[1].getStringValue("ADDRESS"));
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
		float np1 = record1.getFloatValue("NPLANS");
		float mp1 = record1.getFloatValue("MINPRICE");
		float Mp1 = record1.getFloatValue("MAXPRICE");
		float ap1 = record1.getFloatValue("AVGPRICE");
		float np2 = record2.getFloatValue("NPLANS");
		float mp2 = record2.getFloatValue("MINPRICE");
		float Mp2 = record2.getFloatValue("MAXPRICE");
		float ap2 = record2.getFloatValue("AVGPRICE");
		float d = this.distance(np1, mp1, Mp1, ap1, np2, mp2, Mp2, ap2);

		float s = (d - this._threshold) / this._normalizer;
		
		return new MatchWithLikelihood(record1, record2, this.getClass(),(s > 0), s);
	}

	public float distance(
			float numPlans1, float minPrice1, float maxPrice1, float avgPrice1,
			float numPlans2, float minPrice2, float maxPrice2, float avgPrice2)
	{
		if(numPlans1 == 0 || numPlans2 == 0){
			return 0;
		}
		float r1 = numPlans1 / numPlans2;
		float r2 = minPrice1 / minPrice2;
		float r3 = maxPrice1 / maxPrice2;
		float r4 = avgPrice1 / avgPrice2;

		if(r1 > 1){
			r1 = 1 / r1;
		}
		float r = r1;
		
		if(USE_PRICES){
			if(r2 > 1){
				r2 = 1 / r2;
			}
			if(r2 < r){
				r = r2;
			}
			if(r3 > 1){
				r3 = 1 / r3;
			}
			if(r3 < r){
				r = r3;
			}
			if(r4 > 1){
				r4 = 1 / r4;
			}
			if(r4 < r){
				r = r4;
			}
		}
		
		return (r > 1) ? (1 / r) : r;
	}
}
