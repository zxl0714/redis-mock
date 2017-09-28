package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_exists extends AbstractRedisOperation {
    RO_exists(RedisBase base, List<Slice> params) {
        super(base, params, 1, null, null);
    }

    @Override
    public Slice execute() {
        if (base().rawGet(params().get(0)) != null) {
            return Response.integer(1);
        }
        return Response.integer(0);
    }
}
