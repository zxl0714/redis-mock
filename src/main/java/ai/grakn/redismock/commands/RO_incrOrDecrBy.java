package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

import static ai.grakn.redismock.Utils.convertToLong;

abstract class RO_incrOrDecrBy extends AbstractRedisOperation {
    RO_incrOrDecrBy(RedisBase base, List<Slice> params, Integer expectedParams) {
        super(base, params, expectedParams, null, null);
    }

    abstract long incrementOrDecrementValue(List<Slice> params);

    Slice response() {
        Slice key = params().get(0);
        long d = incrementOrDecrementValue(params());
        Slice v = base().rawGet(key);
        if (v == null) {
            base().rawPut(key, new Slice(String.valueOf(d)), -1L);
            return Response.integer(d);
        }

        long r = convertToLong(new String(v.data())) + d;
        base().rawPut(key, new Slice(String.valueOf(r)), -1L);
        return Response.integer(r);
    }
}
