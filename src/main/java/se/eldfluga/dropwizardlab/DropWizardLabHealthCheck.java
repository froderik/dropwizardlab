package se.eldfluga.dropwizardlab;

import com.codahale.metrics.health.HealthCheck;

import java.util.List;

public class DropWizardLabHealthCheck extends HealthCheck {

    private final UserViewerResource resource;

    public DropWizardLabHealthCheck(UserViewerResource resource) {
        this.resource = resource;
    }

    @Override
    protected Result check() throws Exception {
        resource.addViewing(-1, 4711);
        List<List<Object>> viewings = resource.listViewingFor(-1);
        if(viewings.size() > 1) {
            return Result.unhealthy( "an added viewing is not returned" );
        }
        List<Object> oneViewing = viewings.get(0);
        if(oneViewing.get(0) != (Long)4711L) {
            return Result.unhealthy( "the added viewer is not returned" );
        }
        return Result.healthy();
    }
}

