package se.eldfluga.dropwizardlab;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Path("/user_viewers/{viewed_user}")
public class UserViewerResource {

    private DBI dbi;

    private static long TEN_DAYS_IN_MILLIS = 10 * 24 * 60 * 60 * 1000;

    private class ViewerResultSetMapper implements ResultSetMapper<List<Object>> {
        @Override
        public List<Object> map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            List<Object> oneRowList = new ArrayList<Object>(2);
            oneRowList.add( resultSet.getLong("viewer") );
            oneRowList.add( resultSet.getTimestamp("timestamp") );
            return oneRowList;
        }
    }

    public UserViewerResource(DBI dbi) {
        this.dbi = dbi;
    }

    @POST
    @Path("/viewed_by/{viewer_user}")
    public void addViewing(@PathParam("viewer_user") long viewer, @PathParam("viewed_user") long viewed) {
        Handle h = dbi.open();
        try {
            h.execute("insert into user_viewers values(?,?,current timestamp)", viewer, viewed);
        } finally {
            h.close();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<List<Object>> listViewingFor(@PathParam("viewed_user") long viewed) {
        Handle h = dbi.open();
        try {
            return h.createQuery("select viewer, timestamp from user_viewers where viewed=:viewed and timestamp > :ten_days_ago")
                    .bind("viewed", viewed)
                    .bind("ten_days_ago", tenDaysAgo())
                    .map(new ViewerResultSetMapper())
                    .list(10);
        } finally {
            h.close();
        }
    }

    private Timestamp tenDaysAgo() {
        return new Timestamp(System.currentTimeMillis() - TEN_DAYS_IN_MILLIS);
    }
}
