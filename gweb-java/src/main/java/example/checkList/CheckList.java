package example.checkList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tz.GWeb;

public class CheckList {

	static final Logger log = LoggerFactory.getLogger(CheckList.class);

	private String APP_NAME = "checklist";
	GWeb gweb = null;
	
	public CheckList () {
		gweb = GWeb.getInstance();
	}

	public JsonObject checkList(String version) {
		JsonObject output = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
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
	
	public JsonObject historyList(String version) {
		JsonObject output = null;
		try {
			Map<String, Object> param = new HashMap<String, Object>();
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
	
	public static void main(String[] args) {
		CheckList impl = new CheckList();
		Gson gson = new Gson();

		JsonObject out = impl.checkList("1");
		String jsonInString = gson.toJson(out);
		log.debug(jsonInString);

		JsonObject out2 = impl.historyList("1");
		String jsonInString2 = gson.toJson(out2);
		log.debug(jsonInString2);
		
		System.out.println("!![Sync Run]!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		long startTime = System.currentTimeMillis();

		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("!!!!!!!!!!!!!!!!!!!!!! 1111 estimatedTime:" + estimatedTime);

	}
}
