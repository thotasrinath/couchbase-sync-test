package org.example;

import android.support.annotation.NonNull;
import com.couchbase.lite.*;
import okhttp3.WebSocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws URISyntaxException {

        CouchbaseLite.init();

        // Create a database
        System.out.println("Starting DB");
        DatabaseConfiguration cfg = new DatabaseConfiguration();
        Database database = null;
        try {
            database = new Database("mydb", cfg);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }


        // Create a query to fetch documents of type SDK.
        Query listQuery = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(database));
                /*.where(Expression.property("type").equalTo(Expression.string("SDK")));*/

        try {
            for (Result result : listQuery.execute().allResults()) {
                System.out.printf(result.toJSON());

            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }


        //
        // OPTIONAL -- if you have Sync Gateway Installed you can try replication too
        // Create a replicator to push and pull changes to and from the cloud.
        // Be sure to hold a reference somewhere to prevent the Replicator from being GCed
        BasicAuthenticator basAuth = new BasicAuthenticator("sgwuser1", "password".toCharArray());

        ReplicatorConfiguration replConfig =
                new ReplicatorConfiguration(database,
                        new URLEndpoint(new URI("ws://localhost:4984/traveldb")))
                        .setType(ReplicatorType.PULL)
                        .setContinuous(true)
                        .setChannels(List.of("public"))
                        .setAuthenticator(basAuth);


        Replicator replicator = new Replicator(replConfig);

        // Listen to replicator change events.
        // Version using Kotlin Flows to follow shortly ...

        Database finalDatabase = database;
        replicator.addChangeListener(new ReplicatorChangeListener() {
            @Override
            public void changed(@NonNull ReplicatorChange change) {
                if (change.getStatus().getError() != null) {
                    System.out.println("Error code ::  ${err.code}");
                }

                Query listQuery = QueryBuilder.select(SelectResult.all())
                        .from(DataSource.database(finalDatabase));
                /*.where(Expression.property("type").equalTo(Expression.string("SDK")));*/

                try {
                    for (Result result : listQuery.execute().allResults()) {
                        System.out.printf(result.toJSON());

                    }
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });


        // Start replication.
        replicator.start(true);

    }
}
