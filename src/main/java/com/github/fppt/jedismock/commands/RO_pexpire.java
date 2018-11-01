package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;

import java.util.List;

import static com.github.fppt.jedismock.Utils.convertToLong;

class RO_pexpire extends AbstractRedisOperation {
    RO_pexpire(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    long getValue(List<Slice> params){
        return convertToLong(new String(params.get(1).data()));
    }

    Slice response() {
        return Response.integer(base().setTTL(params().get(0), getValue(params())));
    }
}
