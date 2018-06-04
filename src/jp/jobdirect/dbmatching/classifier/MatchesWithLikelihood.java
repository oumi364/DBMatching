package jp.jobdirect.dbmatching.classifier;

import java.util.ArrayList;

import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Matches;
import jp.jobdirect.dbmatching.model.Record;

public class MatchesWithLikelihood implements Matches {

	@Override
	public boolean canMatch(Record[] records) {
		return false;
	}

	@Override
	public Match match(Record[] records) {
		return null;
	}

	@Override
	public Iterable<Match> getMatches() {
		return new ArrayList<Match>();
	}

}
