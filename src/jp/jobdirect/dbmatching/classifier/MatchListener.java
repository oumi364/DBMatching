package jp.jobdirect.dbmatching.classifier;

import jp.jobdirect.dbmatching.model.Match;

public interface MatchListener {
	public boolean matched(Match match);
}
