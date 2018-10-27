package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;

import static ai.grakn.redismock.Utils.deserializeObject;

class RO_smembers extends AbstractRedisOperation {
    RO_smembers(RedisBase base, List<Slice> params) {
        super(base, params,  1, null, null);
    }

    Slice response() {
        Slice key = params().get(0);
        Slice data = base().rawGet(key);
        //Has to be a list because Jedis can only deserialize lists
        LinkedList<Slice> set;
        if (data != null) {
            set = new LinkedList<>(deserializeObject(data));
        } else {
            set = Lists.newLinkedList();
        }

        ImmutableList.Builder<Slice> builder = new ImmutableList.Builder<Slice>();
        set.forEach(element -> builder.add(Response.bulkString(element)));

        return Response.array(builder.build());
    }
}
