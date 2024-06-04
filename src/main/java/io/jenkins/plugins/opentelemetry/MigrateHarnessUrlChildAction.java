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
import io.jenkins.plugins.opentelemetry.embeded.TraceProcessor;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Extension
public class MigrateHarnessUrlChildAction implements RootAction, Describable<MigrateHarnessUrlChildAction> {

    private final ModelObject run;

    private String traceFolder;


    public MigrateHarnessUrlChildAction() {
        this.run = null;
        this.traceFolder = Paths.get(JenkinsOpenTelemetryPluginConfiguration.get().getDirectory() ,"trace/").toString();
    }
    public MigrateHarnessUrlChildAction(ModelObject run) {
        this.run = run;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/harnessmigration/images/48x48/harness-logo.png";
    }

    @Override
    public String getDisplayName() {
        return "Download Traces";
    }

    @Override
    public String getUrlName() {
        return "migrate-to-harness";
    }



    public HttpResponse doIndex(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
        List<String> files = TraceProcessor.convertTraceToJson();
        StringBuilder sb = new StringBuilder();
//        for (String file : files) {
//            sb.append(TraceProcessor.uploadFile(file)).append("\n");
////            res.setContentType("text/html;charset=UTF-8");
//            File openedFile = Path.of(file).toFile();
//            FileInputStream fileStream =  new FileInputStream(openedFile);
//
//            // Write the HTML content to the response
//            res.serveFile(req, fileStream, openedFile.lastModified(), openedFile.length(), "testFile");
//            return null;
//        }
        Path zipFilePath = TraceProcessor.zipDirectory(files);
        File openedFile = zipFilePath.toFile();
        FileInputStream fileStream =  new FileInputStream(openedFile);

        // Write the HTML content to the response
        res.serveFile(req, fileStream, openedFile.lastModified(), openedFile.length(), "harness-traces.zip");
//        res.setContentType("text/html;charset=UTF-8");
//
//
//        // Write the HTML content to the response
//        res.getWriter().write("<html><body><pre>" + sb + "</pre></body></html>");

        return null;
    }

    public String getTraceFolder() {
        return traceFolder;
    }
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

}
