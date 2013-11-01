package lukasync;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class Lukasync {

    //public static String CONF = "/usr/local/etc/lukasync.csv";
    public static String CONF = "./lukasync.csv";
    public static long INITIAL_DELAY = 0;
    public static long DELAY = 60000;

    public static final String META = "./meta.json";
    public static final boolean printDebug = true;


    public static void main(String[] args) {
        if (args.length > 0) {
            for (int x = 0; x < args.length; x++) {
                String arg = args[x];
                switch (arg) {
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
                    case "--help":
                    case "-h":
                        System.out.println("Available Parameters (default value):");
                        System.out.println("\t-c or --config filename, sets the CSV-file to read from (" + CONF + ")");
                        System.out.println("\t-d or --delay X, sets the delay(in seconds) for each update (" + DELAY + ")");
                        System.out.println("\t-h or --help, shows this information");
                        System.out.println("\t-i or --initial-delay X, sets the delay(in seconds) before the first update. (" + INITIAL_DELAY + ")");
                        System.exit(0);
                    case "--initial-delay":
                    case "-i":
                        if (x + 1 < args.length && !args[x + 1].contains("-")) {
                            try {
                                INITIAL_DELAY = Integer.parseInt(args[x + 1]) * 60 * 1000;
                                System.out.println("Sets the initial delay to: " + args[x + 1] + " minutes");
                            } catch (NumberFormatException nfe) {
                                System.out.println("Not a valid number for -i/--initial-delay");
                                System.exit(0);
                            }
                            x++;
                        } else {
                            System.out.println("Not a valid delay for -i/--initial-delay");
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

        Scheduler scheduler = new Scheduler();
        scheduler.scheduleWithFixedDelay(new SyncTask(),
                INITIAL_DELAY,
                10000,
                TimeUnit.MILLISECONDS);
    }

    public static JSONObject fetchConfig() throws IOException, IllegalArgumentException, NullPointerException {
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

    public static JSONObject fetchMeta () throws JSONException, IOException {
        return new JSONObject(FileUtils.readFileToString(new File(META)));
    }

    public static void writeMetaToFile(JSONObject updatedMeta) {
        // TODO actually write the meta to its destination file
        throw new IllegalStateException("Don't call me, brah.");
    }
}
