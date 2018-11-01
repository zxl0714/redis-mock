package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;

import java.util.List;

import static com.github.fppt.jedismock.Utils.convertToLong;

class RO_pexpireat extends AbstractRedisOperation {
    RO_pexpireat(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    Slice response() {
        long deadline = convertToLong(new String(params().get(1).data()));
        return Response.integer(base().setDeadline(params().get(0), deadline));
    }
}
