package jp.jobdirect.dbmatching.classifier;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Matches;
import jp.jobdirect.dbmatching.model.Record;
import jp.jobdirect.dbmatching.model.SimpleMatches;

public abstract class AbstractClassifier implements Classifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8352566697653984052L;

	@Override
	abstract public void train(DataSet dataSetToTrain);
	
	@Override
	abstract public Match classify(Record record1, Record record2);

	@Override
	public Matches classify(Iterable<Record> records1, Iterable<Record> records2) {
		SimpleMatches newMatches = new SimpleMatches();
		
		ArrayList<Record> rs1 = new ArrayList<Record>();
		for(Record r1: records1){
			rs1.add(r1);
		}

		ArrayList<Record> rs2 = new ArrayList<Record>();
		for(Record r2: records2){
			rs2.add(r2);
		}
		
		System.out.println(getClass().getSimpleName() + ": recordset-size=" + rs1.size() + ", " + rs2.size());
		
		long total = (long)rs1.size() * (long)rs2.size();
		
		Calendar startTime = Calendar.getInstance();
		
		long c = 0;
		for(Record r1: rs1){
			for(Record r2: rs2){
				if(c % 10000 == 0 && c > 0){
					Calendar currentTime = Calendar.getInstance();
					long estimatedEndTime = (currentTime.getTimeInMillis() - startTime.getTimeInMillis()) * total / c + startTime.getTimeInMillis();
					Calendar endTime = Calendar.getInstance();
					endTime.setTimeInMillis(estimatedEndTime);
					System.out.println("Pattern " + c + "/" + total + ": estimated end time=" + DateFormat.getDateTimeInstance().format(endTime.getTime()));
				}
				c++;
				Match m = this.classify(r1,  r2);
				if(m != null){
					newMatches.add(m);
				}
			}			
		}
		
		return newMatches;
	}

	@Override
	public void addMatchListener(MatchListener matchListener) {
		// do nothing
	}

	@Override
	public void removeMatchListener(MatchListener matchListener) {
		// do nothing
	}

	@Override
	public void clearMatchListener() {
		// do nothing
	}
}
