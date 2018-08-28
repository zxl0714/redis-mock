package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static ai.grakn.redismock.Utils.deserializeObject;
import static ai.grakn.redismock.Utils.serializeObject;

public class RO_zrem extends AbstractRedisOperation {
    public RO_zrem(RedisBase base, List<Slice> params) {
        super(base, params, 2, null, null);
    }

    @Override
    Slice response() {
        Slice key = params().get(0);
        Slice data = base().rawGet(key);
        Set<Slice> set;
        if (data != null) {
            set = deserializeObject(data);
        } else {
            return Response.NULL;
        }

        if (set.isEmpty()) {
            return Response.NULL;
        }
        Iterator<Slice> it = set.iterator();
        Slice v = it.next();
        it.remove();
        base().rawPut(key, serializeObject(set), -1L);
        return Response.integer(1);
    }
}
