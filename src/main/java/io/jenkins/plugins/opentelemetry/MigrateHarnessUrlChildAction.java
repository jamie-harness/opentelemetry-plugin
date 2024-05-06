package io.jenkins.plugins.opentelemetry;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ModelObject;
import hudson.model.RootAction;
import hudson.model.Run;
import hudson.util.FormValidation;
import hudson.views.ListViewColumnDescriptor;
import io.jenkins.plugins.opentelemetry.authentication.OtlpAuthentication;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import javax.servlet.ServletException;
import javax.ws.rs.POST;
import java.io.IOException;

@Extension
public class MigrateHarnessUrlChildAction implements Action, Describable<MigrateHarnessUrlChildAction> {

    private final ModelObject run;


    public MigrateHarnessUrlChildAction() {
        this.run = null;
    }
    public MigrateHarnessUrlChildAction(ModelObject run) {
        this.run = run;
    }

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


    public String getCallUpgrade()  {
        return "Success!!!!";
    }

    @JavaScriptMethod
    public String getCallUpgrade2()  {
        return "Success2!!!!" + System.currentTimeMillis();
    }


//    public String getJavaScript() {
//        return "/plugin/opentelemetry/js/harnessInner.js";
//    }

//    public HttpResponse doIndex(StaplerRequest req, StaplerResponse res) throws IOException {
//        List<String> files = TraceProcessor.convertTraceToJson();
//        StringBuilder sb = new StringBuilder();
//        for (String file : files) {
//            sb.append(TraceProcessor.uploadFile(file)).append("\n");
//        }
//        res.setContentType("text/html;charset=UTF-8");
//
//        // Write the HTML content to the response
//        res.getWriter().write("<html><body><pre>" + sb + "</pre></body></html>");
//
//        return null;
//    }
    public ModelObject getRun() {
        return run;
    }
    @SuppressWarnings("unchecked")
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
        @JavaScriptMethod
        public String getCallUpgrade3()  {
            return "Success3!!!!";
        }

        public FormValidation doMigrateToHarness(@QueryParameter String harnessAccount, @QueryParameter String harnessProject) {
            return FormValidation.ok("Success DO Migrate");
        }
    }
}
