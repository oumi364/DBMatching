package jp.jobdirect.dbmatching.app;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.icu.text.Transliterator;
import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class HotelNameInclusionClassifier extends AbstractClassifier implements WeakClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = -6724315735779212792L;
	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;
	// private static LevensteinDistance l_algo = new LevensteinDistance();
	private static Transliterator oTransliteratorForZen2Han = Transliterator.getInstance("Halfwidth-Fullwidth");

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
			// float d2 = this.distance(records[1].getStringValue("NAME"),
			// records[0].getStringValue("NAME"));
			// float d = (d1 < d2) ? d1 : d2;

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
		// float d2 = this.distance(name2, name1);
		// float d = (d1 < d2) ? d1 : d2;
		float s = (d - this._threshold) / this._normalizer;
		// System.out.println("d=" + d + ", s=" + s);

		//System.out.println("_threshold:" + this._threshold + " _normalizer:" + this._normalizer);
		//System.out.println("s:" + s + " d:" + d);

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
		dst = SanitizeUtil.sanitizeUniformity(dst);

		this.sanitizedStringCache.put(src, dst);
		return dst;
	}

	@SuppressWarnings("resource")
	public float distance(String name1, String name2) {

		name1 = this.sanitize(name1);
		name2 = this.sanitize(name2);

		// 号、丁目 削除処理
		String[] snitizedStrList = SanitizeUtil.sanitizeName(name1, name2);
		name1 = snitizedStrList[0];
		name2 = snitizedStrList[1];

		float dInclusionOfNameNormalizedT2M = 0.0f;
		float dInclusionOfNameNormalizedM2T = 0.0f;

		// System.out.println(name1 + "<=>" + name2 + ", ");
		if (name1.length() == 0 || name2.length() == 0) {
			// カテゴリがない場合は一致率測定を実施しない。
			return 0f;
		}

		// ユーザ辞書を読み込む。
		// TODO 読み込みは一度で良いと思われる。
		String strSudachiSettings = null;
		String strSudachiPath = System.getProperty("user.dir"); // TODO
																// jsonファイルの配置場所を検討する。
		String strSudachiSettingsPath = Paths.get(strSudachiPath).resolve("sudachi.json").toString();
		try {
			strSudachiSettings = Files.lines(Paths.get(strSudachiSettingsPath))
					.collect(java.util.stream.Collectors.joining());
		} catch (Exception ex) {
			if (DEBUG) {
				System.out.println(ex);
			}
			return 0f;
		}

		try (Dictionary oDictionary = new DictionaryFactory().create(strSudachiPath, strSudachiSettings)) { // TODO
			// シングルトンにする必要があるか検討する。
			Tokenizer oTokenizer = oDictionary.create();
			List<Morpheme> oMorphemeList1 = oTokenizer.tokenize(Tokenizer.SplitMode.B, name1);
			List<Morpheme> oMorphemeList2 = oTokenizer.tokenize(Tokenizer.SplitMode.B, name2);

			// 必要な情報だけ抽出する。
			//List<String> addressList1 = new ArrayList<String>();
			//for (Morpheme morpheme : oMorphemeList1) {
			//	addressList1.add(morpheme.surface());
			//}
			//List<String> addressList2 = new ArrayList<String>();
			//for (Morpheme morpheme : oMorphemeList2) {
			//	addressList2.add(morpheme.surface());
			//}

			//System.out.println("分解1:" + ArrayUtils.toString(addressList1) + " 分解2:" + ArrayUtils.toString(addressList2));

			//List<List<String>> addressLists = SanitizeUtil.sanitizeNameWordList(addressList1, addressList2);
			//addressList1 = addressLists.get(0);
			//addressList2 = addressLists.get(1);

			//System.out.println("分解1:" + ArrayUtils.toString(addressList1) + " 分解2:" + ArrayUtils.toString(addressList2));


			// 文字列のクレンジング
			List<String> nameList1 = new ArrayList<String>();
			for (Morpheme morpheme : oMorphemeList1) {
				if(StringUtils.isNotEmpty(morpheme.surface())) nameList1.add(morpheme.surface());
			}
			List<String> nameList2 = new ArrayList<String>();
			for (Morpheme morpheme : oMorphemeList2) {
				if(StringUtils.isNotEmpty(morpheme.surface())) nameList2.add(morpheme.surface());
			}

			//System.out.println("分解1:" + ArrayUtils.toString(nameList1) + " 分解2:" + ArrayUtils.toString(nameList2));

			List< List<String>> strlists = SanitizeUtil.sanitizeNameWordList(nameList1, nameList2);

			List<String> NameList1 = strlists.get(0);
			List<String> NameList2 = strlists.get(1);

			//System.out.println("分解1:" + ArrayUtils.toString(NameList1) + " 分解2:" + ArrayUtils.toString(NameList2));

//			if (DEBUG) {
//				System.out.println(name1);
//				for (String address : addressList1) {
//					System.out.println(address);
//				}
//				System.out.println();
//				System.out.println(name2);
//				for (String address : addressList2) {
//					System.out.println(address);
//				}
//				System.out.println();
//			}

			// 一致率を計算する。
			dInclusionOfNameNormalizedT2M = calculateInclusionRatio(NameList1, NameList2);
			dInclusionOfNameNormalizedM2T = calculateInclusionRatio(NameList2, NameList1);

			//System.out.println("一致率1:" + 			dInclusionOfNameNormalizedT2M + " 一致率2:" + dInclusionOfNameNormalizedM2T);

		} catch (Exception e) {
			if (DEBUG) {
				System.out.println(e);
			}
			return 0f;
		}

		if (dInclusionOfNameNormalizedT2M >= dInclusionOfNameNormalizedM2T) {
			return dInclusionOfNameNormalizedT2M;
		} else {
			return dInclusionOfNameNormalizedM2T;
		}
	}

	public static float calculateInclusionRatio(List<String> srcList, List<String> dstList) {
		try {
			float dInclusionCount = 0.0f;

			// 比較する。
			for (String address : srcList) {
				if (dstList.contains(address)) {
					dInclusionCount++;
				}
			}

//			System.out.println("IncCnt=" + dInclusionCount);
//			System.out.println("HashSize=" + srcList.size());
			return dInclusionCount / (float) srcList.size();
		} catch (Exception ex) {
			return 0.0f;
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
