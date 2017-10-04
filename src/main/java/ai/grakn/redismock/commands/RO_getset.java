package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_getset extends AbstractRedisOperation {
    RO_getset(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    Slice response() {
        Slice value = base().rawGet(params().get(0));
        base().rawPut(params().get(0), params().get(1), -1L);
        return Response.bulkString(value);
    }
}
