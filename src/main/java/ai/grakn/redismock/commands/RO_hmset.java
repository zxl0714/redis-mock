package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import java.util.List;
import java.util.Map;

class RO_hmset extends RO_hash {
    RO_hmset(RedisBase base, List<Slice> params) {
        super(base, params, null, 2, null);
    }

    @Override
    Slice hashOp(Map<Slice, Slice> map) {
        for (int i = 1; i < params().size(); i += 2) {
            Slice mKey = params().get(i);
            Slice mValue = params().get(i + 1);
            map.put(mKey, mValue);
        }

        return Response.OK;
    }
}
