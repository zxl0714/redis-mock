package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_set extends AbstractRedisOperation {
    RO_set(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    public RO_set(RedisBase base, List<Slice> params, Integer i) {
        super(base, params, i, null,null);
    }

    long valueToSet(List<Slice> params){
        return -1L;
    }

    Slice response() {
        base().rawPut(params().get(0), params().get(1), valueToSet(params()));
        return Response.OK;
    }
}
