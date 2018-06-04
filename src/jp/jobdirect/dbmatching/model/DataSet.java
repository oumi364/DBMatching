package jp.jobdirect.dbmatching.model;

import java.util.Collection;

/**
 * 本件システムの入力データを扱う。
 * 学習に用いるには、getMatches() が教師データを返す必要がある。
 * 識別に用いる場合は、getMatches() は null を返しても良い。
 * 
 * @author kino
 *
 */
public interface DataSet {
	public Collection<Attribute> getAttributes();
	public Collection<Record> getQueryRecords();
	public Collection<Record> getDatabaseRecords();
	public Collection<Match>  getMatches();
	
}
