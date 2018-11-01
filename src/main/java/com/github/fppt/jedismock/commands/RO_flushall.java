package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;

import java.util.List;

class RO_flushall extends AbstractRedisOperation {
    RO_flushall(RedisBase base, List<Slice> params) {
        super(base, params, 0, null, null);
    }

    Slice response(){
        base().clear();
        return Response.OK;
    }
}
