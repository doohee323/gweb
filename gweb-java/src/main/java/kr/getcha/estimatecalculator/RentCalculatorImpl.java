package kr.getcha.estimatecalculator;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.simmetrics.StringMetric;
import org.simmetrics.builders.StringMetricBuilder;
import org.simmetrics.metrics.CosineSimilarity;
import org.simmetrics.simplifiers.Simplifiers;
import org.simmetrics.tokenizers.Tokenizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tz.GWeb;

import example.domain.CarEstimate;

public class RentCalculatorImpl implements RentCalculator {

	static final Logger log = LoggerFactory.getLogger(RentCalculatorImpl.class);

	private String APP_NAME = "gweb";

	private static List<JsonArray> modelArry = new ArrayList<JsonArray>();
	private static JsonArray ownModels = null;

	public static float compareModels(String str1, String str2) {

		// String str1 = "This is a sentence. It is made of words";
		// String str2 = "This sentence is similar. It has almost the same
		// words";

		StringMetric metric = StringMetricBuilder.with(new CosineSimilarity<String>())
				.simplify(Simplifiers.toLowerCase(Locale.ENGLISH)).simplify(Simplifiers.replaceNonWord())
				.tokenize(Tokenizers.whitespace()).build();

		float result = metric.compare(str1, str2); // 0.5720

		return result;
	}

