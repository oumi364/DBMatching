package jp.jobdirect.dbmatching.classifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.jobdirect.dbmatching.model.DataSet;
import jp.jobdirect.dbmatching.model.Match;
import jp.jobdirect.dbmatching.model.Record;

public class AdaBoostStrongClassifier extends AbstractClassifier implements StrongClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = 4781643724984304071L;

	private final int DEFAULT_MAXIMUM_LOOP_COUNT = 10;

	private List<WeakClassifier> _weakClassifiers = new ArrayList<WeakClassifier>();
	private float[]              _weakClassifierWeights;
	private List<MatchListener>  _matchListeners  = new ArrayList<MatchListener>();

	private int _maximumLoopCount = DEFAULT_MAXIMUM_LOOP_COUNT;
	private boolean _verbose = false;

	@Override
	public void train(DataSet dataSetToTrain) {
		// Train weak classifiers
		for(WeakClassifier weakClassifier : this._weakClassifiers){
			weakClassifier.train(dataSetToTrain);
		}

		// Train AdaBoost-ing
		int weakClassifierCount = this._weakClassifiers.size();
		this._weakClassifierWeights = new float[weakClassifierCount];

		// AdaBoost Loop
		Collection<Match> matchesToTrain = dataSetToTrain.getMatches();
		int trainingMatchCount = matchesToTrain.size();
		float[] weights = new float[trainingMatchCount];
		for(int i = 0; i < trainingMatchCount; i++){
			weights[i] = 1.0f / trainingMatchCount;
		}

		if(this._maximumLoopCount > weakClassifierCount * 2){
			this._maximumLoopCount = weakClassifierCount * 2;
		}

		Set<WeakClassifier> weakClassifierDone = new HashSet<WeakClassifier>();

		int loopCount = 0;
		while(loopCount < this._weakClassifiers.size()){
			loopCount++;
			if(loopCount > this._maximumLoopCount){
				break;
			}

			// Find classifier with minimal error
			float minimalError      = Float.MAX_VALUE;
			int   minimalErrorIndex = weakClassifierCount;
			WeakClassifier minimalWeakClassifier = null;

			int i = 0;
			for(WeakClassifier weakClassifier : this._weakClassifiers){
				if(!weakClassifierDone.contains(weakClassifier)){
					// Evaluate error
					float error = 0f;
					int j = 0;
					for(Match match : matchesToTrain){
						Record[] records = match.getRecords();

						boolean m1 = weakClassifier.classify(records[0], records[1]).isMatching();
						boolean m2 = match.isMatching();

						if(m1 != m2){
							error += weights[j];
						}
						{
							if(weights[j] >= 0.1){
								System.out.print("Warning: ");
								System.out.println(weights[j] + "/" + error + ": " + m1 + ": " + m2 + ": WC=" + weakClassifier + ", r1=" + records[0] + ", r2=" + records[1]);
							}
						}

						j++;
					}

					if(error < minimalError){
						minimalError = error;
						minimalErrorIndex = i;
						minimalWeakClassifier = weakClassifier;
					}
				}
				i++;
			}

			// Judge end of training
			System.out.println("Loop " + loopCount + ": index=" + minimalErrorIndex + ", error=" + minimalError);
			if(minimalError > 0.5f){
				break;
			}

			// Calculate alpha
			float alpha = (float)Math.log((1.0f - minimalError) / minimalError) / 2.0f;
			this._weakClassifierWeights[minimalErrorIndex] += alpha;
			System.out.println("minimalError=" + minimalError + ", alpha=" + alpha);

			{
				System.out.print("WeakClassifier weight: ");
				for(i = 0; i < this._weakClassifierWeights.length; i++){
					if(i > 0){
						System.out.print(", ");
					}
					System.out.print(this._weakClassifierWeights[i]);
				}
				System.out.println();
			}

			// Update weights
			WeakClassifier weakClassifier = minimalWeakClassifier;
			float weightSum = 0f;

			float alpha_p = (float)Math.exp(-alpha);
			float alpha_n = 1.0f / alpha_p;

			i = 0;
			for(Match match : matchesToTrain){
				Record[] records = match.getRecords();
				float a = weakClassifier.classify(records[0], records[1]).isMatching() == match.isMatching() ? alpha_p : alpha_n;
//				System.out.println("weight[" + i + "]: " + weights[i] + ", " + a + ", " + (weights[i] * a));
				weights[i] *= a;
				weightSum += weights[i];
				i++;
			}
			for(i = 0; i < trainingMatchCount; i++){
				weights[i] /= weightSum;
			}

			weakClassifierDone.add(weakClassifier);
		}
	}

	@Override
	public Match classify(Record record1, Record record2) {
		float value = 0.0f;



//		System.out.print("Weight: ");
		if(this._verbose){
			System.out.println("classify: r1=" + record1 + ", r2=" + record2);
		}

		//record1.getId()
		//record2.getId()

		if(ExecCheck.ExecJudge(record1.getId(),record2.getId())) {

			Match newMatch;
			if(record1.getStringValue("PREF").equals(record2.getStringValue("PREF"))
					&& record1.getStringValue("CITY").equals(record2.getStringValue("CITY"))) {

				Match[] subMatches = new Match[this._weakClassifiers.size()];

				int j = 0;
				for(WeakClassifier weakClassifier : this._weakClassifiers){
					float weight = this._weakClassifierWeights[j];
					//			System.out.print(weight + ", ");
					boolean match = false;
					if(weight != 0.0f){
						Match m = weakClassifier.classify(record1, record2);
						match = m.isMatching();
						if(this._verbose){
							System.out.print(match + ", ");
						}
						subMatches[j] = m;
					}else{
						if(this._verbose){
							System.out.print("?, ");
						}
					}
					value += (match ? weight : -weight);
					j++;
				}
				boolean matching = (value > 0.0f);

				if(this._verbose){
					System.out.println(value + ", " + matching);
				}
				//		System.out.println();

				newMatch = new MatchWithLikelihood(record1, record2, this.getClass(), matching, value, subMatches);

				for(MatchListener matchListener : this._matchListeners){
					if(matchListener.matched(newMatch)){
						return null;
					}
				}
				return newMatch;

			} else {
				return null;
			}

		} else {
			return null;
		}

	}

	@Override
	public void addWeakClassifier(WeakClassifier weakClassifier) {
		this._weakClassifiers.add(weakClassifier);
		this._weakClassifierWeights = new float[this._weakClassifiers.size()];
	}

	public void setVerbose(boolean verbose){
		this._verbose = verbose;
	}

	@Override
	public void addMatchListener(MatchListener matchListener) {
		this._matchListeners.add(matchListener);
	}

	@Override
	public void removeMatchListener(MatchListener matchListener) {
		this._matchListeners.remove(matchListener);
	}

	@Override
	public void clearMatchListener() {
		this._matchListeners.clear();
	}
}
