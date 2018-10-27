package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_get extends AbstractRedisOperation {
    RO_get(RedisBase base, List<Slice> params) {
        super(base, params, 1, null, null);
    }

    Slice response() {
        return Response.bulkString(base().rawGet(params().get(0)));
    }
}
