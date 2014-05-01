package se.eldfluga.dropwizardlab;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.File;


public class DropWizardLabApplication extends Application<DropWizardLabConfiguration> {

    private DBI dbi;

    public static void main(String[] args) throws Exception {
        new DropWizardLabApplication().run(args);
    }

    @Override
    public String getName() {
        return "dropwizardlab";
    }

    @Override
    public void initialize(Bootstrap<DropWizardLabConfiguration> bootstrap) {
        try {
            Class.forName(EmbeddedDriver.class.getName());
            String dbName = getName() + "db";
            if(new File(dbName).exists()) {
                dbi = new DBI("jdbc:derby:" + dbName);
            } else {
                dbi = new DBI("jdbc:derby:" + dbName + ";create=true" );
                initializeDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    private void initializeDatabase() {
        Handle h = dbi.open();
        try {
            h.execute("create table user_viewers (viewer bigint, viewed bigint, timestamp timestamp)");
            h.execute("create index viewed_index on user_viewers(viewed)");
        } finally {
            h.close();
        }
    }

    @Override
    public void run(DropWizardLabConfiguration configuration,
                    Environment environment) {
        final UserViewerResource viewerResource = new UserViewerResource(dbi);
        environment.jersey().register(viewerResource);
    }

}
