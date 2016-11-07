package example.estimate;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tz.GWeb;

import example.domain.CarEstimate;

public class RentCalculatorImpl implements RentCalculator {

	static final Logger log = LoggerFactory.getLogger(RentCalculatorImpl.class);

	private String APP_NAME = "gweb";

	public CarEstimate getResult(String version, String model, boolean returnCar, int duration, boolean age,
			double deposit, double commission, boolean includeRepair, int ownerType) {
		System.out.println("======================modelNm:" + model);

		CarEstimate caret = null;

		String cache = "false";
		JsonArray params = new JsonArray();

		JsonObject getLineFromRowNum = new JsonObject();
		getLineFromRowNum.addProperty("_sheet", "car_DB");
		getLineFromRowNum.addProperty("_event", "getLineFromRowNum");
		getLineFromRowNum.addProperty("input", model);
		getLineFromRowNum.addProperty("col", 9);
		getLineFromRowNum.addProperty("output", "['I','O','J','P','N','X','Q','Z','L','M','W']");
		params.add(getLineFromRowNum);

		JsonObject updateFromShared = new JsonObject();
		updateFromShared.addProperty("_sheet", "rent_estimate");
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
		script.addProperty("_sheet", "rent_estimate");
		script.addProperty("_event", "script");
		script.addProperty("_script", s);
		params.add(script);

		JsonObject update2 = new JsonObject();
		update2.addProperty("_sheet", "2consignment");
		update2.addProperty("_event", "update");
		update2.addProperty("G2", "과천"); // 1consignment
		update2.addProperty("G3", "전라도"); // 2consignment
		params.add(update2);

		JsonObject getValueFromUpdate = new JsonObject();
		getValueFromUpdate.addProperty("_sheet", "rent_estimate");
		getValueFromUpdate.addProperty("_event", "getValueFromUpdate");
		getValueFromUpdate.addProperty("_main", "true");
		getValueFromUpdate.addProperty("_result", "true");
		getValueFromUpdate.addProperty("_version", version);
		getValueFromUpdate.addProperty("_cache", cache);

		JsonObject _input = new JsonObject();
		_input.addProperty("C4", model); // car name
		_input.addProperty("V5", returnCar);
		_input.addProperty("T5", 4);
		_input.addProperty("W5", duration);
		_input.addProperty("P26", 2);
		_input.addProperty("G9", deposit);
		_input.addProperty("K9", commission);
		_input.addProperty("U5", includeRepair);
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
			result.addProperty("monthlyPay", parseLong(output.get("col_1").toString())); // monthly_payment
			result.addProperty("rate", Double.parseDouble(output.get("col_2").toString())); // rate
			result.addProperty("restValue", Double.parseDouble(output.get("col_2").toString())); // residual_value
			result.addProperty("deliveryCostFirst", parseInt(output.get("col_3").toString())); // 1consignment
			result.addProperty("deliveryCostSecond", parseInt(output.get("col_4").toString())); // 2consignment
			result.addProperty("additionalCost", parseInt(output.get("col_5").toString())); // incidental_expenses
			result.addProperty("insuranceCost", parseInt(output.get("col_6").toString())); // insurance_bill
			result.addProperty("insuranceCostMyCar", parseInt(output.get("col_7").toString())); // self_insurance_bill

			Gson gson = new Gson();
			caret = gson.fromJson(result, CarEstimate.class);

			String key = version + model + returnCar + duration + age + deposit + commission + includeRepair
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

		CarEstimate caret = impl.getResult("1", "Coupe 428i M Sport OE", false, 4, true, 0, 0.01, false, 1);
		String jsonInString = gson.toJson(caret);
		log.debug(jsonInString);

		System.out.println("!![Sync Run]!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		long startTime = System.currentTimeMillis();

		caret = impl.getResult("1", "E 300 Exclusive", true, 4, true, 0, 0.01, false, 1);
		jsonInString = gson.toJson(caret);
		log.debug(jsonInString);

		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("!!!!!!!!!!!!!!!!!!!!!! 1111 estimatedTime:" + estimatedTime);

	}
}
