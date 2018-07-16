package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import java.util.List;
import java.util.Map;

class RO_hlen extends RO_hash {
    RO_hlen(RedisBase base, List<Slice> params) {
        super(base, params, 1, null, null);
    }

    @Override
    Slice hashOp(Map<Slice, Slice> map) {
        return Response.integer(map.size());
    }

}
