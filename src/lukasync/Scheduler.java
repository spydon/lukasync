package lukasync;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Scheduler extends ScheduledThreadPoolExecutor {

    public Scheduler() {
        super(0);

        this.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution (Runnable r, ThreadPoolExecutor executor) {
                // TODO will this work?
                ((ScheduledThreadPoolExecutor) executor)
                        .scheduleWithFixedDelay(r, Lukasync.INITIAL_DELAY, 10000, TimeUnit.MILLISECONDS);
                //System.err.println("Execution of sync got rejected!");
            }
        });
    }
}
