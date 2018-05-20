package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RO_hkeys extends RO_hash {
    RO_hkeys(RedisBase base, List<Slice> params) {
        super(base, params, 1, null, null);
    }

    @Override
    Slice hashOp(Map<Slice, Slice> map) {
        List<Slice> slices = new ArrayList<>();
        
        for (Slice slice : map.keySet()) {
            slices.add(Response.bulkString(slice));
        }
        
        return Response.array(slices);
    }

}
