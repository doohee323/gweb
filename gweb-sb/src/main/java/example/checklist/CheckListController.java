package example.checklist;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import example.checkList.CheckList;

@RestController
@RequestMapping(value = "/rest")
public class CheckListController {

	CheckList checklist = null;

	public CheckListController() {
		checklist = new CheckList();
	}

	// http://localhost:8880/rest/checklist/{"version":"1","owner":"brandon"}
	@RequestMapping(method = RequestMethod.GET, value = "/checklist/{params}")
	public String getCheckList(HttpServletRequest request, HttpServletResponse response, @PathVariable String params) {
		JsonObject output3 = null;
		try {
			JsonObject obj = (JsonObject) new JsonParser().parse(params);
			output3 = checklist.checkList(obj.get("version").getAsString(), obj.get("owner").getAsString());
		} catch (Exception e) {
			e.printStackTrace();
			output3.addProperty("msg", e.getMessage());
		}
		return output3.toString();
	}

	// http://localhost:8880/rest/historyList/{"version":"1","owner":"brandon"}
	@RequestMapping(method = RequestMethod.GET, value = "/historyList/{params}")
	public String historyList(HttpServletRequest request, HttpServletResponse response, @PathVariable String params) {
		JsonObject output3 = null;
		try {
			JsonObject obj = (JsonObject) new JsonParser().parse(params);
			output3 = checklist.historyList(obj.get("version").getAsString(), obj.get("owner").getAsString());
		} catch (Exception e) {
			e.printStackTrace();
			output3.addProperty("msg", e.getMessage());
		}
		return output3.toString();
	}

	// http://localhost:8880/rest/getScore/{"version":"1","owner":"brandon"}
	@RequestMapping(method = RequestMethod.GET, value = "/getScore/{params}")
	public String getScore(HttpServletRequest request, HttpServletResponse response, @PathVariable String params) {
		JsonObject output = null;
		try {
			JsonObject obj = (JsonObject) new JsonParser().parse(params);
			output = checklist.getScore(obj.get("version").getAsString(), obj.get("owner").getAsString());
		} catch (Exception e) {
			e.printStackTrace();
			output.addProperty("msg", e.getMessage());
		}
		return output.toString();
	}

	// http://localhost:8880/rest/getScore/{"version":"1","owner":"brandon"}
	@RequestMapping(method = RequestMethod.PUT, value = "/getScore/{params}")
	public String appendHistory(HttpServletRequest request, HttpServletResponse response, @PathVariable String params) {
		JsonObject output = null;
		try {
			JsonObject obj = (JsonObject) new JsonParser().parse(params);
			output = checklist.getScore(obj.get("version").getAsString(), obj.get("owner").getAsString());
			output = checklist.appendHistory(obj.get("version").getAsString(), obj.get("owner").getAsString(),
					output.get("col_1").getAsDouble(), output.get("col_2").getAsDouble());
		} catch (Exception e) {
			e.printStackTrace();
			output.addProperty("msg", e.getMessage());
		}
		return output.toString();
	}

	// http://localhost:8880/rest/deleteHistory/{"version":"1","owner":"brandon","row":5}
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteHistory/{params}")
	public String deleteHistory(HttpServletRequest request, HttpServletResponse response, @PathVariable String params) {
		JsonObject output = null;
		try {
			JsonObject obj = (JsonObject) new JsonParser().parse(params);
			output = checklist.deleteHistory(obj.get("version").getAsString(), obj.get("owner").getAsString(),
					obj.get("row").getAsInt());
		} catch (Exception e) {
			e.printStackTrace();
			output.addProperty("msg", e.getMessage());
		}
		return output.toString();
	}

}