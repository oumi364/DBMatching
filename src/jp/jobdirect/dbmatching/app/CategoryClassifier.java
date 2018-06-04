package jp.jobdirect.dbmatching.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.spell.LevensteinDistance;

import jp.jobdirect.dbmatching.classifier.AbstractClassifier;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.WeakClassifier;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class CategoryClassifier extends AbstractClassifier implements WeakClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = 7486746343786395723L;
	private static boolean DEBUG = ClassifierApplication.DEBUG_WEAK_CLASSIFIER_SCORES;
	private static LevensteinDistance l_algo = new LevensteinDistance();

	private float _threshold = 0f;
	private float _normalizer = 0f;

	@Override
	public void train(DataSet dataSetToTrain) {
		Collection<Match> matches = dataSetToTrain.getMatches();

		Separation sep = new Separation();
		for(Match match : matches){
			Record[] records = match.getRecords();
			float d = this.distance(records[0].getStringValue("CAT"), records[1].getStringValue("CAT"));

			sep.addValue(d, match.isMatching());

			if(DEBUG){
				System.out.println("Train, " + this.getClass().getSimpleName() + ", " + match.isMatching() + ", " + d + ", " + records[0].getStringValue("CAT") + ", " + records[1].getStringValue("CAT"));
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
		String name1 = record1.getStringValue("CAT");
		String name2 = record2.getStringValue("CAT");

		float d = this.distance(name1, name2);
		float s = (d - this._threshold) / this._normalizer;

		return new MatchWithLikelihood(record1, record2, this.getClass(), (s > 0), s);
	}

	public float distance(String name1, String name2){

		List<String[]> vecList = ReadCategoryVec.getReadCategoryVec().getVecList();

		ArrayList<String[]> CenterVec1 = new ArrayList<String[]>();
		ArrayList<String[]> CenterVec2 = new ArrayList<String[]>();

//		List<double[]> CenterV1 = new ArrayList<double[]>();
		double[] CenterV1 = new double[200];
		double[] CenterV2 = new double[200];
//		Arrays.fill(CenterV1, 0d);
//		Arrays.fill(CenterV2, 0d);

		String[] Name1word = name1.split(",");
		String[] Name2word = name2.split(",");

		if (name1.length() == 0 || name2.length() == 0) {
			// カテゴリがない場合は一致率測定を実施しない。
			return 0f;
		}
		
		// 同じ文字列を省く

		// 各々の中心を求める
		// 各要素が含まれるベクトル配列を作る
		// Name1の中心ベクトルを求める -- CenterV1[]
		for(String word: Name1word){

			for(String[] vec: vecList){
			//	if(Arrays.asList(vec).contains(word)){
//				System.out.println("1st vec = " + Arrays.toString(vec));

//				System.out.println("word = " + word);
//				System.out.println("vec[0] = " + vec[0]);

				if(word.equals(vec[0])){

//					System.out.println("*********** Hit ! Hit! Hit! ***************");
//					System.out.println("word = " + word);
//					System.out.println("vec[0] = " + vec[0]);

					CenterVec1.add(vec);
					break;
				}
			}
		}
//		System.out.println("CenterVec1 = " + ArrayUtils.toString(CenterVec1));

		for(String[] vecL: CenterVec1){

//			System.out.println("CenterVec1 = " + ArrayUtils.toString(vecL));

			for(int i = 0; i < 200; i++){
				CenterV1[i] += Double.parseDouble(vecL[i+1]);
			}
		}
//		System.out.println("Before CenterV1 = " + Arrays.toString(CenterV1));

		for(int i = 0; i < 200; i++){
			CenterV1[i] = CenterV1[i]/(CenterV1.length);
		}
//		System.out.println("After CenterV1 = " + Arrays.toString(CenterV1));

		// Name2の中心ベクトルを求める -- CenterV2[]
		for(String word: Name2word){

			for(String[] vec: vecList){
			//	if(Arrays.asList(vec).contains(word)){
				if(word.equals(vec[0])){
					CenterVec2.add(vec);
					break;
				}
			}
		}

//		System.out.println("CenterVec2 = " + ArrayUtils.toString(CenterVec2));

		for(String[] vecL: CenterVec2){

//			System.out.println("CenterVec2 = " + ArrayUtils.toString(vecL));

			for(int i = 0; i < 200; i++){
				CenterV2[i] += Double.parseDouble(vecL[i+1]);
			}
		}
//		System.out.println("Before CenterV2 = " + Arrays.toString(CenterV2));

		for(int i = 0; i < 200; i++){
			CenterV2[i] = CenterV2[i]/CenterV2.length;
		}
//		System.out.println("After CenterV2 = " + Arrays.toString(CenterV2));

		//Name1とName2のベクトル距離を求める
		double v = 0d;
		for(int i = 0; i < 200; i++){
			v += (CenterV1[i] - CenterV2[i])*(CenterV1[i] - CenterV2[i]);
		}
		v = Math.sqrt(v);

//		System.out.println(name1 + "<=>" + name2 + ", ");
//		System.out.println("VectorDistance = " + v);

		return (float)(v);

	}

	public static List<String[]> readCsv(File f) {
		List<String[]> list = new ArrayList<String[]>();
		try {
			FileInputStream s = new FileInputStream(f);
			InputStreamReader r = new InputStreamReader(s, "utf-8");
			BufferedReader br = new BufferedReader(r);
			String line;
			while((line = br.readLine()) != null) {
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
