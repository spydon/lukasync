package lukasync.job;

import lukasync.client.ServiceClient;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public abstract class Job<S extends ServiceClient, D extends ServiceClient> {
    protected final int jobId;
    protected final S source;
    protected final D destination;

    protected ArrayList<String> issues;

    protected Job(int jobId, S source, D destination) {
        this.jobId = jobId;
        this.source = source;
        this.destination = destination;

        issues = new ArrayList<>();
    }

    public abstract JSONObject execute();

    protected void addIssue (String issueTitle, Throwable t) {
        StringWriter errors = new StringWriter();
        t.printStackTrace(new PrintWriter(errors));

        issues.add("\n" + issueTitle + ":\n" + t.getMessage() + "\n" + errors.toString());
    }

    protected void printFinish (String jobName) {
        System.out.println("\n\n" + jobName + " finished with " + issues.size() + " issues.");

        if (issues.size() > 0) {
            for (String issue : issues) {
                System.out.println(issue);
            }
        }
    }
}
