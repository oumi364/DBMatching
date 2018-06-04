package jp.jobdirect.dbmatching.app;

import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class DefaultMatch implements Match {
	
	private Record _records[] = new Record[2];
	private boolean _matched = false;
	
	public DefaultMatch(Record record1, Record record2, boolean matched){
		this._records[0] = record1;
		this._records[1] = record2;
		this._matched = matched;
	}

	@Override
	public Record[] getRecords() {
		return this._records;
	}

	@Override
	public Boolean isMatching() {
		return this._matched;
	}
	
	@Override
	public String toString(){
		return new StringBuffer()
				.append("match: {\nr1=")
				.append(this._records[0].toString())
				.append(", \nr2=")
				.append(this._records[1].toString())
				.append(", \nmatch=")
				.append(this._matched)
				.append("}")
				.toString();
	}

}
