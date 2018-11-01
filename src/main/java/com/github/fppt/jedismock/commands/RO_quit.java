package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.RedisClient;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;

import java.util.List;

class RO_quit extends AbstractRedisOperation {
    private final RedisClient client;

    RO_quit(RedisBase base, RedisClient client, List<Slice> params) {
        super(base, params,0, null, null);
        this.client = client;
    }

    Slice response() {
        client.sendResponse(Response.clientResponse("quit", Response.OK), "quit");
        client.close();

        return Response.SKIP;
    }
}
