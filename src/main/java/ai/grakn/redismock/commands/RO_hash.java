package ai.grakn.redismock.commands;

import static ai.grakn.redismock.Utils.deserializeObject;
import static ai.grakn.redismock.Utils.serializeObject;

import ai.grakn.redismock.RedisBase;
import ai.grakn.redismock.Slice;
import ai.grakn.redismock.exception.InternalException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class RO_hash extends AbstractRedisOperation {
    
    RO_hash(RedisBase base, List<Slice> params, Integer expectedParams, Integer minParams, Integer factorParams) {
        super(base, params, expectedParams, minParams, factorParams);
    }
    
    abstract Slice hashOp(Map<Slice, Slice> map);

    @Override
    final Slice response() {
        Slice key = params().get(0);
        Slice data = base().rawGet(key);
        Map<Slice, Slice> map;

        if (data != null) {
            map = deserializeObject(data);
        } else {
            map = new HashMap<>();
        }

        Slice result = hashOp(map);
        try {
            base().rawPut(key, serializeObject(map), -1L);
        } catch (Exception e) {
            throw new InternalException(e.getMessage());
        }
        return result;
    }
}
