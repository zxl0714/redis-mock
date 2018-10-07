package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_ping extends AbstractRedisOperation {
    RO_ping(RedisBase base, List<Slice> params) {
        super(base, params,  0, null, null);
    }

    Slice response() {
        if (params().isEmpty()){
            return Response.bulkString(new Slice("PONG"));
        }

        return Response.bulkString(params().get(0));
    }
}
