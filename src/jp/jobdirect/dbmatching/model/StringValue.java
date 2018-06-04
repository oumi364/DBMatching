package jp.jobdirect.dbmatching.model;

public class StringValue implements Value {
	
	private Attribute _attribute;
	private String    _value;
	
	public StringValue(Attribute attribute, String value){
		this._attribute = attribute;
		this._value = value;
	}

	@Override
	public String getString(){
		return this._value;
	}
	
	public Attribute getAttribute(){
		return this._attribute;
	}
}
