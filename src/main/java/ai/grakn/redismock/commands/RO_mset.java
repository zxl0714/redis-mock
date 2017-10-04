package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_mset extends AbstractRedisOperation {
    RO_mset(RedisBase base, List<Slice> params ) {
        super(base, params, null, 0, 2);
    }

    Slice response() {
        for (int i = 0; i < params().size(); i += 2) {
            base().rawPut(params().get(i), params().get(i + 1), -1L);
        }
        return Response.OK;
    }
}
