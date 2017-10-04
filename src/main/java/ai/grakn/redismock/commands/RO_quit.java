package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.RedisClient;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_quit extends AbstractRedisOperation {
    private final RedisClient client;

    RO_quit(RedisBase base, RedisClient client, List<Slice> params) {
        super(base, params,0, null, null);
        this.client = client;
    }

    @Override
    public Slice execute() {
        client.sendResponse(Response.clientResponse("quit", Response.OK), "quit");
        client.close();

        return Response.SKIP;
    }
}
