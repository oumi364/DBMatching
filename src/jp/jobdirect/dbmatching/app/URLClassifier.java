package jp.jobdirect.dbmatching.app;

import java.util.Collection;

import org.apache.commons.lang3.StringEscapeUtils;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class URLClassifier extends AbstractClassifier implements WeakClassifier {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7282675317953212146L;
	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;
//	private static LevensteinDistance l_algo = new LevensteinDistance();
	
	private float _threshold = 0f;
	private float _normalizer = 0f;

	@Override
	public void train(DataSet dataSetToTrain) {
		Collection<Match> matches = dataSetToTrain.getMatches();
		
		Separation sep = new Separation();
		for(Match match : matches){
			Record[] records = match.getRecords();
			float d = this.distance(records[0].getStringValue("URL"), records[1].getStringValue("URL"));
			
			sep.addValue(d, match.isMatching());

			if(DEBUG){
				System.out.println("Train, " + this.getClass().getSimpleName() + ", " + match.isMatching() + ", " + d + ", " + records[0].getStringValue("URL") + ", " + records[1].getStringValue("URL"));
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
		String name1 = record1.getStringValue("URL");
		String name2 = record2.getStringValue("URL");
		
		float d = this.distance(name1, name2);
		float s = (d - this._threshold) / this._normalizer;
//		System.out.println("HotelName: " + (s > 0) + ", d=" + d + ", s=" + s + ": " + name1 + ", " + name2);
		
		return new MatchWithLikelihood(record1, record2, this.getClass(), (s > 0), s);
	}
	
	// com.oki.travelSearchAndOutPut#serarchLv1() に相当
	public float distance(String name1, String name2){

		// 宿のテキストマイニング
		if (name1.length() == 0) {
			return 0f;
		}else{
			name1 = StringEscapeUtils.unescapeHtml4(name1);
		}
		if (name2.length() == 0) {
			return 0f;
		}else{
			name2 = StringEscapeUtils.unescapeHtml4(name2);
		}
		
		// 末尾スラッシュ削除
		name1 = name1.replaceAll("/$",  "");
		name2 = name2.replaceAll("/$",  "");

		// スキーマ削除
		name1 = name1.replaceAll("^https?:", "");
		name2 = name2.replaceAll("^https?:", "");
		
		return name1.equals(name2) ? 1f : 0f;
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
