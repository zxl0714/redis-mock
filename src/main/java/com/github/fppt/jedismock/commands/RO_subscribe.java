package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.RedisClient;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;

import java.util.List;

class RO_subscribe extends AbstractRedisOperation {
    private final RedisClient client;

    RO_subscribe(RedisBase base, RedisClient client, List<Slice> params) {
        super(base, params,null, 0, null);
        this.client = client;
    }

    Slice response() {
        params().forEach(channel -> base().addSubscriber(channel, client));
        List<Slice> numSubscriptions = base().getSubscriptions(client);

        return Response.subscribedToChannel(numSubscriptions);
    }
}
