package io.jenkins.plugins.opentelemetry.embeded;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jenkins.plugins.opentelemetry.JenkinsOpenTelemetryPluginConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tools.ant.util.ResourceUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TraceProcessor {
    public static final String ROOT_ID = "0000000000000000";

    public static List<String> convertTraceToJson() throws IOException {
        Path rootPath = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory(), "trace");
        JSONObject pipelineMap = new JSONObject();

        Files.walkFileTree(rootPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
                String content = new String(Files.readAllBytes(file));
                JSONObject data = new JSONObject(content);

                if (data.has("parentSpanId")) {
                    String parentSpanId = data.getString("parentSpanId");

                    if (pipelineMap.has(parentSpanId)) {
                        JSONArray pipelineArray = pipelineMap.getJSONArray(parentSpanId);
                        pipelineArray.put(data);
                        pipelineMap.put(parentSpanId, sortJsonArray(pipelineArray));
                    } else {
                        JSONArray newArray = new JSONArray();
                        newArray.put(data);
                        pipelineMap.put(parentSpanId, newArray);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return FileVisitResult.CONTINUE;
            }
        });

        JSONArray resultObject = pipelineMap.getJSONArray(ROOT_ID);
        JSONArray queue = new JSONArray();
        queue.putAll(pipelineMap.getJSONArray(ROOT_ID));

        while (queue.length() > 0) {
            JSONObject cursorObject = queue.getJSONObject(0);
            String spanId = cursorObject.getString("spanId");

            if (pipelineMap.has(spanId)) {
                JSONArray childObjects = pipelineMap.getJSONArray(spanId);
                cursorObject.put("children", childObjects);
                queue.putAll(childObjects);
            }

            queue.remove(0);
        }

        List<String> filesToConvert = new ArrayList<>();
        for (int i = 0; i < resultObject.length(); i++) {
            JSONObject result = resultObject.getJSONObject(i);
            String traceId = result.getString("traceId");
            String spanId = result.getString("spanId");
            String fileName = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory(),"result-" + traceId + spanId + ".json").toString();
            filesToConvert.add(fileName);
            try {
                Files.write(Paths.get(fileName), result.toString(2).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filesToConvert;
    }

    public static List<String> convertTraceToPluginList() throws IOException {
        Path rootPath = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory(),"trace/");
        JSONObject pipelineMap = new JSONObject();

        Files.walkFileTree(rootPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    String content = new String(Files.readAllBytes(file));
                    JSONObject data = new JSONObject(content);

                    if (data.has("parentSpanId")) {
                        String parentSpanId = data.getString("parentSpanId");

                        if (pipelineMap.has(parentSpanId)) {
                            JSONArray pipelineArray = pipelineMap.getJSONArray(parentSpanId);
                            pipelineArray.put(data);
                            pipelineMap.put(parentSpanId, sortJsonArray(pipelineArray));
                        } else {
                            JSONArray newArray = new JSONArray();
                            newArray.put(data);
                            pipelineMap.put(parentSpanId, newArray);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return FileVisitResult.CONTINUE;
            }
        });

        JSONArray resultObject = pipelineMap.getJSONArray(ROOT_ID);
        JSONArray queue = new JSONArray();
        queue.putAll(pipelineMap.getJSONArray(ROOT_ID));

        while (queue.length() > 0) {
            JSONObject cursorObject = queue.getJSONObject(0);
            String spanId = cursorObject.getString("spanId");

            if (pipelineMap.has(spanId)) {
                JSONArray childObjects = pipelineMap.getJSONArray(spanId);
                cursorObject.put("children", childObjects);
                queue.putAll(childObjects);
            }

            queue.remove(0);
        }

        List<String> filesToConvert = new ArrayList<>();
        for (int i = 0; i < resultObject.length(); i++) {
            JSONObject result = resultObject.getJSONObject(i);
            String traceId = result.getString("traceId");
            String fileName = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory(),"result-" + traceId + ".json").toString();
            filesToConvert.add(fileName);
            try {
                Files.write(Paths.get(fileName), result.toString(2).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filesToConvert;
    }

    public static String uploadFile(String filePath) {
        String uploadUrl = JenkinsOpenTelemetryPluginConfiguration.get().getHarnessConvertEndpoint();
        StringBuilder result = new StringBuilder();
//        result.append("Attempting to upload file: ").append(filePath).append("\n");
//        result.append("Upload URL: ").append(uploadUrl).append("\n");

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost uploadFile = new HttpPost(uploadUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            File f = new File(filePath);
            if (!f.exists()) {
                return "Error: File does not exist: " + filePath;
            }

            builder.addBinaryBody(
                "jenkinsjsonfile",
                new FileInputStream(f),
                ContentType.APPLICATION_OCTET_STREAM,
                f.getName()
            );

            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);

//            result.append("Executing HTTP request...\n");
            CloseableHttpResponse response = httpClient.execute(uploadFile);
//            result.append("TraceProcessor Response status: ").append(response.getStatusLine()).append("\n");

            HttpEntity responseEntity = response.getEntity();
            String responseBody = EntityUtils.toString(responseEntity, "UTF-8");
            result.append(responseBody);

            return result.toString();
        } catch (Exception e) {
            result.append("Error occurred during file upload:\n");
            result.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");

            // Add stack trace for debugging
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
//            result.append("Stack trace:\n").append(sw.toString());

            return result.toString();
        }
    }


    public static JSONArray sortJsonArray(JSONArray jsonArray) {

        try {
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.get(i));
            }

            Collections.sort(list, (o1, o2) -> {
                try {
                    int age1 = ((JSONObject) o1).getJSONObject("attributesMap").getInt("jenkins.pipeline.step.id");
                    int age2 = ((JSONObject) o2).getJSONObject("attributesMap").getInt("jenkins.pipeline.step.id");
                    return Integer.compare(age1, age2);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return 0;
                }
            });

            // Step 3: Convert the sorted list back to JSONArray
            JSONArray sortedJsonArray = new JSONArray(list);

            // Print the sorted JSONArray
           return sortedJsonArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static Path zipDirectory(List<String> sourceDirPaths) throws IOException {
        Path zipFilePath = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory(),"trace", "result.zip");
        Path relDir = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory());
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
        sourceDirPaths
            .forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(relDir.relativize(Path.of(path)).toString());
                try {
                    zipOutputStream.putNextEntry(zipEntry);
                    Files.copy(Path.of(path), zipOutputStream);
                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    System.err.println("Failed to zip file: " + path + " - " + e.getMessage());
                }
            });
        }
        return zipFilePath;
    }

    public static void writeToFile(String content, String fileName) {
        fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        String directoryPath = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory(),"trace").toString();
        try {
            Path path = Paths.get(directoryPath);
            // Create the directory and its parent directories if they do not exist
            Files.createDirectories(path);
            File myObj = new File(Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory() ,"trace", fileName).toUri());
            if (myObj.createNewFile()) {
                FileWriter myWriter = new FileWriter(myObj);
                myWriter.write(content);
                myWriter.close();
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException ignore) {}
    }

}
