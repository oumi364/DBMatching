package jp.jobdirect.dbmatching.app;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Matches;
import jp.jobdirect.dbmatching.model.Record;

public class DefaultMatches implements Matches {
	
	private Table<Record, Record, Match> _matches = HashBasedTable.create();
	
	public DefaultMatches()
	{
		
	}
	
	public void addMatch(Match match){
		Record records[] = match.getRecords();
		this._matches.put(records[0], records[1],  match);
	}

	@Override
	public boolean canMatch(Record[] records) {
		return this._matches.contains(records[0], records[1]) ||
				this._matches.contains(records[1], records[0]);
	}

	@Override
	public Match match(Record[] records) {
		if(this._matches.contains(records[0], records[1])){
			return this._matches.get(records[0], records[1]);
		}else if(this._matches.contains(records[1], records[0])){
			return this._matches.get(records[1], records[0]);
		}else{
			return null;
		}
	}

	@Override
	public Iterable<Match> getMatches() {
		return this._matches.values();
	}
}

