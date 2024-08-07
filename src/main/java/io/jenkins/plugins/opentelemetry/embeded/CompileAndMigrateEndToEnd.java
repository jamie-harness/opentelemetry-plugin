package io.jenkins.plugins.opentelemetry.embeded;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.Run;
import hudson.model.Job;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Extension
public class CompileAndMigrateEndToEnd implements RootAction {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getIconFileName() {
        return "/plugin/harnessmigration/images/48x48/harness-logo.png";
    }

    @Override
    public String getDisplayName() {
        return "E2E";
    }

    @Override
    public String getUrlName() {
        return "e2e-pipeline-gen";
    }

    public org.kohsuke.stapler.HttpResponse doIndex(StaplerRequest req, StaplerResponse res) throws IOException {
        StringBuilder output = new StringBuilder();
        output.append("Running the complete migrator.\n\n");

        String intermediateJson = "";
        StringBuilder requestDetails = new StringBuilder("Request Details:\n");
        String harnessResponse = "";
        String uploadResult = "";

        try {
            Optional<Run<?, ?>> latestBuild = getLatestBuild(output);
            if (!latestBuild.isPresent()) {
                throw new IOException("No builds found in Jenkins history");
            }

            Run<?, ?> build = latestBuild.get();
            output.append("Latest build: ").append(build.getFullDisplayName()).append("\n");

            List<String> files = TraceProcessor.convertTraceToJson();

            Map<String, String> processResult = processLatestBuild(build, files, output);
            uploadResult = processResult.get("uploadResult");
            intermediateJson = processResult.get("intermediateJson");

            // Send the generated pipeline to Harness
            harnessResponse = sendPipelineToHarness(uploadResult, requestDetails, output);

        } catch (Exception e) {
            output.append("Error processing request: ").append(e.getMessage()).append("\n");
        }

        String htmlOutput = generateHtmlOutput(output.toString(), intermediateJson, uploadResult, requestDetails.toString(), harnessResponse);

        res.setContentType("text/html;charset=UTF-8");
        res.getWriter().write(htmlOutput);
        return null;
    }


