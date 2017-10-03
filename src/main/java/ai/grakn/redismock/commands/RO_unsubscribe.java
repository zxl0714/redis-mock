package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.RedisClient;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

class RO_unsubscribe extends AbstractRedisOperation {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RO_unsubscribe.class);
    private final RedisClient client;

    RO_unsubscribe(RedisBase base, RedisClient client, List<Slice> params) {
        super(base, params,0, null, null);
        this.client = client;
    }

    @Override
    public Slice execute() {
        List<Slice> channelsToUbsubscribeFrom;
        if(params().isEmpty()){
            LOG.debug("No channels specified therefore unsubscribing from all channels");
            channelsToUbsubscribeFrom = base().getSubscriptions(client);
        } else {
            channelsToUbsubscribeFrom = params();
        }

        for (Slice channel : channelsToUbsubscribeFrom) {
            LOG.debug("Unsubscribing from channel [" + channel + "]");
            base().removeSubscriber(channel, client);
            int numSubscriptions = base().getSubscriptions(client).size();
            Slice response = Response.unsubscribe(channel, numSubscriptions);
            try {
                client.sendResponse(Response.clientResponse("unsubscribe", response));
            } catch (IOException e) {
                LOG.error("Unable to unsubscribe from channel [" + channel + "]", e);
            }
        }

        //Skip is sent because we have already responded
        return Response.SKIP;
    }
}
