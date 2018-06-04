package jp.jobdirect.dbmatching.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.orangesignal.csv.CsvConfig;
import com.orangesignal.csv.manager.CsvEntityManager;

import jp.jobdirect.dbmatching.csv.CSVMatch;
import jp.jobdirect.dbmatching.csv.CSVPlans;
import jp.jobdirect.dbmatching.csv.CSVRecord;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Record;

public class DefaultDataSetLoader {

	private static void loadQueryRecordsIntoDataSet(DefaultDataSet dataSet, String path)
	{
		loadRecordsIntoDataSet(dataSet, path, true);
	}

	private static void loadDatabaseRecordsIntoDataSet(DefaultDataSet dataSet, String path)
	{
		loadRecordsIntoDataSet(dataSet, path, false);
	}

	private static void loadRecordsIntoDataSet(DefaultDataSet dataSet, String path, boolean forQuery)
	{
		InputStreamReader reader = null;
		try{
			reader = new InputStreamReader(new FileInputStream(path), "utf8");
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
			return;
		}catch(UnsupportedEncodingException ex){
			ex.printStackTrace();
			return;
		}

		CsvConfig csvConfig = new CsvConfig(',', '"', '"');
		csvConfig.setNullString("?(NULL)?");
//		csvConfig.setSkipLines(1);
		csvConfig.setIgnoreEmptyLines(true);

		List<CSVRecord> list;
		try{
			list = new CsvEntityManager().config(csvConfig).load(CSVRecord.class).from(reader);
		}catch(IOException ex){
			ex.printStackTrace();
			return;
		}

		int i = 0;
		for(CSVRecord record : list){
			float longitude = record.getFloatLongitude();
			float latitude  = record.getFloatLatitude();
			if(longitude < 1.0f || latitude < 1.0f){
				// 飲食検証のため一時的に緯度経度の検証を省略
//				continue;
			}

			DefaultRecord newRecord = new DefaultRecord(dataSet.getAttributes());
			newRecord.setId(record.getId());
			newRecord.setValue("NAME", record.getName());
			newRecord.setValue("PREF", record.getPref());
			newRecord.setValue("CITY", record.getCity());
			newRecord.setValue("ADDRESS", record.getAddress());
			newRecord.setValue("LONGITUDE", record.getFloatLongitude());
			newRecord.setValue("LATITUDE", record.getFloatLatitude());
			newRecord.setValue("PHONE1", record.getPhone1().replaceAll("-", ""));
			newRecord.setValue("REV", record.getRev());
			newRecord.setValue("CAT", record.getCat());

			System.out.println("Record[" + (++i) + "]: " + newRecord.toString());
			System.out.println("ID: " + newRecord.getId());
			if(forQuery){
				dataSet.addQueryRecord(newRecord);
			}else{
				dataSet.addDatabaseRecord(newRecord);
			}
		}

		try{
			reader.close();
		}catch(IOException ex){
			ex.printStackTrace();
			return;
		}
	}

	private static void loadMatchesIntoDataSet(DefaultDataSet dataSet, String path)
	{
		InputStreamReader reader = null;
		try{
			reader = new InputStreamReader(new FileInputStream(path), "utf8");
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
			return;
		}catch(UnsupportedEncodingException ex){
			ex.printStackTrace();
			return;
		}

		CsvConfig csvConfig = new CsvConfig(',', '"', '"');
		csvConfig.setNullString("_(NULL)_");
//		csvConfig.setSkipLines(1);
		csvConfig.setIgnoreEmptyLines(true);

		List<CSVMatch> list;
		try{
			list = new CsvEntityManager().config(csvConfig).load(CSVMatch.class).from(reader);
		}catch(IOException ex){
			ex.printStackTrace();
			return;
		}

		for(CSVMatch match : list){
			Record leftRecord = dataSet.findQueryRecord(match.getLeftId());
			Record rightRecord = dataSet.findDatabaseRecord(match.getRightId());

			if(leftRecord == null){
				System.err.println("Warning: query record with id '" + match.getLeftId() + "' not found.");
				continue;
			}
			if(rightRecord == null){
				System.err.println("Warning: database record with id '" + match.getRightId() + "' not found.");
				continue;
			}
			DefaultMatch newMatch = new DefaultMatch(leftRecord, rightRecord, true);
			dataSet.addMatch(newMatch);
		}

		try{
			reader.close();
		}catch(IOException ex){
			ex.printStackTrace();
			return;
		}
	}

