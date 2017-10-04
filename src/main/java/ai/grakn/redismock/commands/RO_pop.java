package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;

import java.util.LinkedList;
import java.util.List;

import static ai.grakn.redismock.Utils.deserializeObject;
import static ai.grakn.redismock.Utils.serializeObject;

abstract class RO_pop extends AbstractRedisOperation {
    RO_pop(RedisBase base, List<Slice> params ) {
        super(base, params, 1, null, null);
    }

    abstract Slice popper(LinkedList<Slice> list);

    Slice response() {
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
        Slice v = popper(list);
        base().rawPut(key, serializeObject(list), -1L);
        return Response.bulkString(v);
    }
}
