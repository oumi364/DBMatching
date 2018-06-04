package jp.jobdirect.dbmatching.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReadCategoryVec {

	private static ReadCategoryVec readCategoryVec = new ReadCategoryVec();

	List<String[]> vecList = null;

	private ReadCategoryVec() {
		vecList = readCsv(new File("data/CategoryVec.csv"));
	}

	private static List<String[]> readCsv(File f) {
		List<String[]> list = new ArrayList<String[]>();
		try {
			FileInputStream s = new FileInputStream(f);
			InputStreamReader r = new InputStreamReader(s, "utf-8");
			BufferedReader br = new BufferedReader(r);
			String line;
			while ((line = br.readLine()) != null) {
				line = line.substring(0, line.length() - 1);
				String[] ary = line.split(",");
				list.add(ary);
			}
			br.close();
			r.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static ReadCategoryVec getReadCategoryVec() {
		return readCategoryVec;
	}

	public List<String[]> getVecList() {
		return vecList;
	}
}
