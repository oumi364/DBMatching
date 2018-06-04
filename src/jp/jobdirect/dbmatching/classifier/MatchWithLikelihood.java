package jp.jobdirect.dbmatching.classifier;

import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class MatchWithLikelihood implements Match {
	
	private Record _record1;
	private Record _record2;
	private Class<?> _classifierClass;
	private boolean _matching;
	private float _likelihood;
	private Match[] _subMatches;
	
	public MatchWithLikelihood(Record record1, Record record2, Class<?> classifierClass, boolean matching, float likelihood){
		this._record1 = record1;
		this._record2 = record2;
		this._classifierClass = classifierClass;
		this._matching = matching;
		this._likelihood = likelihood;
	}

	public MatchWithLikelihood(Record record1, Record record2, Class<?> classifierClass, boolean matching, float likelihood, Match[] subMatches){
		this._record1 = record1;
		this._record2 = record2;
		this._classifierClass = classifierClass;
		this._matching = matching;
		this._likelihood = likelihood;
		this._subMatches = subMatches;
	}

	@Override
	public Record[] getRecords() {
		return new Record[]{ this._record1, this._record2 };
	}

	@Override
	public Boolean isMatching() {
		return this._matching;
	}
	
	public float getLikelihood() {
		return this._likelihood;
	}
	
	public Class<?> getClassifierClass() {
		return this._classifierClass;
	}
	
	public Match[] getSubMatches() {
		return this._subMatches;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("match={");
		sb.append("match=");
		sb.append(this._matching);
		sb.append(", class=");
		sb.append(this._classifierClass.getSimpleName());
		
		if(this._subMatches != null){
			for(Match m : this._subMatches){
				if(m == null){
					sb.append(", {?}");
				}else if (m instanceof MatchWithLikelihood){
					sb.append(", {");
					sb.append(((MatchWithLikelihood) m)._classifierClass.getSimpleName());
					sb.append(", ");
					sb.append(((MatchWithLikelihood) m).isMatching());
					sb.append(", ");
					sb.append(((MatchWithLikelihood) m).getLikelihood());
					sb.append("}");
				}else{
					sb.append(", {");
					sb.append(m.isMatching());
					sb.append("}");
				}
			}
		}
		
		sb.append(", likelihood=");
		sb.append(this._likelihood);
		sb.append(", record1=");
		sb.append(this._record1);
		sb.append(", record2=");
		sb.append(this._record2);
		sb.append("}");
		return sb.toString();
	}
}
