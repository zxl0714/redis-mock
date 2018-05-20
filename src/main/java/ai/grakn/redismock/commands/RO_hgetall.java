package ai.grakn.redismock.commands;

import static ai.grakn.redismock.Utils.deserializeObject;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RO_hgetall extends AbstractRedisOperation {
    RO_hgetall(RedisBase base, List<Slice> params) {
        super(base, params, 1, null, null);
    }

    @Override
    Slice response() {
        Slice key = params().get(0);
        Slice data = base().rawGet(key);
        Map<Slice, Slice> map;

        if (data != null) {
            map = deserializeObject(data);
        } else {
            map = new HashMap<>();
        }

        return Response.map(map);
    }
}