	public void getModelList() {

		if (!modelArry.isEmpty())
			return;

		String cache = "false";
		JsonArray params = new JsonArray();

		try {
			// _차명매핑 가져오기
			JsonObject models = new JsonObject();
			models.addProperty("_sheet", "_차명매핑");
			models.addProperty("_event", "getList");
			models.addProperty("input", "[1,1,'_end',4]");
			models.addProperty("_cache", cache);
			params.add(models);

			GWeb gweb = GWeb.getInstance();
			ArrayList<Object> arry = new ArrayList<Object>();
			arry.add(params.toString());

			int n = 1 + (int) (Math.random() * 1);
			String appName = APP_NAME + Integer.toString(n);

			JsonObject json = gweb.mainExec(appName, arry);
			JsonObject output = (JsonObject) new JsonParser().parse(json.get("output").toString());

			JsonParser parser = new JsonParser();
			JsonElement elem = parser.parse(output.get("rows").toString());
			ownModels = elem.getAsJsonArray();

			// _이름매핑 가져오기
			params = new JsonArray();
			JsonObject mappings = new JsonObject();
			mappings.addProperty("_sheet", "_이름매핑");
			mappings.addProperty("_event", "getList");
			mappings.addProperty("_cache", cache);
			params.add(mappings);

			ArrayList<Object> arry2 = new ArrayList<Object>();
			arry2.add(params.toString());

			JsonObject json2 = gweb.mainExec(appName, arry2);
			JsonObject output2 = (JsonObject) new JsonParser().parse(json2.get("output").toString());

			JsonParser parser2 = new JsonParser();
			JsonElement elem2 = parser2.parse(output2.get("rows").toString());
			JsonArray trans = elem2.getAsJsonArray();

			// 차량DB 가져오기
			params = new JsonArray();
			JsonObject carIno = new JsonObject();
			carIno.addProperty("_sheet", "차명DB");
			carIno.addProperty("_event", "getList");
			carIno.addProperty("input", "[2,6,'_end',4]");
			carIno.addProperty("_cache", cache);
			params.add(carIno);

			ArrayList<Object> arry3 = new ArrayList<Object>();
			arry3.add(params.toString());

			JsonObject json3 = gweb.mainExec(appName, arry3);
			JsonObject output3 = (JsonObject) new JsonParser().parse(json3.get("output").toString());

			JsonParser parser3 = new JsonParser();
			JsonElement elem3 = parser3.parse(output3.get("rows").toString());
			JsonArray cars = elem3.getAsJsonArray();

			List<JsonArray> ownModels2 = new ArrayList<JsonArray>();
			for (int i = 0; i < ownModels.size(); i++) {
				JsonArray row = ownModels.get(i).getAsJsonArray();
				String id = row.get(0).getAsString();
				String brand = row.get(1).getAsString();
				String model = row.get(2).getAsString();
				String grade = row.get(3).getAsString();

				for (int j = 0; j < trans.size(); j++) {
					String key = trans.get(j).getAsJsonArray().get(0).getAsString();
					String val = trans.get(j).getAsJsonArray().get(1).getAsString();
					if (brand.contains(key)) {
						brand = brand.replace(key, val);
					}
					if (model.contains(key)) {
						model = model.replace(key, val);
					}
					if (grade.contains(key)) {
						grade = grade.replace(key, val);
					}
				}

				JsonArray array = new JsonArray();
				array.add(brand);
				array.add(model);
				array.add(grade);
				array.add(id);
				ownModels2.add(array);
			}

			List<JsonArray> dbModels = new ArrayList<JsonArray>();
			for (int i = 0; i < cars.size(); i++) {
				JsonArray row = cars.get(i).getAsJsonArray();
				String brand = row.get(0).getAsString();
				String model = row.get(1).getAsString();
				String grade = row.get(2).getAsString();

				for (int j = 0; j < trans.size(); j++) {
					String key = trans.get(j).getAsJsonArray().get(0).getAsString();
					String val = trans.get(j).getAsJsonArray().get(1).getAsString();
					if (brand.contains(key)) {
						brand = brand.replace(key, val);
					}
					if (model.contains(key)) {
						model = model.replace(key, val);
					}
					if (grade.contains(key)) {
						grade = grade.replace(key, val);
					}
				}

				JsonArray array = new JsonArray();
				array.add(brand);
				array.add(model);
				array.add(grade);
				dbModels.add(array);
			}

			for (int j = 1; j < ownModels2.size(); j++) {
				String ownNm2 = ownModels2.get(j).getAsJsonArray().get(0).toString() + " "
						+ ownModels2.get(j).getAsJsonArray().get(1).toString() + " "
						+ ownModels2.get(j).getAsJsonArray().get(2).toString();
				ownNm2 = ownNm2.replace("\"", "").replace(",", " ").replace("[", "").replace("]", "");
				float max = 0;
				int rowNum = 0;
				String ownNm = "";
				for (int i = 0; i < dbModels.size(); i++) {
					String dbNm = dbModels.get(i).getAsJsonArray().get(0).getAsString() + " "
							+ dbModels.get(i).getAsJsonArray().get(1).getAsString() + " "
							+ dbModels.get(i).getAsJsonArray().get(2).getAsString();
					if (!ownModels2.get(j).getAsJsonArray().get(0).getAsString()
							.equals(dbModels.get(i).getAsJsonArray().get(0).getAsString())) {
						continue;
					}
					if (ownNm2.indexOf("경유") > -1 || ownNm2.indexOf("휘발유") > -1 || ownNm2.indexOf("LPG") > -1) {
						if (ownNm2.indexOf("경유") > -1 && (dbNm.indexOf("휘발유") > -1 || dbNm.indexOf("LPG") > -1)) {
							continue;
						}
						if (ownNm2.indexOf("휘발유") > -1 && (dbNm.indexOf("경유") > -1 || dbNm.indexOf("LPG") > -1)) {
							continue;
						}
						if (ownNm2.indexOf("LPG") > -1 && (dbNm.indexOf("휘발유") > -1 || dbNm.indexOf("경유") > -1)) {
							continue;
						}
					}
					float score0 = compareModels(dbModels.get(i).getAsJsonArray().get(1).getAsString(),
							ownModels2.get(j).getAsJsonArray().get(1).toString());
					float score = compareModels(dbNm, ownNm2);
					if (max < score && score0 > 0.4 && score > 0.4) {
						if (ownNm2.indexOf("쏘울") > -1 || dbNm.indexOf("레이") > -1) {
							System.out.println("");
						}
						max = score;
						rowNum = i + 1;
						ownNm = cars.get(i).getAsJsonArray().get(1).getAsString() + " "
								+ cars.get(i).getAsJsonArray().get(2).getAsString();
					}
				}
				JsonArray array = new JsonArray();
				array.add(j); // ownModel Inx
				array.add(rowNum); // dbModel Inx
				array.add(ownNm2); // ownModel Nm
				array.add(ownNm); // dbModel Nm
				modelArry.add(array);
			}
		} catch (Exception e) {
			log.error(e.toString());
		}

		for (int i = 0; i < modelArry.size(); i++) {
			System.out.println(modelArry.get(i).toString());
		}

	}

