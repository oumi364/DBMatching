package jp.jobdirect.dbmatching.model;

public class FloatValue implements Value {
	
	private Attribute _attribute;
	private float     _value;
	
	public FloatValue(Attribute attribute, float value){
		this._attribute = attribute;
		this._value = value;
	}
	
	public float getFloat(){
		return this._value;
	}

	@Override
	public String getString(){
		return Float.toString(this._value);
	}

	public Attribute getAttribute(){
		return this._attribute;
	}
}
