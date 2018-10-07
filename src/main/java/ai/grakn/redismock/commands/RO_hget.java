package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import java.util.List;
import java.util.Map;

class RO_hget extends RO_hash {
    RO_hget(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    @Override
    Slice hashOp(Map<Slice, Slice> map) {
        Slice mKey = params().get(1);
        
        return Response.bulkString(map.get(mKey));
    }

    
}
