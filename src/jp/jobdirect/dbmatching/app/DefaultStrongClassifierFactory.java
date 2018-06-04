package jp.jobdirect.dbmatching.app;

import jp.jobdirect.dbmatching.classifier.StrongClassifier;
import jp.jobdirect.dbmatching.classifier.StrongClassifierFactory;

public class DefaultStrongClassifierFactory extends StrongClassifierFactory {
	public StrongClassifier create()
	{
		Class<?> strongClassifierClass;
		try{
			strongClassifierClass = Class.forName(this.getStrongClassifierProperty().getClassName());
		}catch(ClassNotFoundException ex){
			ex.printStackTrace();
			return null;
		}

		StrongClassifier newStrongClassifier;
		try{
			newStrongClassifier = (StrongClassifier)strongClassifierClass.newInstance();
		}catch(IllegalAccessException ex){
			ex.printStackTrace();
			return null;
		}catch(InstantiationException ex){
			ex.printStackTrace();
			return null;
		}

		newStrongClassifier.addWeakClassifier(new AddressDistanceClassifier());
//		newStrongClassifier.addWeakClassifier(new HotelNameDifferenceClassifier());
		newStrongClassifier.addWeakClassifier(new AddressInclusionClassifier());
		newStrongClassifier.addWeakClassifier(new HotelNameDistanceClassifier());
		newStrongClassifier.addWeakClassifier(new HotelNameInclusionClassifier());
		newStrongClassifier.addWeakClassifier(new GeographicCoordinateDistanceClassifier());
//		newStrongClassifier.addWeakClassifier(new PrefectureClassifier());
//		newStrongClassifier.addWeakClassifier(new PlansDistanceClassifier());
		newStrongClassifier.addWeakClassifier(new PhoneNumberClassifier());
//		newStrongClassifier.addWeakClassifier(new NamePhoneticClassifier());
//		newStrongClassifier.addWeakClassifier(new URLClassifier());
//		newStrongClassifier.addWeakClassifier(new ReviewCountClassifier());
//		newStrongClassifier.addWeakClassifier(new CategoryClassifier());

		return newStrongClassifier;
	}

}
