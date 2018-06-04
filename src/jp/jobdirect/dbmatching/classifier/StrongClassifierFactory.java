package jp.jobdirect.dbmatching.classifier;

import java.util.HashMap;
import java.util.Iterator;

public class StrongClassifierFactory {
	private String _baseDirectory = null;
	private HashMap<String, WeakClassifierProperty> _weakClassifierProperties;
	private StrongClassifierProperty _strongClassifierProperty;
	
	public StrongClassifierFactory()
	{
	}
	
	public StrongClassifier create()
	{
		return null;
	}

	/**
	 * @return the baseDirectory
	 */
	public String getBaseDirectory() {
		return _baseDirectory;
	}

	/**
	 * @param baseDirectory the baseDirectory to set
	 */
	public void setBaseDirectory(String baseDirectory) {
		this._baseDirectory = baseDirectory;
	}

	/**
	 * @return the weakClassifierProperties
	 */
	public Iterator<WeakClassifierProperty> getWeakClassifierProperties() {
		return _weakClassifierProperties.values().iterator();
	}

	/**
	 * @param weakClassifierProperties the weakClassifierProperties to set
	 */
	public void addWeakClassifierProperty(WeakClassifierProperty weakClassifierProperty) {
		this._weakClassifierProperties.put(weakClassifierProperty.getID(), weakClassifierProperty);
	}

	/**
	 * @return the strongClassifierProperty
	 */
	public StrongClassifierProperty getStrongClassifierProperty() {
		return _strongClassifierProperty;
	}

	/**
	 * @param strongClassifierProperty the strongClassifierProperty to set
	 */
	public void setStrongClassifierProperty(StrongClassifierProperty strongClassifierProperty) {
		this._strongClassifierProperty = strongClassifierProperty;
	}
}
