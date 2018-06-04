package jp.jobdirect.dbmatching.app;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.ibm.icu.text.Transliterator;

public class SanitizeUtil {

	private static Transliterator oTransliteratorForZen2Han = Transliterator.getInstance("Halfwidth-Fullwidth");


	public static String  sanitizeUniformity(String targetString) {
		targetString = targetString.replaceAll("(\\(株\\)|（株）|\\(有\\)|（有）|\\(社\\)|（社）|（財）)", "");
		targetString = targetString.replaceAll("( |　|・|）|（|\\(|\\)|「|」|-|／|\\&|＆|～|\\?|\\.|？|'|、|【|】|/|の)", "");
		targetString = targetString.replaceAll("(‐|！|－|’|々|”|，|!|『|』|［|］|．|,|･|：|★|＋|〔|〕|:|､|\\+|ぁ|ぃ|ぅ|ぇ|ぉ|ェ|｢)", "");
		targetString = targetString.replaceAll("(―|“|♪|。|｣|\\]|~|☆|＜|＞|<|>|@|\\[|※|＊|×|∞|○|＠|#|\\*|_|°|〈|〉|《|》)", "");
		targetString = oTransliteratorForZen2Han.transliterate(targetString).toUpperCase();
		return targetString;
	}

	public static String[] sanitizeName(String targetStr1, String targetStr2) {

		// 以下70位まで
		//String[] regixList = {"^.*?",".*"};
		//String[] regixTrimList = {"^(.*?)","(.*)"};
		//String dollar = "$1$2";
		//List<String> trimWordList = Arrays.asList("レンタリース","レンタカー","カントリー","キャンプ場",
		//		"ギャラリー","センター","美術館","記念館","資料館","クラブ","ゴルフ","トヨタ","パーク",
		//		"倶楽部","博物館","総合","自然","観光","駅前","高原","八幡","稲荷","歴史","浴場","海岸",
		//		"海水","温泉","公園","古墳","展望","工房","案内","神社","と","山","島","川","店",
		//		"所","旧","村","桜","森","湖","湯","滝","碑","立","跡","道","里","院","館","駅",
		//		"台","園","城","場","大","宮","家","寺");

		// trimWordListの単語が両方に合ったら取り除く

		//for(String trimWord: trimWordList) {
		//	if (targetStr1.matches(regixList[0]+trimWord + regixList[1])
		//			&& targetStr2.matches(regixList[0]+trimWord + regixList[1])) {
		//		targetStr1 = targetStr1.replaceAll(regixTrimList[0]+trimWord + regixTrimList[1], dollar);
		//		targetStr2 = targetStr2.replaceAll(regixTrimList[0]+trimWord + regixTrimList[1], dollar);
		//	}
		//}

		// 例外 数字+丁目、数字+号⇒数字のみ に変換
		if(targetStr1.matches("^.*?[0-9０-９一-九]丁目.*") && targetStr2.matches("^.*?[0-9０-９一-九]丁目.*")) {
			targetStr1 = targetStr1.replaceAll("^(.*?[0-9０-９一-九])丁目(.*)", "$1$2");
			targetStr2 = targetStr2.replaceAll("^(.*?[0-9０-９一-九])丁目(.*)", "$1$2");
		}
		if (targetStr1.matches("^.*?[0-9０-９一-九]号.*") && targetStr2.matches("^.*?[0-9０-９一-九]号.*")) {
			targetStr1 = targetStr1.replaceAll("^(.*?[0-9０-９一-九])号(.*)", "$1$2");
			targetStr2 = targetStr2.replaceAll("^(.*?[0-9０-９一-九])号(.*)", "$1$2");
		}

		String[] returnList = {targetStr1, targetStr2};

		return returnList;
	}

	public static String[] sanitizeName2(String targetStr1, String targetStr2) {

		// 以下70位まで
		String[] regixList = {"^.*?",".*"};
		String[] regixTrimList = {"^(.*?)","(.*)"};
		String dollar = "$1$2";
		List<String> trimWordList = Arrays.asList("レンタリース","レンタカー","カントリー","キャンプ場",
				"ギャラリー","センター","美術館","記念館","資料館","クラブ","ゴルフ","トヨタ","パーク",
				"倶楽部","博物館","総合","自然","観光","駅前","高原","八幡","稲荷","歴史","浴場","海岸",
				"海水","温泉","公園","古墳","展望","工房","案内","神社","と","山","島","川","店",
				"所","旧","村","桜","森","湖","湯","滝","碑","立","跡","道","里","院","館","駅",
				"台","園","城","場","大","宮","家","寺");

		// trimWordListの単語が両方に合ったら取り除く

		for(String trimWord: trimWordList) {
			if (targetStr1.matches(regixList[0]+trimWord + regixList[1])
					&& targetStr2.matches(regixList[0]+trimWord + regixList[1])) {
				targetStr1 = targetStr1.replaceAll(regixTrimList[0]+trimWord + regixTrimList[1], dollar);
				targetStr2 = targetStr2.replaceAll(regixTrimList[0]+trimWord + regixTrimList[1], dollar);
			}
		}

		// 例外 数字+丁目、数字+号⇒数字のみ に変換
		if(targetStr1.matches("^.*?[0-9０-９一-九]丁目.*") && targetStr2.matches("^.*?[0-9０-９一-九]丁目.*")) {
			targetStr1 = targetStr1.replaceAll("^(.*?[0-9０-９一-九])丁目(.*)", "$1$2");
			targetStr2 = targetStr2.replaceAll("^(.*?[0-9０-９一-九])丁目(.*)", "$1$2");
		}
		if (targetStr1.matches("^.*?[0-9０-９一-九]号.*") && targetStr2.matches("^.*?[0-9０-９一-九]号.*")) {
			targetStr1 = targetStr1.replaceAll("^(.*?[0-9０-９一-九])号(.*)", "$1$2");
			targetStr2 = targetStr2.replaceAll("^(.*?[0-9０-９一-九])号(.*)", "$1$2");
		}

		String[] returnList = {targetStr1, targetStr2};

		return returnList;
	}

	public static List< List<String>> sanitizeNameWordList(List<String> strList1, List<String> strList2) {
		List<String> trimWordList = Arrays.asList("レンタリース","レンタカー","カントリー","キャンプ場",
				"ギャラリー","センター","美術館","記念館","資料館","クラブ","ゴルフ","トヨタ","パーク",
				"倶楽部","博物館","総合","自然","観光","駅前","高原","八幡","稲荷","歴史","浴場","海岸",
				"海水","温泉","公園","古墳","展望","工房","案内","神社","と","山","島","川","店",
				"所","旧","村","桜","森","湖","湯","滝","碑","立","跡","道","里","院","館","駅",
				"台","園","城","場","大","宮","家","寺");

		for(String trimWord: trimWordList) {
			if (strList1.contains(trimWord) && strList2.contains(trimWord)) {
				strList1.removeIf(item1 -> item1.equals(trimWord));
				strList2.removeIf(item1 -> item1.equals(trimWord));
			}
		}

		List< List<String>> strlists = Arrays.asList(strList1, strList2);

		return strlists;
	}

	public static String sanitizeNameHotel(String targetStr) {
		// 宿のテキストマイニング
		targetStr = StringEscapeUtils.unescapeHtml4(targetStr);
		targetStr = targetStr.replaceAll("(旅館|ホテル|ビジネス|民宿|＞|＜|）|（|>|<|\\)|\\(| |　)", "");
		targetStr = oTransliteratorForZen2Han.transliterate(targetStr).toUpperCase();
		return targetStr;
	}

}
