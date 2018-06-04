package jp.jobdirect.dbmatching.app;

import jp.jobdirect.dbmatching.model.Attribute;

public class DefaultAttribute implements Attribute {
	
	private String _name;
	private Class<?> _type;
	
	public DefaultAttribute(String name, Class<?> type){
		this._name = name;
		this._type = type;
	}

	@Override
	public String getName() {
		return this._name;
	}

	@Override
	public Class<?> getType() {
		return this._type;
	}

}
