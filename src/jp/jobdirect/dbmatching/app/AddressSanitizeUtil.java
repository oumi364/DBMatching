package jp.jobdirect.dbmatching.app;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.ibm.icu.text.Transliterator;

public class AddressSanitizeUtil {

	private static Transliterator oTransliteratorForZen2Han = Transliterator.getInstance("Halfwidth-Fullwidth");

	private static final boolean REMOVE_ZIP = true; // 郵便番号を消してから比較
	private static final int LIMIT_ADDRESS_LENGTH = 0; // 住所として扱う文字列の最大長。当初20

	static List<SimpleEntry<String, String>> miningMap = Arrays.asList(
			new SimpleEntry<String, String>("(大字|字|〒\\d{7})", ""),
			new SimpleEntry<String, String>("(の|丁目|番地|番|号|−|－|－|ー|—|‐|-|―)", "－"),
			new SimpleEntry<String, String>("ヶ", "ケ"));

	static List<SimpleEntry<String, String>> miningMapNum = Arrays.asList(
			new SimpleEntry<String, String>("一", "１"),
			new SimpleEntry<String, String>("二", "２"),
			new SimpleEntry<String, String>("三", "３"),
			new SimpleEntry<String, String>("四", "４"),
			new SimpleEntry<String, String>("五", "５"),
			new SimpleEntry<String, String>("六", "６"),
			new SimpleEntry<String, String>("七", "７"),
			new SimpleEntry<String, String>("八", "８"),
			new SimpleEntry<String, String>("九", "９"),
			new SimpleEntry<String, String>("十", "０"));

	private static transient HashMap<String, String> sanitizedAddressCache = null;

	public static String sanitize(String name){
		if(sanitizedAddressCache == null){
			sanitizedAddressCache = new HashMap<String, String>();
		}
		String cachedName = sanitizedAddressCache.get(name);
		if(cachedName != null){
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
//			name1 = name1.replaceAll("[^\\x01-\\x7E]", "");
			if (LIMIT_ADDRESS_LENGTH > 0 && sanitizedName.length() > LIMIT_ADDRESS_LENGTH) {
				sanitizedName = sanitizedName.substring(0, LIMIT_ADDRESS_LENGTH);
			}
		}

		for(SimpleEntry<String, String> entry: AddressDistanceClassifier.miningMap){
			String regexString = entry.getKey();
			String replString  = entry.getValue();
			sanitizedName = sanitizedName.replaceAll(regexString, replString);
		} // 都道府県が無い住所に対応

		if(REMOVE_ZIP){
			String n1 = sanitizedName.replaceAll("〒[０-９]*－?[０-９]*", "");
			if(!n1.equals(sanitizedName)){
//				System.out.println(name1 + " -> " + n1);
				sanitizedName = n1;
			}
		}

		for(SimpleEntry<String, String> entry: AddressDistanceClassifier.miningMapNum){
			String regexString = entry.getKey();
			String replString  = entry.getValue();
			sanitizedName = sanitizedName.replaceAll(regexString, replString);
			sanitizedName = sanitizedName.replaceAll(regexString, replString);
		} // 都道府県が無い住所に対応

		// 語尾「ー」を取り除く
		sanitizedName = sanitizedName.replaceAll("－$", "");

		sanitizedAddressCache.put(name, sanitizedName);
		return sanitizedName;
	}

}
