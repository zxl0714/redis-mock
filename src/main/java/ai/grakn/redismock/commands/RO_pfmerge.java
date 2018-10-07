package ai.grakn.redismock.commands;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Response;
import ai.grakn.redismock.Slice;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import static ai.grakn.redismock.Utils.deserializeObject;
import static ai.grakn.redismock.Utils.serializeObject;

class RO_pfmerge extends AbstractRedisOperation {
    RO_pfmerge(RedisBase base, List<Slice> params) {
        super(base, params,null, 0, null);
    }

    Slice response() {
        Slice key = params().get(0);
        Slice data = base().rawGet(key);
        boolean first;

        Set<Slice> set;
        if (data == null) {
            set = Sets.newHashSet();
            first = true;
        } else {
            set = deserializeObject(data);
            first = false;
        }
        for (Slice v : params().subList(1, params().size())) {
            Slice src = base().rawGet(v);
            if (src != null) {
                Set<Slice> s = deserializeObject(src);
                set.addAll(s);
            }
        }

        Slice out = serializeObject(set);
        if (first) {
            base().rawPut(key, out, -1L);
        } else {
            base().rawPut(key, out, null);
        }
        return Response.OK;
    }
}
