package jp.jobdirect.dbmatching.app;

import jp.jobdirect.dbmatching.classifier.StrongClassifierProperty;

public class DefaultStrongClassifierProperty implements StrongClassifierProperty {
	
	private String _id;
	private String _className;
	private String _parameterBase;

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return this._id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this._id = id;
	}

	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this._className = className;
	}

	/**
	 * @param parameterBase the parameterBase to set
	 */
	public void setParameterBase(String parameterBase) {
		this._parameterBase = parameterBase;
	}

	@Override
	public String getClassName() {
		return this._className;
	}

	@Override
	public String getParameterBase() {
		return this._parameterBase;
	}

}
