package jp.jobdirect.dbmatching.app;

import java.util.Collection;
import java.util.HashMap;

import org.apache.lucene.search.spell.LevensteinDistance;

import com.ibm.icu.text.Transliterator;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class HotelNameDistanceClassifier extends AbstractClassifier implements WeakClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = 7017190432926540287L;
	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;
	private static LevensteinDistance l_algo = new LevensteinDistance();
	private static Transliterator oTransliteratorForZen2Han = Transliterator.getInstance("Fullwidth-Halfwidth");

	private float _threshold = 0f;
	private float _normalizer = 0f;

	@Override
	public void train(DataSet dataSetToTrain) {
		Collection<Match> matches = dataSetToTrain.getMatches();

		Separation sep = new Separation();
		String record0 = null;
		String record1 = null;
		for (Match match : matches) {
			Record[] records = match.getRecords();

			// NAMEの文末から特定文字を削る。
			record0 = records[0].getStringValue("NAME");
			record1 = records[1].getStringValue("NAME");

			float d = this.distance(record0, record1);

			sep.addValue(d, match.isMatching());

			if (DEBUG) {
				System.out.println("Train, " + this.getClass().getSimpleName() + ", " + match.isMatching() + ", " + d
						+ ", " + record0 + ", " + record1);
			}
		}

		this._threshold = sep.getThreshold();
		this._normalizer = sep.getNormalizer();
		if (DEBUG) {
			System.out.println(
					this.getClass().getSimpleName() + ", norm=" + this._normalizer + ", threshold=" + this._threshold);
		}
	}

	@Override
	public Match classify(Record record1, Record record2) {
		String name1 = record1.getStringValue("NAME");
		String name2 = record2.getStringValue("NAME");

		float d = this.distance(name1, name2);
		float s = (d - this._threshold) / this._normalizer;
		// System.out.println("HotelName: " + (s > 0) + ", d=" + d + ", s=" + s
		// + ": " + name1 + ", " + name2);

		return new MatchWithLikelihood(record1, record2, this.getClass(), (s > 0), s);
	}

	private transient HashMap<String, String> sanitizedStringCache = null;

	public String sanitize(String src) {
		if (this.sanitizedStringCache == null) {
			this.sanitizedStringCache = new HashMap<String, String>();
		}
		String cachedString = this.sanitizedStringCache.get(src);
		if (cachedString != null) {
			return cachedString;
		}

		String dst = src;

		// 宿のテキストマイニング
		//dst = SanitizeUtil.sanitizeNameHotel(dst);
		dst = SanitizeUtil.sanitizeUniformity(dst);

		this.sanitizedStringCache.put(src, dst);
		return dst;
	}

	// com.oki.travelSearchAndOutPut#serarchLv1() に相当
	public float distance(String name1, String name2) {

		// 寺、院、神社、公園、号、丁目、レンタ削除処理
		//String[] snitizedStrList = SanitizeUtil.sanitizeName(name1, name2);
		//name1 = snitizedStrList[0];
		//name2 = snitizedStrList[1];

		name1 = this.sanitize(name1);
		name2 = this.sanitize(name2);

		//System.out.println(name1 + "<=>" + name2 + ", ");

		// サニタイズ処理2
		String[] snitizedStrList = SanitizeUtil.sanitizeName2(name1, name2);
		name1 = snitizedStrList[0];
		name2 = snitizedStrList[1];

		//System.out.println(name1 + "<=>" + name2 + ", ");

		if (name1.length() == 0 || name2.length() == 0) {
			// ホテル名がない場合は一致率測定を実施しない。
			return 0f;
		} else {
			// ジャロ・ウィンクラー距離(Jaro-WinklerDiatance)
			// ret = j_algo.getDistance(sCompany, dCompany);
			// レーベンシュタイン距離(LevenshteinDiatance)
			// System.out.println("HotelNameDistance: " + name1 + ", " + name2);
			return l_algo.getDistance(name1, name2);
		}
	}

	@Override
	public String toString() {
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