	public CarEstimate getResult(String version, String brand, String model, String grade, boolean returnCar,
			int duration, boolean age, double deposit, double commission, boolean includeRepair, int ownerType) {

		getModelList();

		int ownInx = 0;
		for (int i = 1; i < ownModels.size(); i++) {
			if (ownModels.get(i).toString().indexOf(grade) > -1) {
				if (ownModels.get(i).getAsJsonArray().get(1).toString().equals("\"" + brand + "\"")
						&& ownModels.get(i).getAsJsonArray().get(2).toString().equals("\"" + model + "\"")
						&& ownModels.get(i).getAsJsonArray().get(3).toString().equals("\"" + grade + "\"")) {
					ownInx = i;
					break;
				}
			}
		}
		String modelNm = "";
		for (int i = 1; i < modelArry.size(); i++) {
			if (modelArry.get(i).getAsJsonArray().get(0).getAsInt() == ownInx) {
				modelNm = modelArry.get(i).getAsJsonArray().get(3).getAsString();
				break;
			}
		}

		if (modelNm.equals("")) {
			// not exit!
			return new CarEstimate();
		}
		System.out.println("======================modelNm:" + modelNm);

		// String brand, //차량브랜드
		// String model, //차량모델명
		// String grade, //차량등급명
		// boolean returnCar, //인수 :false, 반납:true
		// int duration, //기간(개월수)
		// boolean age, //true:26세이상, false:26세미만
		// double deposit, //보증금(%)
		// double commission, //수수료(%)
		// boolean includeRepair, //true:정비포함, false:정비불포함
		// int ownerType // 위 참조

		CarEstimate caret = null;

		String cache = "false";
		JsonArray params = new JsonArray();

		JsonObject getLineFromRowNum = new JsonObject();
		getLineFromRowNum.addProperty("_sheet", "차명DB");
		getLineFromRowNum.addProperty("_event", "getLineFromRowNum");
		getLineFromRowNum.addProperty("input", modelNm);
		getLineFromRowNum.addProperty("col", 9);
		getLineFromRowNum.addProperty("output", "['I','O','J','P','N','X','Q','Z','L','M','W']");
		params.add(getLineFromRowNum);

		JsonObject updateFromShared = new JsonObject();
		updateFromShared.addProperty("_sheet", "렌터카견적");
		updateFromShared.addProperty("_event", "updateFromShared");
		updateFromShared.addProperty("N7", "O");
		updateFromShared.addProperty("O7", "J");
		updateFromShared.addProperty("P8", "P");
		updateFromShared.addProperty("S7", "N");
		updateFromShared.addProperty("T7", "X");
		updateFromShared.addProperty("U7", "Q");
		updateFromShared.addProperty("V7", "Z");
		updateFromShared.addProperty("W7", "L");
		updateFromShared.addProperty("X7", "M");
		params.add(updateFromShared);

		String s = "function _func(){ ";
		s += "if (sheet.getRange('W7').getValue() == 0) { ";
		s += "  sheet.getRange('W7').setValue(1.1);";
		s += "} else { ";
		s += "  sheet.getRange('W7').setValue(1.17150); ";
		s += "} ";
		s += "}";

		JsonObject script = new JsonObject();
		script.addProperty("_sheet", "렌터카견적");
		script.addProperty("_event", "script");
		script.addProperty("_script", s);
		params.add(script);

		JsonObject update2 = new JsonObject();
		update2.addProperty("_sheet", "2차탁송료");
		update2.addProperty("_event", "update");
		update2.addProperty("G2", "과천"); // 1차탁송료
		update2.addProperty("G3", "전라도"); // 2차탁송료
		params.add(update2);

		JsonObject getValueFromUpdate = new JsonObject();
		getValueFromUpdate.addProperty("_sheet", "렌터카견적");
		getValueFromUpdate.addProperty("_event", "getValueFromUpdate");
		getValueFromUpdate.addProperty("_main", "true");
		getValueFromUpdate.addProperty("_result", "true");
		getValueFromUpdate.addProperty("_version", version);
		getValueFromUpdate.addProperty("_cache", cache);

		JsonObject _input = new JsonObject();
		_input.addProperty("C4", modelNm); // 견적차종
		_input.addProperty("V5", returnCar); // 인수옵션
		_input.addProperty("T5", 4); // 약정거리
		_input.addProperty("W5", duration); // 계약기간
		_input.addProperty("P26", 2); // 대물/자손
		_input.addProperty("G9", deposit); // 보증금
		_input.addProperty("K9", commission); // 수수료
		_input.addProperty("U5", includeRepair); // 정비상태
		getValueFromUpdate.addProperty("_input", _input.toString());

		JsonObject _output = new JsonObject();
		_output.addProperty("col_1", "I13");
		_output.addProperty("col_2", "N4");
		_output.addProperty("col_3", "U19");
		_output.addProperty("col_4", "B10");
		_output.addProperty("col_5", "C10");
		_output.addProperty("col_6", "D10");
		_output.addProperty("col_7", "O27");
		_output.addProperty("col_8", "O28");
		getValueFromUpdate.addProperty("_output", _output.toString());
		params.add(getValueFromUpdate);

		try {
			GWeb gweb = GWeb.getInstance();
			ArrayList<Object> arry = new ArrayList<Object>();
			arry.add(params.toString());

			int n = 1 + (int) (Math.random() * 1);
			// int n = 3;
			String appName = APP_NAME + Integer.toString(n);

			JsonObject json = gweb.mainExec(appName, arry);
			log.debug(json.toString());
			JsonObject output = (JsonObject) new JsonParser().parse(json.get("output").toString());

			JsonObject result = new JsonObject();
			result.addProperty("version", version); // version
			result.addProperty("monthlyPay", parseLong(output.get("col_1").toString())); // 월납부금
			result.addProperty("rate", Double.parseDouble(output.get("col_2").toString())); // 금리
			result.addProperty("restValue", Double.parseDouble(output.get("col_2").toString())); // 잔존가치
			result.addProperty("deliveryCostFirst", parseInt(output.get("col_3").toString())); // 1차탁송료
			result.addProperty("deliveryCostSecond", parseInt(output.get("col_4").toString())); // 2차탁송료
			result.addProperty("additionalCost", parseInt(output.get("col_5").toString())); // 부대비용
			result.addProperty("insuranceCost", parseInt(output.get("col_6").toString())); // 보험료
			result.addProperty("insuranceCostMyCar", parseInt(output.get("col_7").toString())); // 자차보험료

			Gson gson = new Gson();
			caret = gson.fromJson(result, CarEstimate.class);

			String key = version + brand + model + grade + returnCar + duration + age + deposit + commission + includeRepair
					+ ownerType;
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(key.getBytes(), 0, key.length());
			caret.id = new BigInteger(1, m.digest()).toString(16);
		} catch (Exception e) {
			log.error(e.toString());
		}

		return caret;
	}

