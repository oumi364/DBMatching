package jp.jobdirect.dbmatching.classifier;

import java.io.Serializable;

import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Matches;
import jp.jobdirect.dbmatching.model.Record;

public interface Classifier extends Serializable {
	public void train(DataSet dataSetToTrain);
	public Match classify(Record record1, Record record2);
	public Matches classify(Iterable<Record> records1, Iterable<Record> records2);
	public void addMatchListener(MatchListener matchListener);
	public void removeMatchListener(MatchListener matchListener);
	public void clearMatchListener();
}
