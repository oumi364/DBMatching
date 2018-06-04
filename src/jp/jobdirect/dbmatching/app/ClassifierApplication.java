package jp.jobdirect.dbmatching.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.jobdirect.dbmatching.classifier.Classifier;
import jp.jobdirect.dbmatching.classifier.MatchListener;
import jp.jobdirect.dbmatching.classifier.MatchWithLikelihood;
import jp.jobdirect.dbmatching.classifier.StrongClassifierFactory;
import jp.jobdirect.dbmatching.classifier.WeakClassifierProperty;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Matches;
import jp.jobdirect.dbmatching.model.Record;

public class ClassifierApplication {

	public static boolean DEBUG_WEAK_CLASSIFIER_SCORES = true;
	public static boolean USE_CROSS_VALIDATION = false;
	public static int CROSS_VALIDATION_FOLDS = 5;

	public static void main(String[] args)
	{

		DateFormat df = new SimpleDateFormat();

		// 設定情報を取得する
		Settings settings = DefaultSettings.create();

		// 識別データを取得する
		DefaultDataSet dataSet = (DefaultDataSet)DefaultDataSetLoader.loadDataSet(settings);
		System.out.println("All dataset: " + dataSet);

		// 識別器を構築する
		Classifier classifier = null;
		if(ApplicationSetting.LOAD_MODEL_FROM != null){
			ObjectInputStream is;
			try{
				is = new ObjectInputStream(new FileInputStream(ApplicationSetting.LOAD_MODEL_FROM));
				Object o = is.readObject();
				if(o instanceof Classifier){
					classifier = (Classifier)o;
				}else{
					System.err.println("Object read was not a Classifier instance: " + ApplicationSetting.LOAD_MODEL_FROM);
				}
			}catch(ClassNotFoundException ex){
				System.err.println("Failed to read object from: " + ApplicationSetting.LOAD_MODEL_FROM);
				ex.printStackTrace();
			}catch(IOException ex){
				System.err.println("Failed to load model from: " + ApplicationSetting.LOAD_MODEL_FROM);
				ex.printStackTrace();
			}

		}else{
			StrongClassifierFactory strongClassifierFactory = new DefaultStrongClassifierFactory();
			strongClassifierFactory.setBaseDirectory(settings.getBaseDirectory());
			strongClassifierFactory.setStrongClassifierProperty(settings.getStrongClassifierProperty());
			for(WeakClassifierProperty weakClassifierProperty : settings.getWeakClassifierPropertes()){
				strongClassifierFactory.addWeakClassifierProperty(weakClassifierProperty);
			}
			strongClassifierFactory.setStrongClassifierProperty(settings.getStrongClassifierProperty());
			classifier = strongClassifierFactory.create();
		}

		Calendar startTime = Calendar.getInstance();
		System.out.println("Timing: start: " + df.format(startTime.getTime()));

		// Cross Validation
		int validationCount = USE_CROSS_VALIDATION ? CROSS_VALIDATION_FOLDS : 1;
		for(int validation = 0; validation < validationCount; validation++){
			DefaultDataSet trainingDataSet, evalationgDataSet;
			if(USE_CROSS_VALIDATION){
				trainingDataSet = (DefaultDataSet)dataSet.getTrainingSet(validation, validationCount);
				evalationgDataSet = (DefaultDataSet)dataSet.getTestSet(validation, validationCount);
			}else{
				trainingDataSet = dataSet;
				evalationgDataSet = dataSet;
			}

			if(ApplicationSetting.LOAD_MODEL_FROM == null){
				System.out.println("Train[" + validation + "]: " + trainingDataSet);
				System.out.println("Test[" + validation + "]: " + evalationgDataSet);

				// 学習処理を行う
				trainingDataSet.addNegativeMatches(trainingDataSet.getMatches().size() * 10);
				classifier.train(trainingDataSet);
			}

			if(ApplicationSetting.SAVE_MODEL_TO != null){
				ObjectOutputStream os;
				try{
					os = new ObjectOutputStream(new FileOutputStream(ApplicationSetting.SAVE_MODEL_TO));
					os.writeObject(classifier);
				}catch(IOException ex){
					System.err.println("Failed to save model to: " + ApplicationSetting.SAVE_MODEL_TO);
					ex.printStackTrace();
				}

			}else{

				// 識別処理を行う
				List<Record> recordSubset1 = new ArrayList<Record>();
				List<Record> recordSubset2 = new ArrayList<Record>();

				Set<SimpleEntry<Record, Record>> dataSetMatches = new HashSet<SimpleEntry<Record, Record>>();
				int i = 0;
				for(Match m : evalationgDataSet.getMatches()){
					Record[] r = m.getRecords();
					if(m.isMatching()){
						recordSubset1.add(r[0]);
						recordSubset2.add(r[1]);
						dataSetMatches.add(new SimpleEntry<Record, Record>(r[0], r[1]));
						System.out.println("Correct[" + ++i + "] : " + m);
					}
				}

				// 全体を対象に識別
				recordSubset1.clear();
				recordSubset2.clear();
				for(Record r : evalationgDataSet.getQueryRecords()){
					recordSubset1.add(r);
				}
				for(Record r : evalationgDataSet.getDatabaseRecords()){
					recordSubset2.add(r);
				}

		//		((AdaBoostStrongClassifier)classifier).setVerbose(true);
				final Set<SimpleEntry<Record, Record>> finalizedDataSetMatches = dataSetMatches;
				class MyMatchListener implements MatchListener
				{
					int correctPositiveMatchCount = 0, correctNegativeMatchCount = 0;
					int wrongPositiveMatchCount   = 0, wrongNegativeMatchCount   = 0;
					int totalMatchCount           = 0;
					private int c = 0;

						@Override
						public boolean matched(Match match) {
							Record[] r = match.getRecords();
							boolean correct = finalizedDataSetMatches.contains(new SimpleEntry<Record, Record>(r[0], r[1]));
							if(correct){
								if(match.isMatching()){
									correctPositiveMatchCount++;
								}else{
									wrongNegativeMatchCount++;
								}
							}else{
								if(match.isMatching()){
									wrongPositiveMatchCount++;
								}else{
									correctNegativeMatchCount++;
								}
							}
							totalMatchCount++;

								if(match.isMatching()){
								try{
									File match_file = new File("C:\\work\\match.txt");
									PrintWriter match_pw = new PrintWriter(new BufferedWriter(new FileWriter(match_file,true)));

									System.out.println("Match[" + (++c) + "]: " + correct + ": " + match);

									MatchWithLikelihood ml = (MatchWithLikelihood)match;

									StringBuffer sb = new StringBuffer();
//									sb		.append("Output:").append("\t")
									sb		.append(correct).append("\t")
											.append(ml.getLikelihood()).append("\t");
									for(Match sm : ml.getSubMatches()){
										if(sm != null){
											MatchWithLikelihood sml = (MatchWithLikelihood)sm;
											sb.append(sml.getLikelihood()).append("\t");
										}else{
											sb.append("???").append("\t");
										}
									}

									for(Record rd : ml.getRecords()){
										DefaultRecord dr = (DefaultRecord)rd;
										sb	.append(dr.getId()).append("\t")
										.append(dr.getStringValue("NAME")).append("\t")
										.append(dr.getStringValue("PREF")).append("\t")
										.append(dr.getStringValue("CITY")).append("\t")
										.append(dr.getStringValue("ADDRESS")).append("\t")
										.append(dr.getFloatValue("LATITUDE")).append("\t")
										.append(dr.getFloatValue("LONGITUDE")).append("\t")
										.append(dr.getStringValue("PHONE1")).append("\t")
										.append(dr.getStringValue("REV")).append("\t")
										.append(dr.getStringValue("CAT")).append("\t");
									}

									//System.out.println(sb);
									match_pw.println(sb);
									match_pw.close();
								}catch(IOException e){
									System.out.println(e);
								}

									return false;

								}else{

								try{
									File unmatch_file = new File("C:\\work\\unmatch.txt");
									PrintWriter unmatch_pw = new PrintWriter(new BufferedWriter(new FileWriter(unmatch_file,true)));

									//System.out.println("False[" + (++c) + "]: " + correct + ": " + match);

									MatchWithLikelihood ml = (MatchWithLikelihood)match;

									StringBuffer sb = new StringBuffer();
//									sb		.append("Output:").append("\t")
									sb		.append(correct).append("\t")
											.append(ml.getLikelihood()).append("\t");
									for(Match sm : ml.getSubMatches()){
										if(sm != null){
											MatchWithLikelihood sml = (MatchWithLikelihood)sm;
											sb.append(sml.getLikelihood()).append("\t");
										}else{
											sb.append("???").append("\t");
										}
									}

									for(Record rd : ml.getRecords()){
										DefaultRecord dr = (DefaultRecord)rd;
										sb	.append(dr.getId()).append("\t")
										.append(dr.getStringValue("NAME")).append("\t")
										.append(dr.getStringValue("PREF")).append("\t")
										.append(dr.getStringValue("CITY")).append("\t")
										.append(dr.getStringValue("ADDRESS")).append("\t")
										.append(dr.getFloatValue("LATITUDE")).append("\t")
										.append(dr.getFloatValue("LONGITUDE")).append("\t")
										.append(dr.getStringValue("PHONE1")).append("\t")
										.append(dr.getStringValue("REV")).append("\t")
										.append(dr.getStringValue("CAT")).append("\t");
									}

									//System.out.println(sb);
									unmatch_pw.println(sb);
									unmatch_pw.close();
								}catch(IOException e){
								      System.out.println(e);
								}

									return true;

								}
						}
				};

				MyMatchListener matchListener = new MyMatchListener();
				classifier.clearMatchListener();
				classifier.addMatchListener(matchListener);
				Matches matches = classifier.classify(recordSubset1, recordSubset2);
		//		Matches matches = classifier.classify(records, records);

				System.out.println(new StringBuffer()
						.append("Result: ")
						.append(matchListener.correctPositiveMatchCount).append(", ")
						.append(matchListener.wrongPositiveMatchCount).append(", ")
						.append(matchListener.correctNegativeMatchCount).append(", ")
						.append(matchListener.wrongNegativeMatchCount).append(", ")
						.append(matchListener.totalMatchCount)
						.toString());
				System.out.println(new StringBuffer()
						.append("Rate: ")
						.append("precision=").append(1f * (matchListener.correctPositiveMatchCount + matchListener.correctNegativeMatchCount) / matchListener.totalMatchCount)
						.toString());
			}

//			// 結果の出力
//			int c = 0;
//			for(Match match : matches.getMatches()){
//				Record[] r = match.getRecords();
//				boolean correct = dataSetMatches.contains(new SimpleEntry<Record, Record>(r[0], r[1]));
//				if(match.isMatching()){
//					System.out.println("Match[" + (++c) + "]: " + correct + ": " + match);
//				}
//			}

		}

		Calendar endTime = Calendar.getInstance();
		System.out.println("Timing: start: " + df.format(startTime.getTime()));
		System.out.println("Timing: end: " + df.format(endTime.getTime()));


	}

}
