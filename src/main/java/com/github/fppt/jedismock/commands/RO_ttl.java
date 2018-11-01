package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Response;
import com.github.fppt.jedismock.Slice;

import java.util.List;

class RO_ttl extends AbstractRedisOperation {
    RO_ttl(RedisBase base, List<Slice> params) {
        super(base, params, 1, null, null);
    }

    Slice finalReturn(Long pttl){
        return Response.integer((pttl + 999) / 1000);
    }

    Slice response() {
        Long pttl = base().getTTL(params().get(0));
        if (pttl == null) {
            return Response.integer(-2L);
        }
        if (pttl == -1) {
            return Response.integer(-1L);
        }
        return finalReturn(pttl);
    }
}
