package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

import static ai.grakn.redismock.Utils.convertToLong;

class RO_pexpire extends AbstractRedisOperation {
    RO_pexpire(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    long getValue(List<Slice> params){
        return convertToLong(new String(params.get(1).data()));
    }

    Slice response() {
        return Response.integer(base().setTTL(params().get(0), getValue(params())));
    }
}
