package jp.jobdirect.dbmatching.classifier;

public interface StrongClassifier extends Classifier {
	void addWeakClassifier(WeakClassifier weakClassifier);
}
