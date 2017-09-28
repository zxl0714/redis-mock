package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.LinkedList;
import java.util.List;

import static ai.grakn.redismock.Utils.deserializeObject;
import static ai.grakn.redismock.Utils.serializeObject;

class RO_lpop extends AbstractRedisOperation {
    RO_lpop(RedisBase base,List<Slice> params ) {
        super(base, params, 1, null, null);
    }

    @Override
    public Slice execute() {
        Slice key = params().get(0);
        Slice data = base().rawGet(key);
        LinkedList<Slice> list;
        if (data != null) {
            list = deserializeObject(data);
        } else {
            return Response.NULL;
        }

        if (list.isEmpty()) {
            return Response.NULL;
        }
        Slice v = list.removeFirst();
        base().rawPut(key, serializeObject(list), -1L);
        return Response.bulkString(v);
    }
}
