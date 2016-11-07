package com.tz;

import com.google.api.services.script.Script;

public class AppInfo {

    private String appName;
    private int sheetCount;
    private String scriptId;
    private String clientSecretJson;
    private String functionName;
    private Boolean devMode;
    private String mainSheet;
    private Script service;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getClientSecretJson() {
        return clientSecretJson;
    }

    public void setClientSecretJson(String clientSecretJson) {
        this.clientSecretJson = clientSecretJson;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public Boolean getDevMode() {
        return devMode;
    }

    public void setDevMode(Boolean devMode) {
        this.devMode = devMode;
    }

    public Script getService() {
        return service;
    }

    public void setService(Script service) {
        this.service = service;
    }

    public int getSheetCount() {
        return sheetCount;
    }

    public void setSheetCount(int sheetCount) {
        this.sheetCount = sheetCount;
    }

    public String getMainSheet() {
        return mainSheet;
    }

    public void setMainSheet(String mainSheet) {
        this.mainSheet = mainSheet;
    }

}
