package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.List;

class RO_del extends AbstractRedisOperation {
    RO_del(RedisBase base, List<Slice> params) {
        super(base, params,null, 0, null);
    }

    @Override
    public Slice execute() {
        int count = 0;
        for (Slice key : params()) {
            Slice value = base().rawGet(key);
            base().del(key);
            if (value != null) {
                count++;
            }
        }
        return Response.integer(count);
    }
}
