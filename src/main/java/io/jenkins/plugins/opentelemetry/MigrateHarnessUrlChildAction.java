package io.jenkins.plugins.opentelemetry;

import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import io.jenkins.plugins.opentelemetry.embeded.TraceProcessor;

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

@Extension
public class MigrateHarnessUrlChildAction implements RootAction, Describable<MigrateHarnessUrlChildAction> {

    private final ModelObject run;
    private final String traceFolder;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MigrateHarnessUrlChildAction.class.getName());


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
        String[] selectedPipelines = req.getParameterValues("selectedPipelines");
        if (selectedPipelines == null || selectedPipelines.length == 0) {
            res.sendError(400, "No pipelines selected");
            return;
        }

        List<String> allTraces = TraceProcessor.convertTraceToJson();
        List<String> selectedTraces = filterTracesByPipelines(allTraces, selectedPipelines);

        if (selectedTraces.isEmpty()) {
            res.sendError(400, "No traces found for the selected pipelines");
            return;
        }

        File tempFile = File.createTempFile("harness-traces", ".zip");
        try (FileOutputStream fos = new FileOutputStream(tempFile);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

            Set<String> addedFiles = new HashSet<>(); // To keep track of added files

            for (String tracePath : selectedTraces) {
                File fileToZip = new File(tracePath);
                String fileName = fileToZip.getName();

                // Check if the file has already been added
                if (addedFiles.contains(fileName)) {
                    // If it's a duplicate, create a unique name
                    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String extension = fileName.substring(fileName.lastIndexOf('.'));
                    int counter = 1;
                    while (addedFiles.contains(fileName)) {
                        fileName = baseName + "_" + counter + extension;
                        counter++;
                    }
                }

                addedFiles.add(fileName);

                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                }
            }
        }

        try (FileInputStream fileStream = new FileInputStream(tempFile)) {
            res.setContentType("application/zip");
            res.setHeader("Content-Disposition", "attachment; filename=\"harness-traces.zip\"");
            res.serveFile(req, fileStream, tempFile.lastModified(), tempFile.length(), "harness-traces.zip");
        } finally {
            tempFile.delete();
        }
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