	public static int parseInt(String input) {
		if (input.indexOf(".") > -1) {
			input = input.substring(0, input.indexOf("."));
		}
		return Integer.parseInt(input);
	}

	public static long parseLong(String input) {
		if (input.indexOf(".") > -1) {
			input = input.substring(0, input.indexOf("."));
		}
		return Long.parseLong(input);
	}

	public static void main(String[] args) {
		RentCalculatorImpl impl = new RentCalculatorImpl();
		Gson gson = new Gson();

		// 기아 All New Canival (경유)11인승 노블레스 (A/T)
		CarEstimate caret = impl.getResult("1", "기아", "All New Canival", "(경유)11인승 노블레스 (A/T)", false, 4, true, 0, 0.01,
				false, 1);
		String jsonInString = gson.toJson(caret);
		log.debug(jsonInString);

		System.out.println("!![동기 처리]!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		long startTime = System.currentTimeMillis();

		caret = impl.getResult("1", "그랜저", "5G 그랜저 HG300", "", true, 4, true, 0, 0.01, false, 1);
		jsonInString = gson.toJson(caret);
		log.debug(jsonInString);

		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("!!!!!!!!!!!!!!!!!!!!!! 1111 estimatedTime:" + estimatedTime);

		// caret = impl.getResult("1", "투싼 디젤 1.7", "", true, 4, true, 0, 0.01,
		// true, 1);
		// jsonInString = gson.toJson(caret);
		// log.debug(jsonInString);
		//
		// caret = impl.getResult("3", "K9 5.0 퀀텀", "", true, 1, true, 0, 0.01,
		// true, 1);
		// jsonInString = gson.toJson(caret);
		// log.debug(jsonInString);
		//
		estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("!!!!!!!!!!!!!!!!!!!!!! 1111 estimatedTime:" + estimatedTime);

		// System.out.println("!![비동기
		// 처리]!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		// startTime = System.currentTimeMillis();
		//
		// new Thread(new Runnable() {
		// public void run() {
		// CarEstimate caret = impl.getResult("그랜저", "5G 그랜저 HG300", "", true,
		// 4, true, 0, 0.01, false, 1);
		// String jsonInString = gson.toJson(caret);
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!! 1111");
		// System.out.println(jsonInString);
		// }
		// }).start();
		//
		// new Thread(new Runnable() {
		// public void run() {
		// CarEstimate caret = impl.getResult("1", "투싼 디젤 1.7", "", true, 4,
		// true, 0, 0.01, true, 1);
		// String jsonInString = gson.toJson(caret);
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!! 3333");
		// System.out.println(jsonInString);
		// }
		// }).start();
		//
		// new Thread(new Runnable() {
		// public void run() {
		// CarEstimate caret = impl.getResult("3", "K9 5.0 퀀텀", "", true, 1,
		// true, 0, 0.01, true, 1);
		// String jsonInString = gson.toJson(caret);
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!! 4444");
		// System.out.println(jsonInString);
		// }
		// }).start();
		//
		// do {
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// // estimatedTime = System.currentTimeMillis() - startTime;
		// // System.out.println("============================2222
		// // estimatedTime:" + estimatedTime);
		//
		// } while (true);

	}
}

// JsonObject initDoc = new JsonObject();
// initDoc.addProperty("_folderPath", "외부견적기");
// initDoc.addProperty("_doc", "s_gweb");
// initDoc.addProperty("_event", "initDoc");
// initDoc.addProperty("_docCount", 5);
// params.add(initDoc);

// JsonObject getRowNum = new JsonObject();
// getRowNum.addProperty("_sheet", "차명DB");
// getRowNum.addProperty("_event", "getRowNum");
// getRowNum.addProperty("input", model);
// getRowNum.addProperty("col", 9);
// params.add(getRowNum);
//
// JsonObject getALine = new JsonObject();
// getALine.addProperty("_sheet", "차명DB");
// getALine.addProperty("_event", "getALine");
// getALine.addProperty("output",
// "['I','O','J','P','N','X','Q','Z','L','M','W']");
// params.add(getALine);

// JsonObject update = new JsonObject();
// update.addProperty("_sheet", "렌터카견적");
// update.addProperty("_event", "update");
// update.addProperty("_main", "true");
// update.addProperty("_cache", cache);
// update.addProperty("C4", model); // 견적차종
// update.addProperty("V5", returnCar); // 인수옵션
// update.addProperty("T5", 4); // 약정거리
// update.addProperty("W5", duration); // 계약기간
// update.addProperty("P26", 2); // 대물/자손
// update.addProperty("G9", deposit); // 보증금
// update.addProperty("K9", commission); // 수수료
// update.addProperty("U5", includeRepair); // 정비상태
// update.addProperty("B10", 84000); // 1차탁송료
// update.addProperty("C10", 198000); // 2차탁송료
// params.add(update);
//
// JsonObject getValue = new JsonObject();
// getValue.addProperty("_sheet", "렌터카견적");
// getValue.addProperty("_event", "getValue");
// getValue.addProperty("_result", "true");
// getValue.addProperty("_cache", cache);
// getValue.addProperty("col_1", "I13");
// getValue.addProperty("col_2", "N4");
// getValue.addProperty("col_3", "U19");
// getValue.addProperty("col_4", "B10");
// getValue.addProperty("col_5", "C10");
// getValue.addProperty("col_6", "D10");
// getValue.addProperty("col_7", "O27");
// getValue.addProperty("col_8", "O28");
// params.add(getValue);

// caret = impl.getResult("1", "투싼 디젤 1.7", "", true, 4, true, 0, 0.01,
// true, 1);
// jsonInString = gson.toJson(caret);
// log.debug(jsonInString);
//
// caret = impl.getResult("1", "투싼 디젤 1.7", "", true, 4, true, 0, 0.01,
// true, 1);
// jsonInString = gson.toJson(caret);
// log.debug(jsonInString);
//
// caret = impl.getResult("2", "쉐보레 올란도 7인승 1.6 디젤", "", true, 3, true,
// 0, 0.01, true, 1);
// jsonInString = gson.toJson(caret);
// log.debug(jsonInString);
//
// caret = impl.getResult("3", "K9 5.0 퀀텀", "", true, 1, true, 0, 0.01,
// true, 1);
// jsonInString = gson.toJson(caret);
// log.debug(jsonInString);
//
// caret = impl.getResult("4", "올뉴카니발 아웃도어 11인승 디젤", "", true, 4, true,
// 0, 0.03, true, 1);
// jsonInString = gson.toJson(caret);
// log.debug(jsonInString);
//
// caret = impl.getResult("5", "벨로스터 1.6 GDI", "", true, 3, true, 0,
// 0.01, false, 1);
// jsonInString = gson.toJson(caret);
// log.debug(jsonInString);
//
// caret = impl.getResult("6", "쉐보레 스파크 밴", "", true, 1, true, 0, 0.02,
// true, 1);
// jsonInString = gson.toJson(caret);
// log.debug(jsonInString);