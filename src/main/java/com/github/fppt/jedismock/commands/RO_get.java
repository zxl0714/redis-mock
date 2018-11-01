package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;

import java.util.List;

class RO_get extends AbstractRedisOperation {
    RO_get(RedisBase base, List<Slice> params) {
        super(base, params, 1, null, null);
    }

    Slice response() {
        return Response.bulkString(base().rawGet(params().get(0)));
    }
}
