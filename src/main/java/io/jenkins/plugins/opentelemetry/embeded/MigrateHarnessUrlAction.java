package io.jenkins.plugins.opentelemetry.embeded;

import hudson.Extension;
import hudson.model.Action;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.List;

@Extension
public class MigrateHarnessUrlAction implements Action {
    @Override
    public String getIconFileName() {
        return "/plugin/opentelemetry/images/48x48/harness-logo.png";
    }

    @Override
    public String getDisplayName() {
        return "Migrate To Harness";
    }

    @Override
    public String getUrlName() {
        return "migrate-to-harness";
    }

    public HttpResponse doIndex(StaplerRequest req, StaplerResponse res) throws IOException {
        List<String> files = TraceProcessor.convertTraceToJson();
        StringBuilder sb = new StringBuilder();
        for (String file : files) {
            sb.append(TraceProcessor.uploadFile(file)).append("\n");
        }
        res.setContentType("text/html;charset=UTF-8");

        // Write the HTML content to the response
        res.getWriter().write("<html><body>" + sb.toString() + "</body></html>");

        return null;
    }
}
