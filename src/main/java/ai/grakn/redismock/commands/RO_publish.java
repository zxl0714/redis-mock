package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.RedisClient;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;

class RO_publish extends AbstractRedisOperation {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RO_publish.class);

    RO_publish(RedisBase base, List<Slice> params) {
        super(base, params,2, null, null);
    }

    @Override
    public Slice execute() {
        Slice channel = params().get(0);
        Slice message = params().get(1);

        Set<RedisClient> subscibers = base().getSubscribers(channel);

        subscibers.forEach(subscriber -> {
            try {
                Slice response = Response.publishedMessage(channel, message);
                subscriber.sendResponse(response);
            } catch (IOException e){
                LOG.error("Unable to contact subscriber", e);
            }
        });

        return Response.integer(subscibers.size());
    }
}
