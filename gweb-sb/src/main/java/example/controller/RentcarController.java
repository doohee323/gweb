package example.controller;

import java.math.BigInteger;
import java.security.MessageDigest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import example.domain.CarEstimate;
import example.domain.Rentcar;
import example.estimate.RentCalculatorImpl;
import example.repository.CarEstimateRepository;

@RestController
@RequestMapping(value = "/rest")
public class RentcarController {

	@Autowired
	private CarEstimateRepository carEstimateRepository;

	// http://localhost:8088/rest/rentcar/{"version":"1","brand":"기아","model":"All
	// New
	// Canival","grade":"(경유)11인승 노블레스
	// (A|T)","returnCar":false,"duration":4,"age":true,"deposit":0,"commission":"0.01","includeRepair":false,"ownerType":1}
	@RequestMapping(method = RequestMethod.POST, value = "/rentcar/{params}")
	public String getRentcar(HttpServletRequest request, HttpServletResponse response, @PathVariable String params) {
		JsonObject output3 = null;
		try {
			params = request.getRequestURI();
			params = params.substring("/rest/rentcar/".length(), params.length());
			params = java.net.URLDecoder.decode(params, "UTF-8");
			params = params.replace("|", "/");

			JsonObject obj = (JsonObject) new JsonParser().parse(params);
			Rentcar rentcar = new Rentcar();
			rentcar.version = obj.get("version").getAsString();
			rentcar.model = obj.get("model").getAsString();
			rentcar.returnCar = obj.get("returnCar").getAsBoolean();
			rentcar.duration = obj.get("duration").getAsInt();
			rentcar.age = obj.get("age").getAsBoolean();
			rentcar.deposit = obj.get("deposit").getAsDouble();
			rentcar.commission = obj.get("commission").getAsDouble();
			rentcar.includeRepair = obj.get("includeRepair").getAsBoolean();
			rentcar.ownerType = obj.get("ownerType").getAsInt();

			String key = rentcar.version + rentcar.model + rentcar.returnCar + rentcar.duration + rentcar.age
					+ rentcar.deposit + rentcar.commission + rentcar.includeRepair + rentcar.ownerType;
			MessageDigest m;
			m = MessageDigest.getInstance("MD5");
			m.update(key.getBytes(), 0, key.length());
			String hashedKey = new BigInteger(1, m.digest()).toString(16);

			Gson gson = new Gson();
			CarEstimate carEstimate = carEstimateRepository.findById(hashedKey);
			if (carEstimate == null) {
				RentCalculatorImpl impl = new RentCalculatorImpl();

				CarEstimate caret = impl.getResult(rentcar.version, rentcar.model, rentcar.returnCar, rentcar.duration,
						rentcar.age, rentcar.deposit, rentcar.commission, rentcar.includeRepair, rentcar.ownerType);

				if (caret.id != null) {
					carEstimateRepository.save(caret);
					String jsonInString = gson.toJson(caret);
					output3 = (JsonObject) new JsonParser().parse(jsonInString);
				} else {
					output3 = new JsonObject();
					output3.addProperty("msg", "Not found!");
				}
				carEstimate = caret;
			} else {
				String jsonInString = gson.toJson(carEstimate);
				output3 = (JsonObject) new JsonParser().parse(jsonInString);
			}
		} catch (Exception e) {
			e.printStackTrace();
			output3.addProperty("msg", e.getMessage());
		}
		return output3.toString();
	}

}