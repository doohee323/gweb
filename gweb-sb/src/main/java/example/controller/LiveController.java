package example.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tz.GWeb;

@RestController
@RequestMapping(value = "/rest")
public class LiveController {

	// http://localhost:8088/rest/getLink
	@RequestMapping(method = RequestMethod.GET, value = "/getLink")
	public String getLink(HttpServletRequest request, HttpServletResponse response) {
		JsonObject output = new JsonObject();
		try {
			JsonArray params2 = new JsonArray();

			JsonObject getValue = new JsonObject();
			getValue.addProperty("_sheet", "config");
			getValue.addProperty("_event", "getValue");
			getValue.addProperty("_cache", "false");
			getValue.addProperty("col_1", "A2");
			getValue.addProperty("col_2", "B2");
			getValue.addProperty("col_3", "C2");
			params2.add(getValue);

			GWeb gweb = GWeb.getInstance();
			ArrayList<Object> arry = new ArrayList<Object>();
			arry.add(params2.toString());

			String appName = "translator";
			JsonObject json = gweb.mainExec(appName, arry);
			output = (JsonObject) new JsonParser().parse(json.get("output").toString());

		} catch (Exception e) {
			e.printStackTrace();
			output.addProperty("msg", e.getMessage());
		}
		return output.toString();
	}

	// http://localhost:8088/rest/runningLogs
	@RequestMapping(method = RequestMethod.GET, value = "/runningLogs")
	public String runningLogs(HttpServletRequest request, HttpServletResponse response) {
		JsonObject output = new JsonObject();
		try {
			JsonArray params2 = new JsonArray();

			JsonObject getList = new JsonObject();
			getList.addProperty("_sheet", "running_logs");
			getList.addProperty("_event", "getList");
			getList.addProperty("_cache", "false");
			getList.addProperty("output", "['A','B','C']");
			params2.add(getList);

			GWeb gweb = GWeb.getInstance();
			ArrayList<Object> arry = new ArrayList<Object>();
			arry.add(params2.toString());

			String appName = "translator";
			JsonObject json = gweb.mainExec(appName, arry);
			output = (JsonObject) new JsonParser().parse(json.get("output").toString());

		} catch (Exception e) {
			e.printStackTrace();
			output.addProperty("msg", e.getMessage());
		}
		return output.toString();
	}

}