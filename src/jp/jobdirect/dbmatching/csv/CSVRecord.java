package jp.jobdirect.dbmatching.csv;

import com.orangesignal.csv.annotation.CsvColumn;
import com.orangesignal.csv.annotation.CsvEntity;

@CsvEntity
public class CSVRecord {
//	static final boolean IS_HOTELS = ApplicationSetting.TARGET_HOTEL;

    @CsvColumn(name = "ID")
    public String _id;

    @CsvColumn(name = "NAME")
    public String _name;

    @CsvColumn(name = "PREF")
    public String _pref;

    @CsvColumn(name = "CITY")
    public String _city;

    @CsvColumn(name = "ADDRESS")
    public String _address;

    @CsvColumn(name = "LATITUDE")
    public String _latitude;

    @CsvColumn(name = "LONGITUDE")
    public String _longitude;

    @CsvColumn(name = "TEL")
    public String _phone1;

    @CsvColumn(name = "REV")
    public String _rev;

    @CsvColumn(name = "CAT")
    public String _cat;


	/**
	 * @return the category
	 */
	public String getCat() {
		return this._cat;
	}

	/**
	 * @param category the category to set
	 */
	public void setCat(String cat) {
		this._cat = cat;
	}

	/**
	 * @return the review
	 */
	public String getRev() {
		return this._rev;
	}

	/**
	 * @param review the review to set
	 */
	public void setRev1(String rev) {
		this._rev = rev;
	}

	/**
	 * @return the phone1
	 */
	public String getPhone1() {
		return this._phone1;
	}

	/**
	 * @param phone1 the phone1 to set
	 */
	public void setPhone1(String phone1) {
		this._phone1 = phone1;
	}

	/**
	 * @return the phone2
	 */
//	public String getPhone2() {
//		return this._phone2;
//	}

	/**
	 * @param phone2 the phone2 to set
	 */
//	public void setPhone2(String phone2) {
//		this._phone2 = phone2;
//	}

	/**
	 * @return the phone3
	 */
//	public String getPhone3() {
//		return this._phone3;
//	}

	/**
	 * @param phone3 the phone3 to set
	 */
//	public void setPhone3(String phone3) {
//		this._phone3 = phone3;
//	}

	/**
	 * @return the id
	 */
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
	 * @return the name
	 */
	public String getName() {
		return this._name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this._name = name;
	}

	/**
	 * @return the zip
	 */
//	public String getZip() {
//		return this._zip;
//	}

	/**
	 * @param zip the zip to set
	 */
//	public void setZip(String zip) {
//		this._zip = zip;
//	}

	/**
	 * @return the pref
	 */
	public String getPref() {
		return this._pref;
	}

	/**
	 * @param pref the pref to set
	 */
	public void setPref(String pref) {
		this._pref = pref;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return this._city;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this._city = city;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return this._address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this._address = address;
	}

	/**
	 * @return the latitude
	 */
	public float getFloatLatitude() {
		if(this._latitude.equals("")){
			return 0f;
		}

		String n[] = this._latitude.split("\\.");
		if(n.length <= 2){
			return Float.parseFloat(this._latitude);
		}else if(n.length == 3){
			return Float.parseFloat(n[0]) + Float.parseFloat(n[1]) / 60 + Float.parseFloat(n[2]) / 3600;
		}else if(n.length == 4){
			return Float.parseFloat(n[0]) + Float.parseFloat(n[1]) / 60 + Float.parseFloat(n[2] + "." + n[3]) / 3600;
		}else{
			return 0f;
		}
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setFloatLatitude(float latitude) {
		this._latitude = Float.toString(latitude);
	}

	/**
	 * @return the longitude
	 */
	public float getFloatLongitude() {
		if(this._longitude.equals("")){
			return 0f;
		}

		String n[] = this._longitude.split("\\.");
		if(n.length <= 2){
			return Float.parseFloat(this._longitude);
		}else if(n.length == 3){
			return Float.parseFloat(n[0]) + Float.parseFloat(n[1]) / 60 + Float.parseFloat(n[2]) / 3600;
		}else if(n.length == 4){
			return Float.parseFloat(n[0]) + Float.parseFloat(n[1]) / 60 + Float.parseFloat(n[2] + "." + n[3]) / 3600;
		}else{
			return 0f;
		}
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setFloatLongitude(float longitude) {
		this._longitude = Float.toString(longitude);
	}

	/**
	 * @return the latitude
	 */
	public String getLatitude() {
		return this._latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(String latitude) {
		this._latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public String getLongitude() {
		return this._longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(String longitude) {
		this._longitude = longitude;
	}

	/**
	 * @return the url
	 */
//	public String getUrl() {
//		return this._url;
//	}

	/**
	 * @param url the url to set
	 */
//	public void setUrl(String url) {
//		this._url = url;
//	}

	/**
	 * @return the namePhonetic
	 */
//	public String getNamePhonetic() {
//		return this._namePhonetic;
//	}

	/**
	 * @param namePhonetic the namePhonetic to set
	 */
//	public void setNamePhonetic(String namePhonetic) {
//		this._namePhonetic = namePhonetic;
//	}

}
