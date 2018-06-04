/**
 * 
 */
package jp.jobdirect.dbmatching.model;

/**
 * 単一のデータを扱うインタフェース。
 *
 */
public interface Record {
	public String getId();
	public Attribute[] getAttributes();
	public boolean hasAttribute(Attribute attribute);
	public Value getValue(Attribute attribute);
	public Value getValue(String name);
	public String getStringValue(String name);
	public float  getFloatValue(String name);
}
