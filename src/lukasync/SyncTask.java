package lukasync;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SyncTask implements Runnable {
    private static int iteration = 0;
    private final static String DATE =
            new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date());

    @Override
    public void run () {
        try {
            //throw new IllegalStateException("U DONE GOOFED");

            // TODO consider implementing minimum wait time
            iteration++;
            System.out.println(
                    "\nRunning synchronisation, this is run number " + iteration +
                            " since it last was restarted on " + DATE);

            try {
                JSONObject conf = Lukasync.fetchConfig();
                Synchronizer.doSync(conf);
            } catch (JSONException | NullPointerException | IllegalArgumentException ex) {
                System.err.println("ERROR: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                System.out.println("Will try again in " + Lukasync.DELAY / 1000 / 60 + " minutes");
            }
        } catch (Exception e) {
            System.err.println("ERROR: SyncTask crashed, stacktrace below:");
            e.printStackTrace();
        }
    }

}

