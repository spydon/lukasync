package lukasync;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

public class Lukasync {

    //public static String CONF = "/usr/local/etc/lukasync.csv";
    public static String CONF = "./lukasync.csv";
    public static long INITIAL_DELAY = 0;
    public static long DELAY = 60000;

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

    public static JSONObject fetchConfig() {
        JSONObject conf = new JSONObject();

        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://londonsales.com.au/lukasync",
                    "lukasync",
                    "Daniel1985"
            );

            QueryBuilder serviceQuery = new QueryBuilder(
                    "id, name, type, connection_type," +
                            "address, database_name, username, password",
                    "service",
                    "",
                    "",
                    ""
            );

            String serviceQueryString = serviceQuery.getQuery();
            PreparedStatement servicesStatement = conn.prepareStatement(serviceQueryString);
            ResultSet servicesResult = servicesStatement.executeQuery();
            while (servicesResult.next()) {
                JSONObject service = new JSONObject();

                service.put("name", servicesResult.getString("name"));
                service.put("type", servicesResult.getString("type"));
                service.put("connectionType", servicesResult.getString("connection_type"));
                service.put("address", servicesResult.getString("address"));
                service.put("databaseName", servicesResult.getString("database_name"));
                service.put("username", servicesResult.getString("username"));
                service.put("password", servicesResult.getString("password"));

                conf.putOnce("" + servicesResult.getInt("id"), service);
            }

            servicesResult.close();
            servicesStatement.close();

            QueryBuilder serviceFlowsQuery = new QueryBuilder(
                    "source, destination",
                    "service_flows",
                    "",
                    "",
                    ""
            );

            String serviceFlowsQueryString = serviceFlowsQuery.getQuery();
            PreparedStatement serviceFlowsStatement = conn.prepareStatement(serviceFlowsQueryString);

            ResultSet serviceFlowsResult = serviceFlowsStatement.executeQuery();
            while (serviceFlowsResult.next()) {
                String sourceServiceKey = "" + serviceFlowsResult.getInt("source");

                JSONObject service = conf.getJSONObject(sourceServiceKey);
                JSONArray destinations = null;

                String KEY_DESTINATIONS = "destinations";
                if (!service.has(KEY_DESTINATIONS)) {
                    destinations = new JSONArray();
                    service.put(KEY_DESTINATIONS, destinations);
                } else {
                    destinations = service.getJSONArray(KEY_DESTINATIONS);
                }

                destinations.put("" + serviceFlowsResult.getInt("destination"));
            }

            serviceFlowsResult.close();
            serviceFlowsStatement.close();

            conn.close();
        } catch (Throwable t) {
            System.err.println("Couldn't fetch configuration due to an error:");
            t.printStackTrace();
        }

        System.out.println(conf.toString());
        return conf;
    }
}
