package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import static ai.grakn.redismock.Utils.deserializeObject;

class RO_pfcount extends AbstractRedisOperation {
    RO_pfcount(RedisBase base, List<Slice> params) {
        super(base, params, null, 0, null);
    }

    Slice response() {
        Set<Slice> set = Sets.newHashSet();
        for (Slice key : params()) {
            Slice data = base().rawGet(key);
            if (data == null) {
                continue;
            }

            Set<Slice> s = deserializeObject(data);
            set.addAll(s);
        }
        return Response.integer((long) set.size());
    }
}
