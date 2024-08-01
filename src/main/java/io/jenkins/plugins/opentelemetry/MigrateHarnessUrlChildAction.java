package io.jenkins.plugins.opentelemetry;

import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import io.jenkins.plugins.opentelemetry.embeded.TraceProcessor;
import io.jenkins.plugins.opentelemetry.embeded.HarnessConfig;

import javax.servlet.ServletException;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.json.JSONObject;
import jenkins.model.Jenkins;
import hudson.model.Run;
import hudson.model.TaskListener;

@Extension
public class MigrateHarnessUrlChildAction implements RootAction, Describable<MigrateHarnessUrlChildAction> {

    private final ModelObject run;
    private final String traceFolder;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MigrateHarnessUrlChildAction.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public MigrateHarnessUrlChildAction() {
        this.run = null;
        this.traceFolder = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory(), "trace/").toString();
    }

    public MigrateHarnessUrlChildAction(ModelObject run) {
        this.run = run;
        this.traceFolder = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory(), "trace/").toString();
    }

    @Override
    public String getIconFileName() {
        return "/plugin/harnessmigration/images/48x48/harness-logo.png";
    }

    @Override
    public String getDisplayName() {
        return "Multiple Migration";
    }

    @Override
    public String getUrlName() {
        return "migrate-to-harness";
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws Exception {
        PipelineDebugger.debugPipelineRetrieval(); // Add this line for debugging

        List<Job> pipelines = Jenkins.get().getAllItems(Job.class)
            .stream()
            .filter(job -> job.getClass().getName().contains("WorkflowJob"))
            .collect(Collectors.toList());

        req.setAttribute("pipelines", pipelines);
        req.getView(this, "index.jelly").forward(req, rsp);
    }

    public List<Job> getPipelines() {
        return Jenkins.get().getAllItems(Job.class)
            .stream()
            .filter(job -> job.getClass().getName().contains("WorkflowJob"))
            .collect(Collectors.toList());
    }

    public void doDownloadTraces(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
        final Logger LOGGER = Logger.getLogger(this.getClass().getName());
        LOGGER.info("Starting doDownloadTraces method");

        String[] selectedPipelines = req.getParameterValues("selectedPipelines");
        if (selectedPipelines == null || selectedPipelines.length == 0) {
            LOGGER.warning("No pipelines selected");
            res.sendError(400, "No pipelines selected");
            return;
        }

        List<String> allTraces = TraceProcessor.convertTraceToJson();
        List<String> selectedTraces = filterTracesByPipelines(allTraces, selectedPipelines);

        if (selectedTraces.isEmpty()) {
            LOGGER.warning("No traces found for the selected pipelines");
            res.sendError(400, "No traces found for the selected pipelines");
            return;
        }

        StringBuilder output = new StringBuilder();
        output.append("Processing selected pipelines and sending to Harness.\n\n");

        for (String tracePath : selectedTraces) {
            try {
                LOGGER.info("Processing trace file: " + tracePath);
                String content = new String(Files.readAllBytes(Paths.get(tracePath)));
                JSONObject traceJson = new JSONObject(content);

                // Directly access the name from the JSON
                StringBuilder pipelineName = new StringBuilder(traceJson.getString("name"));
                LOGGER.info("Processing pipeline: " + pipelineName);

                output.append("Processing pipeline: ").append(pipelineName).append("\n");

                String uploadResult = TraceProcessor.uploadFile(tracePath);
                LOGGER.info("Pipeline generated successfully for: " + pipelineName);
                output.append("Pipeline generated successfully.\n");

                String harnessResponse = sendPipelineToHarness(uploadResult, output);
                output.append("Pipeline name: ").append(pipelineName).append("\n");
                LOGGER.info("Pipeline sent to Harness. Response: " + harnessResponse);
                output.append("Pipeline sent to Harness. Response: ").append(harnessResponse).append("\n\n");

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing pipeline", e);
                output.append("Error processing pipeline: ").append(e.getMessage()).append("\n");
            }
        }

        // Generate HTML output
        String htmlOutput = generateHtmlOutput(output.toString());

        res.setContentType("text/html;charset=UTF-8");
        res.getWriter().write(htmlOutput);
        LOGGER.info("doDownloadTraces method completed successfully");
    }

    private String sendPipelineToHarness(String generatedPipeline, StringBuilder output) throws Exception {
        LOGGER.info("Sending pipeline to Harness");
        HttpClient httpClient = HttpClient.newBuilder().build();
        Map<String, String> params = HarnessConfig.getParams();

        String query = params.entrySet().stream()
            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));

        String host = "https://app.harness.io";
        String pathname = "/pipeline/api/pipelines";
        String fullUrl = host + pathname + '?' + query;

        HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(generatedPipeline))
            .uri(URI.create(fullUrl))
            .header("Content-Type", "application/json")
            .header("x-api-key", HarnessConfig.getApiKey())
            .build();

        output.append("Sending pipeline to Harness.\n");
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Pretty-print the response body if it's JSON
        String responseBody = response.body();
        try {
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            responseBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonResponse);
        } catch (IOException e) {
            // If response body is not valid JSON, keep it as is
        }

        return "Status Code: " + response.statusCode() + "\nBody: " + responseBody;
    }

    private String generateHtmlOutput(String summary) {
        return "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Pipeline Migration Results</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: Arial, sans-serif;\n" +
            "            line-height: 1.6;\n" +
            "            color: #333;\n" +
            "            max-width: 800px;\n" +
            "            margin: 0 auto;\n" +
            "            padding: 20px;\n" +
            "        }\n" +
            "        h1 {\n" +
            "            color: #2c3e50;\n" +
            "        }\n" +
            "        pre {\n" +
            "            white-space: pre-wrap;\n" +
            "            word-wrap: break-word;\n" +
            "            background-color: #f8f9fa;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 5px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>Pipeline Migration Results</h1>\n" +
            "    <pre>" + escapeHtml(summary) + "</pre>\n" +
            "</body>\n" +
            "</html>";
    }

    private String escapeHtml(String unsafe) {
        return unsafe
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#039;");
    }

    private Map<String, String> getUniquePipelines() throws IOException {
        List<String> allTraces = TraceProcessor.convertTraceToJson();
        Map<String, String> uniquePipelines = new HashMap<>();

        for (String tracePath : allTraces) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(Paths.get(tracePath)));
                JSONObject traceJson = new JSONObject(content);
                String pipelineName = traceJson.getJSONObject("attributesMap").optString("jenkins.pipeline.name", "Unknown Pipeline");
                String traceId = traceJson.getString("traceId");
                uniquePipelines.put(traceId, pipelineName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return uniquePipelines;
    }

    private List<String> filterTracesByPipelines(List<String> allTraces, String[] selectedPipelines) throws IOException {
        Set<String> selectedPipelineNames = new HashSet<>(Arrays.asList(selectedPipelines));
        LOGGER.info("Selected pipeline names: " + String.join(", ", selectedPipelineNames));

        Map<String, String> latestBuilds = new HashMap<>();
        Pattern buildPattern = Pattern.compile("(.*) #(\\d+)$");

        // First pass: find the latest build for each pipeline
        for (String tracePath : allTraces) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(tracePath)));
                JSONObject traceJson = new JSONObject(content);
                String name = traceJson.optString("name", "");

                Matcher matcher = buildPattern.matcher(name);
                if (matcher.find()) {
                    String pipelineName = matcher.group(1);
                    int buildNumber = Integer.parseInt(matcher.group(2));

                    if (selectedPipelineNames.contains(pipelineName)) {
                        String currentLatest = latestBuilds.get(pipelineName);
                        if (currentLatest == null || buildNumber > Integer.parseInt(currentLatest.split("#")[1])) {
                            latestBuilds.put(pipelineName, name);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.severe("Error processing trace " + tracePath + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Second pass: filter traces based on the latest builds
        return allTraces.stream()
            .filter(tracePath -> {
                try {
                    String content = new String(Files.readAllBytes(Paths.get(tracePath)));
                    JSONObject traceJson = new JSONObject(content);
                    String name = traceJson.optString("name", "");

                    LOGGER.info("Checking trace: " + tracePath + " with name: " + name);

                    boolean matched = latestBuilds.containsValue(name);
                    LOGGER.info("Trace " + (matched ? "matched" : "did not match") + " latest builds");
                    return matched;
                } catch (Exception e) {
                    LOGGER.severe("Error processing trace " + tracePath + ": " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            })
            .collect(Collectors.toList());
    }

    public String getTraceFolder() {
        return traceFolder;
    }

    public ModelObject getRun() {
        return run;
    }

    @Override
    public Descriptor<MigrateHarnessUrlChildAction> getDescriptor() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins has not been started");
        }
        return jenkins.getDescriptorOrDie(getClass());
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<MigrateHarnessUrlChildAction> {
        public FormValidation doCheckPipeline(@QueryParameter String value) {
            if (value.isEmpty()) {
                return FormValidation.error("Please select at least one pipeline");
            }
            return FormValidation.ok();
        }
    }
}