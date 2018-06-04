package jp.jobdirect.dbmatching.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Separation {

	private List<Float> _nvalues = new ArrayList<Float>(), _pvalues = new ArrayList<Float>();
	private int _ncount, _pcount;
	private float _nmin, _nmax, _nsum, _nssum;
	private float _pmin, _pmax, _psum, _pssum;
	
	private static boolean SEARCH_OPTIMAL_THRESHOLD = true; // 20160927-1 以降

	public Separation()
	{
		this._ncount = this._pcount = 0;
		this._nmax = this._nmin = this._nsum = this._nssum = 0f;
		this._pmax = this._pmin = this._psum = this._pssum = 0f;
	}
	
	public void addValue(float value, boolean isPositive){
		if(isPositive){
			this._pvalues.add(value);
			this._pmin = (this._pcount == 0 || this._pmin > value) ? value : this._pmin;
			this._pmax = (this._pcount == 0 || this._pmax < value) ? value : this._pmax;
			this._psum += value;
			this._pssum += value * value;
			this._pcount++;
		}else{
			this._nvalues.add(value);
			this._nmin = (this._ncount == 0 || this._nmin > value) ? value : this._nmin;
			this._nmax = (this._ncount == 0 || this._nmax < value) ? value : this._nmax;
			this._nsum += value;
			this._nssum += value * value;
			this._ncount++;
		}
	}
	
	public float getThreshold()
	{
		float pmean = (this._psum / this._pcount); 
		float nmean = (this._nsum / this._ncount);
		float pvar  = (this._pssum / this._pcount) - pmean * pmean;
		float nvar  = (this._nssum / this._ncount) - nmean * nmean;
		
		if(pvar < 1e-6f){
			pvar = 1e-6f;
		}
		if(nvar < 1e-6f){
			nvar = 1e-6f;
		}
		
		float psigma = (float) Math.sqrt(pvar);
		float nsigma = (float) Math.sqrt(nvar);
		
		float threshold = (pmean * nsigma + nmean * psigma) / (psigma + nsigma);
		
		if(SEARCH_OPTIMAL_THRESHOLD){
			if(this._pmax < this._nmin){
				threshold = (this._pmax + this._nmin) / 2;
			}else if(this._pmin > this._nmax){
				threshold = (this._pmin + this._nmax) / 2;
			}else{
				Collections.sort(this._pvalues);
				Collections.sort(this._nvalues);
				
				int bestCount = this._ncount + this._pcount;
				float bestValue = threshold;
	
				int pindex, nindex;
				for(pindex = 0, nindex = 0; pindex < this._pvalues.size() && nindex < this._nvalues.size(); pindex++){
					float v = this._pvalues.get(pindex);
					if(pindex == 0){
						for(nindex = this._nvalues.size() - 1; nindex > 0 && this._nvalues.get(nindex) > v; nindex--);
					}else{
						while(nindex < this._nvalues.size() && this._nvalues.get(nindex) <= v){ nindex++; }
					}
					
					int count = pindex - nindex;
					if(count < bestCount){
						bestCount = count;
						bestValue = v;
					}
				}
				
				if(bestValue > 0f && bestValue < 1f){
					threshold = bestValue;
				}
			}
		}
		
		String caller = Thread.currentThread().getStackTrace()[2].getClassName();
		System.out.println("Caller: " + caller + ", Min: " + this._pmin + ", " + this._nmin + ", Max: " + this._pmax + ", " + this._nmax + ", Mean: " + pmean + ", " + nmean + ", Sigma: " + psigma + ", " + nsigma + ", Threshold: " + threshold);

		return threshold;
	}
	
	public float getNormalizer()
	{
		return ((this._psum / this._pcount) - (this._nsum / this._ncount));
	
	}
}
