package com.github.sblack4;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.DatabaseWaiters;
import com.oracle.bmc.database.model.AutonomousDatabase;
import com.oracle.bmc.database.requests.GetAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.StartAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.StopAutonomousDatabaseRequest;
import com.oracle.bmc.database.responses.GetAutonomousDatabaseResponse;
import picocli.CommandLine;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;


@Command(name="start",
        sortOptions = false,
        header = "@|fg(5;0;0),bg(0;0;0)  Start an ATP instance with the JAVA OCI SDK |@" )
public class startAutonomousDatabase implements Runnable {
    @Option(names={"-Id", "--adwId"}, required = true, description = "Autonomous ATPConnectionTest ID")
    public String adwId;

    @Option(names={"-cid", "--compartment-id"}, description = "Compartment ID, retrieved from OCI Config")
    public String compartmentId;

    @Option(names={"-c", "--config"}, description = "OCI Config file path, defaults to ${DEFAULT-VALUE}")
    public String configurationFilePath = "~/.oci/config";

    @Option(names={"-p", "--profile"}, description = "OCI profile, defaults to ${DEFAULT-VALUE}")
    public String profile = "DEFAULT";

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Displays this help message and quits.")
    private boolean helpRequested = false;

    @Override
    public void run() {
        // busyn-ness logix
        try {
            AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configurationFilePath, profile);

            System.out.println("\n================================\n");
            System.out.println("Credentials 'n such; ");
            System.out.println(provider.toString());

            DatabaseClient dbClient = new DatabaseClient(provider);

            // Get
            AutonomousDatabase adw = dbClient.getAutonomousDatabase(
                            GetAutonomousDatabaseRequest.builder()
                                    .autonomousDatabaseId(adwId)
                                    .build())
                            .getAutonomousDatabase();

            System.out.println("\n================================\n");
            System.out.println("GET request returned this database:\n" + adw);

            System.out.println("\n================================\n");
            System.out.println("Starting Autonomous Database: \n" + adw);
            dbClient.startAutonomousDatabase(
                StartAutonomousDatabaseRequest.builder()
                        .autonomousDatabaseId(adw.getId())
                        .build());

            DatabaseWaiters waiter = dbClient.getWaiters();
            GetAutonomousDatabaseResponse response = waiter.forAutonomousDatabase(
                                GetAutonomousDatabaseRequest.builder()
                                        .autonomousDatabaseId(adw.getId())
                                        .build(),
                                AutonomousDatabase.LifecycleState.Available
                ).execute();

            System.out.println("\n================================\n");
            System.out.println("Request for Available Instance returned: \n" + response.getAutonomousDatabase());

            System.out.println("\n======== DONE ========\n");
            System.out.println("\n================================\n");

        dbClient.close();
        } catch (Exception ex) {
            System.out.println("================================");
            System.out.println("=== ERROR ===");
            System.out.println("================================");

            System.out.println(ex.toString());
            ex.printStackTrace();
        }


    }

    public static void main(String[] args) {
        CommandLine.run(new startAutonomousDatabase(), args);
    }
}
