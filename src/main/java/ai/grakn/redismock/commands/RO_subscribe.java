package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.RedisClient;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_subscribe extends AbstractRedisOperation {
    private final RedisClient client;

    RO_subscribe(RedisBase base, RedisClient client, List<Slice> params) {
        super(base, params,null, 0, null);
        this.client = client;
    }

    @Override
    public Slice execute() {
        params().forEach(channel -> base().addSubscriber(channel, client));
        List<Slice> numSubscriptions = base().getSubscriptions(client);

        return Response.subscribedToChannel(numSubscriptions);
    }
}
