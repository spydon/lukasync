package lukasync;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Lukasync {

    //public static String CONF = "/usr/local/etc/lukasync.csv";
    public static String CONF = "./lukasync.csv";
    public static boolean printDebug = true;

    private static int DELAY = 60000;
    private static int iteration;
    private final static String DATE = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date());

    public static void main(String[] args) {
        if (args.length > 0) {
            for (int x = 0; x < args.length; x++) {
                String arg = args[x];
                if (arg.equals("--help") || arg.equals("-h")) {
                    System.out.println("Available Parameters:");
                    System.out.println("\t-h or --help, shows this information");
                    System.out.println("\t-c or --config filename, sets the CSV-file to read from");
                    System.out.println("\t-d or --delay X, sets the delay(in seconds) for each update");
                    System.exit(0);
                } else if (arg.equals("--config") || arg.equals("-c")) {
                    if (x + 1 < args.length && !args[x + 1].contains("-")) {
                        CONF = args[x + 1];
                        System.out.println("Sets the config file to: " + CONF);
                        x++;
                    } else {
                        System.out.println("Not a valid config file for -c/--config");
                    }
                } else if (arg.equals("--delay") || arg.equals("-d")) {
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
                } else {
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
        ArrayList<MetaConnection> connList = fetchConfig();
        startSync(connList);
        startTimer();
    }

    private ArrayList<MetaConnection> fetchConfig() {
        ArrayList<MetaConnection> connList = new ArrayList<MetaConnection>();
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            System.out.println("Fetching connection details.");
            fis = new FileInputStream(CONF);
            reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            while (line != null) {
                if (!line.equals("") && !line.startsWith("//")) {
                    String[] details = line.split(",");
                    if (details.length < 7)
                        throw new IllegalArgumentException("Needs to be 7 arguments per line.");
                    connList.add(new MetaConnection(Integer.parseInt(details[0]), details[1], details[2], details[3], details[4], details[5], details[6]));
                }
                line = reader.readLine();
            }
            if (connList.size() == 0)
                throw new IllegalArgumentException(CONF + " seems to be empty.");
        } catch (IllegalArgumentException | IOException ex) {
            System.err.println("FATAL: " + ex.getMessage());
            System.out.println("Will try again in " + DELAY / 1000 / 60 + " minutes");
            startTimer();
        } finally {
            try {
                reader.close();
                fis.close();
            } catch (NullPointerException | IOException ex) {
                System.err.println("FATAL: " + ex.getMessage());
                System.out.println("Will try again in " + DELAY / 1000 / 60 + " minutes");
            }
        }
        return connList;
    }

    private void startSync(ArrayList<MetaConnection> connList) {
        System.out.println("Starting to sync, will sync every " + DELAY / 1000 / 60 + " minutes");
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
        ZurmoClient zurmo = ZurmoClient.build(connList.get(0));
//        zurmo.createNote(1, "super", 2, "BAJSKAKA", new Date());
        zurmo.createUser("EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "EMILIOOOO@e.com", 0, "EMILIOOOO", "EMILIOOOO", "EMILIOOOO", "STATE", 1);
        startTimer();
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
