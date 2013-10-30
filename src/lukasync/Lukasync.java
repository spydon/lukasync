package lukasync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import lukasync.client.EvoposClient;
import lukasync.client.MagentoClient;
import lukasync.client.ZurmoClient;
import lukasync.job.EvoposToZurmoJob;
import lukasync.job.MagentoToZurmoJob;
import lukasync.job.ZurmoToMagentoJob;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Lukasync {

    //public static String CONF = "/usr/local/etc/lukasync.csv";
    public static String CONF = "./lukasync.csv";
    public static String META = "./times.json";
    public static boolean printDebug = true;

    private static int DELAY = 60000;
    private static int iteration;
    private final static String DATE = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date());

    public static void main(String[] args) {
        if (args.length > 0) {
            for (int x = 0; x < args.length; x++) {
                String arg = args[x];
                switch (arg) {
                    case "--help":
                    case "-h":
                        System.out.println("Available Parameters:");
                        System.out.println("\t-h or --help, shows this information");
                        System.out.println("\t-c or --config filename, sets the CSV-file to read from");
                        System.out.println("\t-d or --delay X, sets the delay(in seconds) for each update");
                        System.exit(0);
                    case "--config":
                    case "-c":
                        if (x + 1 < args.length && !args[x + 1].contains("-")) {
                            CONF = args[x + 1];
                            System.out.println("Sets the config file to: " + CONF);
                            x++;
                        } else {
                            System.out.println("Not a valid config file for -c/--config");
                        }
                        break;
                    case "--delay":
                    case "-d":
                        if (x + 1 < args.length && !args[x + 1].contains("-")) {
                            try {
                                DELAY = Integer.parseInt(args[x + 1]) * 60 * 1000;
                                System.out.println("Sets the delay to: " + args[x + 1] + " minutes");
                            } catch (NumberFormatException nfe) {
                                System.out.println("Not a valid number for -d/--delay");
                                System.exit(0);
                            }
                            x++;
                        } else {
                            System.out.println("Not a valid delay for -d/--delay");
                        }
                        break;
                    default:
                        System.out.println("Faulty parameters!");
                        System.exit(0);
                }
            }
        } else {
            System.out.println("Lukasync can take terminal parameters(see --help).");
            System.out.println("It fetches connection details from a CSV-file stored in " + CONF);
        }
        System.out.println("\n    The software is distributed in the hope that it will be useful,");
        System.out.println("    but without any warranty; without even the implied warranty of");
        System.out.println("    merchantability or fitness for a particular purpose.");
        System.out.println("    This software was coded by Lukas Klingsbo(lukas.klingsbo@gmail.com)");
        System.out.println("    and Emilio Nyaray(emilio@nyaray.com)");
        iteration = 1;
        new Lukasync();
    }

    public Lukasync() {
        System.out.println("\nStarting new Lukasync instance, this is run number " + iteration + " since it last was restarted on " + DATE);
        iteration++;
        init();
    }

    private void init() {
        try {
            JSONObject conf = fetchConfig();
            JSONObject meta = fetchTimes();
            startSync(conf, meta);
        } catch (JSONException | NullPointerException | IllegalArgumentException | IOException ex) {
            System.err.println("FATAL: " + ex.getMessage());
            System.out.println("Will try again in " + DELAY / 1000 / 60 + " minutes");
        } finally {
            startTimer();
        }
    }

    private JSONObject fetchConfig() throws IOException, IllegalArgumentException, NullPointerException {
        JSONObject conf = new JSONObject();
        FileInputStream fis = null;
        BufferedReader reader = null;
        System.out.println("Fetching connection details.");
        fis = new FileInputStream(CONF);
        reader = new BufferedReader(new InputStreamReader(fis));
        String line = reader.readLine();
        while (line != null) {
            if (!line.equals("") && !line.startsWith("//")) {
                JSONObject serviceLine = new JSONObject();
                String[] details = line.split(",");
                if (details.length != 9) {
                    reader.close();
                    fis.close();
                    throw new IllegalArgumentException("Needs to be 9 arguments per line.");
                }
//                serviceLine.put("id", Integer.parseInt(details[0]));
                serviceLine.put("name", details[1]);
                serviceLine.put("service", details[2]);
                serviceLine.put("type", details[3]);
                serviceLine.put("address", details[4]);
                serviceLine.put("databasename", details[5]);
                serviceLine.put("username", details[6]);
                serviceLine.put("password", details[7]);
                serviceLine.put("destinations", new JSONArray(details[8].replaceAll("\\.", ",")));
                conf.put(details[0], serviceLine);
            }
            line = reader.readLine();
        }
        reader.close();
        fis.close();

        if (conf.length() == 0)
            throw new IllegalArgumentException(CONF + " seems to be empty.");

        return conf;
    }

    private JSONObject fetchTimes() throws JSONException, IOException {
        return new JSONObject(FileUtils.readFileToString(new File(META)));
    }

    private void startSync(JSONObject conf, JSONObject meta) {
        System.out.println("Starting to sync, will sync every " + DELAY / 1000 / 60 + " minutes");

        for(Object objKey : conf.keySet()) {
            String key = objKey.toString();
            JSONObject sourceLine = conf.getJSONObject(key);
            JSONArray destinations = sourceLine.getJSONArray("destinations");
            JSONObject jobMeta = meta.getJSONObject(key);
            switch(sourceLine.getString("service")) {
            case "zurmo":
                for(int j = 0; j<destinations.length(); j++) {
                    JSONObject destinationLine = (JSONObject) conf.get("" + destinations.get(j));
                    if(destinationLine.get("service").equals("magento")) {
                        ZurmoClient sourceClient = new ZurmoClient(sourceLine);
                        MagentoClient destinationClient = new MagentoClient(destinationLine);
                        new ZurmoToMagentoJob(sourceClient, destinationClient, jobMeta).execute();
                    }
                }
                break;
            case "evopos":
                for(int j = 0; j<destinations.length(); j++) {
                    JSONObject destinationLine = (JSONObject) conf.get("" + destinations.get(j));
                    if(destinationLine.get("service").equals("zurmo")) {
                        EvoposClient sourceClient = new EvoposClient(sourceLine);
                        ZurmoClient destinationClient = new ZurmoClient(destinationLine);
                        new EvoposToZurmoJob(sourceClient, destinationClient, jobMeta).execute();
                    }
                }
                break;
            case "magento":
                for(int j = 0; j<destinations.length(); j++) {
                    JSONObject destinationLine = (JSONObject) conf.get("" + destinations.get(j));
                    if(destinationLine.get("service").equals("zurmo")) {
                        MagentoClient sourceClient = new MagentoClient(destinationLine);
                        ZurmoClient destinationClient = new ZurmoClient(sourceLine);
                        new MagentoToZurmoJob(sourceClient, destinationClient, jobMeta).execute();
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Faulty service: " + sourceLine.getString("service") + "\nin file " + CONF);
            }
            writeMetaToFile(jobMeta);
        }

        //		final Rest rest = new Rest(connList.get(0));
//		connList.remove(0);
//		rest.query("Contacts", "");
//		for(final MetaConnection conn:connList) {
//			new Thread() {
//		    	public void run() {
//		    		System.out.println("Syncing " + conn.getAddress() + " with Zurmo");
//		    		new Synchronizer(conn, rest);
//		    	}
//			}.start();
//		}

//        ZurmoClient zurmo = new ZurmoClient.build(connList.get(0));
        //zurmo.createUser("EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO@e.com", 0, "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "STATE", 1);
        //zurmo.createNote(1, 2, "BAJSKAKA", new Date());
        //zurmo.transferContact(2, 4);
        //zurmo.updateContact(2, 1, null, null, null, null, null, null, null, null, null, null, null);
    }

    private void writeMetaToFile(JSONObject updatedMeta) {
        // TODO Auto-generated method stub

    }

    private void startTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                new Lukasync();
            }
        }, DELAY);
    }
}
