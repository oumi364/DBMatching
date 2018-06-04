package jp.jobdirect.dbmatching.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;

public class SudachiDictionary {

	private static SudachiDictionary sudachiDic = new SudachiDictionary();

	private Dictionary dictionary = null;

	private SudachiDictionary() {
		try {
			String strSudachiPath = System.getProperty("user.dir");

			dictionary = new DictionaryFactory().create(strSudachiPath,
					Files.lines(Paths.get(Paths.get(strSudachiPath).resolve("sudachi.json").toString()))
							.collect(Collectors.joining()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static SudachiDictionary getInstance() {
		return sudachiDic;
	}

	public Dictionary getDictionary() {
		return dictionary;
	}
}
