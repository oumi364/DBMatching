package jp.jobdirect.dbmatching.app;

public interface ApplicationSetting {
//	public static final boolean TARGET_HOTEL = true;
	public static final boolean TARGET_HOTEL = false;

	/** null でない場合、学習結果を指定したファイルに出力する。その場合、識別・評価の処理は行われない。
	 * SAVE_MODEL_TO と LOAD_MODEL_FROM を共に non-null にすると、モデルデータを読み込んで、それを書き込んで、処理を終えてしまう。 */
//	public static String SAVE_MODEL_TO = "model_AB.dat"; // "model.dat";
//	public static String SAVE_MODEL_TO = "model_Bwk.dat"; // "model.dat";
	public static String SAVE_MODEL_TO = null; // "model.dat";


	/** null でない場合、学習結果を指定したファイルから読み込む。その場合、学習の処理は行われない。
	 * SAVE_MODEL_TO と LOAD_MODEL_FROM を共に non-null にすると、モデルデータを読み込んで、それを書き込んで、処理を終えてしまう。 */
	public static String LOAD_MODEL_FROM = "model_A8.dat"; // "model.dat";
//	public static String LOAD_MODEL_FROM = "model_Bwk.dat"; // "model.dat";
//	public static String LOAD_MODEL_FROM = null; // "model.dat";
}
