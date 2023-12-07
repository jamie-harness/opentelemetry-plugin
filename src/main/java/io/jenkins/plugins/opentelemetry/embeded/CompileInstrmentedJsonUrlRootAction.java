package io.jenkins.plugins.opentelemetry.embeded;

import hudson.Extension;
import hudson.model.RootAction;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Extension
public class CompileInstrmentedJsonUrlRootAction implements RootAction {

    @Override
    public String getIconFileName() {
        return "/plugin/opentelemetry/images/48x48/harness-logo.png";
    }

    @Override
    public String getDisplayName() {
        return "Compile Intermediate Json";
    }

    @Override
    public String getUrlName() {
        return "compile-harness-json";
    }

    public HttpResponse doIndex(StaplerRequest req, StaplerResponse res) throws IOException {
        List<String> files = TraceProcessor.convertTraceToJson();
        StringBuilder sb = new StringBuilder();
        for (String file : files) {
            String content = Files.readString(Paths.get(file), StandardCharsets.UTF_8);
            sb.append(content);
            sb.append("\n");
            sb.append("==========================\n");
        }
        res.setContentType("text/html;charset=UTF-8");

        // Write the HTML content to the response
        res.getWriter().write("<html><body><pre>" + sb + "</pre></body></html>");

        return null;
    }
}
