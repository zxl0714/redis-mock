package com.github.fppt.jedismock.commands;

import com.github.fppt.jedismock.RedisBase;
import com.github.fppt.jedismock.Slice;
import com.github.fppt.jedismock.SliceParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class RO_rpoplpush extends AbstractRedisOperation {
    RO_rpoplpush(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    RO_rpoplpush(RedisBase base, List<Slice> params, Integer numExpected) {
        super(base, params, numExpected, null, null);
    }

    Slice response() {
        Slice source = params().get(0);
        Slice target = params().get(1);

        //Pop last one
        Slice result = new RO_rpop(base(), Collections.singletonList(source)).execute();
        Slice valueToPush = SliceParser.consumeParameter(result.data());

        //Push it into the other list
        new RO_lpush(base(), Arrays.asList(target, valueToPush)).execute();

        return result;
    }
}
