package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import java.util.List;
import java.util.Map;

class RO_hdel extends RO_hash {
    RO_hdel(RedisBase base, List<Slice> params) {
        super(base, params, null, 1, null);
    }

    @Override
    Slice hashOp(Map<Slice, Slice> map) {
        boolean deleted = false;
        for (int i = 1; i < params().size(); i++) {
            Slice mKey = params().get(i);
            Slice previous = map.remove(mKey);
            deleted |= previous != null;
        }
        
        return deleted ? Response.integer(1) : Response.integer(0);
    }

    
}
