package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;

import java.util.List;

class RO_getset extends AbstractRedisOperation {
    RO_getset(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    Slice response() {
        Slice value = base().getValue(params().get(0));
        base().putValue(params().get(0), params().get(1));
        return Response.bulkString(value);
    }
}
