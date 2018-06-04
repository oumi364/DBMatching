package jp.jobdirect.dbmatching.app;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.jobdirect.dbmatching.model.Attribute;
import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class DefaultDataSet implements DataSet {

	private static Attribute ATTRIBUTES[] = {
			new DefaultAttribute("ID", String.class),
			new DefaultAttribute("NAME", String.class),
			new DefaultAttribute("PREF", String.class),
			new DefaultAttribute("CITY", String.class),
			new DefaultAttribute("ADDRESS", String.class),
			new DefaultAttribute("LATITUDE", Float.class),
			new DefaultAttribute("LONGITUDE", Float.class),
			new DefaultAttribute("PHONE1", String.class),
			new DefaultAttribute("REV", String.class),
			new DefaultAttribute("CAT", String.class),
	};

	private boolean _isDirty = true;
	private ArrayList<Record> _bogusQueryRecords = new ArrayList<Record>();
	private ArrayList<Record> _bogusDatabaseRecords = new ArrayList<Record>();

	private Map<String, Record> _queryRecords = new HashMap<String, Record>();
	private Map<String, Record> _databaseRecords = new HashMap<String, Record>();
	private ArrayList<String> _queryRecordKeys = new ArrayList<String>();
	private ArrayList<String> _databaseRecordKeys = new ArrayList<String>();
	private Map<SimpleEntry<String, String>, Match> _matches = new HashMap<SimpleEntry<String, String>, Match>();

	@Override
	public Collection<Attribute> getAttributes() {
		return new ArrayList<Attribute>(Arrays.asList(DefaultDataSet.ATTRIBUTES));
	}

	@Override
	public Collection<Record> getQueryRecords() {
		return this._queryRecords.values();
	}

	@Override
	public Collection<Record> getDatabaseRecords() {
		return this._databaseRecords.values();
	}

	@Override
	public Collection<Match> getMatches() {
		return this._matches.values();
	}

	public void addQueryRecord(Record record){
		this._queryRecords.put(record.getId(), record);
		this._queryRecordKeys.add(record.getId());
		this._isDirty = true;
	}

	public void addDatabaseRecord(Record record){
		this._databaseRecords.put(record.getId(), record);
		this._databaseRecordKeys.add(record.getId());
		this._isDirty = true;
	}

	public void addMatch(Match match){
		Record[] records = match.getRecords();
		this._matches.put(new SimpleEntry<String, String>(records[0].getId(), records[1].getId()), match);
		this._isDirty = true;
	}

	public Record findQueryRecord(String id){
		return this._queryRecords.get(id);
	}

	public Record findDatabaseRecord(String id){
		return this._databaseRecords.get(id);
	}

	public Match findMatch(String lid, String rid){
		SimpleEntry<String, String> key1 = new SimpleEntry<String, String>(lid, rid);
		SimpleEntry<String, String> key2 = new SimpleEntry<String, String>(rid, lid);
		if(this._matches.containsKey(key1)){
			return this._matches.get(key1);
		}else if(this._matches.containsKey(key2)){
			return this._matches.get(key2);
		}else{
			return null;
		}
	}

	public Record randomQueryRecord(){
		int r = Random.getInstance().nextInt(this._queryRecordKeys.size());
		return this._queryRecords.get(this._queryRecordKeys.get(r));
	}

	public Record randomDatabaseRecord(){
		int r = Random.getInstance().nextInt(this._databaseRecordKeys.size());
		return this._databaseRecords.get(this._databaseRecordKeys.get(r));
	}

	public void flushIndex(){
		this._bogusQueryRecords.clear();
		this._bogusDatabaseRecords.clear();

		Set<Record> queryRecordMatches = new HashSet<Record>();
		Set<Record> databaseRecordMatches = new HashSet<Record>();
		for(Match m : this._matches.values()){
			Record[] r = m.getRecords();
			queryRecordMatches.add(r[0]);
			databaseRecordMatches.add(r[1]);
		}

		for(Record r : this._queryRecords.values()){
			if(!queryRecordMatches.contains(r)){
				this._bogusQueryRecords.add(r);
			}
		}
		for(Record r : this._databaseRecords.values()){
			if(!databaseRecordMatches.contains(r)){
				this._bogusDatabaseRecords.add(r);
			}
		}

		this._isDirty = false;
	}

	interface Condition {
		boolean evaluate(int i, int n);
	}

	public DataSet getSubset(Condition cond, int folds){
		if(this._isDirty){
			this.flushIndex();
		}

		DefaultDataSet newDataSet = new DefaultDataSet();

		int i = 0;
		for(Match match : this._matches.values()){
			if(cond.evaluate(i, folds)){
				newDataSet.addMatch(match);

				Record[] r = match.getRecords();
				if(newDataSet.findQueryRecord(r[0].getId()) == null){
					newDataSet.addQueryRecord(r[0]);
				}
				if(newDataSet.findDatabaseRecord(r[1].getId()) == null){
					newDataSet.addDatabaseRecord(r[1]);
				}
			}
			i++;
		}

		i = 0;
		for(Record r : this._bogusQueryRecords){
			if(cond.evaluate(i, folds)){
				newDataSet.addQueryRecord(r);
			}
			i++;
		}
		i = 0;
		for(Record r : this._bogusDatabaseRecords){
			if(cond.evaluate(i, folds)){
				newDataSet.addDatabaseRecord(r);
			}
			i++;
		}

		return newDataSet;
	}

	public DataSet getTrainingSet(final int index, int folds){
		return this.getSubset(new Condition(){

			@Override
			public boolean evaluate(int i, int n) {
				return i % n != index;
			}

		}, folds);
	}

	public DataSet getTestSet(final int index, int folds){
		return this.getSubset(new Condition(){

			@Override
			public boolean evaluate(int i, int n) {
				return i % n == index;
			}

		}, folds);
	}

	public void addNegativeMatches(int count){
		for(int i = 0; i < count; i++){
			Match match = null;
			do {
				Record lr = this.randomQueryRecord();
				Record rr = this.randomDatabaseRecord();
				if(!lr.getId().equals(rr.getId()) && this.findMatch(lr.getId(), rr.getId()) == null){
					match = new DefaultMatch(lr, rr, false);
				}
			} while(match == null);

			this.addMatch(match);
		}
	}

	@Override
	public String toString(){
		return new StringBuffer()
				.append("DefaultDataSet: ")
				.append("query-record-count=").append(this._queryRecords.size()).append(", ")
				.append("database-record-count=").append(this._databaseRecords.size()).append(", ")
				.append("match-count=").append(this._matches.size())
				.toString();
	}
}
