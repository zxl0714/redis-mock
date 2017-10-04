package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_setnx extends AbstractRedisOperation {
    RO_setnx(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    Slice response(){
        if (base().rawGet(params().get(0)) == null) {
            base().rawPut(params().get(0), params().get(1), -1L);
            return Response.integer(1);
        }
        return Response.integer(0);
    }
}
