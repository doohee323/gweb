package example.checkList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tz.GWeb;

public class CheckList {

	static final Logger log = LoggerFactory.getLogger(CheckList.class);

	private String APP_NAME = "checklist";
	GWeb gweb = null;

	public CheckList() {
		gweb = GWeb.getInstance();
	}

	public JsonObject checkList(String version, String owner) {
		JsonObject output = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("owner", owner);
			String str = GWeb.parseParam("gconfig/checklist.json", "checkList", param);

			ArrayList<Object> arry = new ArrayList<Object>();
			arry.add(str);

			JsonObject json = gweb.mainExec(APP_NAME, arry);
			log.debug(json.toString());
			output = (JsonObject) new JsonParser().parse(json.get("output").toString());
		} catch (Exception e) {
			log.error(e.toString());
		}
		return output;
	}

	public JsonObject historyList(String version, String owner) {
		JsonObject output = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("owner", owner);
			String str = GWeb.parseParam("gconfig/checklist.json", "historyList", param);

			ArrayList<Object> arry = new ArrayList<Object>();
			arry.add(str);

			JsonObject json = gweb.mainExec(APP_NAME, arry);
			log.debug(json.toString());
			output = (JsonObject) new JsonParser().parse(json.get("output").toString());
		} catch (Exception e) {
			log.error(e.toString());
		}
		return output;
	}

	public JsonObject getScore(String version, String owner) {
		JsonObject output = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("owner", owner);
			String str = GWeb.parseParam("gconfig/checklist.json", "getScore", param);

			ArrayList<Object> arry = new ArrayList<Object>();
			arry.add(str);

			JsonObject json = gweb.mainExec(APP_NAME, arry);
			log.debug(json.toString());
			output = (JsonObject) new JsonParser().parse(json.get("output").toString());
		} catch (Exception e) {
			log.error(e.toString());
		}
		return output;
	}

	public JsonObject update(String version, String owner, JsonObject object) {
		JsonObject output = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("owner", owner);
			String str = GWeb.parseParam("gconfig/checklist.json", "update", param);

			JsonArray paramObj = (JsonArray) new JsonParser().parse(str);
			for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
				paramObj.get(0).getAsJsonObject().addProperty(entry.getKey(), entry.getValue().getAsString());
			}
			ArrayList<Object> arry = new ArrayList<Object>();
			arry.add(paramObj.toString());

			JsonObject json = gweb.mainExec(APP_NAME, arry);
			log.debug(json.toString());
			output = (JsonObject) new JsonParser().parse(json.get("output").toString());
		} catch (Exception e) {
			log.error(e.toString());
		}
		return output;
	}

	public JsonObject appendHistory(String version, String owner, double col_1, double col_2) {
		JsonObject output = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("owner", owner);
			String str = GWeb.parseParam("gconfig/checklist.json", "appendHistory", param);
			JsonArray paramObj = (JsonArray) new JsonParser().parse(str);
			String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
			String date2 = new SimpleDateFormat("yyyyMM").format(new Date());

			paramObj.get(0).getAsJsonObject().addProperty("A", date);
			paramObj.get(0).getAsJsonObject().addProperty("B", date2);
			paramObj.get(0).getAsJsonObject().addProperty("C", col_1);
			paramObj.get(0).getAsJsonObject().addProperty("D", col_2);
			double col_3 = col_2 / col_1 * 100;
			paramObj.get(0).getAsJsonObject().addProperty("E", col_3);

			ArrayList<Object> arry = new ArrayList<Object>();
			arry.add(paramObj.toString());

			JsonObject json = gweb.mainExec(APP_NAME, arry);
			log.debug(json.toString());
			output = (JsonObject) new JsonParser().parse(json.get("output").toString());
		} catch (Exception e) {
			log.error(e.toString());
		}
		return output;
	}

	public JsonObject deleteHistory(String version, String owner, int row) {
		JsonObject output = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("owner", owner);
			String str = GWeb.parseParam("gconfig/checklist.json", "deleteHistory", param);

			JsonArray paramObj = (JsonArray) new JsonParser().parse(str);
			paramObj.get(0).getAsJsonObject().addProperty("row", row);

			ArrayList<Object> arry = new ArrayList<Object>();
			arry.add(paramObj.toString());

			JsonObject json = gweb.mainExec(APP_NAME, arry);
			log.debug(json.toString());
			output = (JsonObject) new JsonParser().parse(json.get("output").toString());
		} catch (Exception e) {
			log.error(e.toString());
		}
		return output;
	}

	public static void main(String[] args) {
		CheckList impl = new CheckList();
		Gson gson = new Gson();

		JsonObject out = impl.checkList("1", "brandon");
		String jsonInString = gson.toJson(out);
		log.debug(jsonInString);

		out = impl.historyList("1", "brandon");
		jsonInString = gson.toJson(out);
		log.debug(jsonInString);

		out = impl.getScore("1", "brandon");
		jsonInString = gson.toJson(out);
		log.debug(jsonInString);
		JsonObject jsonObj = (JsonObject) new JsonParser().parse(jsonInString);

		out = impl.appendHistory("1", "brandon", jsonObj.get("col_1").getAsDouble(),
				jsonObj.get("col_2").getAsDouble());
		jsonInString = gson.toJson(out);
		log.debug(jsonInString);

		out = impl.deleteHistory("1", "brandon", 5);
		jsonInString = gson.toJson(out);
		log.debug(jsonInString);

		JsonObject object = new JsonObject();
		object.addProperty("I3", "2");
		object.addProperty("I4", "");
		object.addProperty("I5", "2");
		object.addProperty("I6", "");
		object.addProperty("I7", "");
		object.addProperty("I8", "");
		out = impl.update("1", "brandon", object);
		jsonInString = gson.toJson(out);
		log.debug(jsonInString);

		System.out.println("!![Sync Run]!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		long startTime = System.currentTimeMillis();

		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("!!!!!!!!!!!!!!!!!!!!!! 1111 estimatedTime:" + estimatedTime);

	}
}
