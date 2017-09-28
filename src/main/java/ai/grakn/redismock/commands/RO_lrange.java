package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;

import static ai.grakn.redismock.Utils.convertToInteger;
import static ai.grakn.redismock.Utils.deserializeObject;

class RO_lrange extends AbstractRedisOperation {
    RO_lrange(RedisBase base, List<Slice> params) {
        super(base, params, 3, null, null);
    }

    @Override
    public Slice execute() {
        Slice key = params().get(0);
        Slice data = base().rawGet(key);
        LinkedList<Slice> list;
        if (data != null) {
            list = deserializeObject(data);
        } else {
            list = Lists.newLinkedList();
        }

        int start = convertToInteger(params().get(1).toString());
        int end = convertToInteger(params().get(2).toString());

        if (start < 0) {
            start = list.size() + start;
            if (start < 0) {
                start = 0;
            }
        }
        if (end < 0) {
            end = list.size() + end;
            if (end < 0) {
                end = 0;
            }
        }
        ImmutableList.Builder<Slice> builder = new ImmutableList.Builder<Slice>();
        for (int i = start; i <= end && i < list.size(); i++) {
            builder.add(Response.bulkString(list.get(i)));
        }
        return Response.array(builder.build());
    }
}
