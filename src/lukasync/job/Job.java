package lukasync.job;

import org.json.JSONObject;

public abstract class Job<S,D> {
    protected final S source;
    protected final D destination;
    protected JSONObject jobMeta;

    protected Job(S source, D destination, JSONObject jobMeta) {
        this.source = source;
        this.destination = destination;
        this.jobMeta = jobMeta;
    }

    public abstract JSONObject execute();
}
