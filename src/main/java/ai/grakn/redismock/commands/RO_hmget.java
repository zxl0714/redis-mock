package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RO_hmget extends RO_hash {
    RO_hmget(RedisBase base, List<Slice> params) {
        super(base, params, null, 2, null);
    }

    @Override
    Slice hashOp(Map<Slice, Slice> map) {
        List<Slice> result = new ArrayList<>(params().size() - 1);
        
        for (int i = 1; i < params().size(); i++) {
            Slice mKey = params().get(i);
            result.add(Response.bulkString(map.get(mKey)));
        }
        
        return Response.array(result);
    }

    
}
