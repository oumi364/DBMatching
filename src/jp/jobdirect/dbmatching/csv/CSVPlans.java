package jp.jobdirect.dbmatching.csv;

import com.orangesignal.csv.annotation.CsvColumn;
import com.orangesignal.csv.annotation.CsvEntity;

@CsvEntity
public class CSVPlans {
    @CsvColumn(name = "YADO_ID")
    public String _id;

    @CsvColumn(name = "NAME")
    public String _name;
    
    @CsvColumn(name = "NPLANS")
    public int _planCount;
    
    @CsvColumn(name = "MINPRICE")
    public int _minimumPrice;
    
    @CsvColumn(name = "MAXPRICE")
    public int _maximumPrice;
    
    @CsvColumn(name = "AVGPRICE")
    public int _averagePrice;

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
	 * @return the planCount
	 */
	public int getPlanCount() {
		return this._planCount;
	}

	/**
	 * @param planCount the planCount to set
	 */
	public void setPlanCount(int planCount) {
		this._planCount = planCount;
	}

	/**
	 * @return the minimumPrice
	 */
	public int getMinimumPrice() {
		return this._minimumPrice;
	}

	/**
	 * @param minimumPrice the minimumPrice to set
	 */
	public void setMinimumPrice(int minimumPrice) {
		this._minimumPrice = minimumPrice;
	}

	/**
	 * @return the maximumPrice
	 */
	public int getMaximumPrice() {
		return this._maximumPrice;
	}

	/**
	 * @param maximumPrice the maximumPrice to set
	 */
	public void setMaximumPrice(int maximumPrice) {
		this._maximumPrice = maximumPrice;
	}

	/**
	 * @return the averagePrice
	 */
	public int getAveragePrice() {
		return this._averagePrice;
	}

	/**
	 * @param averagePrice the averagePrice to set
	 */
	public void setAveragePrice(int averagePrice) {
		this._averagePrice = averagePrice;
	}

    
}
