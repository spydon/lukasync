package lukasync.service;

import lukasync.Lukasync;
import lukasync.client.EvoposClient;
import lukasync.client.MagentoClient;
import lukasync.client.ZurmoClient;
import lukasync.job.EvoposToMagentoJob;
import lukasync.job.EvoposToZurmoJob;
import lukasync.job.MagentoToEvoposJob;
import org.json.JSONArray;
import org.json.JSONObject;

public class Synchronizer {
    public static void doSync (JSONObject conf) {
        System.out.println("Starting to sync, will sync every " + Lukasync.WAIT / 1000 / 60 + " minutes");

        JSONObject jobs = conf.getJSONObject("jobs");

        for(Object objKey : conf.keySet()) {
            String key = objKey.toString();

            if (key.equalsIgnoreCase("jobs")) {
                continue;
            }

            JSONObject sourceLine = conf.getJSONObject(key);
            JSONArray destinations = sourceLine.getJSONArray("destinations");

            switch(sourceLine.getString("type")) {
                case "evoposhq":
                    syncFromEvoposHQ(conf, jobs, sourceLine, destinations);
                    break;

                case "magento":
                    syncFromMagento(conf, jobs, sourceLine, destinations);
                    break;

                case "zurmo":
                    if (destinations.length() > 0) {
                        throw new IllegalStateException("Zurmo services may not have destinations");
                    }

                    break;
                default:
                    throw new IllegalArgumentException("Faulty service: " + sourceLine.getString("type") + "\nin file " + Lukasync.DB);
            }
        }
    }

    private static void syncFromEvoposHQ (JSONObject conf, JSONObject jobs, JSONObject sourceLine, JSONArray destinations) {
        for(int j = 0; j<destinations.length(); j++) {
            EvoposClient sourceClient;
            int jobId;

            JSONObject destinationLine = (JSONObject) conf.get("" + destinations.get(j));
            switch (destinationLine.getString("type")) {
                case "zurmo":
                    sourceClient = new EvoposClient(sourceLine);
                    ZurmoClient zurmoClient = new ZurmoClient(destinationLine);

                    jobId = jobs.getInt(Lukasync.getJobKey(sourceClient.getId(), zurmoClient.getId()));
                    new EvoposToZurmoJob(jobId, sourceClient, zurmoClient).execute();
                    break;

                case "magento":
                    sourceClient = new EvoposClient(sourceLine);
                    MagentoClient magentoClient = new MagentoClient(destinationLine);

                    jobId = jobs.getInt(Lukasync.getJobKey(sourceClient.getId(), magentoClient.getId()));
                    new EvoposToMagentoJob(jobId, sourceClient, magentoClient).execute();
                    break;
            }
        }
    }

    private static void syncFromMagento (JSONObject conf, JSONObject jobs, JSONObject sourceLine, JSONArray destinations) {
        for(int j = 0; j<destinations.length(); j++) {
            MagentoClient sourceClient;
            int jobId;

            JSONObject destinationLine = (JSONObject) conf.get("" + destinations.get(j));
            switch (destinationLine.getString("type")) {
                case "evoposhq":
                    sourceClient = new MagentoClient(sourceLine);
                    EvoposClient evoposClient = new EvoposClient(destinationLine);

                    jobId = jobs.getInt(Lukasync.getJobKey(sourceClient.getId(), evoposClient.getId()));
                    new MagentoToEvoposJob(jobId, sourceClient, evoposClient).execute();
                    break;
            }
        }
    }
}
