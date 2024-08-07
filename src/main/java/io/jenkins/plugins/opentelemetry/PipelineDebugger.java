package io.jenkins.plugins.opentelemetry;

import hudson.model.Job;
import jenkins.model.Jenkins;
import java.util.List;
import java.util.logging.Logger;

public class PipelineDebugger {
    private static final Logger LOGGER = Logger.getLogger(PipelineDebugger.class.getName());

    public static void debugPipelineRetrieval() {
        Jenkins jenkins = Jenkins.get();
        List<Job> jobs = jenkins.getAllItems(Job.class);

        LOGGER.info("Total number of jobs found: " + jobs.size());

        for (Job job : jobs) {
            LOGGER.info("Job name: " + job.getName() + ", Full name: " + job.getFullName() + ", Class: " + job.getClass().getName());

            // Add more detailed logging as needed
            // For example, you might want to check if the job is a pipeline job
            if (job.getClass().getName().contains("WorkflowJob")) {
                LOGGER.info("Found Pipeline job: " + job.getFullName());
            }
        }
    }
}
