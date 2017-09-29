package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.RedisClient;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_unsubscribe extends AbstractRedisOperation {
    private final RedisClient client;

    RO_unsubscribe(RedisBase base, RedisClient client, List<Slice> params) {
        super(base, params,0, null, null);
        this.client = client;
    }

    @Override
    public Slice execute() {
        params().forEach(channel -> base().removeSubscriber(channel, client));
        //TODO: Return correct response
        return Response.OK;
    }
}
