package io.jenkins.plugins.opentelemetry.embeded;

import hudson.Extension;
import hudson.model.RootAction;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.List;

@Extension
public class MigrateHarnessUrlRootAction implements RootAction {

    @Override
    public String getIconFileName() {
        return "/plugin/harnessmigration/images/48x48/harness-logo.png";
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
        String yaml = "pipeline:\n" +
            "  name: statestreet pipeline\n" +
            "  identifier: statestreet-pipeline714668066666\n" +
            "  projectIdentifier: default\n" +
            "  orgIdentifier: default\n" +
            "  tags: {}\n" +
            "  stages:\n" +
            "    - stage:\n" +
            "        name: statestreet-pipeline714668066666\n" +
            "        identifier: statestreet\n" +
            "        description: \"\"\n" +
            "        type: CI\n" +
            "        spec:\n" +
            "          cloneCodebase: true\n" +
            "          platform:\n" +
            "            os: Linux\n" +
            "            arch: Amd64\n" +
            "          runtime:\n" +
            "            type: Cloud\n" +
            "            spec: {}\n" +
            "          execution:\n" +
            "            steps:\n" +
            "              - stepGroup:\n" +
            "                  name: Build\n" +
            "                  identifier: build\n" +
            "                  steps:\n" +
            "                      - step:\n" +
            "                          type: Run\n" +
            "                          name: sh step\n" +
            "                          identifier: sh_c2\n" +
            "                          spec:\n" +
            "                            shell: Sh\n" +
            "                            command: echo 'Windows not support Ant Build'\n" +
            "                      - step:\n" +
            "                          type: Run\n" +
            "                          name: sh step\n" +
            "                          identifier: sh_b3\n" +
            "                          spec:\n" +
            "                            shell: Sh\n" +
            "                            command: echo 'Windows not support Ant Build'\n" +
            "                      - step:\n" +
            "                          type: Run\n" +
            "                          name: sh step\n" +
            "                          identifier: sh_1a\n" +
            "                          spec:\n" +
            "                            shell: Sh\n" +
            "                            command: echo 'Windows not support Ant Build'\n";
        sb.append(yaml).append("\n");
        for (String file : files) {
            sb.append(TraceProcessor.uploadFile(file)).append("\n");
        }
        res.setContentType("text/html;charset=UTF-8");

        // Write the HTML content to the response
        res.getWriter().write("<html><body><pre>" + sb + "</pre></body></html>");

        return null;
    }
}
