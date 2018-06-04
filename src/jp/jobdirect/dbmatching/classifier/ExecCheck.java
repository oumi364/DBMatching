package jp.jobdirect.dbmatching.classifier;

import java.util.HashSet;


public class ExecCheck {

	static HashSet<String> ExecIdList = new HashSet<String>();
	//static List<String> ExecIdList = new ArrayList<String>();
	//static String ExecIdList[];

	public static boolean ExecJudge(String id1, String id2) {


		if(id1.equals(id2)) {
			return false;
		}

		StringBuilder buf_f = new StringBuilder();
		buf_f.append(id1);
		buf_f.append(id2);
		String str_f = buf_f.toString();

		StringBuilder buf_b = new StringBuilder();
		buf_b.append(id2);
		buf_b.append(id1);
		String str_b = buf_b.toString();


		if( ExecIdList.contains(str_b)) {
			return false;
		}

		ExecIdList.add(str_f);

		return true;

	}

}
