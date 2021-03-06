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

	// http://localhost:8880/rest/checkList/{"version":"1","owner":"brandon"}
	@RequestMapping(method = RequestMethod.GET, value = "/checkList/{params}")
	public String checkList(HttpServletRequest request, HttpServletResponse response, @PathVariable String params) {
		JsonObject output = null;
		try {
			JsonObject obj = (JsonObject) new JsonParser().parse(params);
			output = checklist.checkList(obj.get("version").getAsString(), obj.get("owner").getAsString());
		} catch (Exception e) {
			e.printStackTrace();
			output.addProperty("msg", e.getMessage());
		}
		return output.toString();
	}

	// http://localhost:8880/rest/historyList/{"version":"1","owner":"brandon"}
	@RequestMapping(method = RequestMethod.GET, value = "/historyList/{params}")
	public String historyList(HttpServletRequest request, HttpServletResponse response, @PathVariable String params) {
		JsonObject output = null;
		try {
			JsonObject obj = (JsonObject) new JsonParser().parse(params);
			output = checklist.historyList(obj.get("version").getAsString(), obj.get("owner").getAsString());
		} catch (Exception e) {
			e.printStackTrace();
			output.addProperty("msg", e.getMessage());
		}
		return output.toString();
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

	// http://localhost:8880/rest/appendHistory/{"version":"1","owner":"brandon"}
	@RequestMapping(method = RequestMethod.POST, value = "/appendHistory/{params}")
	public String appendHistory(HttpServletRequest request, HttpServletResponse response, @PathVariable String params) {
		JsonObject output = null;
		try {
			JsonObject obj = (JsonObject) new JsonParser().parse(params);
			output = checklist.appendHistory(obj.get("version").getAsString(), obj.get("owner").getAsString(),
					obj.get("col_1").getAsDouble(), obj.get("col_2").getAsDouble());
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

	// http://localhost:8880/rest/update/{"version":"1","owner":"brandon"}
	@RequestMapping(method = RequestMethod.PUT, value = "/update/{params}")
	public String update(HttpServletRequest request, HttpServletResponse response, @PathVariable String params) {
		JsonObject output = null;
		try {
			JsonObject object = (JsonObject) new JsonParser().parse(params);
			object.remove("version");
			object.remove("owner");
			JsonObject obj = (JsonObject) new JsonParser().parse(params);
			output = checklist.update(obj.get("version").getAsString(), obj.get("owner").getAsString(), object);
		} catch (Exception e) {
			e.printStackTrace();
			output.addProperty("msg", e.getMessage());
		}
		return output.toString();
	}

}