package kr.getcha.estimatecalculator;

import example.domain.CarEstimate;

public interface RentCalculator {

	public static final int OWNER_TYPE_PRIVATE = 1; // 개인
	public static final int OWNER_TYPE_INDIVIDUAL_BUSINESS = 2;// 개인사업자
	public static final int OWNER_TYPE_CORPORATE_BUSINESS = 3;// 법인사업자

	public CarEstimate getResult(String version, // version
			String brand, // 차량브랜드
			String model, // 차량모델명
			String grade, // 차량등급명
			boolean returnCar, // 인수 :false, 반납:true
			int duration, // 기간(개월수)
			boolean age, // true:26세이상, false:26세미만
			double deposit, // 보증금(%)
			double commission, // 수수료(%)
			boolean includeRepair, // true:정비포함, false:정비불포함
			int ownerType // 위 참조
	);
}
