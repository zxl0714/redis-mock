package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

import static ai.grakn.redismock.Utils.convertToLong;

class RO_pexpireat extends AbstractRedisOperation {
    RO_pexpireat(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    Slice response() {
        long deadline = convertToLong(new String(params().get(1).data()));
        return Response.integer(base().setDeadline(params().get(0), deadline));
    }
}
