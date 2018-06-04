package jp.jobdirect.dbmatching.app;

public class Random extends java.util.Random {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7788128026495949103L;
	private static long _theSeed = 123456789L;
	private static Random _instance = new Random();
	
	private Random()
	{
		super(_theSeed);
	}
	
	public static Random getInstance()
	{
		return Random._instance;
	}
	
}
