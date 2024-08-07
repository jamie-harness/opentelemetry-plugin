package io.jenkins.plugins.opentelemetry.embeded;

import io.jenkins.plugins.opentelemetry.JenkinsOpenTelemetryPluginConfiguration;

import java.util.HashMap;
import java.util.Map;

public class HarnessConfig {

    public static String getApiKey() {
        return JenkinsOpenTelemetryPluginConfiguration.get().getAPI_KEY();
    }

    public static Map<String, String> getParams() {

        String accountIdentifier = JenkinsOpenTelemetryPluginConfiguration.get().getAccountIdentifier();
        String orgIdentifier = JenkinsOpenTelemetryPluginConfiguration.get().getOrgIdentifier();
        String projectIdentifier = JenkinsOpenTelemetryPluginConfiguration.get().getProjectIdentifier();

        Map<String, String> params = new HashMap<>();
        params.put("accountIdentifier", accountIdentifier);
        params.put("orgIdentifier", orgIdentifier);
        params.put("projectIdentifier",projectIdentifier );
        params.put("branch", "string");
        params.put("repoIdentifier", "string");
        params.put("rootFolder", "string");
        params.put("filePath", "string");
        params.put("commitMsg", "string");
        params.put("isNewBranch", "false");
        params.put("baseBranch", "string");
        params.put("connectorRef", "string");
        params.put("storeType", "INLINE");
        params.put("repoName", "string");
        params.put("isHarnessCodeRepo", "true");
        return params;
    }
}