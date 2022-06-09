package com.github.fppt.jedismock.operations.strings;

import com.github.fppt.jedismock.datastructures.RMString;
import com.github.fppt.jedismock.operations.AbstractRedisOperation;
import com.github.fppt.jedismock.operations.RedisCommand;
import com.github.fppt.jedismock.server.Response;
import com.github.fppt.jedismock.datastructures.Slice;
import com.github.fppt.jedismock.storage.RedisBase;

import java.util.List;

@RedisCommand("getset")
class GetSet extends AbstractRedisOperation {
    GetSet(RedisBase base, List<Slice> params) {
        super(base, params);
    }

    protected Slice response() {
        Slice value = base().getSlice(params().get(0));
        base().putValue(params().get(0), RMString.create(params().get(1).toString()));
        return Response.bulkString(value);
    }
}