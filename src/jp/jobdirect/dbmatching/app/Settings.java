package jp.jobdirect.dbmatching.app;

import java.util.Collection;

import jp.jobdirect.dbmatching.classifier.StrongClassifierProperty;
import jp.jobdirect.dbmatching.classifier.WeakClassifierProperty;

public interface Settings {
	String getBaseDirectory();

	Collection<WeakClassifierProperty> getWeakClassifierPropertes();

	StrongClassifierProperty getStrongClassifierProperty();

}
