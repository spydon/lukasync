package lukasync;

import lukasync.client.EvoposClient;
import lukasync.client.MagentoClient;
import lukasync.client.ZurmoClient;
import lukasync.job.EvoposToZurmoJob;
import lukasync.job.MagentoToZurmoJob;
import lukasync.job.ZurmoToMagentoJob;
import org.json.JSONArray;
import org.json.JSONObject;

public class Synchronizer {
    public static void doSync (JSONObject conf) {
        System.out.println("Starting to sync, will sync every " + Lukasync.WAIT / 1000 / 60 + " minutes");

        for(Object objKey : conf.keySet()) {
            String key = objKey.toString();
            JSONObject sourceLine = conf.getJSONObject(key);
            JSONArray destinations = sourceLine.getJSONArray("destinations");

            switch(sourceLine.getString("service")) {
                case "zurmo":
                    for(int j = 0; j<destinations.length(); j++) {
                        JSONObject destinationLine = (JSONObject) conf.get("" + destinations.get(j));
                        if(destinationLine.get("service").equals("magento")) {
                            ZurmoClient sourceClient = new ZurmoClient(sourceLine);
                            MagentoClient destinationClient = new MagentoClient(destinationLine);
                            new ZurmoToMagentoJob(sourceClient, destinationClient).execute();
                        }
                    }
                    break;

                case "evopos":
                    for(int j = 0; j<destinations.length(); j++) {
                        JSONObject destinationLine = (JSONObject) conf.get("" + destinations.get(j));
                        if(destinationLine.get("service").equals("zurmo")) {
                            EvoposClient sourceClient = new EvoposClient(sourceLine);
                            ZurmoClient destinationClient = new ZurmoClient(destinationLine);
                            new EvoposToZurmoJob(sourceClient, destinationClient).execute();
                        }
                    }
                    break;

                case "magento":
                    for(int j = 0; j<destinations.length(); j++) {
                        JSONObject destinationLine = (JSONObject) conf.get("" + destinations.get(j));
                        if(destinationLine.get("service").equals("zurmo")) {
                            MagentoClient sourceClient = new MagentoClient(destinationLine);
                            ZurmoClient destinationClient = new ZurmoClient(sourceLine);
                            new MagentoToZurmoJob(sourceClient, destinationClient).execute();
                        }
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Faulty service: " + sourceLine.getString("service") + "\nin file " + Lukasync.DB);
            }
        }

//        ZurmoClient zurmo = new ZurmoClient.build(connList.get(0));
        //zurmo.createUser("EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO@e.com", 0, "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "STATE", 1);
        //zurmo.createNote(1, 2, "BAJSKAKA", new Date());
        //zurmo.transferContact(2, 4);
        //zurmo.updateContact(2, 1, null, null, null, null, null, null, null, null, null, null, null);
    }
}
