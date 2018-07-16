package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import java.util.List;
import java.util.Map;

class RO_hsetnx extends RO_hash {
    RO_hsetnx(RedisBase base, List<Slice> params) {
        super(base, params, 3, null, null);
    }

    @Override
    Slice hashOp(Map<Slice, Slice> map) {
        Slice mKey = params().get(1);
        Slice mValue = params().get(2);
        
        if (map.containsKey(mKey)) {
            return Response.integer(0);
        }
        
        map.put(mKey, mValue);
        
        return Response.integer(1);
    }

    
}
