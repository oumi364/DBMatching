package jp.jobdirect.dbmatching.app;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class PhoneNumberClassifier extends AbstractClassifier implements WeakClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = -2380528390463935951L;

	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;

	private float _threshold = 0f;
	private float _normalizer = 0f;

//	private static boolean USE_DIFF_DELTA = true;
//	private static boolean USE_ADDRESS_PART_REGEX = true;
	private final static String ZERO = "0";
	private final static String NULL = "null";

	public PhoneNumberClassifier(){
	}

	@Override
	public void train(DataSet dataSetToTrain) {
		Collection<Match> matches = dataSetToTrain.getMatches();

		Separation sep = new Separation();
		for(Match match : matches){
			Record[] records = match.getRecords();
			if(!hasPhoneNumBoth(records[0], records[1])) {
				continue;
			}

			// 電話番号がある場合のみ比較
			 float	d = this.distance(records[0], records[1]);
			sep.addValue(d, match.isMatching());

			if(DEBUG){
				System.out.println("Train, " + this.getClass().getSimpleName() + ", " + match.isMatching() + ", " + d + ", " + records[0].getStringValue("PHONE1") + "/" + records[0].getStringValue("PHONE2") + "/" + records[0].getStringValue("PHONE3") + ", " + records[1].getStringValue("PHONE1") + "/" + records[1].getStringValue("PHONE2") + "/" + records[1].getStringValue("PHONE3"));
			}
		}

		this._threshold = sep.getThreshold();
		this._normalizer = sep.getNormalizer();
		if(DEBUG){
			System.out.println(this.getClass().getSimpleName() + ", norm=" + this._normalizer + ", threshold=" + this._threshold);
		}
	}

	// 電話番号ありなし判定。両方ある場合のみtrue
	private boolean hasPhoneNumBoth(Record record1, Record record2) {
		// PHONE1しか使用していないため、PHONE1のみ比較する
		return isNotZeroNull(record1.getStringValue("PHONE1")) && isNotZeroNull(record2.getStringValue("PHONE1"));
	}

	private boolean isNotZeroNull(String phoneNum) {
		return !(ZERO.equals(phoneNum) || StringUtils.isEmpty(phoneNum));
	}

	@Override
	public Match classify(Record record1, Record record2) {

		float d = 0f;
		if(!hasPhoneNumBoth(record1, record2)) {
			d = 0f;
		} else {
			d = this.distance(record1, record2);
		}
		float s = (d - this._threshold) / this._normalizer;

		return new MatchWithLikelihood(record1, record2, this.getClass(),(s > 0), s);
	}

	public float distance(Record record1, Record record2) {
		String phone1[] = new String[]{ record1.getStringValue("PHONE1"), record1.getStringValue("PHONE2"), record1.getStringValue("PHONE3") };
		String phone2[] = new String[]{ record2.getStringValue("PHONE1"), record2.getStringValue("PHONE2"), record2.getStringValue("PHONE3") };

		for(int i = 0; i < phone1.length; i++){
			if(phone1[i] == null || phone1[i].equals("") || phone1[i].matches("^9+$")){
				continue;
			}
			for(int j = 0; j < phone2.length; j++){
				if(phone2[j] == null || phone2[j].equals("") || phone2[j].matches("^9+$")){
					continue;
				}

				if(phone1[i].equals(phone2[j])){
					return 1f;
				}
			}
		}

		return 0f;
	}
}
