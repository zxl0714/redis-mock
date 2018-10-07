package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import ai.grakn.redismock.exception.InternalException;
import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;

import static ai.grakn.redismock.Utils.deserializeObject;
import static ai.grakn.redismock.Utils.serializeObject;

abstract class RO_push extends AbstractRedisOperation {
    RO_push(RedisBase base, List<Slice> params) {
        super(base, params,null, 1, null);
    }

    abstract void pusher(LinkedList<Slice> list, Slice slice);

    Slice response() {
        Slice key = params().get(0);
        Slice data = base().rawGet(key);
        LinkedList<Slice> list;

        if (data != null) {
            list = deserializeObject(data);
        } else {
            list = Lists.newLinkedList();
        }

        for (int i = 1; i < params().size(); i++) {
            pusher(list, params().get(i));
        }
        try {
            base().rawPut(key, serializeObject(list), -1L);
        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        }
        return Response.integer(list.size());
    }
}
