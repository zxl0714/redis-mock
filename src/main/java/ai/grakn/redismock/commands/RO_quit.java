package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.RedisClient;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

class RO_quit extends AbstractRedisOperation {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RO_quit.class);
    private final RedisClient client;

    RO_quit(RedisBase base, RedisClient client, List<Slice> params) {
        super(base, params,0, null, null);
        this.client = client;
    }

    @Override
    public Slice execute() {
        try {
            client.sendResponse(Response.clientResponse("quit", Response.OK));
        } catch (IOException e) {
            LOG.error("unable to acknowledge closing connection", e);
        }

        client.close();

        return Response.SKIP;
    }
}
