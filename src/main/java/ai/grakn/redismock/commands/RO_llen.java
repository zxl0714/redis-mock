package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;

import static ai.grakn.redismock.Utils.deserializeObject;

class RO_llen extends AbstractRedisOperation {
    RO_llen(RedisBase base, List<Slice> params) {
        super(base, params,  1, null, null);
    }

    Slice response() {
        Slice key = params().get(0);
        Slice data = base().rawGet(key);
        LinkedList<Slice> list;
        if (data != null) {
            list = deserializeObject(data);
        } else {
            list = Lists.newLinkedList();
        }
        return Response.integer(list.size());
    }
}
