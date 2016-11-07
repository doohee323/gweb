package com.tz;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.script.Script;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Command-line sample for the Google OAuth2 API
 *
 * @author Dewey Hong
 */
public class GWeb {

	static final Logger log = LoggerFactory.getLogger(GWeb.class);

	private static GWeb singleton = null;

	/** OAuth 2.0 scopes. */
	private static final List<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/drive.scripts",
			"https://www.googleapis.com/auth/drive", "https://www.googleapis.com/auth/script.storage",
			"https://www.googleapis.com/auth/spreadsheets");

	/** Directory to store user credentials. */
	private static java.io.File dataStoreDir = null;

	private static FileDataStoreFactory dataStoreFactory;
	private static HttpTransport httpTransport;
	private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
	private static Gson gson;
	private static JsonObject conf = null;

	public static GWeb getInstance() {
		if (singleton == null) {
			singleton = new GWeb();
		}
		return singleton;
	}

	private AppInfo initApp(String appName) {
		AppInfo appInfo = null;
		try {
			Object result = CacheManager.getInstance().get(appName);
			if (result != null) {
				appInfo = (AppInfo) result;
			} else {
				appInfo = gson.fromJson(conf.get(appName).toString(), AppInfo.class);
				appInfo.setAppName(appName);
				String configFile = "./gweb.conf";
				String path = ConfigUtil.getConfPath(configFile);
				path = path.substring(0, path.lastIndexOf("/") + 1) + "stored_oauth2/" + appName; // "stored_oauth2"
				dataStoreDir = new java.io.File(path);
				if (!dataStoreDir.exists()) {
					log.debug("oauth2 file not exist!: " + dataStoreDir.getAbsolutePath());
				}
				dataStoreFactory = new FileDataStoreFactory(dataStoreDir);

				// authorize
				GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
						new InputStreamReader(GWeb.class.getResourceAsStream(appInfo.getClientSecretJson())));
				if (clientSecrets.getDetails().getClientId().startsWith("Enter")
						|| clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
					System.exit(1);
				}

				GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
						clientSecrets, SCOPES).setDataStoreFactory(dataStoreFactory).build();
				Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
						.authorize("user");

				Oauth2 oauth2 = new Oauth2.Builder(httpTransport, jsonFactory, credential).setApplicationName(appName)
						.build();

				// tokenInfo
				try {
					Tokeninfo tokeninfo = oauth2.tokeninfo().setAccessToken(credential.getAccessToken()).execute();
					log.debug(tokeninfo.toPrettyString());
					if (!tokeninfo.getAudience().equals(clientSecrets.getDetails().getClientId())) {
						log.error("ERROR: audience does not match our client ID!");
					}

					// get script
					Script service = new Script.Builder(httpTransport, jsonFactory, setHttpTimeout(credential))
							.setApplicationName(appName).build();

					appInfo.setService(service);
					CacheManager.getInstance().put(appName, appInfo);
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e.getMessage());
				}

				// duplicate sheets for loadbalancing
				Operation op = null;
				try {
					ArrayList<Object> arry = new ArrayList<Object>();
					JsonObject copySheet = new JsonObject();
					copySheet.addProperty("sheet", appInfo.getMainSheet());
					copySheet.addProperty("count", appInfo.getSheetCount());
					arry.add(copySheet.toString());

					ExecutionRequest request = null;
					if (appInfo.getDevMode()) {
						request = new ExecutionRequest().setFunction("run").setParameters(arry).setDevMode(true); // Optional.
					} else {
						request = new ExecutionRequest().setFunction("run").setParameters(arry);
					}

					op = appInfo.getService().scripts().run(appInfo.getScriptId(), request).execute();
					if (op.getError() != null) {
						log.error(getScriptError(op));
					} else {
						log.debug(op.getResponse().toString());
					}
					String str = op.getResponse().get("result").toString();
					log.debug(str);
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return appInfo;
	}

	private GWeb() {
		try {
			gson = new Gson();

			String configFile = "gweb.conf";
			final Reader reader = ConfigUtil.getFileReader(configFile);
			try {
				conf = gson.fromJson(reader, JsonObject.class);
			} catch (final Exception e) {
				e.printStackTrace();
				log.error("I didn't like your config file (" + configFile + "): ");
				Runtime.getRuntime().exit(1);
			} finally {
				if (reader != null) {
					reader.close();
				}
			}

			String logConfigFile = "logback.xml";
			ConfigUtil.setLogbackConfig(logConfigFile);

			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}

	private static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
		return new HttpRequestInitializer() {
			public void initialize(HttpRequest httpRequest) throws IOException {
				requestInitializer.initialize(httpRequest);
				httpRequest.setReadTimeout(380000);
			}
		};
	}

	public static String getScriptError(Operation op) {
		if (op.getError() == null) {
			return null;
		}
		Map<String, Object> detail = op.getError().getDetails().get(0);
		List<Map<String, Object>> stacktrace = (List<Map<String, Object>>) detail.get("scriptStackTraceElements");

		java.lang.StringBuilder sb = new StringBuilder("\nScript error message: ");
		sb.append(detail.get("errorMessage"));
		sb.append("\nScript error type: ");
		sb.append(detail.get("errorType"));

		if (stacktrace != null) {
			sb.append("\nScript error stacktrace:");
			for (Map<String, Object> elem : stacktrace) {
				sb.append("\n  ");
				sb.append(elem.get("function"));
				sb.append(":");
				sb.append(elem.get("lineNumber"));
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	public JsonObject mainExec(String appName, ArrayList params) {
		Operation op = null;
		long startTime = System.currentTimeMillis();
		String key = gson.toJson(params);
		try {
			Object result = null;
			String hashedKey = null;
			if (key.indexOf("\"_cache\":\"true\"") > -1) {
				MessageDigest m;
				m = MessageDigest.getInstance("MD5");
				m.update(key.getBytes(), 0, key.length());
				hashedKey = new BigInteger(1, m.digest()).toString(16);
				result = CacheManager.getInstance().get(hashedKey);
			}
			if (result != null) {
				String str = result.toString();
				str = str.replace("@type=type.googleapis.com/google.apps.script.v1.ExecutionResponse, ", "");
				str = str.replace("result=", "\"result\":");
				return (JsonObject) ((JsonObject) new JsonParser().parse(str)).get("result");
			}

			AppInfo appInfo = initApp(appName);

			ExecutionRequest request = null;
			if (appInfo.getDevMode()) {
				request = new ExecutionRequest().setFunction(appInfo.getFunctionName()).setParameters(params)
						.setDevMode(true); // Optional.
			} else {
				request = new ExecutionRequest().setFunction(appInfo.getFunctionName()).setParameters(params);
			}

			op = appInfo.getService().scripts().run(appInfo.getScriptId(), request).execute();
			if (op.getError() != null) {
				log.error(getScriptError(op));
			} else {
				log.debug(op.getResponse().toString());
				if (key.indexOf("\"_cache\":\"true\"") > -1) {
					CacheManager.getInstance().put(hashedKey, op.getResponse());
				}
			}
			long estimatedTime = System.currentTimeMillis() - startTime;
			log.debug("============================" + appName + " estimatedTime:" + estimatedTime);
			String str = op.getResponse().get("result").toString();
			return (JsonObject) new JsonParser().parse(str);
		} catch (GoogleJsonResponseException e) {
			e.printStackTrace(System.out);
			log.error(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace(System.out);
			log.error(e.getMessage());
		} catch (Throwable t) {
			t.printStackTrace();
			log.error(t.getMessage());
		}
		return null;
	}

}