    private String generateHtmlOutput(String summary, String intermediateJson, String uploadResult, String requestDetails, String harnessResponse) {
        return "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Migration Output</title>\n" +
            "    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.24.1/prism.min.js\"></script>\n" +
            "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.24.1/themes/prism.min.css\">\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: Arial, sans-serif;\n" +
            "            line-height: 1.6;\n" +
            "            color: #333;\n" +
            "            max-width: 800px;\n" +
            "            margin: 0 auto;\n" +
            "            padding: 20px;\n" +
            "        }\n" +
            "        h1, h2 {\n" +
            "            color: #2c3e50;\n" +
            "        }\n" +
            "        .section {\n" +
            "            margin-bottom: 20px;\n" +
            "            padding: 15px;\n" +
            "            background-color: #f8f9fa;\n" +
            "            border-radius: 5px;\n" +
            "        }\n" +
            "        .collapsible {\n" +
            "            background-color: #3498db;\n" +
            "            color: white;\n" +
            "            cursor: pointer;\n" +
            "            padding: 18px;\n" +
            "            width: 100%;\n" +
            "            border: none;\n" +
            "            text-align: left;\n" +
            "            outline: none;\n" +
            "            font-size: 15px;\n" +
            "        }\n" +
            "        .active, .collapsible:hover {\n" +
            "            background-color: #2980b9;\n" +
            "        }\n" +
            "        .content {\n" +
            "            padding: 0 18px;\n" +
            "            display: none;\n" +
            "            overflow: hidden;\n" +
            "            background-color: #f1f1f1;\n" +
            "        }\n" +
            "        pre {\n" +
            "            white-space: pre-wrap;\n" +
            "            word-wrap: break-word;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>Migration Output</h1>\n" +
            "    \n" +
            "    <div class=\"section\">\n" +
            "        <h2>Process Summary</h2>\n" +
            "        <pre id=\"summary\">" + escapeHtml(summary) + "</pre>\n" +
            "    </div>\n" +
            "\n" +
            "    <button type=\"button\" class=\"collapsible\">Intermediate JSON</button>\n" +
            "    <div class=\"content\">\n" +
            "        <pre><code class=\"language-json\" id=\"json-content\">" + escapeHtml(intermediateJson) + "</code></pre>\n" +
            "    </div>\n" +
            "\n" +
            "    <button type=\"button\" class=\"collapsible\">Harness Pipeline</button>\n" +
            "    <div class=\"content\">\n" +
            "        <pre><code class=\"language-json\" id=\"pipeline-content\">" + escapeHtml(uploadResult) + "</code></pre>\n" +
            "    </div>\n" +
            "\n" +
            "    <button type=\"button\" class=\"collapsible\">Request Details</button>\n" +
            "    <div class=\"content\">\n" +
            "        <pre id=\"request-details\">" + escapeHtml(requestDetails) + "</pre>\n" +
            "    </div>\n" +
            "\n" +
            "    <button type=\"button\" class=\"collapsible\">Harness API Response</button>\n" +
            "    <div class=\"content\">\n" +
            "        <pre><code class=\"language-json\" id=\"api-response\">" + escapeHtml(harnessResponse) + "</code></pre>\n" +
            "    </div>\n" +
            "\n" +
            "    <script>\n" +
            "        // Initialize Prism for syntax highlighting\n" +
            "        Prism.highlightAll();\n" +
            "\n" +
            "        // Add collapsible functionality\n" +
            "        var coll = document.getElementsByClassName(\"collapsible\");\n" +
            "        for (var i = 0; i < coll.length; i++) {\n" +
            "            coll[i].addEventListener(\"click\", function() {\n" +
            "                this.classList.toggle(\"active\");\n" +
            "                var content = this.nextElementSibling;\n" +
            "                if (content.style.display === \"block\") {\n" +
            "                    content.style.display = \"none\";\n" +
            "                } else {\n" +
            "                    content.style.display = \"block\";\n" +
            "                }\n" +
            "            });\n" +
            "        }\n" +
            "    </script>\n" +
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

    private Map<String, String> processLatestBuild(Run<?, ?> build, List<String> files, StringBuilder output) throws IOException {
        String buildName = build.getFullDisplayName();

        Optional<String> matchingFile = files.stream()
            .filter(file -> {
                try {
                    String content = Files.readString(Path.of(file), StandardCharsets.UTF_8);
                    JsonNode rootNode = objectMapper.readTree(content);
                    String name = rootNode.path("name").asText();
                    return name.equals(buildName);
                } catch (IOException e) {
                    return false;
                }
            })
            .findFirst();

        if (!matchingFile.isPresent()) {
            throw new IOException("No matching file found for the latest build: " + buildName);
        }

        Path filePath = Path.of(matchingFile.get());
        output.append("Matching file found: ").append(filePath).append("\n");

        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        output.append("YAML Pipeline generated successfully.\n");

        String uploadResult = TraceProcessor.uploadFile(filePath.toString());

        Map<String, String> result = new HashMap<>();
        result.put("uploadResult", uploadResult);
        result.put("intermediateJson", content);
        return result;
    }

    private Optional<Run<?, ?>> getLatestBuild(StringBuilder output) {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return Optional.empty();
        }

        return jenkins.getAllItems(Job.class).stream()
            .<Run<?, ?>>map(Job::getLastBuild)
            .filter(build -> build != null)
            .peek(build -> output.append("Considering build: ").append(build.getFullDisplayName()).append(", Time: ").append(build.getTimeInMillis()).append("\n"))
            .max(Comparator.comparing(Run::getTimeInMillis));
    }

    private String sendPipelineToHarness(String generatedPipeline, StringBuilder requestDetails, StringBuilder output) throws Exception {
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

        // Print request details
        requestDetails.append("URL: ").append(fullUrl).append("\n");
        requestDetails.append("Method: ").append(request.method()).append("\n");
        requestDetails.append("Headers:\n");
        request.headers().map().forEach((key, values) -> {
            requestDetails.append("  ").append(key).append(": ");
//            requestDetails.append(String.join(", ", values)).append("\n");

            if (key.equalsIgnoreCase("x-api-key")) {
                requestDetails.append("[REDACTED]\n");
            } else {
                requestDetails.append(String.join(", ", values)).append("\n");
            }
        });
        requestDetails.append("Request Body:\n\n").append(generatedPipeline);
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
        output.append("Pipeline sent successfully, check API Response for detailed logs.\n");

        return "Status Code: " + response.statusCode() + "\nBody: " + responseBody;
    }
}
// DON'T UNDO BEFORE THIS.