package jp.jobdirect.dbmatching.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import jp.jobdirect.dbmatching.classifier.StrongClassifierProperty;
import jp.jobdirect.dbmatching.classifier.WeakClassifierProperty;

public class DefaultSettings implements Settings {
	
	private String[] _packageBases = new String[0];
	private String   _strongClassifierId;
	private String   _strongClassifierClass;
	private String   _strongClassifierParameterBase;
	
	static public DefaultSettings create(){
		DefaultSettings newSettings = new DefaultSettings();
		Properties properties = new Properties();
		try{
			properties.load(new FileInputStream("default.properties"));
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		newSettings.load(properties);
		
		return newSettings;
	}
	
	public void load(Properties properties){
		// Load packageBases
		List<String> packageBases = new ArrayList<String>();
		for(int i = 0; ; i++){
			StringBuffer key = new StringBuffer();
			key.append("package.base");
			if(i > 0){ key.append(".").append(i); }
			if(properties.containsKey(key.toString())){
				String value = properties.getProperty(key.toString());
				packageBases.add(value);
			}else if(i > 0){
				break;
			}
		}
		this._packageBases = packageBases.toArray(new String[0]);
		
		// Load strongClassifier*
		this._strongClassifierId            = properties.getProperty("strongClassifier.id", "");
		this._strongClassifierClass         = properties.getProperty("strongClassifier.className", "");
		this._strongClassifierParameterBase = properties.getProperty("strongClassifier.parameterBase", "");
		
	}

	@Override
	public String getBaseDirectory() {
		return "";
	}

	@Override
	public Collection<WeakClassifierProperty> getWeakClassifierPropertes() {
		return new ArrayList<WeakClassifierProperty>();
	}

	@Override
	public StrongClassifierProperty getStrongClassifierProperty() {
		
		String classFullName = this._strongClassifierClass;
		for(String packageName : this._packageBases){
			String classNameCandidate = packageName + "." + this._strongClassifierClass;
//			System.out.println(classNameCandidate);
			try{
				Class.forName(classNameCandidate);
			}catch(ClassNotFoundException ex){
				continue;
			}
			classFullName = classNameCandidate;
			break;
		}
		
		DefaultStrongClassifierProperty defaultStrongClassifierProperty = new DefaultStrongClassifierProperty();
		defaultStrongClassifierProperty.setId(this._strongClassifierId);
		defaultStrongClassifierProperty.setClassName(classFullName);
		defaultStrongClassifierProperty.setParameterBase(this._strongClassifierParameterBase);
		
		return defaultStrongClassifierProperty;
	}

}
