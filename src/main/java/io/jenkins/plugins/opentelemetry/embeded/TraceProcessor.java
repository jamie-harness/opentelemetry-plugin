package io.jenkins.plugins.opentelemetry.embeded;

import io.jenkins.plugins.opentelemetry.JenkinsOpenTelemetryPluginConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

public class TraceProcessor {
    public static final String ROOT_ID = "0000000000000000";

    public static List<String> convertTraceToJson() throws IOException {
        Path rootPath = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory() +  "trace/");
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
            String fileName = JenkinsOpenTelemetryPluginConfiguration.get().getDirectory() + "result-" + traceId + ".json";
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
        Path rootPath = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory() +  "trace/");
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
            String fileName = JenkinsOpenTelemetryPluginConfiguration.get().getDirectory() + "result-" + traceId + ".json";
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
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost uploadFile = new HttpPost(uploadUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            File f = new File(filePath);
            builder.addBinaryBody(
                "jenkinsjsonfile",
                new FileInputStream(f),
                ContentType.APPLICATION_OCTET_STREAM,
                f.getName()
            );

            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            CloseableHttpResponse response = httpClient.execute(uploadFile);
            HttpEntity responseEntity = response.getEntity();
            return  EntityUtils.toString(responseEntity, "UTF-8");

        } catch (Exception e) {
            return "Something Went Wrong\n" + e + "\n" + uploadUrl;
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
}
