package jp.jobdirect.dbmatching.model;

public interface Matches {
	public boolean canMatch(Record records[]);
	public Match match(Record records[]);
	public Iterable<Match> getMatches();
}
