package jp.jobdirect.dbmatching.model;

public interface Match {
	public Record[] getRecords();
	public Boolean isMatching();
}
