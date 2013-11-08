package lukasync.service;

import lukasync.Lukasync;

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
                System.err.println("Execution of sync got rejected! Trying again in " + Lukasync.WAIT / 1000 / 60); // TODO will this work?

                ((ScheduledThreadPoolExecutor) executor)
                        .scheduleWithFixedDelay(r, Lukasync.WAIT, Lukasync.WAIT, TimeUnit.MILLISECONDS);
            }
        });
    }
}