	private static void loadPlansIntoDataSet(DefaultDataSet dataSet, String path)
	{

		InputStreamReader reader = null;
		try{
			reader = new InputStreamReader(new FileInputStream(path), "utf8");
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
			return;
		}catch(UnsupportedEncodingException ex){
			ex.printStackTrace();
			return;
		}

		CsvConfig csvConfig = new CsvConfig(',', '"', '"');
		csvConfig.setNullString("_(NULL)_");
//		csvConfig.setSkipLines(1);
		csvConfig.setIgnoreEmptyLines(true);

		List<CSVPlans> list;
		try{
			list = new CsvEntityManager().config(csvConfig).load(CSVPlans.class).from(reader);
		}catch(IOException ex){
			ex.printStackTrace();
			return;
		}

		for(CSVPlans plans : list){
			Record record = dataSet.findQueryRecord(plans.getId());
			if(record == null){
				record = dataSet.findDatabaseRecord(plans.getId());
			}
			if(record == null){
				System.err.println("Warning: record with id '" + plans.getId() + "' not found.");
				continue;
			}

			if(record instanceof DefaultRecord){
				DefaultRecord defaultRecord = (DefaultRecord)record;
				defaultRecord.setValue("NPLANS", plans.getPlanCount());
				defaultRecord.setValue("MINPRICE", plans.getMinimumPrice());
				defaultRecord.setValue("MAXPRICE", plans.getMaximumPrice());
				defaultRecord.setValue("AVGPRICE", plans.getAveragePrice());
			}
		}

		try{
			reader.close();
		}catch(IOException ex){
			ex.printStackTrace();
			return;
		}
	}


	public static DataSet loadDataSet(Settings properties)
	{
		DefaultDataSet defaultDataSet = new DefaultDataSet();

//		loadRecordsIntoDataSet(defaultDataSet, "data/Jaran.201508.csv");
//		loadRecordsIntoDataSet(defaultDataSet, "data/Jaran.20160819.csv");
//		loadRecordsIntoDataSet(defaultDataSet, "data/Jaran.20160830.csv");
//		loadQueryRecordsIntoDataSet(defaultDataSet, "data/Jaran.20160913.csv");
		loadQueryRecordsIntoDataSet(defaultDataSet, "data/src.csv");

//		loadRecordsIntoDataSet(defaultDataSet, "data/Rakuten.all.201508.csv");
//		loadRecordsIntoDataSet(defaultDataSet, "data/Rakuten.all.201508.nonullgeo.csv");
//		loadRecordsIntoDataSet(defaultDataSet, "data/Rakuten.20160830.csv");
//		loadDatabaseRecordsIntoDataSet(defaultDataSet, "data/Rakuten.20160913.csv");
		loadDatabaseRecordsIntoDataSet(defaultDataSet, "data/dst.csv");

//		loadMatchesIntoDataSet(defaultDataSet, "data/matched.Jaran.Rakuten.csv");
//		loadMatchesIntoDataSet(defaultDataSet, "data/matched.Jaran.Rakuten.20160830.csv");
		loadMatchesIntoDataSet(defaultDataSet, "data/match.csv");
//		loadRecordsIntoDataSet(defaultDataSet, "data/J.csv");
//		loadRecordsIntoDataSet(defaultDataSet, "data/R.csv");
//		loadMatchesIntoDataSet(defaultDataSet, "data/M.csv");

//		loadPlansIntoDataSet(defaultDataSet, "data/plans.Jaran.Rakuten.20160815.csv");

		// add random negative matches
//		defaultDataSet.addNegativeMatches(defaultDataSet.getMatches().size() * 10);

		return defaultDataSet;
	}
}
