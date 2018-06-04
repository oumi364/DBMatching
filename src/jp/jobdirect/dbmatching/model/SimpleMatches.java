package jp.jobdirect.dbmatching.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

public class SimpleMatches implements Matches {
	
	private Map<SimpleEntry<Record, Record>, Match> _matches;
	
	public SimpleMatches()
	{
		this._matches = new HashMap<SimpleEntry<Record, Record>, Match>();
	}

	@Override
	public boolean canMatch(Record[] records) {
		return this._matches.containsKey(new SimpleEntry<Record, Record>(records[0], records[1]))
				|| this._matches.containsKey(new SimpleEntry<Record, Record>(records[1], records[0]));
	}

	@Override
	public Match match(Record[] records) {
		SimpleEntry<Record, Record> key;
		if(this._matches.containsKey(key = new SimpleEntry<Record, Record>(records[0], records[1]))){
			return this._matches.get(key);
		}
		if(this._matches.containsKey(key = new SimpleEntry<Record, Record>(records[1], records[0]))){
			return this._matches.get(key);
		}
		return null;
	}

	@Override
	public Iterable<Match> getMatches() {
		return this._matches.values();
	}
	
	public void add(Match match) {
		Record[] records = match.getRecords();
		this._matches.put(new SimpleEntry<Record, Record>(records[0], records[1]), match);
	}

}
