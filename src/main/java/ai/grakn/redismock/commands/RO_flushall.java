package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

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
