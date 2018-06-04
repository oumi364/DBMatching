package jp.jobdirect.dbmatching.csv;

import com.orangesignal.csv.annotation.CsvColumn;
import com.orangesignal.csv.annotation.CsvEntity;

import jp.jobdirect.dbmatching.app.ApplicationSetting;

@CsvEntity
public class CSVMatch {
	static final boolean IS_HOTELS = ApplicationSetting.TARGET_HOTEL;

    @CsvColumn(name = "SRC_ID")
    public String _leftId;

    @CsvColumn(name = "DST_ID")
    public String _rightId;

    @CsvColumn(name = "SRC_NAME")
    public String _leftName;

    @CsvColumn(name = "DST_NAME")
    public String _rightName;

	public String getLeftId() {
		return _leftId;
	}

	public void setLeftId(String leftId) {
		_leftId = leftId;
	}

	public String getRightId() {
		return _rightId;
	}

	public void setRightId(String rightId) {
		_rightId = rightId;
	}

	public String getLeftName() {
		return _leftName;
	}

	public void setLeftName(String leftName) {
		_leftName = leftName;
	}

	public String getRightName() {
		return _rightName;
	}

	public void setRightName(String rightName) {
		_rightName = rightName;
	}
}
