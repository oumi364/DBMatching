package jp.jobdirect.dbmatching.app;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;

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

public class HotelNameDifferenceClassifier extends AbstractClassifier implements WeakClassifier {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7700059755394745114L;
	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;
	private static Transliterator oTransliteratorForHanToZen = Transliterator.getInstance("Halfwidth-Fullwidth");
	
	private float _threshold = 0f;
	private float _normalizer = 0f;
	
	private static float STOP_COEFF = 20.0f;
	private static Set<String> stopWords = new HashSet<String>();
	{
		stopWords.add("新");
		stopWords.add("別館");
		stopWords.add("新館");
		stopWords.add("別邸");
		stopWords.add("本館");
		stopWords.add("タワー");
		stopWords.add("アネックス");
		stopWords.add("１");
		stopWords.add("２");
		stopWords.add("３");
	}
	
	private static boolean CHECK_COMMON_TERMS = true;
	private static Set<String> commonTerms = new HashSet<String>();
	{
		commonTerms.add("ホテル");
		commonTerms.add("ペンション");
		commonTerms.add("旅館");
		commonTerms.add("荘");
		commonTerms.add("イン");
		commonTerms.add("民宿");
		commonTerms.add("ビジネス");
	}
	
	public HotelNameDifferenceClassifier()
	{
		System.out.println(new StringBuilder()
				.append(this.getClass().getSimpleName()).append(": ")
				.append("checkCommonTerms: ").append(CHECK_COMMON_TERMS)
				.toString());
	}

	@Override
	public void train(DataSet dataSetToTrain) {
		Collection<Match> matches = dataSetToTrain.getMatches();
		
		Separation sep = new Separation();
		for(Match match : matches){
			Record[] records = match.getRecords();
			float d = this.distance(records[0].getStringValue("NAME"), records[1].getStringValue("NAME"));
			
			sep.addValue(d, match.isMatching());

			if(DEBUG){
				System.out.println("Train, " + this.getClass().getSimpleName() + ", " + match.isMatching() + ", " + d + ", " + records[0].getStringValue("NAME") + ", " + records[1].getStringValue("NAME"));
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
		String name1 = record1.getStringValue("NAME");
		String name2 = record2.getStringValue("NAME");
		
		float d = this.distance(name1, name2);
		float s = (d - this._threshold) / this._normalizer;
//		System.out.println("HotelName: " + (s > 0) + ", d=" + d + ", s=" + s + ": " + name1 + ", " + name2);
		
		return new MatchWithLikelihood(record1, record2, this.getClass(), (s > 0), s);
	}
	
	private transient HashMap<String, String> sanitizedStringCache = null;
	private String sanitize(String src){
		if(this.sanitizedStringCache == null){
			this.sanitizedStringCache = new HashMap<String, String>();
		}
		String cachedString = this.sanitizedStringCache.get(src);
		if(cachedString != null){
			return cachedString;
		}
		
		String dst = src;
		// 宿のテキストマイニング
		dst = StringEscapeUtils.unescapeHtml4(dst);

		// サニタイズ（頻出語削除）を行わない
		if(!CHECK_COMMON_TERMS){
			dst = dst.replaceAll("(旅館|ホテル|ビジネス|民宿|＞|＜|）|（|>|<|\\)|\\(| |　)", "");
		}

		dst = oTransliteratorForHanToZen.transliterate(dst).toUpperCase();

		if(CHECK_COMMON_TERMS){
			for(String s : commonTerms){
				dst = dst.replaceAll(s, "");
			}
		}
		
		this.sanitizedStringCache.put(src, dst);
		return dst;
	}

	// com.oki.travelSearchAndOutPut#serarchLv1() に相当
	public float distance(String name1, String name2){
		name1 = this.sanitize(name1);
		name2 = this.sanitize(name2);
		
//		System.out.println(name1 + "<=>" + name2 + ", ");
		if (name1.length() == 0 || name2.length() == 0) {
			// ホテル名がない場合は一致率測定を実施しない。
			return 0f;
		} else {
			// ジャロ・ウィンクラー距離(Jaro-WinklerDiatance)
			// ret = j_algo.getDistance(sCompany, dCompany);
			// レーベンシュタイン距離(LevenshteinDiatance)
//			System.out.println("HotelNameDistance: " + name1 + ", " + name2);
			
			Patch patch = DiffUtils.diff(Chars.asList(name1.toCharArray()), Chars.asList(name2.toCharArray()));
//			System.out.println(name1 + ":" + name2);
			int len1 = name1.length();
			int len2 = name2.length();
			int clen = 0;
			int mlen = (len1 > len2) ? len1 : len2;
			int dlen = 0;
			for(Delta d : patch.getDeltas()){
				String from = String.valueOf(ArrayUtils.toPrimitive(d.getOriginal().getLines().toArray(new Character[0])));
				String to   = String.valueOf(ArrayUtils.toPrimitive(d.getRevised().getLines().toArray(new Character[0])));
				switch(d.getType()){
				case CHANGE:
//					System.out.println(from + " -> " + to);
					int flen = from.length();
					int tlen = to.length();
					int ftlen = (flen < tlen) ? flen : tlen;
					if(stopWords.contains(from) || stopWords.contains(to)){
						clen += ftlen * STOP_COEFF;
					}else{
						clen += ftlen;
					}
					break;
				case DELETE:
//					System.out.println(from + " -> xxx");
					if(stopWords.contains(from)){
						dlen += from.length() * STOP_COEFF;
					}else{
						dlen += from.length();
					}
					break;
				case INSERT:
//					System.out.println("xxx -> " + to);
					if(stopWords.contains(to)){
						dlen += to.length() * STOP_COEFF;
					}else{
						dlen += to.length();
					}
					break;
				}
			}
			
			float score = 1 - 1f * (clen + dlen) / mlen;
			return score < 0 ? 0 : score;
		}
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
