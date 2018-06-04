package jp.jobdirect.dbmatching.app;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jp.jobdirect.dbmatching.model.Attribute;
import jp.jobdirect.dbmatching.model.FloatValue;
import jp.jobdirect.dbmatching.model.Record;
import jp.jobdirect.dbmatching.model.StringValue;
import jp.jobdirect.dbmatching.model.Value;

public class DefaultRecord implements Record {

	private String _id;
	private Collection<Attribute> _attributes;
	private Map<String, Value> _values = new HashMap<String, Value>();

	public DefaultRecord(Collection<Attribute> attributes){
		this._attributes = attributes;
	}

	@Override
	public Attribute[] getAttributes() {
		return this._attributes.toArray(new Attribute[0]);
	}

	@Override
	public boolean hasAttribute(Attribute attribute) {
		for(Attribute a : this._attributes){
			if(a.getName().equals(attribute.getName())){
				return true;
			}
		}
		return false;
	}

	@Override
	public String getId(){
		return this._id;
	}

	public void setId(String id){
		this._id = id;
	}

	@Override
	public Value getValue(Attribute attribute) {
		if(this._values.containsKey(attribute.getName())){
			return this._values.get(attribute.getName());
		}else{
			return null;
		}
	}

	@Override
	public Value getValue(String name) {
		if(this._values.containsKey(name)){
			return this._values.get(name);
		}else{
			return null;
		}
	}

	@Override
	public String getStringValue(String name){
		Value value = this.getValue(name);
		if(value != null && value instanceof StringValue){
			return ((StringValue)value).getString();
		}else{
			return null;
		}
	}

	@Override
	public float getFloatValue(String name){
		Value value = this.getValue(name);
		if(value != null && value instanceof FloatValue){
			return ((FloatValue)value).getFloat();
		}else{
			return 0f;
		}
	}

	public void setValue(String key, String value){
		Value newValue = null;
		for(Attribute a : this._attributes){
			if(a.getName().equals(key)){
				newValue = new StringValue(a, value);
				break;
			}
		}

		if(newValue == null){
			//throw
		}else{
			this._values.put(key, newValue);
		}
	}

	public void setValue(String key, float value){
		Value newValue = null;
		for(Attribute a : this._attributes){
			if(a.getName().equals(key)){
				newValue = new FloatValue(a, value);
				break;
			}
		}

		if(newValue == null){
			//throw
		}else{
			this._values.put(key, newValue);
		}
	}

	@Override
	public String toString(){
		Value nameValue = this._values.get("NAME");
		Value prefValue = this._values.get("PREF");
		Value cityValue = this._values.get("CITY");
		Value addressValue = this._values.get("ADDRESS");
		Value latitudeValue = this._values.get("LATITUDE");
		Value longitudeValue = this._values.get("LONGITUDE");
		Value phone1Value = this._values.get("PHONE1");
		Value revValue = this._values.get("REV");
		Value catValue = this._values.get("CAT");
		return new StringBuffer()
				.append("record: {id=")
				.append(this.getId())
				.append(", name=")
				.append(nameValue.getString())
				.append(", pref=")
				.append(prefValue.getString())
				.append(", city=")
				.append(cityValue.getString())
				.append(", address=")
				.append(addressValue.getString())
				.append(", latitude=")
				.append(latitudeValue.getString())
				.append(", longitude=")
				.append(longitudeValue.getString())
				.append(", tel=")
				.append(phone1Value.getString())
				.append(", review=")
				.append(revValue.getString())
				.append(", category=")
				.append(catValue.getString())
				.append("}")
				.toString();
	}
}
