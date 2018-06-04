package jp.jobdirect.dbmatching.app;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.search.spell.LevensteinDistance;

import com.google.common.primitives.Chars;
import com.ibm.icu.text.Transliterator;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class AddressDistanceClassifier extends AbstractClassifier implements WeakClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = -6356318567209720640L;
	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;
	private static LevensteinDistance l_algo = new LevensteinDistance();
	private static Transliterator oTransliteratorForZen2Han = Transliterator.getInstance("Halfwidth-Fullwidth");

	private float _threshold = 0f;
	private float _normalizer = 0f;

	private static Set<String> stopWords = new HashSet<String>();
	private static final float STOP_COEFF = 1f;
	private static final boolean REMOVE_ZIP = true; // 郵便番号を消してから比較
	private static final boolean BANCHI_INCLUDES_BUILDING = true; // 番地以降をマッチする際に、ビル名を含めてマッチさせる
	private static final int LIMIT_ADDRESS_LENGTH = 0; // 住所として扱う文字列の最大長。当初20
	private static final boolean SPLIT_ADDRESS_PARTS = true; // 住所を都道府県・市区町村・それ以外、に分けてから処理をする

	private static Pattern REGEX_PATTERN_PREF = Pattern.compile("^(京都府|[^都道府県]*?[都道府県])(.*)$");
	private static Pattern REGEX_PATTERN_CITY = Pattern.compile(
			"^ *(((札幌市|仙台市|さいたま市|千葉市|横浜市|川崎市|相模原市|新潟市|静岡市|浜松市|名古屋市|京都市|大阪市|堺市|神戸市|岡山市|広島市|北九州市|福岡市|熊本市)[ 　]*([^区]{1,5}区)?)"
					+ "|" + "(大町市|田村市|柴田郡村田町|神崎郡市川町|東村山市|武蔵村山市|羽村市|町田市|村上市|村山市|市原市|市川市|[^市区町村]*?[市区町村])(.$))$");

	static List<SimpleEntry<String, String>> miningMap = Arrays.asList(
			new SimpleEntry<String, String>("(大字|字|〒\\d{7})", ""),
			new SimpleEntry<String, String>("(の|丁目|番地|番|号|−|－|－|ー|—|‐|-|―)", "－"),
			new SimpleEntry<String, String>("ヶ", "ケ"));

	static List<SimpleEntry<String, String>> miningMapNum = Arrays.asList(new SimpleEntry<String, String>("一", "１"),
			new SimpleEntry<String, String>("二", "２"), new SimpleEntry<String, String>("三", "３"),
			new SimpleEntry<String, String>("四", "４"), new SimpleEntry<String, String>("五", "５"),
			new SimpleEntry<String, String>("六", "６"), new SimpleEntry<String, String>("七", "７"),
			new SimpleEntry<String, String>("八", "８"), new SimpleEntry<String, String>("九", "９"),
			new SimpleEntry<String, String>("十", "０"));

	private static boolean USE_DIFF_DELTA = true;
	private static boolean USE_ADDRESS_PART_REGEX = true;

	public AddressDistanceClassifier() {
		System.out.println(
				new StringBuffer().append(this.getClass().getSimpleName()).append(": ").append("サニタイズによる全角文字消滅バグ修正版: ")
						.append("useDiffDelta=").append(USE_DIFF_DELTA).append("useAddressPartRegex=")
						.append(USE_ADDRESS_PART_REGEX).append("remove-zip=").append(REMOVE_ZIP));
	}

	@Override
	public void train(DataSet dataSetToTrain) {
		Collection<Match> matches = dataSetToTrain.getMatches();

		Separation sep = new Separation();
		String record0 = null;
		String record1 = null;
		for (Match match : matches) {
			Record[] records = match.getRecords();
			// System.out.println("name1:" + records[0].getStringValue("NAME")
			// + " name2:" + records[1].getStringValue("NAME"));

			// ADDRESSの先頭からCITYを削る。
			record0 = records[0].getStringValue("ADDRESS").replaceFirst(records[0].getStringValue("CITY"), "");
			record1 = records[1].getStringValue("ADDRESS").replaceFirst(records[1].getStringValue("CITY"), "");

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
		// ADDRESSの先頭からCITYを削る。
		String name1 = record1.getStringValue("ADDRESS").replaceFirst(record1.getStringValue("CITY"), "");
		String name2 = record2.getStringValue("ADDRESS").replaceFirst(record2.getStringValue("CITY"), "");

		float d = this.distance(name1, name2);
		float s = (d - this._threshold) / this._normalizer;

		return new MatchWithLikelihood(record1, record2, this.getClass(), (s > 0), s);
	}

	// private HashMap<String, String[]> splitAddressCache = new HashMap<String,
	// String[]>();
	private transient HashMap<String, String> sanitizedAddressCache = null;

	private String sanitize(String name) {
		if (this.sanitizedAddressCache == null) {
			this.sanitizedAddressCache = new HashMap<String, String>();
		}
		String cachedName = this.sanitizedAddressCache.get(name);
		if (cachedName != null) {
			return cachedName;
		}

		// 住所のテキストマイニング
		String sanitizedName = name;
		if (sanitizedName.length() != 0) {
			sanitizedName = StringEscapeUtils.unescapeHtml4(sanitizedName);
		}

		if (sanitizedName.length() != 0) {
			sanitizedName = oTransliteratorForZen2Han.transliterate(sanitizedName).toUpperCase();
			sanitizedName = sanitizedName.replaceAll("　", "");
			sanitizedName = sanitizedName.replaceAll("，", "");
			// name1 = name1.replaceAll("[^\\x01-\\x7E]", "");
			if (LIMIT_ADDRESS_LENGTH > 0 && sanitizedName.length() > LIMIT_ADDRESS_LENGTH) {
				sanitizedName = sanitizedName.substring(0, LIMIT_ADDRESS_LENGTH);
			}
		}

		for (SimpleEntry<String, String> entry : AddressDistanceClassifier.miningMap) {
			String regexString = entry.getKey();
			String replString = entry.getValue();
			sanitizedName = sanitizedName.replaceAll(regexString, replString);
		} // 都道府県が無い住所に対応

		if (REMOVE_ZIP) {
			String n1 = sanitizedName.replaceAll("〒[０-９]*ー?[０-９]*", "");
			if (!n1.equals(sanitizedName)) {
				// System.out.println(name1 + " -> " + n1);
				sanitizedName = n1;
			}
		}

		this.sanitizedAddressCache.put(name, sanitizedName);
		return sanitizedName;
	}

	// com.oki.travelSearchAndOutPut#serarchLv2() に相当
	public float distance(String name1, String name2) {
		name1 = this.sanitize(name1);
		name2 = this.sanitize(name2);

		float minScore = Float.MAX_VALUE;
		if (SPLIT_ADDRESS_PARTS) {
			String[] a1 = splitAddress(name1);
			String[] a2 = splitAddress(name2);

			if (a1.length > 1 && a2.length > 1) {
				float s = l_algo.getDistance(a1[0], a2[0]);
				if (minScore > s) {
					minScore = s;
				}

				if (a1.length > 2 && a2.length > 2) {
					s = l_algo.getDistance(a1[1], a2[1]);
					if (minScore > s) {
						minScore = s;
					}

					name1 = a1[2];
					name2 = a2[2];

				} else {
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (int i = 1; i < a1.length; i++) {
						sb1.append(a1[i]);
					}
					for (int i = 1; i < a2.length; i++) {
						sb2.append(a2[i]);
					}
					name1 = sb1.toString();
					name2 = sb2.toString();
				}

			} else {
				// do nothing
			}

		}

		for (SimpleEntry<String, String> entry : AddressDistanceClassifier.miningMapNum) {
			String regexString = entry.getKey();
			String replString = entry.getValue();
			name1 = name1.replaceAll(regexString, replString);
			name2 = name2.replaceAll(regexString, replString);
		} // 都道府県が無い住所に対応

		// 語尾「ー」を取り除く
		name1 = name1.replaceAll("－$", "");
		name2 = name2.replaceAll("－$", "");

		// Levenstein Distance: 同一文字列で 1.0 を返す。
		float levensteinDistance = l_algo.getDistance(name1.trim(), name2.trim());

		if (USE_ADDRESS_PART_REGEX) {
			String pattern = BANCHI_INCLUDES_BUILDING ? "[０１２３４５６７８９ー]+.*$" : "[０１２３４５６７８９ー]*$";
			Pattern p1 = Pattern.compile(pattern);
			Matcher m1 = p1.matcher(name1);
			Matcher m2 = p1.matcher(name2);

			String n11 = name1, n12 = "";
			String n21 = name2, n22 = "";
			if (m1.find()) {
				n11 = name1.substring(0, m1.start());
				n12 = name1.substring(m1.start());
			}
			if (m2.find()) {
				n21 = name2.substring(0, m2.start());
				n22 = name2.substring(m2.start());
			}

			float l1 = l_algo.getDistance(n11, n21);
			float l2 = l_algo.getDistance(n12, n22);

			// System.out.println(new StringBuilder()
			// .append("distance: ").append(levensteinDistance).append(",
			// ").append(l1).append(", ").append(l2).append(" ")
			// .append("name1: ").append(name1).append(",
			// ").append(n11).append(", ").append(n12).append(" ")
			// .append("name2: ").append(name2).append(",
			// ").append(n21).append(", ").append(n22).append(" ")
			// .toString());

			levensteinDistance = (l1 < l2) ? l1 : l2;
			if (minScore > levensteinDistance) {
				minScore = levensteinDistance;
			}
		}

		if (USE_DIFF_DELTA) {
			Patch patch = DiffUtils.diff(Chars.asList(name1.toCharArray()), Chars.asList(name2.toCharArray()));
			// System.out.println(name1 + ":" + name2 + ": l-dist=" +
			// levensteinDistance);
			int len1 = name1.length();
			int len2 = name2.length();
			int clen = 0;
			int mlen = (len1 > len2) ? len1 : len2;
			int dlen = 0;
			for (Delta d : patch.getDeltas()) {
				String from = String
						.valueOf(ArrayUtils.toPrimitive(d.getOriginal().getLines().toArray(new Character[0])));
				String to = String.valueOf(ArrayUtils.toPrimitive(d.getRevised().getLines().toArray(new Character[0])));
				switch (d.getType()) {
				case CHANGE:
					// System.out.println(from + " -> " + to);
					int flen = from.length();
					int tlen = to.length();
					int ftlen = (flen < tlen) ? flen : tlen;
					if (stopWords.contains(from) || stopWords.contains(to)) {
						clen += ftlen * STOP_COEFF;
					} else {
						clen += ftlen;
					}
					break;
				case DELETE:
					// System.out.println(from + " -> xxx");
					if (stopWords.contains(from)) {
						dlen += from.length() * STOP_COEFF;
					} else {
						dlen += from.length();
					}
					break;
				case INSERT:
					// System.out.println("xxx -> " + to);
					if (stopWords.contains(to)) {
						dlen += to.length() * STOP_COEFF;
					} else {
						dlen += to.length();
					}
					break;
				}
			}

		}
		return minScore;
	}

	private static String[] splitAddress(String address) {
		Matcher prefMatcher = REGEX_PATTERN_PREF.matcher(address);

		String prefString = "";
		if (prefMatcher.find()) {
			prefString = prefMatcher.group(1);
			address = prefMatcher.group(2);
			if (address == null) {
				address = "";
			}
		} else {
			return new String[] { address };
		}

		Matcher cityMatcher = REGEX_PATTERN_CITY.matcher(address);

		String cityString = "";
		if (cityMatcher.find()) {
			cityString = cityMatcher.group(1);
			address = cityMatcher.group(2);
			if (address == null) {
				address = "";
			}
		} else {
			return new String[] { prefString, address };
		}

		return new String[] { prefString, cityString, address };
	}
}
