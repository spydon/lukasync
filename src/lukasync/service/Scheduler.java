package lukasync.service;

import lukasync.Lukasync;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Scheduler extends ScheduledThreadPoolExecutor {

    public Scheduler() {
        super(0);

        final Timer t = new Timer();

        this.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution (final Runnable r, final ThreadPoolExecutor executor) {
                System.err.println("Execution of sync got rejected! Trying again in " + Lukasync.WAIT / 1000 / 60 + " minutes");

                final TimerTask rescheduleTask = new TimerTask() {
                    @Override
                    public void run () {
                        ((ScheduledThreadPoolExecutor) executor)
                                .scheduleWithFixedDelay(r, Lukasync.WAIT, Lukasync.WAIT, TimeUnit.MILLISECONDS);
                    }
                };

                t.schedule(rescheduleTask, Lukasync.WAIT);
            }
        });
    }
}
