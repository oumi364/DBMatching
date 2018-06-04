package jp.jobdirect.dbmatching.app;

import jp.jobdirect.dbmatching.model.Attribute;
import jp.jobdirect.dbmatching.model.Value;

public class DefaultValue implements Value {
//	private Attribute _attribute;
	private Object _value;

	public DefaultValue(Attribute attribute, String value){
//		this._attribute = attribute;
		this._value = value;
	}
	public DefaultValue(Attribute attribute, float value){
//		this._attribute = attribute;
		this._value = value;
	}
	
	public String getString(){
		if(this._value == null){
			return "(nil)";
		}else{
			return this._value.toString();
		}
	}
	
	public float getFloat(){
		if(this._value instanceof Float){
			return (Float)this._value;
		}else if(this._value instanceof String){
			return Float.parseFloat((String)this._value);
		}else{
			return Float.parseFloat(this._value.toString());
		}
	}
}
